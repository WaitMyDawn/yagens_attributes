package yagen.waitmydawn.yagens_attributes.core.loot;

import java.util.*;
import static yagen.waitmydawn.yagens_attributes.core.loot.LootRule.*;
import static yagen.waitmydawn.yagens_attributes.core.loot.LootRewards.*;

public final class BuiltinMobLoot {
    private BuiltinMobLoot() {}
    public static List<LootRule> rules() {
        var rules = new ArrayList<LootRule>();

        rules.add(append("minecraft:entities/wither", "minecraft:wither",
                pool(item("yagens_attributes:kuva").weight(1).when(PLAYER_KILL).count(4, 24)),
                pool(item("yagens_attributes:endo").weight(1).when(PLAYER_KILL).count(8, 16)).chance(0.8),
                pool(mod(galvanized(0, 50)).when(PLAYER_KILL)).chance(0.05)));

        rules.add(append("minecraft:entities/witch", "minecraft:witch",
                pool(mod(specific("yagens_attributes:toxin_addition", 0, 80)).when(PLAYER_KILL)).chance(0.05)));

        rules.add(append("minecraft:entities/sniffer", "minecraft:sniffer",
                pool(item("minecraft:sniffer_egg"))));

        var breezeAndBogged = List.of(
                pool(item("yagens_attributes:endo").weight(1).when(PLAYER_KILL).when(new EnchantedChance("minecraft:looting", 0.1, 0.2, 0.05)).count(1, 2)));
        rules.add(append("minecraft:entities/breeze", "minecraft:breeze", breezeAndBogged));
        rules.add(append("minecraft:entities/bogged", "minecraft:bogged", breezeAndBogged));

        rules.add(append("minecraft:entities/ender_dragon", "minecraft:ender_dragon",
                pool(mod(weighted(0, 30, 19, 1, 20, 80)).when(PLAYER_KILL)),
                pool(mod(veiled())).chance(0.1)));

        rules.add(append("minecraft:entities/evoker", "minecraft:evoker",
                pool(item("yagens_attributes:endo").when(PLAYER_KILL).when(new EnchantedChance("minecraft:looting", 0.1, 0.2, 0.05)).count(1, 4))));

        rules.add(append("minecraft:entities/blaze", "minecraft:blaze",
                pool(mod(specific("yagens_attributes:heat_addition", 0, 50)).when(PLAYER_KILL)).chance(0.05)));

        rules.add(append("minecraft:entities/piglin_brute", "minecraft:piglin_brute",
                pool(item("yagens_attributes:endo").weight(1).when(PLAYER_KILL).when(new EnchantedChance("minecraft:looting", 0.2, 0.3, 0.05)).count(2, 6).function(new EnchantedCount("minecraft:looting", new Range(0, 1), 0)))));

        rules.add(append("minecraft:entities/warden", "minecraft:warden",
                pool(mod(weighted(50, 40, 9, 1, 0, 80)).when(PLAYER_KILL))));

        rules.add(append("minecraft:entities/elder_guardian", "minecraft:elder_guardian",
                pool(item("yagens_attributes:endo").count(2, 8)),
                pool(mod(weighted(5, 4, 1, 0, 50, 100)))));

        return List.copyOf(rules);
    }
}
