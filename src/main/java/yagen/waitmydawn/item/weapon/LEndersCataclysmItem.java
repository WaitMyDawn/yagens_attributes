package yagen.waitmydawn.item.weapon;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class LEndersCataclysmItem {
    private LEndersCataclysmItem() {
    }

    public static final Supplier<Item> THE_INCINERATOR =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("cataclysm", "the_incinerator")
            );

    public static final Supplier<Item> THE_ANNIHILATOR =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("cataclysm", "the_annihilator")
            );

    public static final Supplier<Item> SOUL_RENDER =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("cataclysm", "soul_render")
            );

    public static final Supplier<Item> WRATH_OF_THE_DESERT =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("cataclysm", "wrath_of_the_desert")
            );

    public static final Supplier<Item> GAUNTLET_OF_MAELSTROM =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("cataclysm", "gauntlet_of_maelstrom")
            );

    public static final Supplier<Item> GAUNTLET_OF_BULWARK =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("cataclysm", "gauntlet_of_bulwark")
            );

    public static final Supplier<Item> GAUNTLET_OF_GUARD =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("cataclysm", "gauntlet_of_guard")
            );

    public static final Supplier<Item> CERAUNUS =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("cataclysm", "ceraunus")
            );

    public static final Supplier<Item> ASTRAPE =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("cataclysm", "astrape")
            );

    public static final Supplier<Item> ANCIENT_SPEAR =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("cataclysm", "ancient_spear")
            );

    public static final Supplier<Item> TIDAL_CLAWS =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("cataclysm", "tidal_claws")
            );

    public static final Supplier<Item> VOID_ASSAULT_SHOULDER_WEAPON =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("cataclysm", "void_assault_shoulder_weapon")
            );

    public static final Supplier<Item> WITHER_ASSAULT_SHOULDER_WEAPON =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("cataclysm", "wither_assault_shoulder_weapon")
            );

    public static final Supplier<Item> LASER_GATLING =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("cataclysm", "laser_gatling")
            );

    public static final Supplier<Item> MEAT_SHREDDER =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("cataclysm", "meat_shredder")
            );

    public static final Supplier<Item> THE_IMMOLATOR =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("cataclysm", "the_immolator")
            );

    public static final Supplier<Item> BULWARK_OF_THE_FLAME =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("cataclysm", "bulwark_of_the_flame")
            );

    public static final Supplier<Item> CURSED_BOW =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("cataclysm", "cursed_bow")
            );

    public static final Supplier<Item> CORAL_BARDICHE =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("cataclysm", "coral_bardiche")
            );

    public static final Supplier<Item> CORAL_SPEAR =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("cataclysm", "coral_spear")
            );
}
