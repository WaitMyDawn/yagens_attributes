package yagen.waitmydawn.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public abstract class NBTOverrideItemModel implements BakedModel {
    private final BakedModel original;
    private final ItemOverrides itemOverrides;

    private static final ModelBaker NULL_MODEL_BAKER = new ModelBaker() {
        public Function<Material, TextureAtlasSprite> getModelTextureGetter() {
            return null;
        }

        @Override
        public @Nullable UnbakedModel getTopLevelModel(ModelResourceLocation location) {
            return null;
        }

        public BakedModel bake(ResourceLocation location, ModelState state, Function<Material, TextureAtlasSprite> sprites) {
            return null;
        }

        @Override
        public @Nullable BakedModel bakeUncached(UnbakedModel model, ModelState state, Function<Material, TextureAtlasSprite> sprites) {
            return null;
        }

        public UnbakedModel getModel(ResourceLocation resourceLocation) {
            return null;
        }

        public @Nullable BakedModel bake(ResourceLocation resourceLocation, ModelState modelState) {
            return null;
        }
    };

    public NBTOverrideItemModel(BakedModel original, ModelBakery loader) {
        this.original = original;
        BlockModel missing = null;

        this.itemOverrides = new ItemOverrides(NULL_MODEL_BAKER, missing, Collections.emptyList()) {
            @Override
            public BakedModel resolve(@NotNull BakedModel original, @NotNull ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity livingEntity, int seed) {
                return getModelFromStack(itemStack).map(resourceLocation -> Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(resourceLocation))).orElse(original);
            }
        };
    }

    abstract Optional<ResourceLocation> getModelFromStack(ItemStack itemStack);

    @NotNull
    @Override
    public ItemOverrides getOverrides() {
        return itemOverrides;
    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand) {
        return original.getQuads(state, side, rand);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return original.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return original.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return original.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return original.isCustomRenderer();
    }

    @NotNull
    @Override
    public TextureAtlasSprite getParticleIcon() {
        return original.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return original.getTransforms();
    }
}
