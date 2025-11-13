package yagen.waitmydawn.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import yagen.waitmydawn.YagensAttributes;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class LootTableRegistry {
    private static final Set<ResourceKey<LootTable>> LOCATIONS = new HashSet<>();
    private static final Set<ResourceKey<LootTable>> IMMUTABLE_LOCATIONS = Collections.unmodifiableSet(LOCATIONS);

    public static final ResourceLocation MISSION_EXTERMINATE_TREASURE =
            ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "chests/mission/exterminate_treasure");
    public static final ResourceLocation MISSION_DEFENSE_TREASURE =
            ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "chests/mission/defense_treasure");
    public static final ResourceLocation MISSION_SURVIVAL_TREASURE =
            ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "chests/mission/survival_treasure");
    public static final ResourceLocation MISSION_ASSASSINATION_TREASURE =
            ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "chests/mission/assassination_treasure");

    public static final ResourceKey<LootTable> MISSION_EXTERMINATE_TREASURE_KEY = register(MISSION_EXTERMINATE_TREASURE);
    public static final ResourceKey<LootTable> MISSION_DEFENSE_TREASURE_KEY = register(MISSION_DEFENSE_TREASURE);
    public static final ResourceKey<LootTable> MISSION_SURVIVAL_TREASURE_KEY = register(MISSION_SURVIVAL_TREASURE);
    public static final ResourceKey<LootTable> MISSION_ASSASSINATION_TREASURE_KEY = register(MISSION_ASSASSINATION_TREASURE);

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
