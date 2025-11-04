package yagen.waitmydawn.api.events;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import yagen.waitmydawn.YagensAttributes;
import java.util.ArrayList;
import java.util.List;


@EventBusSubscriber(modid = YagensAttributes.MODID, bus = EventBusSubscriber.Bus.GAME)
public class MissionEvent {
    private static boolean executed = false;
    private static final List<EntityType<? extends Monster>> MONSTER_TYPES = new ArrayList<>();

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        if (executed) return;
        executed = true;
        BuiltInRegistries.ENTITY_TYPE.stream()
                .filter(type -> type.getCategory() == MobCategory.MONSTER)
                .forEach(type -> {
                    try {
                        MONSTER_TYPES.add((EntityType<? extends Monster>) type);
                    } catch (ClassCastException e) {
                        System.out.println("fail to transform to Monster: " + type);
                    }
                });
//        System.out.println("Monster pool created!");
//        MONSTER_TYPES.forEach(type ->
//                System.out.println("find Monster : " + type.toString())
//        );
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
}
