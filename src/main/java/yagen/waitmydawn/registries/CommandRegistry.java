package yagen.waitmydawn.registries;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import yagen.waitmydawn.command.ClearMissionCommand;

@EventBusSubscriber()
public class CommandRegistry {
    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        var commandDispatcher = event.getDispatcher();
        var commandBuildContext = event.getBuildContext();

        ClearMissionCommand.register(commandDispatcher);
    }
}
