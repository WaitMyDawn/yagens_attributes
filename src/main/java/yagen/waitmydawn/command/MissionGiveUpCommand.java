package yagen.waitmydawn.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import yagen.waitmydawn.api.mission.MissionData;

public class MissionGiveUpCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("MissionGiveUp")
                        .requires(src -> src.hasPermission(0))
                        .executes(ctx -> {
                            MissionData data = MissionData.get(ctx.getSource().getServer());
                            Player player = ctx.getSource().getPlayerOrException();
                            ServerLevel level = ctx.getSource().getLevel();
                            var active = data.getPlayerActiveTask(player);
                            if (level.isClientSide||active == null) return 0;
                            ResourceLocation taskId = active.getKey();
                            data.setFailed(level,level.dimension().location(),taskId);
                            ctx.getSource().sendSuccess(
                                    () -> Component.literal("Mission ["+taskId+"] failed!"),
                                    true);
                            return 1;
                        })
        );
    }
}