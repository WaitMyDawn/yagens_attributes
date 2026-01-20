package yagen.waitmydawn.registries;

import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import yagen.waitmydawn.YagensAttributes;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import yagen.waitmydawn.entity.BladeEntity;
import yagen.waitmydawn.entity.ReservoirEntity;

import java.util.function.Supplier;


public class EntityRegistry {
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, YagensAttributes.MODID);

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }

    public static final Supplier<EntityType<BladeEntity>> BLADE =
            ENTITIES.register("blade_entity",
                    () -> EntityType.Builder.<BladeEntity>of(
                                    BladeEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(32)
                            .updateInterval(1)
                            .build("blade_entity"));

    public static final Supplier<EntityType<ReservoirEntity>> RESERVOIR =
            ENTITIES.register("reservoir",
                    () -> EntityType.Builder.<ReservoirEntity>of(
                            ReservoirEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .build("reservoir"));
}

