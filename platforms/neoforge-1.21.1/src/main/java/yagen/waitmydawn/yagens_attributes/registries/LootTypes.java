package yagen.waitmydawn.yagens_attributes.registries;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.*;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.loot.*;

public final class LootTypes {
    public static final DeferredRegister<LootPoolEntryType> ENTRIES = DeferredRegister.create(Registries.LOOT_POOL_ENTRY_TYPE, YagensAttributes.MODID);
    public static final DeferredHolder<LootPoolEntryType, LootPoolEntryType> MOD_REWARD = ENTRIES.register("mod_reward", () -> new LootPoolEntryType(ModRewardEntry.CODEC));
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> MODIFIERS = DeferredRegister.create(net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, YagensAttributes.MODID);
    public static final java.util.function.Supplier<MapCodec<TableResultModifier>> TABLE_RESULT = MODIFIERS.register("table_result", () -> TableResultModifier.CODEC);
    private LootTypes() {}
}
