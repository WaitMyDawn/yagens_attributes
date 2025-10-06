package yagen.waitmydawn.entity.others;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.function.Supplier;

public class DarkDoppelgangerEntity {
    private DarkDoppelgangerEntity() {}

    public static final Supplier<EntityType<?>> DARK_DOPPELGANGER =
            () -> BuiltInRegistries.ENTITY_TYPE.get(
                    ResourceLocation.fromNamespaceAndPath("darkdoppelganger", "dark_doppelganger")
            );
}
