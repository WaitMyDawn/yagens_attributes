package yagen.waitmydawn.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import yagen.waitmydawn.YagensAttributes;

import java.util.OptionalLong;


public class DimensionRegistry {
    public static final ResourceKey<Level> MIRROR_LEVEL =
            ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "mirror"));

    public static final ResourceKey<DimensionType> MIRROR =
            ResourceKey.create(Registries.DIMENSION_TYPE,
                    ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "mirror"));

    public static void bootstrap(BootstrapContext<DimensionType> context) {
        context.register(MIRROR, new DimensionType(
                OptionalLong.empty(),
                true,
                false,
                false,
                true,
                1.0,
                true,
                false,
                -64,
                384,
                384,
                BlockTags.INFINIBURN_OVERWORLD,
                BuiltinDimensionTypes.OVERWORLD_EFFECTS,
                0.0f,
                new DimensionType.MonsterSettings(false, false, UniformInt.of(0, 7), 0)
        ));
    }
}