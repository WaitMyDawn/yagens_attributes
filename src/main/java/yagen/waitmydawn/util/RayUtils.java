package yagen.waitmydawn.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;

import javax.annotation.Nullable;

public final class RayUtils {

    /**
     * ray trace from eyes of player to get entities
     *
     * @param player        player
     * @param distance      max range
     * @param fluids        get fluids?
     * @param ignoreBlocks  ignore blocks for entities?
     * @return return entities orElse null
     */
    @Nullable
    public static EntityHitResult rayLivingEntity(
            Player player,
            double distance,
            boolean fluids,
            boolean ignoreBlocks) {

        Level level = player.level();
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(distance));

        ClipContext.Fluid fluidMode = fluids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE;
        ClipContext.Block blockMode = ignoreBlocks
                ? ClipContext.Block.OUTLINE
                : ClipContext.Block.COLLIDER;
        BlockHitResult blockHit = level.clip(
                new ClipContext(eye, end, blockMode, fluidMode, player));

        double blockDist = distance;
        if (!ignoreBlocks && blockHit.getType() != HitResult.Type.MISS) {
            blockDist = blockHit.getLocation().distanceTo(eye);
        }

        AABB box = player.getBoundingBox()
                .expandTowards(look.scale(blockDist))
                .inflate(1.0);

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                player,
                eye,
                eye.add(look.scale(blockDist)),
                box,
                e -> !e.isSpectator()
                        && e.isPickable()
                        && e instanceof LivingEntity,
                0.0F);

        if (entityHit != null) {
            double entityDist = entityHit.getLocation().distanceTo(eye);
            if (!ignoreBlocks && blockHit.getType() != HitResult.Type.MISS) {
                return entityDist <= blockDist ? entityHit : null;
            }
            return entityHit;
        }
        return null;
    }

    @Nullable
    public static BlockHitResult rayBlock(Player player, double distance, boolean ignoreFluid) {
        Level level = player.level();
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(distance));

        ClipContext.Fluid fluidMode = ignoreFluid
                ? ClipContext.Fluid.NONE
                : ClipContext.Fluid.ANY;

        return level.clip(new ClipContext(eye, end,
                ClipContext.Block.COLLIDER,
                fluidMode, player));
    }

    @Nullable
    public static BlockHitResult rayFluid(Player player, double distance, boolean ignoreBlock) {
        Level level = player.level();
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(distance));

        ClipContext.Block blockMode = ignoreBlock
                ? ClipContext.Block.OUTLINE
                : ClipContext.Block.COLLIDER;

        return level.clip(new ClipContext(eye, end,
                blockMode,
                ClipContext.Fluid.ANY,
                player));
    }

    @Nullable
    public static LivingEntity getTargetedLiving(Player player, double range) {
        EntityHitResult r = rayLivingEntity(player, range, false, false);
        return r != null && r.getEntity() instanceof LivingEntity liv ? liv : null;
    }

    @Nullable
    public static BlockState getTargetedBlock(Player player, double range, boolean ignoreFluid) {
        BlockHitResult r = rayBlock(player, range, ignoreFluid);
        return r != null && r.getType() == HitResult.Type.BLOCK
                ? player.level().getBlockState(r.getBlockPos())
                : null;
    }

    @Nullable
    public static BlockPos getTargetedBlockPos(Player player, double range, boolean ignoreFluid) {
        BlockHitResult r = rayBlock(player, range, ignoreFluid);
        return r != null && r.getType() == HitResult.Type.BLOCK ? r.getBlockPos() : null;
    }

    @Nullable
    public static BlockState getTargetedFluid(Player player, double range, boolean ignoreBlock) {
        BlockHitResult r = rayFluid(player, range, ignoreBlock);
        return r != null && r.getType() == HitResult.Type.BLOCK
                && player.level().getFluidState(r.getBlockPos()).isSource()
                ? player.level().getBlockState(r.getBlockPos())
                : null;
    }

    @Nullable
    public static BlockPos getTargetedFluidPos(Player player, double range, boolean ignoreBlock) {
        BlockHitResult r = rayFluid(player, range, ignoreBlock);
        return r != null && r.getType() == HitResult.Type.BLOCK
                && player.level().getFluidState(r.getBlockPos()).isSource()
                ? r.getBlockPos()
                : null;
    }

    private RayUtils() {}
}