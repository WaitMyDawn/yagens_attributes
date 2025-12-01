package yagen.waitmydawn.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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
    public static final ResourceLocation MIRROR_EFFECT =
            ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "mirror_effect");

    public static final TagKey<Block> EMPTY_INFINIBURN =
            TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("air"));

    public static void bootstrap(BootstrapContext<DimensionType> context) {
        context.register(MIRROR, new DimensionType(OptionalLong.of(18000L),
                false, true, false, true,
                1, true, false,
                0, 128, 128,
                EMPTY_INFINIBURN,
                MIRROR_EFFECT,
                0.05f,
                new DimensionType.MonsterSettings(
                        true, false, ConstantInt.of(7), 15)));
    }
}