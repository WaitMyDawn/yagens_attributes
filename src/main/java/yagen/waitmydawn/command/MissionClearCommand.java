package yagen.waitmydawn.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import yagen.waitmydawn.api.mission.MissionData;

public class MissionClearCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("MissionClear")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> {
                            MissionData data = MissionData.get(ctx.getSource().getServer());
                            data.clearAll();
                            ctx.getSource().sendSuccess(
                                    () -> Component.literal("Clear missions finished!"),
                                    true);
                            return 1;
                        })
        );
    }
}