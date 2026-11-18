package yagen.waitmydawn.yagens_attributes.client;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import java.util.function.Consumer;
import static net.minecraft.commands.Commands.literal;

public final class ClientUiCommands {
    private ClientUiCommands() {}
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, Consumer<HudElement> open,
                                Runnable openStatus, Runnable openStatusCount) {
        var client = literal("client").executes(context -> { open.accept(HudElement.COMBO); return 1; });
        for (var element : HudElement.values())
            client.then(literal(element.id()).executes(context -> { open.accept(element); return 1; }));
        client.then(literal("status").executes(context -> { openStatus.run(); return 1; }));
        client.then(literal("status_count").executes(context -> { openStatusCount.run(); return 1; }));
        dispatcher.register(literal("ya").then(client));
    }
}
