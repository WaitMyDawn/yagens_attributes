package yagen.waitmydawn.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import yagen.waitmydawn.api.mission.MissionData;

import java.util.Objects;

import static yagen.waitmydawn.api.mission.MissionData.clearSummonedEntities;

public class MissionClearCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("MissionClear")
                        .requires(src -> src.hasPermission(4))
                        .executes(ctx -> {
                            MissionData data = MissionData.get(ctx.getSource().getServer());
                            ServerLevel level = ctx.getSource().getLevel();
                            Player player = ctx.getSource().getPlayerOrException();
                            var active = data.getPlayerActiveTask(player);
                            if (level.isClientSide||active == null) return 0;
                            ResourceLocation taskId = active.getKey();
                            data.clearAll(level,taskId);
                            int clearCount= clearSummonedEntities(Objects.requireNonNull(ctx.getSource().getLevel()));
                            ctx.getSource().sendSuccess(
                                    () -> Component.literal("Clear missions finished! Discard "+clearCount+" entities"),
                                    true);
                            return 1;
                        })
        );
    }
}