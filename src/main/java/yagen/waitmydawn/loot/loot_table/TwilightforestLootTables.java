package yagen.waitmydawn.loot.loot_table;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.loot.AppendLootModifier;
import yagen.waitmydawn.loot.RandomizeModFunction;
import yagen.waitmydawn.registries.ItemRegistry;
import yagen.waitmydawn.util.SupportedMod;

import java.util.List;
import java.util.function.BiConsumer;

public class TwilightforestLootTables {
    public static final String NAMESPACE = YagensAttributes.MODID;

    public static void registerAllTwilightforest(GlobalLootModifierProvider provider) {
        final String SUPPORTED_MOD = SupportedMod.TWILIGHTFOREST.getValue();

        for (String path : CHESTS_LOOT_TABLES) {
            String appendId = "append_to_" + path.replace('/', '_');
            String tableId = SUPPORTED_MOD + ":chests/" + path;
            String poolId = NAMESPACE + ":chests/" + SUPPORTED_MOD + "_additional_" + path.replace('/', '_');
            provider.add(appendId,
                    new AppendLootModifier(
                            new LootItemCondition[]{
                                    new LootTableIdCondition.Builder(ResourceLocation.parse(tableId)).build()
                            },
                            poolId
                    ));
        }

        for (String path : ENTITIES_LOOT_TABLES) {
            String appendId = "append_to_" + path.replace('/', '_');
            String tableId = SUPPORTED_MOD + ":entities/" + path;
            String poolId = NAMESPACE + ":entities/" + SUPPORTED_MOD + "_additional_" + path.replace('/', '_');
            provider.add(appendId,
                    new AppendLootModifier(
                            new LootItemCondition[]{
                                    new LootTableIdCondition.Builder(ResourceLocation.parse(tableId)).build()
                            },
                            poolId
                    ));
        }

        for (String path : BOSSES_LOOT_TABLES) {
            String appendId = "append_to_" + path.replace('/', '_');
            String tableId = SUPPORTED_MOD + ":entities/" + path;
            String poolId = NAMESPACE + ":entities/bosses/" + SUPPORTED_MOD + "_additional_" + path.replace('/', '_');
            provider.add(appendId,
                    new AppendLootModifier(
                            new LootItemCondition[]{
                                    new LootTableIdCondition.Builder(ResourceLocation.parse(tableId)).build()
                            },
                            poolId
                    ));
        }
    }

    public static void acceptAllTwilightforest(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> consumer) {
        for (String path : CHESTS_LOOT_TABLES) {
            String tableKey = "chests/twilightforest_additional_" + path.replace('/', '_');
            consumer.accept(
                    ResourceKey.create(Registries.LOOT_TABLE,
                            ResourceLocation.fromNamespaceAndPath(NAMESPACE, tableKey)),
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(LootItem.lootTableItem(ItemRegistry.MOD_ESSENCE.get())
                                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2)))))
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(LootItem.lootTableItem(ItemRegistry.MOD.get())
                                            .apply(RandomizeModFunction.builder(50, 40, 10, 0, 0, 50)))
                                    .when(LootItemRandomChanceCondition.randomChance(0.5f))
                            ));
        }
        for (String path : ENTITIES_LOOT_TABLES) {
            String tableKey = "entities/twilightforest_additional_" + path.replace('/', '_');
            consumer.accept(
                    ResourceKey.create(Registries.LOOT_TABLE,
                            ResourceLocation.fromNamespaceAndPath(NAMESPACE, tableKey)),
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(LootItem.lootTableItem(ItemRegistry.MOD_ESSENCE.get())
                                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))))
                                    .when(LootItemRandomChanceCondition.randomChance(0.015f)))
                            );
        }
        for (String path : BOSSES_LOOT_TABLES) {
            String tableKey = "entities/bosses/twilightforest_additional_" + path.replace('/', '_');
            consumer.accept(
                    ResourceKey.create(Registries.LOOT_TABLE,
                            ResourceLocation.fromNamespaceAndPath(NAMESPACE, tableKey)),
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(LootItem.lootTableItem(ItemRegistry.MOD_ESSENCE.get())
                                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 32)))))
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(LootItem.lootTableItem(ItemRegistry.MOD.get())
                                            .apply(RandomizeModFunction.builder(30, 40, 10, 0, 0, 50)))
                                    )
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(LootItem.lootTableItem(ItemRegistry.MOD.get())
                                            .apply(RandomizeModFunction.builder(20, 50, 30, 0, 20, 70)))
                                    .when(LootItemRandomChanceCondition.randomChance(0.8f))
                            ));
        }
    }

    public static final List<String> CHESTS_LOOT_TABLES = List.of(
            "aurora_cache",
            "aurora_room",
            "basement",
            "darktower_boss",
            "darktower_cache",
            "darktower_key",
            "fancy_well",
            "foundation_basement",
            "graveyard",
            "hedge_maze",
            "hill_1",
            "hill_2",
            "hill_3",
            "labyrinth_dead_end",
            "labyrinth_room",
            "labyrinth_vault",
            "labyrinth_vault_jackpot",
            "quest_grove_dropper",
            "stronghold_boss",
            "stronghold_cache",
            "stronghold_room",
            "tower_library",
            "tower_room",
            "tree_cache",
            "troll_garden",
            "troll_vault",
            "troll_vault_with_lamp",
            "useless",
            "well"
    );

    public static final List<String> BOSSES_LOOT_TABLES = List.of(
            "naga","lich","hydra","minoshroom","knight_phantom","ur_ghast","alpha_yeti","snow_queen"
    );

    public static final List<String> ENTITIES_LOOT_TABLES = List.of(
            "adherent",
            "armored_giant",
            "bighorn_sheep",
            "blockchain_goblin",
            "boar",
            "carminite_broodling",
            "carminite_ghastguard",
            "carminite_ghastling",
            "carminite_golem",
            "death_tome",
            "death_tome_books",
            "death_tome_hurt",
            "deer",
            "dwarf_rabbit",
            "fire_beetle",
            "giant_miner",
            "harbinger_cube",
            "hedge_spider",
            "helmet_crab",
            "hostile_wolf",
            "ice_crystal",
            "king_spider",
            "kobold",
            "lich_minion",
            "lower_goblin_knight",
            "loyal_zombie",
            "maze_slime",
            "minotaur",
            "mist_wolf",
            "mosquito_swarm",
            "penguin",
            "pinch_beetle",
            "plateau_boss",
            "questing_ram_rewards",
            "quest_ram",
            "raven",
            "redcap",
            "redcap_sapper",
            "rising_zombie",
            "roving_cube",
            "skeleton_druid",
            "slime_beetle",
            "snow_guardian",
            "squirrel",
            "stable_ice_core",
            "swarm_spider",
            "tiny_bird",
            "towerwood_borer",
            "troll",
            "unstable_ice_core",
            "upper_goblin_knight",
            "winter_wolf",
            "wraith",
            "yeti"
    );
}
