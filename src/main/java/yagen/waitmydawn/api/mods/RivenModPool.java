package yagen.waitmydawn.api.mods;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;
import yagen.waitmydawn.item.weapon.IronsSpellbooksItem;
import yagen.waitmydawn.item.weapon.LEndersCataclysmItem;

import java.util.List;
import java.util.Set;

public class RivenModPool {
    private RivenModPool() {
    }

    public static record RivenEntry(Item item, double disposition) {
    }

    /* 实际用于随机选取的列表 */
//    public static final List<Item> POOL = List.of(
//            Items.DIAMOND_SWORD,
//            Items.DIAMOND_AXE,
//            Items.NETHERITE_SWORD,
//            Items.NETHERITE_AXE,
//            Items.BOW,
//            Items.TRIDENT,
//            Items.CROSSBOW,
//
//            IronsSpellbooksItem.KEEPER_FLAMBERGE.get(),
//            IronsSpellbooksItem.MAGEHUNTER.get(),
//            LEndersCataclysmItem.THE_INCINERATOR.get(),
//            LEndersCataclysmItem.ASTRAPE.get()
//    );
//
//    public static final Set<Item> LOOKUP = Set.copyOf(POOL);
//
//    public static Item randomWeapon(RandomSource random) {
//        return POOL.get(random.nextInt(POOL.size()));
//    }

    private static final ImmutableList<RivenEntry> buildEntries() {
        ImmutableList.Builder<RivenEntry> builder = ImmutableList.builder();

        builder.add(new RivenEntry(Items.DIAMOND_SWORD, 1.35f));
        builder.add(new RivenEntry(Items.DIAMOND_AXE, 1.35f));
        builder.add(new RivenEntry(Items.NETHERITE_SWORD, 1.3f));
        builder.add(new RivenEntry(Items.NETHERITE_AXE, 1.3f));
        builder.add(new RivenEntry(Items.BOW, 1.0f));
        builder.add(new RivenEntry(Items.TRIDENT, 1.2f));
        builder.add(new RivenEntry(Items.CROSSBOW, 0.95f));

        if (ModList.get().isLoaded("irons_spellbooks")) {
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

        if (ModList.get().isLoaded("cataclysm")) {
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

        return builder.build();
    }

    public static final ImmutableList<RivenEntry> ENTRIES = buildEntries();

    /* 快速取 disposition */
    public static double getDisposition(Item item) {
        return ENTRIES.stream()
                .filter(e -> e.item() == item)
                .map(RivenEntry::disposition)
                .findFirst()
                .orElse(0.01);
    }

    /* 随机选一把武器 */
    public static Item randomWeapon(RandomSource random) {
        Item item = ENTRIES.get(random.nextInt(ENTRIES.size())).item();
        while(item==Items.AIR) item = ENTRIES.get(random.nextInt(ENTRIES.size())).item();
        return item;
    }

    /* 如果你想随机拿到整行记录，也可以： */
    public static RivenEntry randomRivenEntry(RandomSource random) {
        return ENTRIES.get(random.nextInt(ENTRIES.size()));
    }

}
