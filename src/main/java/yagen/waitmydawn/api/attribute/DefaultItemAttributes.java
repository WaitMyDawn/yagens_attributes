package yagen.waitmydawn.api.attribute;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.fml.ModList;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.item.weapon.IceAndFireCEItem;
import yagen.waitmydawn.item.weapon.IronsSpellbooksItem;
import yagen.waitmydawn.item.weapon.LEndersCataclysmItem;

import java.util.LinkedHashMap;
import java.util.Map;


public class DefaultItemAttributes {
    // default map
    private static final Map<Item, Map<Attribute, Double>> DEFAULTS;

    static {
        Map<Item, Map<Attribute, Double>> temp = new LinkedHashMap<>();
        /**
         * defaults
         */
        // sword
        temp.put(Items.WOODEN_SWORD, Map.of(
                YAttributes.CRITICAL_CHANCE.get(), 0.1,
                YAttributes.STATUS_CHANCE.get(), 0.1));
        temp.put(Items.STONE_SWORD, Map.of(
                YAttributes.CRITICAL_CHANCE.get(), 0.1,
                YAttributes.STATUS_CHANCE.get(), 0.1));
        temp.put(Items.IRON_SWORD, Map.of(
                YAttributes.CRITICAL_CHANCE.get(), 0.12,
                YAttributes.STATUS_CHANCE.get(), 0.12));
        temp.put(Items.GOLDEN_SWORD, Map.of(
                YAttributes.CRITICAL_CHANCE.get(), 0.1,
                YAttributes.STATUS_CHANCE.get(), 0.3));
        temp.put(Items.DIAMOND_SWORD, Map.of(
                YAttributes.CRITICAL_CHANCE.get(), 0.15,
                YAttributes.STATUS_CHANCE.get(), 0.15));
        temp.put(Items.NETHERITE_SWORD, Map.of(
                YAttributes.CRITICAL_CHANCE.get(), 0.18,
                YAttributes.STATUS_CHANCE.get(), 0.18));
        // axe
        temp.put(Items.WOODEN_AXE, Map.of(
                YAttributes.CRITICAL_CHANCE.get(), 0.1,
                YAttributes.STATUS_CHANCE.get(), 0.1));
        temp.put(Items.STONE_AXE, Map.of(
                YAttributes.CRITICAL_CHANCE.get(), 0.1,
                YAttributes.STATUS_CHANCE.get(), 0.1));
        temp.put(Items.IRON_AXE, Map.of(
                YAttributes.CRITICAL_CHANCE.get(), 0.12,
                YAttributes.STATUS_CHANCE.get(), 0.12));
        temp.put(Items.GOLDEN_AXE, Map.of(
                YAttributes.CRITICAL_CHANCE.get(), 0.1,
                YAttributes.STATUS_CHANCE.get(), 0.3));
        temp.put(Items.DIAMOND_AXE, Map.of(
                YAttributes.CRITICAL_CHANCE.get(), 0.15,
                YAttributes.STATUS_CHANCE.get(), 0.15));
        temp.put(Items.NETHERITE_AXE, Map.of(
                YAttributes.CRITICAL_CHANCE.get(), 0.18,
                YAttributes.STATUS_CHANCE.get(), 0.18));
        // bows and trident
        temp.put(Items.BOW, Map.of(
                YAttributes.CRITICAL_CHANCE.get(), 0.4,
                YAttributes.STATUS_CHANCE.get(), 0.2));
        temp.put(Items.CROSSBOW, Map.of(
                YAttributes.CRITICAL_CHANCE.get(), 0.3,
                YAttributes.STATUS_CHANCE.get(), 0.25));
        temp.put(Items.TRIDENT, Map.of(
                YAttributes.CRITICAL_CHANCE.get(), 0.25,
                YAttributes.STATUS_CHANCE.get(), 0.15));
        // mace
        temp.put(Items.MACE, Map.of(
                YAttributes.CRITICAL_CHANCE.get(), 0.28,
                YAttributes.CRITICAL_DAMAGE.get(), 0.5,
                YAttributes.STATUS_CHANCE.get(), 0.05
        ));

        if (ModList.get().isLoaded("irons_spellbooks")) {
            if (IronsSpellbooksItem.KEEPER_FLAMBERGE.get() != Items.AIR)
                temp.put(IronsSpellbooksItem.KEEPER_FLAMBERGE.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.15,
                        YAttributes.CRITICAL_DAMAGE.get(), -0.2,
                        YAttributes.STATUS_CHANCE.get(), 0.18
                ));
            if (IronsSpellbooksItem.MAGEHUNTER.get() != Items.AIR)
                temp.put(IronsSpellbooksItem.MAGEHUNTER.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.1,
                        YAttributes.CRITICAL_DAMAGE.get(), -0.4,
                        YAttributes.STATUS_CHANCE.get(), 0.2,
                        YAttributes.LIFE_STEAL.get(), 0.05
                ));
            if (IronsSpellbooksItem.SPELLBREAKER.get() != Items.AIR)
                temp.put(IronsSpellbooksItem.SPELLBREAKER.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.3,
                        YAttributes.STATUS_CHANCE.get(), 0.25
                ));
            if (IronsSpellbooksItem.LEGIONNAIRE_FLAMBERGE.get() != Items.AIR)
                temp.put(IronsSpellbooksItem.LEGIONNAIRE_FLAMBERGE.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.15,
                        YAttributes.STATUS_CHANCE.get(), 0.22
                ));

            if (IronsSpellbooksItem.AMETHYST_RAPIER.get() != Items.AIR)
                temp.put(IronsSpellbooksItem.AMETHYST_RAPIER.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.14,
                        YAttributes.CRITICAL_DAMAGE.get(), -0.2,
                        YAttributes.STATUS_CHANCE.get(), 0.3
                ));

            if (IronsSpellbooksItem.MISERY.get() != Items.AIR)
                temp.put(IronsSpellbooksItem.MISERY.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.3,
                        YAttributes.STATUS_CHANCE.get(), 0.1
                ));

            if (IronsSpellbooksItem.AUTOLOADER_CROSSBOW.get() != Items.AIR)
                temp.put(IronsSpellbooksItem.AUTOLOADER_CROSSBOW.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.3,
                        YAttributes.STATUS_CHANCE.get(), 0.25
                ));

            if (IronsSpellbooksItem.HELLRAZOR.get() != Items.AIR)
                temp.put(IronsSpellbooksItem.HELLRAZOR.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.3,
                        YAttributes.CRITICAL_DAMAGE.get(), 0.2,
                        YAttributes.STATUS_CHANCE.get(), 0.1
                ));

            if (IronsSpellbooksItem.DECREPIT_SCYTHE.get() != Items.AIR)
                temp.put(IronsSpellbooksItem.DECREPIT_SCYTHE.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.25,
                        YAttributes.STATUS_CHANCE.get(), 0.1
                ));

            if (IronsSpellbooksItem.ICE_GREATSWORD.get() != Items.AIR)
                temp.put(IronsSpellbooksItem.ICE_GREATSWORD.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.25,
                        YAttributes.STATUS_CHANCE.get(), 0.2
                ));
        }

        if (ModList.get().isLoaded("cataclysm")) {
            if (LEndersCataclysmItem.THE_INCINERATOR.get() != Items.AIR)
                temp.put(LEndersCataclysmItem.THE_INCINERATOR.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.25,
                        YAttributes.STATUS_CHANCE.get(), 0.15,
                        YAttributes.LIFE_STEAL.get(), 0.05
                ));
            if (LEndersCataclysmItem.WRATH_OF_THE_DESERT.get() != Items.AIR)
                temp.put(LEndersCataclysmItem.WRATH_OF_THE_DESERT.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.4,
                        YAttributes.STATUS_CHANCE.get(), 0.2
                ));

            if (LEndersCataclysmItem.GAUNTLET_OF_MAELSTROM.get() != Items.AIR)
                temp.put(LEndersCataclysmItem.GAUNTLET_OF_MAELSTROM.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.15,
                        YAttributes.STATUS_CHANCE.get(), 0.15
                ));

            if (LEndersCataclysmItem.GAUNTLET_OF_BULWARK.get() != Items.AIR)
                temp.put(LEndersCataclysmItem.GAUNTLET_OF_BULWARK.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.15,
                        YAttributes.STATUS_CHANCE.get(), 0.15
                ));

            if (LEndersCataclysmItem.GAUNTLET_OF_GUARD.get() != Items.AIR)
                temp.put(LEndersCataclysmItem.GAUNTLET_OF_GUARD.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.15,
                        YAttributes.STATUS_CHANCE.get(), 0.15
                ));

            if (LEndersCataclysmItem.MEAT_SHREDDER.get() != Items.AIR)
                temp.put(LEndersCataclysmItem.MEAT_SHREDDER.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.05,
                        YAttributes.CRITICAL_DAMAGE.get(), 0.5,
                        YAttributes.STATUS_CHANCE.get(), 0.3
                ));

            if (LEndersCataclysmItem.VOID_ASSAULT_SHOULDER_WEAPON.get() != Items.AIR)
                temp.put(LEndersCataclysmItem.VOID_ASSAULT_SHOULDER_WEAPON.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.5,
                        YAttributes.STATUS_CHANCE.get(), 0.3
                ));

            if (LEndersCataclysmItem.WITHER_ASSAULT_SHOULDER_WEAPON.get() != Items.AIR)
                temp.put(LEndersCataclysmItem.WITHER_ASSAULT_SHOULDER_WEAPON.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.4,
                        YAttributes.STATUS_CHANCE.get(), 0.2
                ));

            if (LEndersCataclysmItem.CURSED_BOW.get() != Items.AIR)
                temp.put(LEndersCataclysmItem.CURSED_BOW.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.4,
                        YAttributes.STATUS_CHANCE.get(), 0.2
                ));

            if (LEndersCataclysmItem.LASER_GATLING.get() != Items.AIR)
                temp.put(LEndersCataclysmItem.LASER_GATLING.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.2,
                        YAttributes.CRITICAL_DAMAGE.get(), -0.2,
                        YAttributes.STATUS_CHANCE.get(), 0.2
                ));

            if (LEndersCataclysmItem.THE_ANNIHILATOR.get() != Items.AIR)
                temp.put(LEndersCataclysmItem.THE_ANNIHILATOR.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.28,
                        YAttributes.CRITICAL_DAMAGE.get(), 0.5,
                        YAttributes.STATUS_CHANCE.get(), 0.05
                ));

            if (LEndersCataclysmItem.SOUL_RENDER.get() != Items.AIR)
                temp.put(LEndersCataclysmItem.SOUL_RENDER.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.2,
                        YAttributes.STATUS_CHANCE.get(), 0.2
                ));

            if (LEndersCataclysmItem.CERAUNUS.get() != Items.AIR)
                temp.put(LEndersCataclysmItem.CERAUNUS.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.35,
                        YAttributes.STATUS_CHANCE.get(), 0.1
                ));

            if (LEndersCataclysmItem.ASTRAPE.get() != Items.AIR)
                temp.put(LEndersCataclysmItem.ASTRAPE.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.2,
                        YAttributes.STATUS_CHANCE.get(), 0.2
                ));

            if (LEndersCataclysmItem.ANCIENT_SPEAR.get() != Items.AIR)
                temp.put(LEndersCataclysmItem.ANCIENT_SPEAR.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.2,
                        YAttributes.STATUS_CHANCE.get(), 0.2
                ));

            if (LEndersCataclysmItem.TIDAL_CLAWS.get() != Items.AIR)
                temp.put(LEndersCataclysmItem.TIDAL_CLAWS.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.15,
                        YAttributes.STATUS_CHANCE.get(), 0.25
                ));

            if (LEndersCataclysmItem.THE_IMMOLATOR.get() != Items.AIR)
                temp.put(LEndersCataclysmItem.THE_IMMOLATOR.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.28,
                        YAttributes.CRITICAL_DAMAGE.get(), 0.5,
                        YAttributes.STATUS_CHANCE.get(), 0.05
                ));

            if (LEndersCataclysmItem.INFERNAL_FORGE.get() != Items.AIR)
                temp.put(LEndersCataclysmItem.INFERNAL_FORGE.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.28,
                        YAttributes.CRITICAL_DAMAGE.get(), 0.5,
                        YAttributes.STATUS_CHANCE.get(), 0.05
                ));

            if (LEndersCataclysmItem.VOID_FORGE.get() != Items.AIR)
                temp.put(LEndersCataclysmItem.VOID_FORGE.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.28,
                        YAttributes.CRITICAL_DAMAGE.get(), 0.5,
                        YAttributes.STATUS_CHANCE.get(), 0.05
                ));

            if (LEndersCataclysmItem.ATHAME.get() != Items.AIR)
                temp.put(LEndersCataclysmItem.ATHAME.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.25,
                        YAttributes.CRITICAL_DAMAGE.get(), 1.0,
                        YAttributes.STATUS_CHANCE.get(), 0.3
                ));

            if (LEndersCataclysmItem.CORAL_SPEAR.get() != Items.AIR)
                temp.put(LEndersCataclysmItem.CORAL_SPEAR.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.25,
                        YAttributes.STATUS_CHANCE.get(), 0.15
                ));

            if (LEndersCataclysmItem.CORAL_BARDICHE.get() != Items.AIR)
                temp.put(LEndersCataclysmItem.CORAL_BARDICHE.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.25,
                        YAttributes.STATUS_CHANCE.get(), 0.15
                ));

            if (LEndersCataclysmItem.BLACK_STEEL_SWORD.get() != Items.AIR)
                temp.put(LEndersCataclysmItem.BLACK_STEEL_SWORD.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.15,
                        YAttributes.STATUS_CHANCE.get(), 0.15
                ));

            if (LEndersCataclysmItem.BLACK_STEEL_AXE.get() != Items.AIR)
                temp.put(LEndersCataclysmItem.BLACK_STEEL_AXE.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.15,
                        YAttributes.STATUS_CHANCE.get(), 0.15
                ));

        }

        if (ModList.get().isLoaded("iceandfire")) {
            if (IceAndFireCEItem.SILVER_SWORD.get() != Items.AIR)
                temp.put(IceAndFireCEItem.SILVER_SWORD.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.1,
                        YAttributes.STATUS_CHANCE.get(), 0.28
                ));
            if (IceAndFireCEItem.SILVER_AXE.get() != Items.AIR)
                temp.put(IceAndFireCEItem.SILVER_AXE.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.1,
                        YAttributes.STATUS_CHANCE.get(), 0.28
                ));
            if (IceAndFireCEItem.COPPER_SWORD.get() != Items.AIR)
                temp.put(IceAndFireCEItem.COPPER_SWORD.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.12,
                        YAttributes.STATUS_CHANCE.get(), 0.28
                ));
            if (IceAndFireCEItem.COPPER_AXE.get() != Items.AIR)
                temp.put(IceAndFireCEItem.COPPER_AXE.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.12,
                        YAttributes.STATUS_CHANCE.get(), 0.28
                ));
            if (IceAndFireCEItem.DRAGONBONE_SWORD.get() != Items.AIR)
                temp.put(IceAndFireCEItem.DRAGONBONE_SWORD.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.18,
                        YAttributes.STATUS_CHANCE.get(), 0.18
                ));
            if (IceAndFireCEItem.DRAGONBONE_AXE.get() != Items.AIR)
                temp.put(IceAndFireCEItem.DRAGONBONE_AXE.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.18,
                        YAttributes.STATUS_CHANCE.get(), 0.18
                ));
            if (IceAndFireCEItem.DRAGONBONE_BOW.get() != Items.AIR)
                temp.put(IceAndFireCEItem.DRAGONBONE_BOW.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.45,
                        YAttributes.STATUS_CHANCE.get(), 0.1
                ));
            if (IceAndFireCEItem.DRAGONSTEEL_FIRE_SWORD.get() != Items.AIR)
                temp.put(IceAndFireCEItem.DRAGONSTEEL_FIRE_SWORD.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.3,
                        YAttributes.STATUS_CHANCE.get(), 0.1
                ));
            if (IceAndFireCEItem.DRAGONSTEEL_FIRE_AXE.get() != Items.AIR)
                temp.put(IceAndFireCEItem.DRAGONSTEEL_FIRE_AXE.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.3,
                        YAttributes.STATUS_CHANCE.get(), 0.1
                ));
            if (IceAndFireCEItem.DRAGONSTEEL_ICE_SWORD.get() != Items.AIR)
                temp.put(IceAndFireCEItem.DRAGONSTEEL_ICE_SWORD.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.25,
                        YAttributes.CRITICAL_DAMAGE.get(), 0.2,
                        YAttributes.STATUS_CHANCE.get(), 0.1
                ));
            if (IceAndFireCEItem.DRAGONSTEEL_ICE_AXE.get() != Items.AIR)
                temp.put(IceAndFireCEItem.DRAGONSTEEL_ICE_AXE.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.24,
                        YAttributes.CRITICAL_DAMAGE.get(), 0.2,
                        YAttributes.STATUS_CHANCE.get(), 0.1
                ));
            if (IceAndFireCEItem.DRAGONSTEEL_LIGHTNING_SWORD.get() != Items.AIR)
                temp.put(IceAndFireCEItem.DRAGONSTEEL_LIGHTNING_SWORD.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.15,
                        YAttributes.STATUS_CHANCE.get(), 0.25
                ));
            if (IceAndFireCEItem.DRAGONSTEEL_LIGHTNING_AXE.get() != Items.AIR)
                temp.put(IceAndFireCEItem.DRAGONSTEEL_LIGHTNING_AXE.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.15,
                        YAttributes.STATUS_CHANCE.get(), 0.25
                ));
            if (IceAndFireCEItem.DREAD_SWORD.get() != Items.AIR)
                temp.put(IceAndFireCEItem.DREAD_SWORD.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.3,
                        YAttributes.STATUS_CHANCE.get(), 0.1
                ));
            if (IceAndFireCEItem.DREAD_KNIGHT_SWORD.get() != Items.AIR)
                temp.put(IceAndFireCEItem.DREAD_KNIGHT_SWORD.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.2,
                        YAttributes.STATUS_CHANCE.get(), 0.1
                ));
            if (IceAndFireCEItem.DRAGONBONE_SWORD_FIRE.get() != Items.AIR)
                temp.put(IceAndFireCEItem.DRAGONBONE_SWORD_FIRE.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.25,
                        YAttributes.STATUS_CHANCE.get(), 0.1
                ));
            if (IceAndFireCEItem.DRAGONBONE_SWORD_ICE.get() != Items.AIR)
                temp.put(IceAndFireCEItem.DRAGONBONE_SWORD_ICE.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.2,
                        YAttributes.CRITICAL_DAMAGE.get(), 0.2,
                        YAttributes.STATUS_CHANCE.get(), 0.1
                ));
            if (IceAndFireCEItem.DRAGONBONE_SWORD_LIGHTNING.get() != Items.AIR)
                temp.put(IceAndFireCEItem.DRAGONBONE_SWORD_LIGHTNING.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.15,
                        YAttributes.STATUS_CHANCE.get(), 0.2
                ));
            if (IceAndFireCEItem.HIPPOGRYPH_SWORD.get() != Items.AIR)
                temp.put(IceAndFireCEItem.HIPPOGRYPH_SWORD.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.15,
                        YAttributes.STATUS_CHANCE.get(), 0.15
                ));
            if (IceAndFireCEItem.DEATHWORM_GAUNTLET_YELLOW.get() != Items.AIR)
                temp.put(IceAndFireCEItem.DEATHWORM_GAUNTLET_YELLOW.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.2,
                        YAttributes.STATUS_CHANCE.get(), 0.2
                ));
            if (IceAndFireCEItem.DEATHWORM_GAUNTLET_WHITE.get() != Items.AIR)
                temp.put(IceAndFireCEItem.DEATHWORM_GAUNTLET_WHITE.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.2,
                        YAttributes.STATUS_CHANCE.get(), 0.2
                ));
            if (IceAndFireCEItem.DEATHWORM_GAUNTLET_RED.get() != Items.AIR)
                temp.put(IceAndFireCEItem.DEATHWORM_GAUNTLET_RED.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.2,
                        YAttributes.STATUS_CHANCE.get(), 0.2
                ));
            if (IceAndFireCEItem.COCKATRICE_SCEPTER.get() != Items.AIR)
                temp.put(IceAndFireCEItem.COCKATRICE_SCEPTER.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.05,
                        YAttributes.STATUS_CHANCE.get(), 0.25
                ));
            if (IceAndFireCEItem.STYMPHALIAN_DAGGER.get() != Items.AIR)
                temp.put(IceAndFireCEItem.STYMPHALIAN_DAGGER.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.1,
                        YAttributes.STATUS_CHANCE.get(), 0.22
                ));
            if (IceAndFireCEItem.AMPHITHERE_MACUAHUITL.get() != Items.AIR)
                temp.put(IceAndFireCEItem.AMPHITHERE_MACUAHUITL.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.22,
                        YAttributes.STATUS_CHANCE.get(), 0.14
                ));
            if (IceAndFireCEItem.TIDE_TRIDENT.get() != Items.AIR)
                temp.put(IceAndFireCEItem.TIDE_TRIDENT.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.25,
                        YAttributes.STATUS_CHANCE.get(), 0.15
                ));
            if (IceAndFireCEItem.GHOST_SWORD.get() != Items.AIR)
                temp.put(IceAndFireCEItem.GHOST_SWORD.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.15,
                        YAttributes.STATUS_CHANCE.get(), 0.25
                ));
            if (IceAndFireCEItem.TROLL_WEAPON_AXE.get() != Items.AIR)
                temp.put(IceAndFireCEItem.TROLL_WEAPON_AXE.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.25,
                        YAttributes.STATUS_CHANCE.get(), 0.1
                ));
            if (IceAndFireCEItem.TROLL_WEAPON_COLUMN.get() != Items.AIR)
                temp.put(IceAndFireCEItem.TROLL_WEAPON_COLUMN.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.25,
                        YAttributes.STATUS_CHANCE.get(), 0.1
                ));
            if (IceAndFireCEItem.TROLL_WEAPON_COLUMN_FOREST.get() != Items.AIR)
                temp.put(IceAndFireCEItem.TROLL_WEAPON_COLUMN_FOREST.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.25,
                        YAttributes.STATUS_CHANCE.get(), 0.1
                ));
            if (IceAndFireCEItem.TROLL_WEAPON_COLUMN_FROST.get() != Items.AIR)
                temp.put(IceAndFireCEItem.TROLL_WEAPON_COLUMN_FROST.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.25,
                        YAttributes.STATUS_CHANCE.get(), 0.1
                ));
            if (IceAndFireCEItem.TROLL_WEAPON_HAMMER.get() != Items.AIR)
                temp.put(IceAndFireCEItem.TROLL_WEAPON_HAMMER.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.25,
                        YAttributes.STATUS_CHANCE.get(), 0.1
                ));
            if (IceAndFireCEItem.TROLL_WEAPON_TRUNK.get() != Items.AIR)
                temp.put(IceAndFireCEItem.TROLL_WEAPON_TRUNK.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.25,
                        YAttributes.STATUS_CHANCE.get(), 0.1
                ));
            if (IceAndFireCEItem.TROLL_WEAPON_TRUNK_FROST.get() != Items.AIR)
                temp.put(IceAndFireCEItem.TROLL_WEAPON_TRUNK_FROST.get(), Map.of(
                        YAttributes.CRITICAL_CHANCE.get(), 0.25,
                        YAttributes.STATUS_CHANCE.get(), 0.1
                ));
        }

        DEFAULTS = Map.copyOf(temp);
    }

    public static void apply(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;

        // skip if marked
        if (stack.has(DataComponents.CUSTOM_DATA)
                && stack.get(DataComponents.CUSTOM_DATA).contains("yagens_attributes_default_applied")) {
            return;
        }

        var item = stack.getItem();
        var map = DEFAULTS.get(item);
        if (map == null) return;

        // get modifiers
        var existing = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        // copy existed
        final ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        existing.modifiers().forEach(e -> builder.add(e.attribute(), e.modifier(), e.slot()));

        map.forEach((attr, value) -> {
            ResourceLocation key = BuiltInRegistries.ATTRIBUTE.getKey(attr);
            builder.add(
                    BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attr),
                    new AttributeModifier(
                            ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID,
                                    "default_" + key.getPath()),
                            value,
                            AttributeModifier.Operation.ADD_VALUE
                    ),
                    EquipmentSlotGroup.MAINHAND
            );
        });

        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
        // mark
        stack.set(DataComponents.CUSTOM_DATA,
                stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                        .update(tag -> tag.putBoolean("yagens_attributes_default_applied", true)));
    }

}
