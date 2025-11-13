package yagen.waitmydawn.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import yagen.waitmydawn.api.mission.MissionData;

import java.util.Objects;

public class MissionCompleteCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("MissionComplete")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> {
                            MissionData data = MissionData.get(ctx.getSource().getServer());
                            Player player = EntityArgument.getPlayer(ctx, "player");
                            Level level = player.level();
                            var active = data.getPlayerActiveTask(player);
                            if (level.isClientSide||active == null) return 0;
                            ResourceLocation taskId = active.getKey();
                            data.setCompleted((ServerLevel)level,level.dimension().location(),taskId,true);
                            ctx.getSource().sendSuccess(
                                    () -> Component.literal("Mission ["+taskId+"] completed!"),
                                    true);
                            return 1;
                        })
        ));
    }
}