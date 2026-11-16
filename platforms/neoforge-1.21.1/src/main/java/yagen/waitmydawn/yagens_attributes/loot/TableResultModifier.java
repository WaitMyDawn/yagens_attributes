package yagen.waitmydawn.yagens_attributes.loot;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.*;

/** Replaces only the current modifier-chain result; source resources and later modifiers remain untouched. */
public final class TableResultModifier extends LootModifier {
    public static final MapCodec<TableResultModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> codecStart(instance).and(instance.group(
            ResourceKey.codec(Registries.LOOT_TABLE).fieldOf("table").forGetter(value -> value.table), Codec.BOOL.optionalFieldOf("replace", false).forGetter(value -> value.replace))).apply(instance, TableResultModifier::new));
    private final ResourceKey<LootTable> table;
    private final boolean replace;
    public TableResultModifier(LootItemCondition[] conditions, ResourceKey<LootTable> table, boolean replace) { super(conditions); this.table = table; this.replace = replace; }
    @Override protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generated, LootContext context) {
        var reference = context.getLevel().getServer().reloadableRegistries().lookup().lookup(Registries.LOOT_TABLE).flatMap(registry -> registry.get(table));
        if (reference.isEmpty()) return generated;
        var result = replace ? new ObjectArrayList<ItemStack>() : generated;
        // Raw generation avoids re-entering global modifiers for the same source context.
        reference.get().value().getRandomItemsRaw(context, result::add); return result;
    }
    @Override public MapCodec<? extends IGlobalLootModifier> codec() { return CODEC; }
}
