package yagen.waitmydawn.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import static yagen.waitmydawn.api.mission.MissionHandler.randomMonsterType;
import static yagen.waitmydawn.api.mission.MissionHandler.summonEntity;

public class SummonItem extends Item {

    public SummonItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, @NotNull Player player,
                                                           @NotNull InteractionHand hand) {
        if (!level.isClientSide) {
            ServerPlayer serverPlayer = (ServerPlayer) player;

            HitResult result = serverPlayer.pick(10.0D, 1.0F, false);
            if (result.getType() != HitResult.Type.BLOCK)
                return InteractionResultHolder.fail(player.getItemInHand(hand));

            Vec3 spawnPos = Vec3.atCenterOf(((BlockHitResult) result).getBlockPos());
            Vec3 finalPos = new Vec3(spawnPos.x, spawnPos.y + 1, spawnPos.z);
            summonEntity(randomMonsterType(serverPlayer.getRandom()),
                    serverPlayer.serverLevel(),
                    finalPos
//                    serverPlayer.position().add(serverPlayer.getLookAngle())
            );
            player.sendSystemMessage(Component.literal("Entity Summoned!"));
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand),
                level.isClientSide);
    }
}