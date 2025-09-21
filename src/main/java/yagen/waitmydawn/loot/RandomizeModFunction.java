package yagen.waitmydawn.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.IModContainer;
import yagen.waitmydawn.api.mods.ModRarity;
import yagen.waitmydawn.registries.LootRegistry;

import java.util.List;

import static yagen.waitmydawn.api.util.ModCompat.TRANSFORM_POOL_BY_RARITY;

public class RandomizeModFunction extends LootItemConditionalFunction {

    public static final MapCodec<RandomizeModFunction> CODEC = RecordCodecBuilder.mapCodec(inst ->
                    commonFields(inst).and(
                            inst.group(
                                    Codec.INT.optionalFieldOf("common_weight", 50).forGetter(f -> f.commonWeight),
                                    Codec.INT.optionalFieldOf("uncommon_weight", 40).forGetter(f -> f.uncommonWeight),
                                    Codec.INT.optionalFieldOf("rare_weight", 10).forGetter(f -> f.rareWeight),
                                    Codec.INT.optionalFieldOf("legendary_weight", 0).forGetter(f -> f.legendaryWeight),
                                    Codec.INT.optionalFieldOf("level_percent_inf", 0).forGetter(f -> f.levelPercentInf),
                                    Codec.INT.optionalFieldOf("level_percent_sup", 50).forGetter(f -> f.levelPercentSup)
//                            Codec.INT.optionalFieldOf("riven_weight", 0).forGetter(f -> f.rivenWeight)
                            )
                    ).apply(inst, RandomizeModFunction::new)
    );

    private final int commonWeight, uncommonWeight, rareWeight, legendaryWeight, levelPercentInf, levelPercentSup;

    public RandomizeModFunction(List<LootItemCondition> conditions,
                                int commonWeight, int uncommonWeight, int rareWeight,
                                int legendaryWeight, int levelPercentInf, int levelPercentSup) {
        super(conditions);
        this.commonWeight = commonWeight;
        this.uncommonWeight = uncommonWeight;
        this.rareWeight = rareWeight;
        this.legendaryWeight = legendaryWeight;
        this.levelPercentInf = levelPercentInf;
        this.levelPercentSup = levelPercentSup;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext ctx) {
        YagensAttributes.LOGGER.info("RandomizeModFunction called!");
        RandomSource rand = ctx.getRandom();

        int total = commonWeight + uncommonWeight + rareWeight + legendaryWeight;
        int r = rand.nextInt(total);

        ModRarity chosen;
        if (r < commonWeight) chosen = ModRarity.COMMON;
        else if (r < commonWeight + uncommonWeight) chosen = ModRarity.UNCOMMON;
        else if (r < commonWeight + uncommonWeight + rareWeight) chosen = ModRarity.RARE;
        else chosen = ModRarity.LEGENDARY;

        List<AbstractMod> pool = TRANSFORM_POOL_BY_RARITY.get(chosen);
        if (pool == null || pool.isEmpty()) return stack;

        AbstractMod mod = pool.get(rand.nextInt(pool.size()));
        // Sup - Inf can't be 0
        int modLevel = Math.max(1, (int) Math.round(mod.getMaxLevel() * (levelPercentInf + rand.nextInt(levelPercentSup - levelPercentInf)) / 100.0));

        IModContainer.createModContainer(mod, modLevel, stack);
        return stack;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootRegistry.RANDOMIZE_MOD_FUNCTION.get();
    }

    /* 带参数的 builder */
    public static LootItemFunction.Builder builder(int common, int uncommon, int rare, int legendary, int levelPercentInf, int levelPercentSup) {
        return new LootItemFunction.Builder() {
            @Override
            public LootItemFunction build() {
                return new RandomizeModFunction(List.of(), common, uncommon, rare, legendary, levelPercentInf, levelPercentSup);
            }
        };
    }
}