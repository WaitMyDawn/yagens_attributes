package yagen.waitmydawn.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import yagen.waitmydawn.api.mission.MissionData;

import java.util.Objects;

import static yagen.waitmydawn.api.mission.MissionData.clearSummonedEntities;

public class MissionClearCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("MissionClear")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> {
                            MissionData data = MissionData.get(ctx.getSource().getServer());
                            data.clearAll();
                            int clearCount= clearSummonedEntities(Objects.requireNonNull(ctx.getSource().getLevel()));
                            ctx.getSource().sendSuccess(
                                    () -> Component.literal("Clear missions finished! Discard "+clearCount+" entities"),
                                    true);
                            return 1;
                        })
        );
    }
}