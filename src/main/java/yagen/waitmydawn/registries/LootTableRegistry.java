package yagen.waitmydawn.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mission.MissionType;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class LootTableRegistry {
    private static final Set<ResourceKey<LootTable>> LOCATIONS = new HashSet<>();
    private static final Set<ResourceKey<LootTable>> IMMUTABLE_LOCATIONS = Collections.unmodifiableSet(LOCATIONS);

    public static final ResourceLocation[][] MISSION_TREASURES = new ResourceLocation[MissionType.values().length][3];
    public static final ResourceKey<LootTable>[][] MISSION_TREASURE_KEYS = new ResourceKey[MissionType.values().length][3];

    static {
        for (MissionType type : MissionType.values()) {
            for (int level = 0; level < 3; level++) {
                String path = "chests/mission/" + type.name().toLowerCase() + "_treasure_" + level;
                MISSION_TREASURES[type.ordinal()][level] =
                        ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, path);
                MISSION_TREASURE_KEYS[type.ordinal()][level] =
                        register(MISSION_TREASURES[type.ordinal()][level]);
            }
        }
    }

    public static ResourceLocation getMissionTreasureLocation(MissionType type, int level) {
        return MISSION_TREASURES[type.ordinal()][level];
    }

    public static String getMissionTreasurePath(MissionType type, int level) {
        return MISSION_TREASURES[type.ordinal()][level].getPath();
    }

    public static ResourceKey<LootTable> getMissionTreasureKey(MissionType type, int level) {
        return MISSION_TREASURE_KEYS[type.ordinal()][level];
    }

    private static ResourceKey<LootTable> register(ResourceLocation resourceLocation) {
        return register(ResourceKey.create(Registries.LOOT_TABLE, resourceLocation));
    }

    private static ResourceKey<LootTable> register(ResourceKey<LootTable> name) {
        if (LOCATIONS.add(name)) {
            return name;
        } else {
            throw new IllegalArgumentException(name.location() + " is already a registered built-in loot table");
        }
    }

    public static Set<ResourceKey<LootTable>> all() {
        return IMMUTABLE_LOCATIONS;
    }
}
