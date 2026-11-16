package yagen.waitmydawn.yagens_attributes.loot;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.*;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.core.loot.ModReward;
import yagen.waitmydawn.yagens_attributes.data.*;
import yagen.waitmydawn.yagens_attributes.registries.*;
import yagen.waitmydawn.yagens_attributes.setup.*;
import java.util.*;
import java.util.function.Consumer;

public final class ModRewardEntry extends LootPoolSingletonContainer {
    public static final MapCodec<ModRewardEntry> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(ModRewardCodec.CODEC.fieldOf("reward").forGetter(entry -> entry.reward)).and(singletonFields(instance)).apply(instance, ModRewardEntry::new));
    private final ModReward reward;
    private record Pools(ModCatalog catalog, Map<ModInfo.Rarity, List<String>> values) {}
    private record Rivens(Map<net.minecraft.resources.ResourceLocation, WeaponProfile> weapons, RivenRules rules, double meleeProbability) {}
    private static volatile Pools cachedPools = new Pools(null, Map.of());
    private static volatile Rivens cachedRivens = new Rivens(null, null, Double.NaN);
    private ModRewardEntry(ModReward reward, int weight, int quality, List<LootItemCondition> conditions, List<LootItemFunction> functions) { super(weight, quality, conditions, functions); this.reward = reward; }
    @Override public LootPoolEntryType getType() { return LootTypes.MOD_REWARD.get(); }
    @Override public void createItemStack(Consumer<ItemStack> output, LootContext context) {
        var catalog = ModDefinitions.INSTANCE.catalog(); var random = context.getRandom();
        if (reward.method() == ModReward.Method.VEILED_RIVEN) {
            var profiles = CombatData.WEAPONS.profiles();
            var snapshot = cachedRivens;
            if (profiles != snapshot.weapons() || snapshot.rules() != catalog.rivenRules()) {
                var weapons = RivenService.weapons(); var rules = catalog.rivenRules();
                double probability = rules == null ? Double.NaN : ModReward.meleeProbability(rules.candidates(RivenRoll.Pool.MELEE, weapons).size(), rules.candidates(RivenRoll.Pool.RANGED, weapons).size());
                snapshot = new Rivens(profiles, rules, probability); cachedRivens = snapshot;
            }
            if (Double.isFinite(snapshot.meleeProbability())) output.accept(new ItemStack(random.nextDouble() < snapshot.meleeProbability() ? ModItems.UNKNOWN_MELEE_RIVEN.get() : ModItems.UNKNOWN_RANGED_RIVEN.get())); return;
        }
        var snapshot = cachedPools;
        if (catalog != snapshot.catalog()) {
            var pools = new EnumMap<ModInfo.Rarity, List<String>>(ModInfo.Rarity.class);
            catalog.definitions().entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> pools.computeIfAbsent(e.getValue().rarity(), key -> new ArrayList<>()).add(e.getKey()));
            pools.replaceAll((key, value) -> List.copyOf(value)); snapshot = new Pools(catalog, Map.copyOf(pools)); cachedPools = snapshot;
        }
        reward.select(snapshot.values(), random::nextInt, random::nextDouble).ifPresent(id -> {
            var definition = catalog.definitions().get(id);
            if (definition != null) output.accept(ModTransactions.stack(id, reward.rank(definition.maxLevel(), random.nextDouble())));
        });
    }
}
