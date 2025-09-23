package yagen.waitmydawn.api.mods;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;
import yagen.waitmydawn.item.weapon.IceAndFireCEItem;
import yagen.waitmydawn.item.weapon.IronsSpellbooksItem;
import yagen.waitmydawn.item.weapon.LEndersCataclysmItem;
import yagen.waitmydawn.item.weapon.TwilightForestItem;
import yagen.waitmydawn.util.SupportedMod;

public class RivenModPool {
    private RivenModPool() {
    }

    public static record RivenEntry(Item item, double disposition) {
    }

    private static final ImmutableList<RivenEntry> buildEntries() {
        ImmutableList.Builder<RivenEntry> builder = ImmutableList.builder();

        builder.add(new RivenEntry(Items.DIAMOND_SWORD, 1.35f));
        builder.add(new RivenEntry(Items.DIAMOND_AXE, 1.35f));
        builder.add(new RivenEntry(Items.NETHERITE_SWORD, 1.3f));
        builder.add(new RivenEntry(Items.NETHERITE_AXE, 1.3f));
        builder.add(new RivenEntry(Items.BOW, 1.0f));
        builder.add(new RivenEntry(Items.TRIDENT, 1.2f));
        builder.add(new RivenEntry(Items.CROSSBOW, 0.95f));

        if (ModList.get().isLoaded(SupportedMod.IRONSSPELLBOOK.getValue())) {
            builder.add(new RivenEntry(IronsSpellbooksItem.KEEPER_FLAMBERGE.get(), 0.9f));
            builder.add(new RivenEntry(IronsSpellbooksItem.MAGEHUNTER.get(), 1.0f));
            builder.add(new RivenEntry(IronsSpellbooksItem.SPELLBREAKER.get(), 0.9f));
            builder.add(new RivenEntry(IronsSpellbooksItem.LEGIONNAIRE_FLAMBERGE.get(), 0.85f));
            builder.add(new RivenEntry(IronsSpellbooksItem.AMETHYST_RAPIER.get(), 0.8f));
            builder.add(new RivenEntry(IronsSpellbooksItem.AUTOLOADER_CROSSBOW.get(), 0.85f));
            builder.add(new RivenEntry(IronsSpellbooksItem.DECREPIT_SCYTHE.get(), 0.85f));
            builder.add(new RivenEntry(IronsSpellbooksItem.HELLRAZOR.get(), 0.75f));
            builder.add(new RivenEntry(IronsSpellbooksItem.ICE_GREATSWORD.get(), 0.75f));
        }

        if (ModList.get().isLoaded(SupportedMod.CATACLYSM.getValue())) {
            builder.add(new RivenEntry(LEndersCataclysmItem.THE_INCINERATOR.get(), 0.65f));
            builder.add(new RivenEntry(LEndersCataclysmItem.ASTRAPE.get(), 0.7f));
            builder.add(new RivenEntry(LEndersCataclysmItem.THE_ANNIHILATOR.get(), 0.95f));
            builder.add(new RivenEntry(LEndersCataclysmItem.SOUL_RENDER.get(), 0.7f));
            builder.add(new RivenEntry(LEndersCataclysmItem.WRATH_OF_THE_DESERT.get(), 1.0f));
            builder.add(new RivenEntry(LEndersCataclysmItem.GAUNTLET_OF_MAELSTROM.get(), 0.85f));
            builder.add(new RivenEntry(LEndersCataclysmItem.GAUNTLET_OF_BULWARK.get(), 0.85f));
            builder.add(new RivenEntry(LEndersCataclysmItem.GAUNTLET_OF_GUARD.get(), 0.85f));
            builder.add(new RivenEntry(LEndersCataclysmItem.CERAUNUS.get(), 1.0f));
            builder.add(new RivenEntry(LEndersCataclysmItem.ANCIENT_SPEAR.get(), 0.8f));
            builder.add(new RivenEntry(LEndersCataclysmItem.TIDAL_CLAWS.get(), 0.75f));
            builder.add(new RivenEntry(LEndersCataclysmItem.VOID_ASSAULT_SHOULDER_WEAPON.get(), 1.2f));
            builder.add(new RivenEntry(LEndersCataclysmItem.WITHER_ASSAULT_SHOULDER_WEAPON.get(), 1.25f));
            builder.add(new RivenEntry(LEndersCataclysmItem.LASER_GATLING.get(), 0.9f));
            builder.add(new RivenEntry(LEndersCataclysmItem.MEAT_SHREDDER.get(), 0.7f));
            builder.add(new RivenEntry(LEndersCataclysmItem.THE_IMMOLATOR.get(), 0.8f));
            builder.add(new RivenEntry(LEndersCataclysmItem.CURSED_BOW.get(), 1.0f));
            builder.add(new RivenEntry(LEndersCataclysmItem.INFERNAL_FORGE.get(), 0.95f));
            builder.add(new RivenEntry(LEndersCataclysmItem.VOID_FORGE.get(), 0.9f));
            builder.add(new RivenEntry(LEndersCataclysmItem.ATHAME.get(), 1.55f));
            builder.add(new RivenEntry(LEndersCataclysmItem.CORAL_BARDICHE.get(), 1.2f));
            builder.add(new RivenEntry(LEndersCataclysmItem.CORAL_SPEAR.get(), 1.2f));
            builder.add(new RivenEntry(LEndersCataclysmItem.BLACK_STEEL_SWORD.get(), 1.35f));
            builder.add(new RivenEntry(LEndersCataclysmItem.BLACK_STEEL_AXE.get(), 1.35f));
        }

        if (ModList.get().isLoaded(SupportedMod.IAF.getValue())) {
            builder.add(new RivenEntry(IceAndFireCEItem.SILVER_SWORD.get(), 1.35f));
            builder.add(new RivenEntry(IceAndFireCEItem.SILVER_AXE.get(), 1.35f));
            builder.add(new RivenEntry(IceAndFireCEItem.COPPER_SWORD.get(), 1.4f));
            builder.add(new RivenEntry(IceAndFireCEItem.COPPER_AXE.get(), 1.4f));
            builder.add(new RivenEntry(IceAndFireCEItem.DRAGONBONE_SWORD.get(), 1.25f));
            builder.add(new RivenEntry(IceAndFireCEItem.DRAGONBONE_AXE.get(), 1.25f));
            builder.add(new RivenEntry(IceAndFireCEItem.DRAGONBONE_BOW.get(), 0.95f));
            builder.add(new RivenEntry(IceAndFireCEItem.DRAGONSTEEL_FIRE_SWORD.get(), 0.55f));
            builder.add(new RivenEntry(IceAndFireCEItem.DRAGONSTEEL_FIRE_AXE.get(), 0.55f));
            builder.add(new RivenEntry(IceAndFireCEItem.DRAGONSTEEL_ICE_SWORD.get(), 0.55f));
            builder.add(new RivenEntry(IceAndFireCEItem.DRAGONSTEEL_ICE_AXE.get(), 0.55f));
            builder.add(new RivenEntry(IceAndFireCEItem.DRAGONSTEEL_LIGHTNING_SWORD.get(), 0.55f));
            builder.add(new RivenEntry(IceAndFireCEItem.DRAGONSTEEL_LIGHTNING_AXE.get(), 0.55f));
            builder.add(new RivenEntry(IceAndFireCEItem.DREAD_SWORD.get(), 1.35f));
            builder.add(new RivenEntry(IceAndFireCEItem.DREAD_KNIGHT_SWORD.get(), 0.75f));
            builder.add(new RivenEntry(IceAndFireCEItem.DRAGONBONE_SWORD_FIRE.get(), 1.0f));
            builder.add(new RivenEntry(IceAndFireCEItem.DRAGONBONE_SWORD_ICE.get(), 1.0f));
            builder.add(new RivenEntry(IceAndFireCEItem.DRAGONBONE_SWORD_LIGHTNING.get(), 1.0f));
            builder.add(new RivenEntry(IceAndFireCEItem.HIPPOGRYPH_SWORD.get(), 1.2f));
            builder.add(new RivenEntry(IceAndFireCEItem.DEATHWORM_GAUNTLET_YELLOW.get(), 1.0f));
            builder.add(new RivenEntry(IceAndFireCEItem.DEATHWORM_GAUNTLET_WHITE.get(), 1.0f));
            builder.add(new RivenEntry(IceAndFireCEItem.DEATHWORM_GAUNTLET_RED.get(), 1.0f));
            builder.add(new RivenEntry(IceAndFireCEItem.COCKATRICE_SCEPTER.get(), 0.95f));
            builder.add(new RivenEntry(IceAndFireCEItem.STYMPHALIAN_DAGGER.get(), 1.0f));
            builder.add(new RivenEntry(IceAndFireCEItem.AMPHITHERE_MACUAHUITL.get(), 0.9f));
            builder.add(new RivenEntry(IceAndFireCEItem.TIDE_TRIDENT.get(), 0.75f));
            builder.add(new RivenEntry(IceAndFireCEItem.GHOST_SWORD.get(), 0.75f));
            builder.add(new RivenEntry(IceAndFireCEItem.TROLL_WEAPON_AXE.get(), 1.2f));
            builder.add(new RivenEntry(IceAndFireCEItem.TROLL_WEAPON_COLUMN.get(), 1.2f));
            builder.add(new RivenEntry(IceAndFireCEItem.TROLL_WEAPON_COLUMN_FROST.get(), 1.2f));
            builder.add(new RivenEntry(IceAndFireCEItem.TROLL_WEAPON_COLUMN_FOREST.get(), 1.2f));
            builder.add(new RivenEntry(IceAndFireCEItem.TROLL_WEAPON_HAMMER.get(), 1.2f));
            builder.add(new RivenEntry(IceAndFireCEItem.TROLL_WEAPON_TRUNK.get(), 1.2f));
            builder.add(new RivenEntry(IceAndFireCEItem.TROLL_WEAPON_TRUNK_FROST.get(), 1.2f));
        }

        if(ModList.get().isLoaded(SupportedMod.TWILIGHTFOREST.getValue())) {
            builder.add(new RivenEntry(TwilightForestItem.IRONWOOD_SWORD.get(), 1.35f));
            builder.add(new RivenEntry(TwilightForestItem.IRONWOOD_AXE.get(), 1.35f));
            builder.add(new RivenEntry(TwilightForestItem.FIERY_SWORD.get(), 1.1f));
            builder.add(new RivenEntry(TwilightForestItem.STEELEAF_SWORD.get(), 1.3f));
            builder.add(new RivenEntry(TwilightForestItem.STEELEAF_AXE.get(), 1.3f));
            builder.add(new RivenEntry(TwilightForestItem.GOLDEN_MINOTAUR_AXE.get(), 1.05f));
            builder.add(new RivenEntry(TwilightForestItem.DIAMOND_MINOTAUR_AXE.get(), 0.95f));
            builder.add(new RivenEntry(TwilightForestItem.KNIGHTMETAL_SWORD.get(), 1.0f));
            builder.add(new RivenEntry(TwilightForestItem.KNIGHTMETAL_AXE.get(), 1.0f));
            builder.add(new RivenEntry(TwilightForestItem.ICE_SWORD.get(), 1.1f));
            builder.add(new RivenEntry(TwilightForestItem.GLASS_SWORD.get(), 0.5f));
            builder.add(new RivenEntry(TwilightForestItem.GIANT_SWORD.get(), 0.75f));
            builder.add(new RivenEntry(TwilightForestItem.TRIPLE_BOW.get(), 0.7f));
            builder.add(new RivenEntry(TwilightForestItem.SEEKER_BOW.get(), 0.9f));
            builder.add(new RivenEntry(TwilightForestItem.ICE_BOW.get(), 0.8f));
            builder.add(new RivenEntry(TwilightForestItem.ENDER_BOW.get(), 0.8f));
        }

        return builder.build();
    }

    public static final ImmutableList<RivenEntry> ENTRIES = buildEntries();

    public static double getDisposition(Item item) {
        return ENTRIES.stream()
                .filter(e -> e.item() == item)
                .map(RivenEntry::disposition)
                .findFirst()
                .orElse(0.01);
    }

    public static Item randomWeapon(RandomSource random) {
        Item item = ENTRIES.get(random.nextInt(ENTRIES.size())).item();
        while (item == Items.AIR) item = ENTRIES.get(random.nextInt(ENTRIES.size())).item();
        return item;
    }

    public static RivenEntry randomRivenEntry(RandomSource random) {
        return ENTRIES.get(random.nextInt(ENTRIES.size()));
    }

}
