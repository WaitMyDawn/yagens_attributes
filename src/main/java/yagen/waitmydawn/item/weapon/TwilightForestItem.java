package yagen.waitmydawn.item.weapon;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class TwilightForestItem {
    private TwilightForestItem() {
    }

    public static final Supplier<Item> IRONWOOD_SWORD =
            () -> BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("twilightforest", "ironwood_sword"));

    public static final Supplier<Item> IRONWOOD_SHOVEL =
            () -> BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("twilightforest", "ironwood_shovel"));

    public static final Supplier<Item> IRONWOOD_PICKAXE =
            () -> BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("twilightforest", "ironwood_pickaxe"));

    public static final Supplier<Item> IRONWOOD_AXE =
            () -> BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("twilightforest", "ironwood_axe"));

    public static final Supplier<Item> IRONWOOD_HOE =
            () -> BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("twilightforest", "ironwood_hoe"));

    public static final Supplier<Item> FIERY_SWORD =
            () -> BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("twilightforest", "fiery_sword"));

    public static final Supplier<Item> FIERY_PICKAXE =
            () -> BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("twilightforest", "fiery_pickaxe"));

    public static final Supplier<Item> STEELEAF_SWORD =
            () -> BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("twilightforest", "steeleaf_sword"));

    public static final Supplier<Item> STEELEAF_SHOVEL =
            () -> BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("twilightforest", "steeleaf_shovel"));

    public static final Supplier<Item> STEELEAF_PICKAXE =
            () -> BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("twilightforest", "steeleaf_pickaxe"));

    public static final Supplier<Item> STEELEAF_AXE =
            () -> BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("twilightforest", "steeleaf_axe"));

    public static final Supplier<Item> STEELEAF_HOE =
            () -> BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("twilightforest", "steeleaf_hoe"));

    public static final Supplier<Item> GOLDEN_MINOTAUR_AXE =
            () -> BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("twilightforest", "gold_minotaur_axe"));

    public static final Supplier<Item> DIAMOND_MINOTAUR_AXE =
            () -> BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("twilightforest", "diamond_minotaur_axe"));

    public static final Supplier<Item> MAZEBREAKER_PICKAXE =
            () -> BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("twilightforest", "mazebreaker_pickaxe"));

    public static final Supplier<Item> KNIGHTMETAL_SWORD =
            () -> BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("twilightforest", "knightmetal_sword"));

    public static final Supplier<Item> KNIGHTMETAL_PICKAXE =
            () -> BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("twilightforest", "knightmetal_pickaxe"));

    public static final Supplier<Item> KNIGHTMETAL_AXE =
            () -> BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("twilightforest", "knightmetal_axe"));

    public static final Supplier<Item> ICE_SWORD =
            () -> BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("twilightforest", "ice_sword"));

    public static final Supplier<Item> GLASS_SWORD =
            () -> BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("twilightforest", "glass_sword"));

    public static final Supplier<Item> GIANT_PICKAXE =
            () -> BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("twilightforest", "giant_pickaxe"));

    public static final Supplier<Item> GIANT_SWORD =
            () -> BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("twilightforest", "giant_sword"));

    public static final Supplier<Item> TRIPLE_BOW =
            () -> BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("twilightforest", "triple_bow"));

    public static final Supplier<Item> SEEKER_BOW =
            () -> BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("twilightforest", "seeker_bow"));

    public static final Supplier<Item> ICE_BOW =
            () -> BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("twilightforest", "ice_bow"));

    public static final Supplier<Item> ENDER_BOW =
            () -> BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("twilightforest", "ender_bow"));
}
