package yagen.waitmydawn.registries;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.loot.*;

import java.util.function.Supplier;

public class LootRegistry {
    public static final DeferredRegister<LootItemFunctionType<?>> LOOT_FUNCTIONS = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, YagensAttributes.MODID);
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, YagensAttributes.MODID);

    public static void register(IEventBus eventBus) {
        LOOT_FUNCTIONS.register(eventBus);
        LOOT_MODIFIER_SERIALIZERS.register(eventBus);
    }

    public static final Supplier<MapCodec<? extends IGlobalLootModifier>> ADD_ITEM = LOOT_MODIFIER_SERIALIZERS.register("add_item", () -> AddItemModifier.CODEC);
    public static final Supplier<MapCodec<? extends IGlobalLootModifier>> APPEND_LOOT_MODIFIER = LOOT_MODIFIER_SERIALIZERS.register("append_loot", AppendLootModifier.CODEC);
    public static final Supplier<MapCodec<? extends IGlobalLootModifier>> REPLACE_LOOT_MODIFIER = LOOT_MODIFIER_SERIALIZERS.register("replace_loot", ReplaceLootModifier.CODEC);

    public static final Supplier<LootItemFunctionType<?>> RANDOMIZE_MOD_FUNCTION =
            LOOT_FUNCTIONS.register("randomize_mod", () -> new LootItemFunctionType<>(RandomizeModFunction.CODEC));
    public static final Supplier<LootItemFunctionType<?>> SPECIALIZE_MOD_FUNCTION =
            LOOT_FUNCTIONS.register("specialize_mod", () -> new LootItemFunctionType<>(SpecializeModFunction.CODEC));
    public static final Supplier<LootItemFunctionType<?>> SET_FORMA_FUNCTION =
            LOOT_FUNCTIONS.register("set_forma",
                    () -> new LootItemFunctionType<>(SetFormaFunction.CODEC));
    public static final Supplier<LootItemFunctionType<?>> SET_ENDO_FUNCTION =
            LOOT_FUNCTIONS.register("set_endo",
                    () -> new LootItemFunctionType<>(SetEndoFunction.CODEC));
    public static final Supplier<LootItemFunctionType<?>> RARITY_MOD_FUNCTION =
            LOOT_FUNCTIONS.register("rarity_mod",
                    () -> new LootItemFunctionType<>(RarityModFunction.CODEC));
}

