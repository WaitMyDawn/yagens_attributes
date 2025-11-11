package yagen.waitmydawn.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import static yagen.waitmydawn.api.mission.MissionHandler.randomMonsterType;
import static yagen.waitmydawn.api.mission.MissionHandler.summonEntity;

public class SummonItem extends Item {

    public SummonItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player,
                                                  InteractionHand hand) {
        if (!level.isClientSide) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            summonEntity(randomMonsterType(serverPlayer.getRandom()),
                    serverPlayer.serverLevel(),
                    serverPlayer.position().add(serverPlayer.getLookAngle().scale(3)));
            player.sendSystemMessage(Component.literal("Entity Summoned!"));
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand),
                level.isClientSide);
    }
}