package yagen.waitmydawn.yagens_attributes.data;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import yagen.waitmydawn.yagens_attributes.client.ClientUiCommands;
import yagen.waitmydawn.yagens_attributes.client.HudElement;
import yagen.waitmydawn.yagens_attributes.command.ModCommands;
import java.util.ArrayList;
import java.util.List;

public final class ClientUiContracts {
    private static int checks;
    private ClientUiContracts() {}
    public static void run() throws CommandSyntaxException {
        var opened = new ArrayList<HudElement>();
        var dispatcher = new CommandDispatcher<CommandSourceStack>();
        var statusOpened = new java.util.concurrent.atomic.AtomicInteger();
        var countsOpened = new java.util.concurrent.atomic.AtomicInteger();
        ClientUiCommands.register(dispatcher, opened::add, statusOpened::incrementAndGet, countsOpened::incrementAndGet);
        var root = dispatcher.getRoot().getChild("ya"); var client = root.getChild("client");
        check(root.canUse(null) && client.canUse(null) && client.getChild("combo").canUse(null));
        check(dispatcher.execute("ya client combo", null) == 1 && opened.equals(List.of(HudElement.COMBO)));
        check(dispatcher.execute("ya client", null) == 1 && opened.size() == 2);
        check(dispatcher.getCompletionSuggestions(dispatcher.parse("ya client c", null)).join().getList().stream().anyMatch(value -> value.getText().equals("combo")));
        check(dispatcher.getRoot().getChild("ya").getChild("stats") == null);
        try { dispatcher.execute("ya client unknown", null); throw new AssertionError("Unknown HUD accepted"); }
        catch (CommandSyntaxException expected) { checks++; }
        check(opened.size() == 2);
        check(client.getChild("buffs").canUse(null));
        check(dispatcher.execute("ya client buffs", null) == 1 && opened.get(2) == HudElement.BUFFS);
        check(client.getChild("status").canUse(null));
        check(dispatcher.execute("ya client status", null) == 1 && statusOpened.get() == 1 && opened.size() == 3);
        check(client.getChild("status_count").canUse(null));
        check(dispatcher.execute("ya client status_count", null) == 1 && countsOpened.get() == 1 && statusOpened.get() == 1);
        for (boolean clientFirst : new boolean[] {false, true}) {
            var merged = new CommandDispatcher<CommandSourceStack>();
            if (clientFirst) ClientUiCommands.register(merged, opened::add, statusOpened::incrementAndGet, countsOpened::incrementAndGet);
            ModCommands.register(merged);
            if (!clientFirst) ClientUiCommands.register(merged, opened::add, statusOpened::incrementAndGet, countsOpened::incrementAndGet);
            for (String command : List.of("mods", "stats", "polarize", "weaponxp", "status"))
                check(merged.getRoot().getChild("ya").getChild(command) != null);
            check(merged.execute("ya client combo", null) == 1);
            check(merged.execute("ya client status", null) == 1);
            check(merged.execute("ya client status_count", null) == 1);
        }
        System.out.println("Passed " + checks + " local UI command/permission/merge checks");
    }
    private static void check(boolean condition) { checks++; if (!condition) throw new AssertionError("Client command mismatch"); }
}
