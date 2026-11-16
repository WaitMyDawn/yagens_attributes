package yagen.waitmydawn.yagens_attributes.data;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.*;
import net.minecraft.server.network.*;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.network.*;
import yagen.waitmydawn.yagens_attributes.setup.PanelService;
import java.util.*;

final class ModFilterRuntimeChecks {
    private static int checks;
    private static final class Capture extends ServerGamePacketListenerImpl {
        PanelPayload panel;
        Capture(ServerPlayer player) { super(player.server, new Connection(PacketFlow.SERVERBOUND), player, CommonListenerCookie.createInitial(player.getGameProfile(), false)); }
        @Override public void send(Packet<?> packet) {
            if (packet instanceof ClientboundCustomPayloadPacket custom && custom.payload() instanceof PanelPayload payload) panel = payload;
        }
    }
    static int run(ServerLevel level, JsonObject report) {
        var player = FakePlayerFactory.get(level, new GameProfile(UUID.fromString("5e7c853c-d706-4e75-aad1-eabf79ba502a"), "[FilterChecks]"));
        player.getInventory().clearContent(); var state = WorldState.get(player.server);
        for (var entry : state.ledger.owned(player.getUUID())) { state.cycle(entry.id(), null); if (entry.weapon() != null) state.ledger.release(player.getUUID(), entry.id()); state.ledger.withdraw(player.getUUID(), entry.id()); }
        int i = 0;
        for (var card : ModDefinitions.INSTANCE.catalog().creative()) {
            boolean occupied = i++ % 4 == 0;
            state.ledger.add(new ModLedger.Entry(UUID.randomUUID(), player.getUUID(), occupied ? UUID.randomUUID() : null, false, card.bonuses(), card.info(), occupied ? 0 : -1));
        }
        var first = state.ledger.owned(player.getUUID()).stream().filter(ModFilterRuntimeChecks::wanted).findFirst().orElseThrow();
        state.ledger.add(new ModLedger.Entry(UUID.randomUUID(), player.getUUID(), null, false, first.bonuses(), first.info(), -1));
        var filter = new ModFilter(List.of(List.of(ModFilter.Tag.RARE, ModFilter.Tag.LEGENDARY), List.of(ModFilter.Tag.AVAILABLE), List.of(ModFilter.Tag.LEVELED)));
        var previous = player.connection;
        try {
            var capture = new Capture(player);
            long copies = state.ledger.owned(player.getUUID()).stream().filter(ModFilterRuntimeChecks::wanted).count();
            var one = query(player, capture, filter, 0, "");
            check(one.total() == copies - 1 && one.copies() == copies && one.mods().size() == PanelPayload.PAGE_SIZE);
            check(one.pages() > 1 && one.page() == 0);
            for (var mod : one.mods()) check(mod.weapon() == null && mod.info().level() > 0 && (mod.info().rarity() == ModInfo.Rarity.RARE || mod.info().rarity() == ModInfo.Rarity.LEGENDARY));
            var two = query(player, capture, filter, 1, "");
            check(two.page() == 1 && two.total() == one.total() && two.copies() == one.copies());
            var ids = one.mods().stream().map(PanelPayload.ModView::id).toList();
            check(two.mods().stream().noneMatch(mod -> ids.contains(mod.id())));
            var last = query(player, capture, filter, 999, ""); check(last.page() == one.pages() - 1);
            var impossible = query(player, capture, new ModFilter(List.of(List.of(ModFilter.Tag.RARE), List.of(ModFilter.Tag.LEGENDARY))), 0, "");
            check(impossible.total() == 0 && impossible.copies() == 0 && impossible.mods().isEmpty() && impossible.pages() == 1);
            var absent = query(player, capture, filter, 0, "missing fixture text 17"); check(absent.mods().isEmpty());
            var bound = state.ledger.owned(player.getUUID()).stream().filter(entry -> entry.weapon() != null && entry.info().level() > 0 && (entry.info().rarity() == ModInfo.Rarity.RARE || entry.info().rarity() == ModInfo.Rarity.LEGENDARY)).findFirst().orElseThrow();
            state.ledger.release(player.getUUID(), bound.id());
            var released = query(player, capture, filter, 0, ""); check(released.total() == one.total() + 1 && released.copies() == one.copies() + 1);
            var all = query(player, capture, ModFilter.NONE, 0, ""); check(all.total() > one.total() && all.copies() == ModDefinitions.INSTANCE.catalog().creative().size() + 1);
            check(player.getMainHandItem().isEmpty());
        } finally { player.connection = previous; }
        report.addProperty("mod_filter_checks", checks); return checks;
    }
    private static boolean wanted(ModLedger.Entry entry) {
        return entry.weapon() == null && entry.info().level() > 0 && (entry.info().rarity() == ModInfo.Rarity.RARE || entry.info().rarity() == ModInfo.Rarity.LEGENDARY);
    }
    private static PanelPayload query(ServerPlayer player, Capture capture, ModFilter filter, int page, String search) {
        capture.panel = null;
        PanelService.handle(player, new PanelRequest(3000 + checks, PanelRequest.STORAGE, page, PanelRequest.REFRESH, null, null, -1, new ModInventory.Query(search, ModInfo.Category.ALL, ModInventory.Order.QUALITY, filter), "zh_cn"));
        check(capture.panel != null && capture.panel.error().isEmpty()); return capture.panel;
    }
    private static void check(boolean value) { checks++; if (!value) throw new AssertionError("Mod filter runtime " + checks); }
}
