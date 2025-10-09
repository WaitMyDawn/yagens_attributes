package yagen.waitmydawn.item.weapon;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class IronsSpellbooksItem {
    private IronsSpellbooksItem() {}

    public static final Supplier<Item> TWILIGHT_GALE =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "twilight_gale")
            );

    public static final Supplier<Item> KEEPER_FLAMBERGE =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "keeper_flamberge")
            );
    public static final Supplier<Item> MAGEHUNTER =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "magehunter")
            );
    public static final Supplier<Item> SPELLBREAKER =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "spellbreaker")
            );
    public static final Supplier<Item> LEGIONNAIRE_FLAMBERGE =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "legionnaire_flamberge")
            );
    public static final Supplier<Item> AMETHYST_RAPIER =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "amethyst_rapier")
            );
    public static final Supplier<Item> MISERY =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "misery")
            );
    public static final Supplier<Item> AUTOLOADER_CROSSBOW =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "autoloader_crossbow")
            );
    public static final Supplier<Item> HELLRAZOR =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "hellrazor")
            );
    public static final Supplier<Item> DECREPIT_SCYTHE =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "decrepit_scythe")
            );
    public static final Supplier<Item> ICE_GREATSWORD =
            () -> BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "boreal_blade")
            );

}
