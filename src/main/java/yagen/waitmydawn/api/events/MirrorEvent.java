package yagen.waitmydawn.api.events;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.registries.DimensionRegistry;

@EventBusSubscriber(modid = YagensAttributes.MODID, bus = EventBusSubscriber.Bus.GAME)
public class MirrorEvent {
    @SubscribeEvent
    public static void TreasureClearEvent(PlayerContainerEvent.Open event) {
        if (event.getContainer() instanceof ChestMenu menu) {
            if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            if (serverPlayer.level() instanceof ServerLevel serverLevel) {
                if (serverLevel.dimension() == DimensionRegistry.MIRROR_LEVEL) {
                    HitResult hit = serverPlayer.pick(64.0, 0.0f, false);
                    if (hit.getType() == HitResult.Type.BLOCK) {
                        BlockEntity blockEntity = serverLevel.getBlockEntity(((BlockHitResult) hit).getBlockPos());
                        if (blockEntity instanceof ChestBlockEntity chest)
                            if (!chest.getPersistentData().getString("TaskId").equals("")
                                    || chest.getPersistentData().getBoolean("MirrorProtection")) return;
                        if (blockEntity != null) {
                            blockEntity.getPersistentData().putBoolean("MirrorProtection", true);
                        }
                        menu.getContainer().clearContent();
                        menu.broadcastChanges();
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof ServerLevel level) {
            if (level.dimension() == DimensionRegistry.MIRROR_LEVEL) {
                BlockPos pos = event.getPos();

                var blockState = level.getBlockState(pos);
                if (blockState.getBlock() instanceof ChestBlock
                        || blockState.getBlock() instanceof BarrelBlock
                        || blockState.getBlock() instanceof ShulkerBoxBlock) {

                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof ServerLevel level) {
            if (level.dimension() == DimensionRegistry.MIRROR_LEVEL) {
                if (event.getEntity() instanceof Player)
                    if (event.getPlacedBlock().getCollisionShape(level, event.getPos()) != Shapes.block())
                        event.setCanceled(true);
            }
        }
    }
}
