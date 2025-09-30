package yagen.waitmydawn.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import static yagen.waitmydawn.render.BladeStormRenderer.spawnBlade;

public class BladeStormTargets {
    private static final WeakHashMap<ServerPlayer, List<LivingEntity>> TARGETS = new WeakHashMap<>();

    public static void add(ServerPlayer player, LivingEntity target) {
        TARGETS.computeIfAbsent(player, k -> new ArrayList<>()).add(target);
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
    }

    public static List<LivingEntity> get(ServerPlayer player) {
        return TARGETS.getOrDefault(player, List.of());
    }

    public static void clear(ServerPlayer player) {
        List<LivingEntity> list = TARGETS.remove(player);
        if (list != null) list.forEach(e -> e.removeEffect(MobEffects.GLOWING));
    }

    public static void execute(ServerPlayer player) {
        List<LivingEntity> list = TARGETS.remove(player);
        if (list == null) return;
        for (LivingEntity e : list) {
            if (e.isAlive() && e.level() == player.level()) {
                spawnBlade(player,e);
            }
            e.removeEffect(MobEffects.GLOWING);
        }
    }
}
