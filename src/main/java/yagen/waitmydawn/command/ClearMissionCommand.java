package yagen.waitmydawn.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import yagen.waitmydawn.api.mission.MissionData;

public class ClearMissionCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("clearMissions")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> {
                            MissionData data = MissionData.get(ctx.getSource().getServer());
                            data.clearAll();
                            ctx.getSource().sendSuccess(
                                    () -> Component.literal("clear missions finished!"),
                                    true);
                            return 1;
                        })
        );
    }
}