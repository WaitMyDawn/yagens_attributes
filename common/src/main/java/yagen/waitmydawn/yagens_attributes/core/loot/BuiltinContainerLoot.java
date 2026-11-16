package yagen.waitmydawn.yagens_attributes.core.loot;

import java.util.*;
import static yagen.waitmydawn.yagens_attributes.core.loot.LootRule.*;
import static yagen.waitmydawn.yagens_attributes.core.loot.LootRewards.*;

public final class BuiltinContainerLoot {
    private BuiltinContainerLoot() {}
    public static List<LootRule> rules() {
        var rules = new ArrayList<LootRule>();

        var commonContainers = List.of(
                pool(item("yagens_attributes:endo").count(1, 4)).chance(0.8),
                pool(mod(weighted(5, 4, 1, 0, 0, 80))).chance(0.8));
        rules.add(append("minecraft:chests/abandoned_mineshaft", "", commonContainers));
        rules.add(append("minecraft:chests/bastion_bridge", "", commonContainers));
        rules.add(append("minecraft:chests/bastion_other", "", commonContainers));
        rules.add(append("minecraft:chests/bastion_hoglin_stable", "", commonContainers));
        rules.add(append("minecraft:chests/igloo_chest", "", commonContainers));
        rules.add(append("minecraft:chests/nether_bridge", "", commonContainers));
        rules.add(append("minecraft:chests/ruined_portal", "", commonContainers));
        rules.add(append("minecraft:chests/simple_dungeon", "", commonContainers));
        rules.add(append("minecraft:chests/stronghold_corridor", "", commonContainers));
        rules.add(append("minecraft:chests/stronghold_crossing", "", commonContainers));
        rules.add(append("minecraft:chests/trial_chambers/corridor", "", commonContainers));
        rules.add(append("minecraft:chests/trial_chambers/entrance", "", commonContainers));
        rules.add(append("minecraft:chests/underwater_ruin_big", "", commonContainers));
        rules.add(append("minecraft:chests/underwater_ruin_small", "", commonContainers));

        rules.add(append("minecraft:chests/bastion_treasure", "",
                pool(item("yagens_attributes:endo").count(12, 48)),
                pool(mod(weighted(5, 4, 1, 0, 0, 80)).count(1, 3)),
                pool(item("yagens_attributes:kuva").count(1, 8))));

        rules.add(append("minecraft:chests/buried_treasure", "",
                pool(mod(weighted(2, 1, 1, 0, 50, 100)).count(1, 2)),
                pool(item("yagens_attributes:endo").count(6, 20))));

        rules.add(append("minecraft:chests/desert_pyramid", "",
                pool(item("yagens_attributes:endo").count(4, 16)).chance(0.8),
                pool(mod(weighted(5, 4, 1, 0, 0, 80)).count(1, 2)).chance(0.8)));

        rules.add(append("minecraft:chests/end_city_treasure", "",
                pool(item("yagens_attributes:endo").count(10, 32)).chance(0.8),
                pool(mod(weighted(5, 4, 1, 0, 0, 80)).count(1, 2)).chance(0.8),
                pool(mod(weighted(0, 0, 1, 0, 0, 100)))));

        var ancientCity = List.of(
                pool(item("yagens_attributes:endo").count(4, 24),
                        item("yagens_attributes:kuva").count(1, 4)).rolls(1, 2),
                pool(mod(weighted(5, 4, 1, 0, 0, 80)).count(1, 1)).chance(0.8),
                pool(mod(veiled())).chance(0.1));
        rules.add(append("minecraft:chests/ancient_city", "", ancientCity));
        rules.add(append("minecraft:chests/ancient_city_ice_box", "", ancientCity));

        rules.add(append("minecraft:chests/jungle_temple", "",
                pool(item("yagens_attributes:endo").count(1, 40)),
                pool(mod(weighted(5, 4, 1, 0, 0, 80))).chance(0.8)));

        rules.add(append("minecraft:chests/shipwreck_treasure", "",
                pool(item("yagens_attributes:endo").count(1, 24)),
                pool(mod(weighted(5, 4, 1, 0, 0, 80))).chance(0.8)));

        rules.add(append("minecraft:chests/stronghold_library", "",
                pool(item("yagens_attributes:endo").count(3, 14)),
                pool(mod(weighted(5, 4, 1, 0, 0, 80)).count(1, 3))));

        rules.add(append("minecraft:chests/trial_chambers/intersection", "",
                pool(item("yagens_attributes:endo").count(10, 46)),
                pool(mod(weighted(5, 4, 1, 0, 0, 80)).count(1, 2))));

        rules.add(append("minecraft:chests/trial_chambers/reward", "",
                pool(item("yagens_attributes:endo").count(1, 4)).rolls(1, 2).chance(0.5)));

        rules.add(append("minecraft:chests/trial_chambers/reward_ominous", "",
                pool(item("yagens_attributes:endo").count(2, 4),
                        item("yagens_attributes:kuva").count(2, 8)).rolls(2, 3),
                pool(mod(weighted(5, 4, 1, 0, 0, 80))).chance(0.6)));

        rules.add(append("minecraft:chests/woodland_mansion", "",
                pool(item("yagens_attributes:endo").count(2, 8)),
                pool(mod(weighted(5, 4, 1, 0, 0, 80)).count(1, 2))));

        rules.add(append("minecraft:spawners/ominous/trial_chamber/consumables", "",
                pool(item("yagens_attributes:endo").count(1, 4)).chance(0.5),
                pool(mod(weighted(5, 4, 1, 0, 0, 80))).chance(0.05),
                pool(item("yagens_attributes:kuva").count(1, 2)).chance(0.5)));

        rules.add(append("minecraft:spawners/trial_chamber/consumables", "",
                pool(item("yagens_attributes:endo").count(1, 4)).chance(0.05)));

        return List.copyOf(rules);
    }
}
