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
import yagen.waitmydawn.item.weapon.*;
import yagen.waitmydawn.util.SupportedMod;

import java.util.LinkedHashMap;
import java.util.Map;


public class DefaultItemAttributes {
    // default map
    public static final Map<Item, Map<Attribute, Double>> DEFAULTS;

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

        if (ModList.get().isLoaded(SupportedMod.IRONSSPELLBOOKS.getValue())) {
            temp.put(IronsSpellbooksItem.KEEPER_FLAMBERGE.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.15,
                    YAttributes.CRITICAL_DAMAGE.get(), -0.2,
                    YAttributes.STATUS_CHANCE.get(), 0.18
            ));
            temp.put(IronsSpellbooksItem.MAGEHUNTER.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.1,
                    YAttributes.CRITICAL_DAMAGE.get(), -0.4,
                    YAttributes.STATUS_CHANCE.get(), 0.2,
                    YAttributes.LIFE_STEAL.get(), 0.05
            ));
            temp.put(IronsSpellbooksItem.SPELLBREAKER.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.3,
                    YAttributes.STATUS_CHANCE.get(), 0.25
            ));
            temp.put(IronsSpellbooksItem.LEGIONNAIRE_FLAMBERGE.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.15,
                    YAttributes.STATUS_CHANCE.get(), 0.22
            ));

            temp.put(IronsSpellbooksItem.AMETHYST_RAPIER.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.14,
                    YAttributes.CRITICAL_DAMAGE.get(), -0.2,
                    YAttributes.STATUS_CHANCE.get(), 0.3
            ));

            temp.put(IronsSpellbooksItem.MISERY.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.3,
                    YAttributes.STATUS_CHANCE.get(), 0.1
            ));

            temp.put(IronsSpellbooksItem.AUTOLOADER_CROSSBOW.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.3,
                    YAttributes.STATUS_CHANCE.get(), 0.25
            ));

            temp.put(IronsSpellbooksItem.HELLRAZOR.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.3,
                    YAttributes.CRITICAL_DAMAGE.get(), 0.2,
                    YAttributes.STATUS_CHANCE.get(), 0.1
            ));

            temp.put(IronsSpellbooksItem.DECREPIT_SCYTHE.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.25,
                    YAttributes.STATUS_CHANCE.get(), 0.1
            ));

            temp.put(IronsSpellbooksItem.ICE_GREATSWORD.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.25,
                    YAttributes.STATUS_CHANCE.get(), 0.2
            ));
        }

        if (ModList.get().isLoaded(SupportedMod.CATACLYSM.getValue())) {
            temp.put(LEndersCataclysmItem.THE_INCINERATOR.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.25,
                    YAttributes.STATUS_CHANCE.get(), 0.15,
                    YAttributes.LIFE_STEAL.get(), 0.05
            ));
            temp.put(LEndersCataclysmItem.WRATH_OF_THE_DESERT.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.4,
                    YAttributes.STATUS_CHANCE.get(), 0.2
            ));

            temp.put(LEndersCataclysmItem.GAUNTLET_OF_MAELSTROM.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.15,
                    YAttributes.STATUS_CHANCE.get(), 0.15
            ));

            temp.put(LEndersCataclysmItem.GAUNTLET_OF_BULWARK.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.15,
                    YAttributes.STATUS_CHANCE.get(), 0.15
            ));

            temp.put(LEndersCataclysmItem.GAUNTLET_OF_GUARD.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.15,
                    YAttributes.STATUS_CHANCE.get(), 0.15
            ));

            temp.put(LEndersCataclysmItem.MEAT_SHREDDER.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.05,
                    YAttributes.CRITICAL_DAMAGE.get(), 0.5,
                    YAttributes.STATUS_CHANCE.get(), 0.3
            ));

            temp.put(LEndersCataclysmItem.VOID_ASSAULT_SHOULDER_WEAPON.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.5,
                    YAttributes.STATUS_CHANCE.get(), 0.3
            ));

            temp.put(LEndersCataclysmItem.WITHER_ASSAULT_SHOULDER_WEAPON.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.4,
                    YAttributes.STATUS_CHANCE.get(), 0.2
            ));

            temp.put(LEndersCataclysmItem.CURSED_BOW.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.4,
                    YAttributes.STATUS_CHANCE.get(), 0.2
            ));

            temp.put(LEndersCataclysmItem.LASER_GATLING.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.2,
                    YAttributes.CRITICAL_DAMAGE.get(), -0.2,
                    YAttributes.STATUS_CHANCE.get(), 0.2
            ));

            temp.put(LEndersCataclysmItem.THE_ANNIHILATOR.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.28,
                    YAttributes.CRITICAL_DAMAGE.get(), 0.5,
                    YAttributes.STATUS_CHANCE.get(), 0.05
            ));

            temp.put(LEndersCataclysmItem.SOUL_RENDER.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.2,
                    YAttributes.STATUS_CHANCE.get(), 0.2
            ));

            temp.put(LEndersCataclysmItem.CERAUNUS.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.35,
                    YAttributes.STATUS_CHANCE.get(), 0.1
            ));

            temp.put(LEndersCataclysmItem.ASTRAPE.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.2,
                    YAttributes.STATUS_CHANCE.get(), 0.2
            ));

            temp.put(LEndersCataclysmItem.ANCIENT_SPEAR.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.2,
                    YAttributes.STATUS_CHANCE.get(), 0.2
            ));

            temp.put(LEndersCataclysmItem.TIDAL_CLAWS.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.15,
                    YAttributes.STATUS_CHANCE.get(), 0.25
            ));

            temp.put(LEndersCataclysmItem.THE_IMMOLATOR.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.28,
                    YAttributes.CRITICAL_DAMAGE.get(), 0.5,
                    YAttributes.STATUS_CHANCE.get(), 0.05
            ));

            temp.put(LEndersCataclysmItem.INFERNAL_FORGE.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.28,
                    YAttributes.CRITICAL_DAMAGE.get(), 0.5,
                    YAttributes.STATUS_CHANCE.get(), 0.05
            ));

            temp.put(LEndersCataclysmItem.VOID_FORGE.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.28,
                    YAttributes.CRITICAL_DAMAGE.get(), 0.5,
                    YAttributes.STATUS_CHANCE.get(), 0.05
            ));

            temp.put(LEndersCataclysmItem.ATHAME.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.25,
                    YAttributes.CRITICAL_DAMAGE.get(), 1.0,
                    YAttributes.STATUS_CHANCE.get(), 0.3
            ));

            temp.put(LEndersCataclysmItem.CORAL_SPEAR.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.25,
                    YAttributes.STATUS_CHANCE.get(), 0.15
            ));

            temp.put(LEndersCataclysmItem.CORAL_BARDICHE.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.25,
                    YAttributes.STATUS_CHANCE.get(), 0.15
            ));

            temp.put(LEndersCataclysmItem.BLACK_STEEL_SWORD.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.15,
                    YAttributes.STATUS_CHANCE.get(), 0.15
            ));

            temp.put(LEndersCataclysmItem.BLACK_STEEL_AXE.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.15,
                    YAttributes.STATUS_CHANCE.get(), 0.15
            ));

        }

        if (ModList.get().isLoaded(SupportedMod.IAF.getValue())) {
            temp.put(IceAndFireCEItem.SILVER_SWORD.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.1,
                    YAttributes.STATUS_CHANCE.get(), 0.28
            ));
            temp.put(IceAndFireCEItem.SILVER_AXE.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.1,
                    YAttributes.STATUS_CHANCE.get(), 0.28
            ));
            temp.put(IceAndFireCEItem.COPPER_SWORD.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.12,
                    YAttributes.STATUS_CHANCE.get(), 0.28
            ));
            temp.put(IceAndFireCEItem.COPPER_AXE.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.12,
                    YAttributes.STATUS_CHANCE.get(), 0.28
            ));
            temp.put(IceAndFireCEItem.DRAGONBONE_SWORD.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.18,
                    YAttributes.STATUS_CHANCE.get(), 0.18
            ));
            temp.put(IceAndFireCEItem.DRAGONBONE_AXE.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.18,
                    YAttributes.STATUS_CHANCE.get(), 0.18
            ));
            temp.put(IceAndFireCEItem.DRAGONBONE_BOW.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.45,
                    YAttributes.STATUS_CHANCE.get(), 0.1
            ));
            temp.put(IceAndFireCEItem.DRAGONSTEEL_FIRE_SWORD.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.3,
                    YAttributes.STATUS_CHANCE.get(), 0.1
            ));
            temp.put(IceAndFireCEItem.DRAGONSTEEL_FIRE_AXE.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.3,
                    YAttributes.STATUS_CHANCE.get(), 0.1
            ));
            temp.put(IceAndFireCEItem.DRAGONSTEEL_ICE_SWORD.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.25,
                    YAttributes.CRITICAL_DAMAGE.get(), 0.2,
                    YAttributes.STATUS_CHANCE.get(), 0.1
            ));
            temp.put(IceAndFireCEItem.DRAGONSTEEL_ICE_AXE.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.24,
                    YAttributes.CRITICAL_DAMAGE.get(), 0.2,
                    YAttributes.STATUS_CHANCE.get(), 0.1
            ));
            temp.put(IceAndFireCEItem.DRAGONSTEEL_LIGHTNING_SWORD.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.15,
                    YAttributes.STATUS_CHANCE.get(), 0.25
            ));
            temp.put(IceAndFireCEItem.DRAGONSTEEL_LIGHTNING_AXE.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.15,
                    YAttributes.STATUS_CHANCE.get(), 0.25
            ));
            temp.put(IceAndFireCEItem.DREAD_SWORD.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.3,
                    YAttributes.STATUS_CHANCE.get(), 0.1
            ));
            temp.put(IceAndFireCEItem.DREAD_KNIGHT_SWORD.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.2,
                    YAttributes.STATUS_CHANCE.get(), 0.1
            ));
            temp.put(IceAndFireCEItem.DRAGONBONE_SWORD_FIRE.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.25,
                    YAttributes.STATUS_CHANCE.get(), 0.1
            ));
            temp.put(IceAndFireCEItem.DRAGONBONE_SWORD_ICE.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.2,
                    YAttributes.CRITICAL_DAMAGE.get(), 0.2,
                    YAttributes.STATUS_CHANCE.get(), 0.1
            ));
            temp.put(IceAndFireCEItem.DRAGONBONE_SWORD_LIGHTNING.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.15,
                    YAttributes.STATUS_CHANCE.get(), 0.2
            ));
            temp.put(IceAndFireCEItem.HIPPOGRYPH_SWORD.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.15,
                    YAttributes.STATUS_CHANCE.get(), 0.15
            ));
            temp.put(IceAndFireCEItem.DEATHWORM_GAUNTLET_YELLOW.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.2,
                    YAttributes.STATUS_CHANCE.get(), 0.2
            ));
            temp.put(IceAndFireCEItem.DEATHWORM_GAUNTLET_WHITE.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.2,
                    YAttributes.STATUS_CHANCE.get(), 0.2
            ));
            temp.put(IceAndFireCEItem.DEATHWORM_GAUNTLET_RED.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.2,
                    YAttributes.STATUS_CHANCE.get(), 0.2
            ));
            temp.put(IceAndFireCEItem.COCKATRICE_SCEPTER.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.05,
                    YAttributes.STATUS_CHANCE.get(), 0.25
            ));
            temp.put(IceAndFireCEItem.STYMPHALIAN_DAGGER.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.1,
                    YAttributes.STATUS_CHANCE.get(), 0.22
            ));
            temp.put(IceAndFireCEItem.AMPHITHERE_MACUAHUITL.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.22,
                    YAttributes.STATUS_CHANCE.get(), 0.14
            ));
            temp.put(IceAndFireCEItem.TIDE_TRIDENT.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.25,
                    YAttributes.STATUS_CHANCE.get(), 0.15
            ));
            temp.put(IceAndFireCEItem.GHOST_SWORD.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.15,
                    YAttributes.STATUS_CHANCE.get(), 0.25
            ));
            temp.put(IceAndFireCEItem.TROLL_WEAPON_AXE.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.25,
                    YAttributes.STATUS_CHANCE.get(), 0.1
            ));
            temp.put(IceAndFireCEItem.TROLL_WEAPON_COLUMN.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.25,
                    YAttributes.STATUS_CHANCE.get(), 0.1
            ));
            temp.put(IceAndFireCEItem.TROLL_WEAPON_COLUMN_FOREST.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.25,
                    YAttributes.STATUS_CHANCE.get(), 0.1
            ));
            temp.put(IceAndFireCEItem.TROLL_WEAPON_COLUMN_FROST.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.25,
                    YAttributes.STATUS_CHANCE.get(), 0.1
            ));
            temp.put(IceAndFireCEItem.TROLL_WEAPON_HAMMER.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.25,
                    YAttributes.STATUS_CHANCE.get(), 0.1
            ));
            temp.put(IceAndFireCEItem.TROLL_WEAPON_TRUNK.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.25,
                    YAttributes.STATUS_CHANCE.get(), 0.1
            ));
            temp.put(IceAndFireCEItem.TROLL_WEAPON_TRUNK_FROST.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.25,
                    YAttributes.STATUS_CHANCE.get(), 0.1
            ));
        }

        if (ModList.get().isLoaded(SupportedMod.TWILIGHTFOREST.getValue())) {
            temp.put(TwilightForestItem.IRONWOOD_SWORD.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.15,
                    YAttributes.STATUS_CHANCE.get(), 0.15
            ));
            temp.put(TwilightForestItem.IRONWOOD_AXE.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.15,
                    YAttributes.STATUS_CHANCE.get(), 0.15
            ));
            temp.put(TwilightForestItem.FIERY_SWORD.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.2,
                    YAttributes.STATUS_CHANCE.get(), 0.2
            ));
            temp.put(TwilightForestItem.STEELEAF_SWORD.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.12,
                    YAttributes.STATUS_CHANCE.get(), 0.18
            ));
            temp.put(TwilightForestItem.STEELEAF_AXE.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.12,
                    YAttributes.STATUS_CHANCE.get(), 0.18
            ));
            temp.put(TwilightForestItem.GOLDEN_MINOTAUR_AXE.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.1,
                    YAttributes.STATUS_CHANCE.get(), 0.3
            ));
            temp.put(TwilightForestItem.DIAMOND_MINOTAUR_AXE.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.25,
                    YAttributes.STATUS_CHANCE.get(), 0.15
            ));
            temp.put(TwilightForestItem.KNIGHTMETAL_SWORD.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.25,
                    YAttributes.STATUS_CHANCE.get(), 0.1
            ));
            temp.put(TwilightForestItem.KNIGHTMETAL_AXE.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.25,
                    YAttributes.STATUS_CHANCE.get(), 0.1
            ));
            temp.put(TwilightForestItem.ICE_SWORD.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.18,
                    YAttributes.CRITICAL_DAMAGE.get(), 0.2,
                    YAttributes.STATUS_CHANCE.get(), 0.1
            ));
            temp.put(TwilightForestItem.GLASS_SWORD.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.35,
                    YAttributes.CRITICAL_DAMAGE.get(), 0.5
            ));
            temp.put(TwilightForestItem.GIANT_SWORD.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.35,
                    YAttributes.STATUS_CHANCE.get(), 0.08
            ));
            temp.put(TwilightForestItem.TRIPLE_BOW.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.5,
                    YAttributes.CRITICAL_DAMAGE.get(), -0.5,
                    YAttributes.MULTISHOT.get(), 2.0,
                    YAttributes.STATUS_CHANCE.get(), 0.2
            ));
            temp.put(TwilightForestItem.SEEKER_BOW.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.4,
                    YAttributes.STATUS_CHANCE.get(), 0.2
            ));
            temp.put(TwilightForestItem.ICE_BOW.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.35,
                    YAttributes.CRITICAL_DAMAGE.get(), 0.5,
                    YAttributes.STATUS_CHANCE.get(), 0.1
            ));
            temp.put(TwilightForestItem.ENDER_BOW.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.4,
                    YAttributes.STATUS_CHANCE.get(), 0.2
            ));
        }

        if (ModList.get().isLoaded(SupportedMod.L2ARCHERY.getValue()))
        {
            temp.put(L2ArcheryItem.STARTER_BOW.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.4,
                    YAttributes.STATUS_CHANCE.get(), 0.2
            ));

            temp.put(L2ArcheryItem.IRON_BOW.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.4,
                    YAttributes.STATUS_CHANCE.get(), 0.2
            ));

            temp.put(L2ArcheryItem.MASTER_BOW.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.35,
                    YAttributes.STATUS_CHANCE.get(), 0.25
            ));

            temp.put(L2ArcheryItem.GLOW_AIM_BOW.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.4,
                    YAttributes.STATUS_CHANCE.get(), 0.2
            ));

            temp.put(L2ArcheryItem.MAGNIFY_BOW.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.4,
                    YAttributes.STATUS_CHANCE.get(), 0.2
            ));

            temp.put(L2ArcheryItem.ENDER_AIM_BOW.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.4,
                    YAttributes.STATUS_CHANCE.get(), 0.2
            ));

            temp.put(L2ArcheryItem.EAGLE_BOW.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.4,
                    YAttributes.STATUS_CHANCE.get(), 0.2
            ));

            temp.put(L2ArcheryItem.EXPLOSION_BOW.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.3,
                    YAttributes.STATUS_CHANCE.get(), 0.3
            ));

            temp.put(L2ArcheryItem.FLAME_BOW.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.4,
                    YAttributes.STATUS_CHANCE.get(), 0.2
            ));

            temp.put(L2ArcheryItem.FROZE_BOW.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.4,
                    YAttributes.STATUS_CHANCE.get(), 0.2
            ));

            temp.put(L2ArcheryItem.BLACKSTONE_BOW.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.5,
                    YAttributes.STATUS_CHANCE.get(), 0.1
            ));

            temp.put(L2ArcheryItem.STORM_BOW.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.4,
                    YAttributes.STATUS_CHANCE.get(), 0.2
            ));

            temp.put(L2ArcheryItem.TURTLE_BOW.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.4,
                    YAttributes.STATUS_CHANCE.get(), 0.2
            ));

            temp.put(L2ArcheryItem.EARTH_BOW.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.5,
                    YAttributes.STATUS_CHANCE.get(), 0.1
            ));

            temp.put(L2ArcheryItem.WIND_BOW.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.4,
                    YAttributes.STATUS_CHANCE.get(), 0.2
            ));

            temp.put(L2ArcheryItem.WINTER_BOW.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.4,
                    YAttributes.STATUS_CHANCE.get(), 0.2
            ));

            temp.put(L2ArcheryItem.GAIA_BOW.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.4,
                    YAttributes.STATUS_CHANCE.get(), 0.2
            ));

            temp.put(L2ArcheryItem.VOID_BOW.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.4,
                    YAttributes.STATUS_CHANCE.get(), 0.2
            ));

            temp.put(L2ArcheryItem.SUN_BOW.get(), Map.of(
                    YAttributes.CRITICAL_CHANCE.get(), 0.4,
                    YAttributes.STATUS_CHANCE.get(), 0.2
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
