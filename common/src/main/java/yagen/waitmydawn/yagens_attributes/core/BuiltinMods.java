package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;
import yagen.waitmydawn.yagens_attributes.core.mods.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.*;

/** Data-generation catalog; runtime definitions come exclusively from data packs. */
public final class BuiltinMods {
    private static final List<BuiltinMod> CONTENT = List.of(
            new UniversalNanocubeMod(),
            new GazeOfPainMod(),
            new TorturousEntanglementMod(),
            new BoneFingerBoxMod(),
            new HeatAdditionMod(),
            new ColdAdditionMod(),
            new ElectricityAdditionMod(),
            new ToxinAdditionMod(),
            new OrganShatterMod(),
            new MultishotMod(),
            new FlameFeatherMod(),
            new SlashBoostMod(),
            new ImpactBoostMod(),
            new PunctureBoostMod(),
            new CloudpiercingFlameMod(),
            new ShatteredVenomDollMod(),
            new WinterTalismanMod(),
            new ChargeCageMod(),
            new BodyCountMod(),
            new BloodRushMod(),
            new WeepingWoundsMod(),
            new DriftingContactMod(),
            new FuryMod(),
            new ReachMod(),
            new SpeedTriggerMod(),
            new CriticalDelayMod(),
            new SpoiledStrikeMod(),
            new HammerShotMod(),
            new ConditionOverloadMod(),
            new BerserkerFuryMod(),
            new PrimedUniversalNanocubeMod(),
            new PrimedReachMod(),
            new PrimedFuryMod(), new LethalTorrentMod(), new GalvanizedChamberMod(), new QuickeningMod());
    private BuiltinMods() {}

    public static ModCatalog catalog() {
        var result = new LinkedHashMap<String, ModDefinition>();
        for (var mod : CONTENT) {
            if (result.put(mod.id(), mod.definition()) != null) throw new IllegalStateException("Duplicate built-in Mod: " + mod.id());
        }
        var qualities = new EnumMap<Rarity, ModCatalog.Quality>(Rarity.class);
        quality(qualities, Rarity.COMMON, 0xB87333, 1, 0);
        quality(qualities, Rarity.UNCOMMON, 0xC0C0C0, 2, 10);
        quality(qualities, Rarity.RARE, 0xFFD700, 3, 20);
        quality(qualities, Rarity.GALVANIZED, 0x636A8E, 4, 30);
        quality(qualities, Rarity.LEGENDARY, 0xFF8C00, 4, 40);
        quality(qualities, Rarity.RIVEN, 0x8A2BE2, 5, 50);
        var polarities = new EnumMap<Polarity, ModCatalog.Polarity>(Polarity.class);
        for (var polarity : Polarity.values())
            polarities.put(polarity, new ModCatalog.Polarity(polarity, "polarity.yagens_attributes." + polarity.key(), polarity.symbol()));
        return new ModCatalog(result, qualities, polarities, BuiltinRivens.rules());
    }

    private static void quality(Map<Rarity, ModCatalog.Quality> result, Rarity type, int color, int base, int order) {
        result.put(type, new ModCatalog.Quality(type, "rarity.yagens_attributes." + type.key(), color, base, order,
                "Base Endo cost for upgrading level 0 to 1. An upgrade from level L to L+1 costs base_endo * 2^L. Recycling grants floor(2^(base_endo-1) + base_endo*(2^level-1)/2) Endo per card. Color is an RGB hex value; order controls ascending Creative-tab sorting."));
    }
}
