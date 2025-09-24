package yagen.waitmydawn.loot.loot_table;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.loot.AppendLootModifier;
import yagen.waitmydawn.util.SupportedMod;

import java.util.List;

public class DungeonsAriseLootTables {
    public static void registerAllDungeonsArise(GlobalLootModifierProvider provider) {
        final String SUPPORTED_MOD = SupportedMod.DUNGEONS_ARISE.getValue();
        final String NAMESPACE = YagensAttributes.MODID;

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

        for (String path : POTS_LOOT_TABLES) {
            String appendId = "append_to_" + path.replace('/', '_');
            String tableId = SUPPORTED_MOD + ":pots/" + path;
            String poolId = NAMESPACE + ":pots/" + SUPPORTED_MOD + "_additional_" + path.replace('/', '_');
            provider.add(appendId,
                    new AppendLootModifier(
                            new LootItemCondition[]{
                                    new LootTableIdCondition.Builder(ResourceLocation.parse(tableId)).build()
                            },
                            poolId
                    ));
        }

        for (String path : SPAWNERS_LOOT_TABLES) {
            String appendId = "append_to_" + path.replace('/', '_');
            String tableId = SUPPORTED_MOD + ":spawners/" + path;
            String poolId = NAMESPACE + ":spawners/" + SUPPORTED_MOD + "_additional_" + path.replace('/', '_');
            provider.add(appendId,
                    new AppendLootModifier(
                            new LootItemCondition[]{
                                    new LootTableIdCondition.Builder(ResourceLocation.parse(tableId)).build()
                            },
                            poolId
                    ));
        }

        for (String path : ARCHEOLOGY_LOOT_TABLES) {
            String appendId = "append_to_" + path.replace('/', '_');
            String tableId = SUPPORTED_MOD + ":archeology/" + path;
            String poolId = NAMESPACE + ":archeology/" + SUPPORTED_MOD + "_additional_" + path.replace('/', '_');
            provider.add(appendId,
                    new AppendLootModifier(
                            new LootItemCondition[]{
                                    new LootTableIdCondition.Builder(ResourceLocation.parse(tableId)).build()
                            },
                            poolId
                    ));
        }
    }

    public static final List<String> CHESTS_LOOT_TABLES = List.of(
            "mines_treasure_big", "mines_treasure_medium", "mines_treasure_small",
            "abandoned_temple/abandoned_temple_entrance", "abandoned_temple/abandoned_temple_map", "abandoned_temple/abandoned_temple_top",
            "aviary/aviary_barrels", "aviary/aviary_normal", "aviary/aviary_treasure",
            "bandit_towers/bandit_towers_barrels", "bandit_towers/bandit_towers_gardens", "bandit_towers/bandit_towers_normal", "bandit_towers/bandit_towers_rooms", "bandit_towers/bandit_towers_supply", "bandit_towers/bandit_towers_treasure",
            "bandit_village/bandit_village_barrels", "bandit_village/bandit_village_normal", "bandit_village/bandit_village_supply", "bandit_village/bandit_village_tents",
            "bathhouse/bathhouse_barrels", "bathhouse/bathhouse_normal",
            "ceryneian_hind/ceryneian_hind_treasure",
            "coliseum/coliseum_treasure",
            "fishing_hut/fishing_hut_barrels",
            "foundry/foundry_chains", "foundry/foundry_lava_pit", "foundry/foundry_normal", "foundry/foundry_passage_exterior", "foundry/foundry_passage_normal", "foundry/foundry_treasure",
            "greenwood_pub/greenwood_pub_barrels_hallways", "greenwood_pub/greenwood_pub_barrels_normal", "greenwood_pub/greenwood_pub_normal",
            "heavenly_challenger/heavenly_challenger_normal", "heavenly_challenger/heavenly_challenger_supply", "heavenly_challenger/heavenly_challenger_theater", "heavenly_challenger/heavenly_challenger_treasure",
            "heavenly_conqueror/heavenly_conqueror_barrels", "heavenly_conqueror/heavenly_conqueror_normal", "heavenly_conqueror/heavenly_conqueror_treasure",
            "heavenly_rider/heavenly_rider_barrels", "heavenly_rider/heavenly_rider_normal", "heavenly_rider/heavenly_rider_treasure",
            "illager_campsite/illager_campsite_map", "illager_campsite/illager_campsite_supply", "illager_campsite/illager_campsite_tent",
            "illager_corsair/illager_corsair_barrels", "illager_corsair/illager_corsair_supply", "illager_corsair/illager_corsair_treasure",
            "illager_fort/illager_fort_barrels", "illager_fort/illager_fort_normal", "illager_fort/illager_fort_treasure",
            "illager_galley/illager_galley_barrels", "illager_galley/illager_galley_supply", "illager_galley/illager_galley_treasure",
            "illager_windmill/illager_windmill_barrels", "illager_windmill/illager_windmill_treasure",
            "infested_temple/infested_temple_room_bookshelf", "infested_temple/infested_temple_room_forge", "infested_temple/infested_temple_room_garden", "infested_temple/infested_temple_room_normal", "infested_temple/infested_temple_room_supply", "infested_temple/infested_temple_room_table", "infested_temple/infested_temple_top_treasure", "infested_temple/infested_temple_vault_normal", "infested_temple/infested_temple_vault_ominous", "infested_temple/infested_temple_vault_treasure",
            "jungle_tree_house/jungle_tree_house_barrels", "jungle_tree_house/jungle_tree_house_normal", "jungle_tree_house/jungle_tree_house_treasure",
            "keep_kayra/keep_kayra_garden_normal", "keep_kayra/keep_kayra_garden_treasure", "keep_kayra/keep_kayra_library_normal", "keep_kayra/keep_kayra_library_treasure", "keep_kayra/keep_kayra_normal", "keep_kayra/keep_kayra_treasure",
            "kisegi_sanctuary/kisegi_sanctuary_basement", "kisegi_sanctuary/kisegi_sanctuary_normal", "kisegi_sanctuary/kisegi_sanctuary_top", "kisegi_sanctuary/kisegi_sanctuary_treasure", "kisegi_sanctuary/kisegi_sanctuary_vault_normal", "kisegi_sanctuary/kisegi_sanctuary_vault_normal_treasure", "kisegi_sanctuary/kisegi_sanctuary_vault_ominous", "kisegi_sanctuary/kisegi_sanctuary_vault_ominous_treasure",
            "lighthouse/lighthouse_top",
            "mechanical_nest/mechanical_nest_equipment", "mechanical_nest/mechanical_nest_normal", "mechanical_nest/mechanical_nest_supply", "mechanical_nest/mechanical_nest_treasure",
            "merchant_campsite/merchant_campsite_map", "merchant_campsite/merchant_campsite_supply", "merchant_campsite/merchant_campsite_tent",
            "mining_system/mining_system_barrels", "mining_system/mining_system_treasure",
            "monastery/monastery_barrels", "monastery/monastery_bridges", "monastery/monastery_map",
            "mushroom_house/mushroom_house_barrels", "mushroom_house/mushroom_house_normal", "mushroom_house/mushroom_house_treasure",
            "mushroom_mines/mushroom_mines_barrels", "mushroom_mines/mushroom_mines_ores", "mushroom_mines/mushroom_mines_tools", "mushroom_mines/mushroom_mines_treasure",
            "mushroom_village/mushroom_village_barrels", "mushroom_village/mushroom_village_treasure",
            "plague_asylum/plague_asylum_barrels", "plague_asylum/plague_asylum_cells", "plague_asylum/plague_asylum_normal", "plague_asylum/plague_asylum_potions", "plague_asylum/plague_asylum_storage", "plague_asylum/plague_asylum_treasure",
            "scorched_mines/scorched_mines_barrels", "scorched_mines/scorched_mines_housing", "scorched_mines/scorched_mines_hub", "scorched_mines/scorched_mines_normal", "scorched_mines/scorched_mines_treasure",
            "shiraz_palace/shiraz_palace_elite", "shiraz_palace/shiraz_palace_gardens", "shiraz_palace/shiraz_palace_library", "shiraz_palace/shiraz_palace_normal", "shiraz_palace/shiraz_palace_rooms", "shiraz_palace/shiraz_palace_supply", "shiraz_palace/shiraz_palace_towers", "shiraz_palace/shiraz_palace_treasure",
            "small_blimp/small_blimp_coal_storage", "small_blimp/small_blimp_redstone_chamber", "small_blimp/small_blimp_treasure",
            "small_prairie_house/small_prairie_house_barrels", "small_prairie_house/small_prairie_house_normal", "small_prairie_house/small_prairie_house_ruined",
            "thornborn_towers/thornborn_towers_barrels", "thornborn_towers/thornborn_towers_rooms", "thornborn_towers/thornborn_towers_top_rooms", "thornborn_towers/thornborn_towers_top_treasure",
            "typhon/typhon_treasure",
            "undead_pirate_ship/undead_pirate_ship_barrels", "undead_pirate_ship/undead_pirate_ship_enchants", "undead_pirate_ship/undead_pirate_ship_supply", "undead_pirate_ship/undead_pirate_ship_treasure",
            "wishing_well/wishing_well_treasure"
    );

    public static final List<String> ENTITIES_LOOT_TABLES = List.of(
            "gladiator_loot"
    );

    public static final List<String> POTS_LOOT_TABLES = List.of(
            "common/common_pots",
            "infested_temple/infested_temple_pots",
            "kisegi_sanctuary/kisegi_sanctuary_pots"
    );

    public static final List<String> SPAWNERS_LOOT_TABLES = List.of(
            "common/common_normal", "common/common_ominous",
            "infested_temple/infested_temple_normal", "infested_temple/infested_temple_ominous",
            "kisegi_sanctuary/kisegi_sanctuary_normal", "kisegi_sanctuary/kisegi_sanctuary_ominous"
    );

    public static final List<String> ARCHEOLOGY_LOOT_TABLES = List.of(
            "common/common_archeology",
            "kisegi_sanctuary/kisegi_sanctuary_archeology"
    );
}
