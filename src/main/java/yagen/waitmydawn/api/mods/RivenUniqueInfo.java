package yagen.waitmydawn.api.mods;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.RandomSource;

import java.util.*;

public record RivenUniqueInfo(String key, int weight, double baseValue) {

    public MutableComponent format(int modLevel) {
        double finalValue = baseValue * modLevel;
        return Component.translatable(key, finalValue);
    }

    public static List<RivenUniqueInfo> MELEE_POSITIVE = new ArrayList<>();
    public static List<RivenUniqueInfo> MELEE_NEGATIVE = new ArrayList<>();
    public static List<RivenUniqueInfo> PROJECTILE_POSITIVE = new ArrayList<>();
    public static List<RivenUniqueInfo> PROJECTILE_NEGATIVE = new ArrayList<>();
    public static List<RivenUniqueInfo> STAFF_POSITIVE = new ArrayList<>();
    public static List<RivenUniqueInfo> STAFF_NEGATIVE = new ArrayList<>();

    public static final Set<String> ATTRIBUTES_MODID = new HashSet<>();

    public static void reset() {
        MELEE_POSITIVE.clear();
        MELEE_NEGATIVE.clear();
        PROJECTILE_POSITIVE.clear();
        PROJECTILE_NEGATIVE.clear();
        STAFF_POSITIVE.clear();
        STAFF_NEGATIVE.clear();
        ATTRIBUTES_MODID.clear();
    }

    public static List<RivenUniqueInfo> draw(List<RivenUniqueInfo> pool, int n, RandomSource rnd) {
        if (n <= 0 || pool.isEmpty()) return List.of();
        if (n >= pool.size()) {
            return new ArrayList<>(pool);
        }
        List<RivenUniqueInfo> tempPool = new ArrayList<>(pool);
        List<RivenUniqueInfo> result = new ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            if (tempPool.isEmpty()) break;

            int totalWeight = 0;
            for (RivenUniqueInfo info : tempPool) {
                totalWeight += info.weight();
            }

            if (totalWeight <= 0) {
                result.add(tempPool.remove(rnd.nextInt(tempPool.size())));
                continue;
            }

            int target = rnd.nextInt(totalWeight);

            int currentWeight = 0;
            for (int j = 0; j < tempPool.size(); j++) {
                RivenUniqueInfo info = tempPool.get(j);
                currentWeight += info.weight();

                if (currentWeight > target) {
                    result.add(info);
                    tempPool.remove(j);
                    break;
                }
            }
        }

        return result;
    }
}