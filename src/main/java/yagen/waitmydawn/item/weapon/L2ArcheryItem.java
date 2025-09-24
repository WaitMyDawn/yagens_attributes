package yagen.waitmydawn.item.weapon;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class L2ArcheryItem {
    private L2ArcheryItem() {}
    public static final Supplier<Item> STARTER_BOW =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("l2archery", "starter_bow")
            );

    public static final Supplier<Item> IRON_BOW =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("l2archery", "iron_bow")
            );

    public static final Supplier<Item> MASTER_BOW =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("l2archery", "master_bow")
            );

    public static final Supplier<Item> GLOW_AIM_BOW =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("l2archery", "glow_aim_bow")
            );

    public static final Supplier<Item> MAGNIFY_BOW =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("l2archery", "magnify_bow")
            );

    public static final Supplier<Item> ENDER_AIM_BOW =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("l2archery", "ender_aim_bow")
            );

    public static final Supplier<Item> EAGLE_BOW =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("l2archery", "eagle_bow")
            );

    public static final Supplier<Item> EXPLOSION_BOW =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("l2archery", "explosion_bow")
            );

    public static final Supplier<Item> FLAME_BOW =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("l2archery", "flame_bow")
            );

    public static final Supplier<Item> FROZE_BOW =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("l2archery", "froze_bow")
            );

    public static final Supplier<Item> BLACKSTONE_BOW =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("l2archery", "slow_bow")
            );

    public static final Supplier<Item> STORM_BOW =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("l2archery", "storm_bow")
            );

    public static final Supplier<Item> TURTLE_BOW =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("l2archery", "turtle_bow")
            );

    public static final Supplier<Item> EARTH_BOW =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("l2archery", "earth_bow")
            );

    public static final Supplier<Item> WIND_BOW =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("l2archery", "wind_bow")
            );

    public static final Supplier<Item> WINTER_BOW =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("l2archery", "winter_bow")
            );

    public static final Supplier<Item> GAIA_BOW =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("l2archery", "gaia_bow")
            );

    public static final Supplier<Item> VOID_BOW =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("l2archery", "void_bow")
            );

    public static final Supplier<Item> SUN_BOW =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("l2archery", "sun_bow")
            );
}
