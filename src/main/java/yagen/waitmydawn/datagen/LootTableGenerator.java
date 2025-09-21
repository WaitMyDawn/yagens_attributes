package yagen.waitmydawn.datagen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.IModContainer;
import yagen.waitmydawn.loot.*;
import yagen.waitmydawn.registries.BlockRegistry;
import yagen.waitmydawn.registries.ItemRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import yagen.waitmydawn.loot.RandomizeModFunction;

public class LootTableGenerator {
    static class BlocksGenerator extends BlockLootSubProvider {
        HashSet<Block> knownBlocks = new HashSet<>();
        private static final Set<Item> EXPLOSION_RESISTANT = Stream.of(
                        BlockRegistry.MOD_OPERATION_BLOCK.get(),
                        BlockRegistry.MOD_RECYCLE_BLOCK.get()
                )
                .map(ItemLike::asItem)
                .collect(Collectors.toSet());

        public BlocksGenerator(HolderLookup.Provider pRegistries) {
            super(EXPLOSION_RESISTANT, FeatureFlags.REGISTRY.allFlags(), pRegistries);
        }

        @Override
        protected void generate() {
            this.add(BlockRegistry.MOD_ESSENCE_BLOCK.get(), p_249875_ -> this.createSingleItemTable(ItemRegistry.MOD_ESSENCE_BLOCK_ITEM.get()));
            this.add(BlockRegistry.MOD_OPERATION_BLOCK.get(), p_249875_ -> this.createSingleItemTable(ItemRegistry.MOD_OPERATION_BLOCK_ITEM.get()));
            this.add(BlockRegistry.MOD_RECYCLE_BLOCK.get(), p_249875_ -> this.createSingleItemTable(ItemRegistry.MOD_RECYCLE_BLOCK_ITEM.get()));
        }

        protected LootTable.Builder createMultipleDrops(Block pBlock, Item item, float minDrops, float maxDrops) {
            HolderLookup.RegistryLookup<Enchantment> registrylookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
            return this.createSilkTouchDispatchTable(pBlock,
                    this.applyExplosionDecay(pBlock, LootItem.lootTableItem(item)
                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(minDrops, maxDrops)))
                            .apply(ApplyBonusCount.addOreBonusCount(registrylookup.getOrThrow(Enchantments.FORTUNE)))
                    ));
        }

        @Override
        protected void add(Block pBlock, Function<Block, LootTable.Builder> pFactory) {
            knownBlocks.add(pBlock);
            super.add(pBlock, pFactory);
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return knownBlocks;
        }
    }

    static class ChestsGenerator implements LootTableSubProvider {
        private final HolderLookup.Provider registries;

        public ChestsGenerator(HolderLookup.Provider registries) {
            this.registries = registries;
        }

        @Override
        public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> consumer) {
            consumer.accept(
                    ResourceKey.create(
                            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("yagens_attributes",
                                    "chests/additional_ancient_city_loot")),
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD_ESSENCE.get())
                                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 12)))))
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.KUVA.get())
                                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3)))))
                            .withPool(
                                    LootPool.lootPool()
                                            .setRolls(ConstantValue.exactly(1))
                                            .add(
                                                    LootItem.lootTableItem(ItemRegistry.MOD_ESSENCE_BLOCK_ITEM.get())
                                                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))))
                                            .when(LootItemRandomChanceCondition.randomChance(0.30f)))
                            .withPool(
                                    LootPool.lootPool()
                                            .setRolls(ConstantValue.exactly(1))
                                            .add(
                                                    LootItem.lootTableItem(ItemRegistry.UNKNOWN_RIVEN.get())
                                                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))))
                                            .when(LootItemRandomChanceCondition.randomChance(0.10f)))
                            .withPool(
                                    LootPool.lootPool()
                                            .setRolls(ConstantValue.exactly(1))
                                            .add(
                                                    LootItem.lootTableItem(ItemRegistry.MOD.get())
                                                            .apply(RandomizeModFunction.builder(50, 40, 10, 0, 10, 60)))
                                            .when(LootItemRandomChanceCondition.randomChance(0.9f)))
                            .withPool(
                                    LootPool.lootPool()
                                            .setRolls(ConstantValue.exactly(1))
                                            .add(
                                                    LootItem.lootTableItem(ItemRegistry.MOD.get())
                                                            .apply(RandomizeModFunction.builder(0, 50, 40, 10, 40, 80)))
                                            .when(LootItemRandomChanceCondition.randomChance(0.8f)))
            );
            consumer.accept(
                    ResourceKey.create(
                            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("yagens_attributes",
                                    "chests/additional_igloo_chest_loot")),
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD_ESSENCE.get())
                                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 4)))))
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD.get())
                                                    .apply(SpecializeModFunction.builder("yagens_attributes:cold_tool_mod", 5, 80)))
                            )
            );
            consumer.accept(
                    ResourceKey.create(
                            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("yagens_attributes",
                                    "chests/additional_abandoned_mineshaft_loot")),
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD_ESSENCE.get())
                                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 4))))
                                    .when(LootItemRandomChanceCondition.randomChance(0.3f)))
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD.get())
                                                    .apply(RandomizeModFunction.builder(50, 40, 10, 0, 0, 30)))
                                    .when(LootItemRandomChanceCondition.randomChance(0.35f)))
            );
            consumer.accept(
                    ResourceKey.create(
                            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("yagens_attributes",
                                    "chests/additional_bastion_bridge_loot")),
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD_ESSENCE.get())
                                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 4))))
                                    .when(LootItemRandomChanceCondition.randomChance(0.8f)))
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD.get())
                                                    .apply(RandomizeModFunction.builder(50, 40, 15, 0, 0, 30)))
                                    .when(LootItemRandomChanceCondition.randomChance(0.6f)))
            );
            consumer.accept(
                    ResourceKey.create(
                            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("yagens_attributes",
                                    "chests/additional_bastion_hoglin_stable_loot")),
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD_ESSENCE.get())
                                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 4))))
                                    .when(LootItemRandomChanceCondition.randomChance(0.8f)))
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD.get())
                                                    .apply(RandomizeModFunction.builder(50, 40, 15, 0, 0, 30)))
                                    .when(LootItemRandomChanceCondition.randomChance(0.6f)))
            );
            consumer.accept(
                    ResourceKey.create(
                            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("yagens_attributes",
                                    "chests/additional_bastion_other_loot")),
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD_ESSENCE.get())
                                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 4))))
                                    .when(LootItemRandomChanceCondition.randomChance(0.8f)))
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD.get())
                                                    .apply(RandomizeModFunction.builder(50, 40, 15, 0, 0, 30)))
                                    .when(LootItemRandomChanceCondition.randomChance(0.6f)))
            );
            consumer.accept(
                    ResourceKey.create(
                            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("yagens_attributes",
                                    "chests/additional_bastion_treasure_loot")),
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD_ESSENCE.get())
                                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 7))))
                            )
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.KUVA.get())
                                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))))
                            )
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD.get())
                                                    .apply(RandomizeModFunction.builder(50, 40, 15, 0, 0, 80)))
                            )
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD.get())
                                                    .apply(RandomizeModFunction.builder(20, 30, 15, 0, 20, 60)))
                                    .when(LootItemRandomChanceCondition.randomChance(0.8f)))
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD.get())
                                                    .apply(SpecializeModFunction.builder("yagens_attributes:heat_tool_mod", 60, 80)))
                            )
            );
            consumer.accept(
                    ResourceKey.create(
                            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("yagens_attributes",
                                    "chests/additional_buried_treasure_loot")),
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD_ESSENCE.get())
                                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 5))))
                            )
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD.get())
                                                    .apply(RandomizeModFunction.builder(50, 40, 10, 0, 0, 40)))
                            )
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD.get())
                                                    .apply(RandomizeModFunction.builder(50, 40, 10, 0, 0, 40)))
                                    .when(LootItemRandomChanceCondition.randomChance(0.4f)))
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD.get())
                                                    .apply(RandomizeModFunction.builder(50, 40, 10, 0, 0, 40)))
                                    .when(LootItemRandomChanceCondition.randomChance(0.4f)))
            );
            consumer.accept(
                    ResourceKey.create(
                            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("yagens_attributes",
                                    "chests/additional_desert_pyramid_loot")),
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD_ESSENCE.get())
                                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))
                                    .when(LootItemRandomChanceCondition.randomChance(0.5f)))
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD.get())
                                                    .apply(RandomizeModFunction.builder(50, 40, 10, 0, 0, 40)))
                                    .when(LootItemRandomChanceCondition.randomChance(0.8f)))
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD.get())
                                                    .apply(RandomizeModFunction.builder(50, 40, 10, 0, 0, 30)))
                                    .when(LootItemRandomChanceCondition.randomChance(0.2f)))
            );
            consumer.accept(
                    ResourceKey.create(
                            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("yagens_attributes",
                                    "chests/additional_end_city_treasure_loot")),
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD_ESSENCE.get())
                                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 8))))
                            )
                            .withPool(
                                    LootPool.lootPool()
                                            .setRolls(ConstantValue.exactly(1))
                                            .add(
                                                    LootItem.lootTableItem(ItemRegistry.UNKNOWN_RIVEN.get())
                                                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))))
                                            .when(LootItemRandomChanceCondition.randomChance(0.10f)))
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD.get())
                                                    .apply(RandomizeModFunction.builder(50, 40, 10, 0, 60, 90)))
                                    .when(LootItemRandomChanceCondition.randomChance(0.8f)))
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD.get())
                                                    .apply(RandomizeModFunction.builder(30, 20, 10, 1, 20, 70)))
                                    .when(LootItemRandomChanceCondition.randomChance(0.5f)))
            );
            consumer.accept(
                    ResourceKey.create(
                            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("yagens_attributes",
                                    "chests/additional_jungle_temple_loot")),
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD_ESSENCE.get())
                                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 6))))
                            )
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD.get())
                                                    .apply(RandomizeModFunction.builder(50, 40, 10, 0, 0, 40)))
                                    .when(LootItemRandomChanceCondition.randomChance(0.8f)))
            );
            consumer.accept(
                    ResourceKey.create(
                            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("yagens_attributes",
                                    "chests/additional_nether_bridge_loot")),
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD_ESSENCE.get())
                                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5))))
                                    .when(LootItemRandomChanceCondition.randomChance(0.9f)))
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD.get())
                                                    .apply(RandomizeModFunction.builder(50, 40, 10, 0, 10, 60)))
                                    .when(LootItemRandomChanceCondition.randomChance(0.9f)))
            );
            consumer.accept(
                    ResourceKey.create(
                            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("yagens_attributes",
                                    "chests/additional_pillager_outpost_loot")),
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD_ESSENCE.get())
                                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))
                                    .when(LootItemRandomChanceCondition.randomChance(0.7f)))
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD.get())
                                                    .apply(RandomizeModFunction.builder(50, 40, 0, 0, 0, 50)))
                                    .when(LootItemRandomChanceCondition.randomChance(0.7f)))
            );
            consumer.accept(
                    ResourceKey.create(
                            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("yagens_attributes",
                                    "chests/additional_ruined_portal_loot")),
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD_ESSENCE.get())
                                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))
                                    .when(LootItemRandomChanceCondition.randomChance(0.7f)))
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD.get())
                                                    .apply(RandomizeModFunction.builder(50, 40, 0, 0, 0, 50)))
                                    .when(LootItemRandomChanceCondition.randomChance(0.3f)))
            );
            consumer.accept(
                    ResourceKey.create(
                            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("yagens_attributes",
                                    "chests/additional_shipwreck_treasure_loot")),
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD_ESSENCE.get())
                                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))
                                    .when(LootItemRandomChanceCondition.randomChance(0.7f)))
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD_ESSENCE.get())
                                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))
                                    .when(LootItemRandomChanceCondition.randomChance(0.7f)))
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD.get())
                                                    .apply(RandomizeModFunction.builder(50, 40, 5, 0, 0, 50)))
                                    .when(LootItemRandomChanceCondition.randomChance(0.5f)))
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD.get())
                                                    .apply(RandomizeModFunction.builder(50, 40, 5, 0, 0, 50)))
                                    .when(LootItemRandomChanceCondition.randomChance(0.5f)))
            );
            consumer.accept(
                    ResourceKey.create(
                            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("yagens_attributes",
                                    "chests/additional_simple_dungeon_loot")),
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD_ESSENCE.get())
                                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))
                                    .when(LootItemRandomChanceCondition.randomChance(0.7f)))
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD.get())
                                                    .apply(RandomizeModFunction.builder(50, 40, 0, 0, 0, 50)))
                                    .when(LootItemRandomChanceCondition.randomChance(0.5f)))
            );
            consumer.accept(
                    ResourceKey.create(
                            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("yagens_attributes",
                                    "chests/additional_stronghold_corridor_loot")),
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD_ESSENCE.get())
                                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))
                                    .when(LootItemRandomChanceCondition.randomChance(0.7f)))
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD.get())
                                                    .apply(RandomizeModFunction.builder(50, 40, 5, 0, 0, 50)))
                                    .when(LootItemRandomChanceCondition.randomChance(0.5f)))
            );
            consumer.accept(
                    ResourceKey.create(
                            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("yagens_attributes",
                                    "chests/additional_stronghold_crossing_loot")),
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD_ESSENCE.get())
                                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))
                                    .when(LootItemRandomChanceCondition.randomChance(0.7f)))
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD.get())
                                                    .apply(RandomizeModFunction.builder(50, 40, 5, 0, 0, 50)))
                                    .when(LootItemRandomChanceCondition.randomChance(0.5f)))
            );
            consumer.accept(
                    ResourceKey.create(
                            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("yagens_attributes",
                                    "chests/additional_stronghold_library_loot")),
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD_ESSENCE.get())
                                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 8))))
                            )
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD.get())
                                                    .apply(RandomizeModFunction.builder(50, 40, 20, 5, 0, 50)))
                                    .when(LootItemRandomChanceCondition.randomChance(0.8f)))
            );
            consumer.accept(
                    ResourceKey.create(
                            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("yagens_attributes",
                                    "chests/additional_underwater_ruin_big_loot")),
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD_ESSENCE.get())
                                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 8))))
                                    .when(LootItemRandomChanceCondition.randomChance(0.6f)))
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD.get())
                                                    .apply(RandomizeModFunction.builder(50, 40, 2, 0, 0, 50)))
                                    .when(LootItemRandomChanceCondition.randomChance(0.1f)))
            );
            consumer.accept(
                    ResourceKey.create(
                            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("yagens_attributes",
                                    "chests/additional_underwater_ruin_small_loot")),
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD_ESSENCE.get())
                                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 4))))
                                    .when(LootItemRandomChanceCondition.randomChance(0.4f)))
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD.get())
                                                    .apply(RandomizeModFunction.builder(50, 40, 0, 0, 0, 50)))
                                    .when(LootItemRandomChanceCondition.randomChance(0.1f)))
            );
            consumer.accept(
                    ResourceKey.create(
                            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("yagens_attributes",
                                    "chests/additional_woodland_mansion_loot")),
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD_ESSENCE.get())
                                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 6))))
                                    .when(LootItemRandomChanceCondition.randomChance(0.8f)))
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD.get())
                                                    .apply(RandomizeModFunction.builder(50, 40, 10, 0, 0, 50)))
                                    .when(LootItemRandomChanceCondition.randomChance(0.6f)))
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD.get())
                                                    .apply(RandomizeModFunction.builder(50, 40, 10, 0, 0, 50)))
                                    .when(LootItemRandomChanceCondition.randomChance(0.6f)))
            );
            consumer.accept(
                    ResourceKey.create(
                            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("yagens_attributes",
                                    "spawners/ominous/trial_chamber/additional_tco_consumables_loot")),
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD_ESSENCE.get())
                                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))))
                                    .when(LootItemRandomChanceCondition.randomChance(0.8f)))
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD.get())
                                                    .apply(RandomizeModFunction.builder(50, 40, 10, 0, 0, 50)))
                                    .when(LootItemRandomChanceCondition.randomChance(0.2f)))
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.KUVA.get())
                                                    .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))))
                                    .when(LootItemRandomChanceCondition.randomChance(0.5f)))
            );
            consumer.accept(
                    ResourceKey.create(
                            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("yagens_attributes",
                                    "chests/trial_chamber/additional_reward_ominous_loot")),
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD_ESSENCE.get())
                                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(7, 18))))
                            )
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD_ESSENCE_BLOCK_ITEM.get())
                                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))))
                                    .when(LootItemRandomChanceCondition.randomChance(0.4f)))
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD.get())
                                                    .apply(RandomizeModFunction.builder(50, 40, 10, 0, 0, 50)))
                                    .when(LootItemRandomChanceCondition.randomChance(0.8f)))
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.KUVA.get())
                                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 7)))))
            );
            consumer.accept(
                    ResourceKey.create(
                            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("yagens_attributes",
                                    "entities/additional_witch_loot")),
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD_ESSENCE.get())
                                                    .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))))
                                    .when(LootItemRandomChanceCondition.randomChance(0.02f)))
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD.get())
                                                    .apply(SpecializeModFunction.builder("yagens_attributes:toxin_tool_mod", 5, 60)))
                                    .when(LootItemRandomChanceCondition.randomChance(0.05f)))
            );
            consumer.accept(
                    ResourceKey.create(
                            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("yagens_attributes",
                                    "entities/additional_breeze_loot")),
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD_ESSENCE.get())
                                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))))
                                    .when(LootItemRandomChanceCondition.randomChance(0.05f)))
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD.get())
                                                    .apply(SpecializeModFunction.builder("yagens_attributes:fury_tool_mod", 0, 50)))
                                    .when(LootItemRandomChanceCondition.randomChance(0.05f)))
            );
            consumer.accept(
                    ResourceKey.create(
                            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("yagens_attributes",
                                    "entities/additional_elder_guardian_loot")),
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD_ESSENCE.get())
                                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 7))))
                                    .when(LootItemRandomChanceCondition.randomChance(0.8f)))
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD.get())
                                                    .apply(RandomizeModFunction.builder(500, 400, 100, 1, 0, 50)))
                                    .when(LootItemRandomChanceCondition.randomChance(0.8f)))
            );
            consumer.accept(
                    ResourceKey.create(
                            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("yagens_attributes",
                                    "entities/additional_warden_loot")),
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD_ESSENCE.get())
                                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 7))))
                                    .when(LootItemRandomChanceCondition.randomChance(0.8f)))
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD.get())
                                                    .apply(RandomizeModFunction.builder(35, 45, 15, 5, 0, 50)))
                            )
            );
            consumer.accept(
                    ResourceKey.create(
                            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("yagens_attributes",
                                    "entities/additional_wither_loot")),
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD_ESSENCE.get())
                                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 5))))
                            )
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.KUVA.get())
                                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 6))))
                                    .when(LootItemRandomChanceCondition.randomChance(0.5f)))
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD.get())
                                                    .apply(RandomizeModFunction.builder(20, 40, 40, 0, 0, 50)))
                            )
            );
            consumer.accept(
                    ResourceKey.create(
                            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("yagens_attributes",
                                    "entities/additional_ender_dragon_loot")),
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD_ESSENCE.get())
                                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 14))))
                            )
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.UNKNOWN_RIVEN.get())
                                                    .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))))
                                    .when(LootItemRandomChanceCondition.randomChance(0.5f)))
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(
                                            LootItem.lootTableItem(ItemRegistry.MOD.get())
                                                    .apply(RandomizeModFunction.builder(20, 50, 30, 0, 0, 50)))
                            )
            );
            // generate bottom
        }
    }

    static class GlobalModifierGenerator extends GlobalLootModifierProvider {
        public GlobalModifierGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries, YagensAttributes.MODID);
        }

        @Override
        protected void start() {
            this.add("carrot_to_short_grass",
                    new AddItemModifier(new LootItemCondition[]{
                            LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.SHORT_GRASS).build(),
                            LootItemRandomChanceCondition.randomChance(0.1f).build()},
                            Items.CARROT));

            this.add("carrot_to_tall_grass",
                    new AddItemModifier(new LootItemCondition[]{
                            LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.TALL_GRASS).build(),
                            LootItemRandomChanceCondition.randomChance(0.1f).build()},
                            Items.CARROT));

            this.add("potato_to_short_grass",
                    new AddItemModifier(new LootItemCondition[]{
                            LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.SHORT_GRASS).build(),
                            LootItemRandomChanceCondition.randomChance(0.1f).build()},
                            Items.POTATO));

            this.add("potato_to_tall_grass",
                    new AddItemModifier(new LootItemCondition[]{
                            LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.TALL_GRASS).build(),
                            LootItemRandomChanceCondition.randomChance(0.1f).build()},
                            Items.POTATO));

            this.add("sniffer_egg_from_sniffer",
                    new AddItemModifier(new LootItemCondition[]{
                            new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace(
                                    "entities/sniffer")).build()},
                            Items.SNIFFER_EGG));

            this.add("mod_essence_from_creeper",
                    new AddItemModifier(new LootItemCondition[]{
                            new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace(
                                    "entities/creeper")).build(),
                            LootItemRandomChanceCondition.randomChance(0.01f).build()},
                            ItemRegistry.MOD_ESSENCE.get()));

            this.add("mod_essence_from_evoker",
                    new AddItemModifier(new LootItemCondition[]{
                            new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace(
                                    "entities/evoker")).build(),
                            LootItemRandomChanceCondition.randomChance(0.03f).build()},
                            ItemRegistry.MOD_ESSENCE.get()));

            this.add("mod_essence_from_vex",
                    new AddItemModifier(new LootItemCondition[]{
                            new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace(
                                    "entities/vex")).build(),
                            LootItemRandomChanceCondition.randomChance(0.02f).build()},
                            ItemRegistry.MOD_ESSENCE.get()));

            this.add("mod_essence_from_shulker",
                    new AddItemModifier(new LootItemCondition[]{
                            new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace(
                                    "entities/shulker")).build(),
                            LootItemRandomChanceCondition.randomChance(0.02f).build()},
                            ItemRegistry.MOD_ESSENCE.get()));

            this.add("mod_essence_from_blaze",
                    new AddItemModifier(new LootItemCondition[]{
                            new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace(
                                    "entities/blaze")).build(),
                            LootItemRandomChanceCondition.randomChance(0.015f).build()},
                            ItemRegistry.MOD_ESSENCE.get()));

            this.add("mod_essence_from_wither_skeleton",
                    new AddItemModifier(new LootItemCondition[]{
                            new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace(
                                    "entities/wither_skeleton")).build(),
                            LootItemRandomChanceCondition.randomChance(0.015f).build()},
                            ItemRegistry.MOD_ESSENCE.get()));

            /**
             * AppendLootModifier append loot pools, the pools generated by ChestsGenerator
             */
            this.add("append_to_ancient_city",
                    new AppendLootModifier(
                            new LootItemCondition[]{
                                    new LootTableIdCondition.Builder(ResourceLocation.parse("minecraft:chests/ancient_city")).build()
                            },
                            "yagens_attributes:chests/additional_ancient_city_loot"
                    ));
            this.add("append_to_igloo_chest",
                    new AppendLootModifier(
                            new LootItemCondition[]{
                                    new LootTableIdCondition.Builder(ResourceLocation.parse("minecraft:chests/igloo_chest")).build()
                            },
                            "yagens_attributes:chests/additional_igloo_chest_loot"
                    ));
            this.add("append_to_bastion_bridge",
                    new AppendLootModifier(
                            new LootItemCondition[]{
                                    new LootTableIdCondition.Builder(ResourceLocation.parse("minecraft:chests/bastion_bridge")).build()
                            },
                            "yagens_attributes:chests/additional_bastion_bridge_loot"
                    ));
            this.add("append_to_bastion_hoglin_stable",
                    new AppendLootModifier(
                            new LootItemCondition[]{
                                    new LootTableIdCondition.Builder(ResourceLocation.parse("minecraft:chests/bastion_hoglin_stable")).build()
                            },
                            "yagens_attributes:chests/additional_bastion_hoglin_stable_loot"
                    ));
            this.add("append_to_bastion_other",
                    new AppendLootModifier(
                            new LootItemCondition[]{
                                    new LootTableIdCondition.Builder(ResourceLocation.parse("minecraft:chests/bastion_other")).build()
                            },
                            "yagens_attributes:chests/additional_bastion_other_loot"
                    ));
            this.add("append_to_bastion_treasure",
                    new AppendLootModifier(
                            new LootItemCondition[]{
                                    new LootTableIdCondition.Builder(ResourceLocation.parse("minecraft:chests/bastion_treasure")).build()
                            },
                            "yagens_attributes:chests/additional_bastion_treasure_loot"
                    ));
            this.add("append_to_buried_treasure",
                    new AppendLootModifier(
                            new LootItemCondition[]{
                                    new LootTableIdCondition.Builder(ResourceLocation.parse("minecraft:chests/buried_treasure")).build()
                            },
                            "yagens_attributes:chests/additional_buried_treasure_loot"
                    ));
            this.add("append_to_desert_pyramid",
                    new AppendLootModifier(
                            new LootItemCondition[]{
                                    new LootTableIdCondition.Builder(ResourceLocation.parse("minecraft:chests/desert_pyramid")).build()
                            },
                            "yagens_attributes:chests/additional_desert_pyramid_loot"
                    ));
            this.add("append_to_end_city_treasure",
                    new AppendLootModifier(
                            new LootItemCondition[]{
                                    new LootTableIdCondition.Builder(ResourceLocation.parse("minecraft:chests/end_city_treasure")).build()
                            },
                            "yagens_attributes:chests/additional_end_city_treasure_loot"
                    ));
            this.add("append_to_jungle_temple",
                    new AppendLootModifier(
                            new LootItemCondition[]{
                                    new LootTableIdCondition.Builder(ResourceLocation.parse("minecraft:chests/jungle_temple")).build()
                            },
                            "yagens_attributes:chests/additional_jungle_temple_loot"
                    ));
            this.add("append_to_nether_bridge",
                    new AppendLootModifier(
                            new LootItemCondition[]{
                                    new LootTableIdCondition.Builder(ResourceLocation.parse("minecraft:chests/nether_bridge")).build()
                            },
                            "yagens_attributes:chests/additional_nether_bridge_loot"
                    ));
            this.add("append_to_pillager_outpost",
                    new AppendLootModifier(
                            new LootItemCondition[]{
                                    new LootTableIdCondition.Builder(ResourceLocation.parse("minecraft:chests/pillager_outpost")).build()
                            },
                            "yagens_attributes:chests/additional_pillager_outpost_loot"
                    ));
            this.add("append_to_ruined_portal",
                    new AppendLootModifier(
                            new LootItemCondition[]{
                                    new LootTableIdCondition.Builder(ResourceLocation.parse("minecraft:chests/ruined_portal")).build()
                            },
                            "yagens_attributes:chests/additional_ruined_portal_loot"
                    ));
            this.add("append_to_shipwreck_treasure",
                    new AppendLootModifier(
                            new LootItemCondition[]{
                                    new LootTableIdCondition.Builder(ResourceLocation.parse("minecraft:chests/shipwreck_treasure")).build()
                            },
                            "yagens_attributes:chests/additional_shipwreck_treasure_loot"
                    ));
            this.add("append_to_simple_dungeon",
                    new AppendLootModifier(
                            new LootItemCondition[]{
                                    new LootTableIdCondition.Builder(ResourceLocation.parse("minecraft:chests/simple_dungeon")).build()
                            },
                            "yagens_attributes:chests/additional_simple_dungeon_loot"
                    ));
            this.add("append_to_stronghold_corridor",
                    new AppendLootModifier(
                            new LootItemCondition[]{
                                    new LootTableIdCondition.Builder(ResourceLocation.parse("minecraft:chests/stronghold_corridor")).build()
                            },
                            "yagens_attributes:chests/additional_stronghold_corridor_loot"
                    ));
            this.add("append_to_stronghold_crossing",
                    new AppendLootModifier(
                            new LootItemCondition[]{
                                    new LootTableIdCondition.Builder(ResourceLocation.parse("minecraft:chests/stronghold_crossing")).build()
                            },
                            "yagens_attributes:chests/additional_stronghold_crossing_loot"
                    ));
            this.add("append_to_stronghold_library",
                    new AppendLootModifier(
                            new LootItemCondition[]{
                                    new LootTableIdCondition.Builder(ResourceLocation.parse("minecraft:chests/stronghold_library")).build()
                            },
                            "yagens_attributes:chests/additional_stronghold_library_loot"
                    ));
            this.add("append_to_underwater_ruin_big",
                    new AppendLootModifier(
                            new LootItemCondition[]{
                                    new LootTableIdCondition.Builder(ResourceLocation.parse("minecraft:chests/underwater_ruin_big")).build()
                            },
                            "yagens_attributes:chests/additional_underwater_ruin_big_loot"
                    ));
            this.add("append_to_underwater_ruin_small",
                    new AppendLootModifier(
                            new LootItemCondition[]{
                                    new LootTableIdCondition.Builder(ResourceLocation.parse("minecraft:chests/underwater_ruin_small")).build()
                            },
                            "yagens_attributes:chests/additional_underwater_ruin_small_loot"
                    ));
            this.add("append_to_woodland_mansion",
                    new AppendLootModifier(
                            new LootItemCondition[]{
                                    new LootTableIdCondition.Builder(ResourceLocation.parse("minecraft:chests/woodland_mansion")).build()
                            },
                            "yagens_attributes:chests/additional_woodland_mansion_loot"
                    ));
            /**
             * trial_chamber Loot
             */
            this.add("append_to_reward_ominous",
                    new AppendLootModifier(
                            new LootItemCondition[]{
                                    new LootTableIdCondition.Builder(ResourceLocation.parse("minecraft:chests/trial_chambers/reward_ominous")).build()
                            },
                            "yagens_attributes:chests/trial_chamber/additional_reward_ominous_loot"
                    ));
            this.add("append_to_tco_consumables",
                    new AppendLootModifier(
                            new LootItemCondition[]{
                                    new LootTableIdCondition.Builder(ResourceLocation.parse("minecraft:spawners/ominous/trial_chamber/consumables")).build()
                            },
                            "yagens_attributes:spawners/ominous/trial_chamber/additional_tco_consumables_loot"
                    ));
            /**
             * Entities Loot
             */
            this.add("append_to_witch",
                    new AppendLootModifier(
                            new LootItemCondition[]{
                                    new LootTableIdCondition.Builder(ResourceLocation.parse("minecraft:entities/witch")).build()
                            },
                            "yagens_attributes:entities/additional_witch_loot"
                    ));
            this.add("append_to_breeze",
                    new AppendLootModifier(
                            new LootItemCondition[]{
                                    new LootTableIdCondition.Builder(ResourceLocation.parse("minecraft:entities/breeze")).build()
                            },
                            "yagens_attributes:entities/additional_breeze_loot"
                    ));
            this.add("append_to_elder_guardian",
                    new AppendLootModifier(
                            new LootItemCondition[]{
                                    new LootTableIdCondition.Builder(ResourceLocation.parse("minecraft:entities/elder_guardian")).build()
                            },
                            "yagens_attributes:entities/additional_elder_guardian_loot"
                    ));
            this.add("append_to_warden",
                    new AppendLootModifier(
                            new LootItemCondition[]{
                                    new LootTableIdCondition.Builder(ResourceLocation.parse("minecraft:entities/warden")).build()
                            },
                            "yagens_attributes:entities/additional_warden_loot"
                    ));
            this.add("append_to_wither",
                    new AppendLootModifier(
                            new LootItemCondition[]{
                                    new LootTableIdCondition.Builder(ResourceLocation.parse("minecraft:entities/wither")).build()
                            },
                            "yagens_attributes:entities/additional_wither_loot"
                    ));
            this.add("append_to_ender_dragon",
                    new AppendLootModifier(
                            new LootItemCondition[]{
                                    new LootTableIdCondition.Builder(ResourceLocation.parse("minecraft:entities/ender_dragon")).build()
                            },
                            "yagens_attributes:entities/additional_ender_dragon_loot"
                    ));
        }

    }

}
