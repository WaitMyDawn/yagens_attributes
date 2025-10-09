package yagen.waitmydawn.entity.others;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import yagen.waitmydawn.item.Mod;

import java.util.function.Supplier;

public class ModularGolemsEntity {
    private ModularGolemsEntity() {}

    public static final Supplier<EntityType<?>> METAL_GOLEM =
            () -> BuiltInRegistries.ENTITY_TYPE.get(
                    ResourceLocation.fromNamespaceAndPath("modulargolems", "metal_golem")
            );

    public static final Supplier<EntityType<?>> HUMANOID_GOLEM =
            () -> BuiltInRegistries.ENTITY_TYPE.get(
                    ResourceLocation.fromNamespaceAndPath("modulargolems", "humanoid_golem")
            );

    public static final Supplier<EntityType<?>> DOG_GOLEM =
            () -> BuiltInRegistries.ENTITY_TYPE.get(
                    ResourceLocation.fromNamespaceAndPath("modulargolems", "dog_golem")
            );
}
