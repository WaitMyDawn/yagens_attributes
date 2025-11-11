package yagen.waitmydawn.api.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.entity.SummonEntityBlackList;

import java.util.ArrayList;
import java.util.List;


@EventBusSubscriber(modid = YagensAttributes.MODID, bus = EventBusSubscriber.Bus.GAME)
public class MissionEvent {
    @SubscribeEvent
    public static void exterminateEvent(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Monster monster)) return;
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;
    }


    private static boolean executed = false;
    private static final List<EntityType<? extends Monster>> MONSTER_TYPES = new ArrayList<>();
    private static final List<EntityType<? extends Monster>> BOSS_TYPES = new ArrayList<>();

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        if (executed) return;
        executed = true;
        BuiltInRegistries.ENTITY_TYPE.stream()
                .filter(type -> type.getCategory() == MobCategory.MONSTER)
                .filter(type -> !type.is(Tags.EntityTypes.BOSSES))
                .filter(type -> !type.is(SummonEntityBlackList.MONSTER_BLACK_LIST))
                .forEach(type -> {
                    try {
                        MONSTER_TYPES.add((EntityType<? extends Monster>) type);
                    } catch (ClassCastException e) {
                        System.out.println("fail to transform to Monster: " + type);
                    }
                });
        BuiltInRegistries.ENTITY_TYPE.stream()
                .filter(type -> (type.is(Tags.EntityTypes.BOSSES)
                        || BuiltInRegistries.ENTITY_TYPE.getKey(type).toString().equals("born_in_chaos_v1:lord_pumpkinhead")
                ))
                .filter(type -> !type.is(SummonEntityBlackList.BOSS_BLACK_LIST))
                .forEach(type -> {
                    try {
                        BOSS_TYPES.add((EntityType<? extends Monster>) type);
                    } catch (ClassCastException e) {
                        System.out.println("fail to transform to Monster (Boss): " + type);
                    }
                });
        MONSTER_TYPES.forEach(type ->
                System.out.println("find Monster : " + type.toString())
        );
        BOSS_TYPES.forEach(type ->
                System.out.println("find Monster (Boss) : " + type.toString())
        );
    }

    public static <T extends Mob> T summonEntity(EntityType<T> type,
                                                 ServerLevel level,
                                                 Vec3 pos) {
        T mob = type.create(level);
        if (mob == null) return null;
        mob.moveTo(pos.x, pos.y + 2, pos.z, level.random.nextFloat() * 360F, 0);

        level.addFreshEntity(mob);
        return mob;
    }

    public static EntityType<? extends Monster> randomMonsterType(RandomSource random) {
        if (MONSTER_TYPES.isEmpty()) {
            return EntityType.ZOMBIE;
        }
        return MONSTER_TYPES.get(random.nextInt(MONSTER_TYPES.size()));
    }

    public static EntityType<? extends Monster> randomBossType(RandomSource random) {
        return BOSS_TYPES.get(random.nextInt(BOSS_TYPES.size()));
    }
}
