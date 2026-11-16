package yagen.waitmydawn.yagens_attributes.core;

import yagen.waitmydawn.yagens_attributes.core.loot.ModReward;
import java.util.*;

final class ModRewardTests {
    static void run() {
        var spec = new ModReward(ModReward.Method.WEIGHTED, Map.of(ModInfo.Rarity.COMMON, 1D, ModInfo.Rarity.RARE, 3D), "", 10, 80);
        var pools = Map.of(ModInfo.Rarity.COMMON, List.of("example:irregular"), ModInfo.Rarity.RARE, List.of("elsewhere:anything"), ModInfo.Rarity.GALVANIZED, List.of("example:layered"));
        check(spec.select(pools, bound -> 0, () -> .1).orElseThrow().equals("example:irregular")); check(spec.select(pools, bound -> 0, () -> .5).orElseThrow().equals("elsewhere:anything"));
        check(spec.select(Map.of(ModInfo.Rarity.RARE, List.of("example:only")), bound -> 0, () -> 0).orElseThrow().equals("example:only")); check(spec.select(Map.of(), bound -> 0, () -> 0).isEmpty());
        check(spec.rank(10, 0) == 1 && spec.rank(10, 1) == 8 && spec.rank(0, .5) == 0);
        var galvanized = new ModReward(ModReward.Method.GALVANIZED, Map.of(), "", 0, 100); check(galvanized.select(pools, bound -> 0, () -> 0).orElseThrow().equals("example:layered"));
        check(Double.isNaN(ModReward.meleeProbability(0, 0))); check(ModReward.meleeProbability(0, 1) == 0); check(ModReward.meleeProbability(1, 0) == 1); check(ModReward.meleeProbability(1, 1) == .5);
        check(Math.abs(ModReward.meleeProbability(16, 4) - 2D / 3) < 1E-12);
        for (int max : new int[]{0,1,5,8,10,60}) for (double sample : new double[]{0,.000001,.499,.5,.999999,1}) { int level = spec.rank(max, sample); check(level >= 0 && level <= max); }
        rejects(() -> new ModReward(ModReward.Method.WEIGHTED, Map.of(ModInfo.Rarity.RIVEN, 1D), "", 0, 100));
        rejects(() -> new ModReward(ModReward.Method.WEIGHTED, Map.of(ModInfo.Rarity.COMMON, 0D), "", 0, 100));
        rejects(() -> new ModReward(ModReward.Method.SPECIFIC, Map.of(), "example:test", 80, 10)); rejects(() -> new ModReward(ModReward.Method.GALVANIZED, Map.of(), "", -1, 100));
        System.out.println("Passed " + checks + " Mod loot selection/rank/weapon-pool contracts");
    }
    private static int checks;
    private static void check(boolean condition) { if (!condition) throw new AssertionError("Reward contract " + checks); checks++; }
    private static void rejects(Runnable action) { try { action.run(); } catch (IllegalArgumentException expected) { checks++; return; } throw new AssertionError("Expected rejection"); }
}
