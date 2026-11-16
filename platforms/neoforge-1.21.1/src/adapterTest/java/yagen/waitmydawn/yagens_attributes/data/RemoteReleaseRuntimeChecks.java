package yagen.waitmydawn.yagens_attributes.data;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.network.PanelRequest;
import yagen.waitmydawn.yagens_attributes.registries.Components;
import yagen.waitmydawn.yagens_attributes.setup.*;
import java.util.*;

final class RemoteReleaseRuntimeChecks {
    private static int checks;
    static int run(ServerLevel level, JsonObject report) {
        var owner = FakePlayerFactory.get(level, new GameProfile(UUID.fromString("1b1f56b6-3e45-49c2-8c41-655f99aac803"), "[ReleaseOwner]"));
        var holder = FakePlayerFactory.get(level, new GameProfile(UUID.fromString("0299b25c-8dde-46e2-a15b-28a8ff79dc98"), "[ReleaseHolder]"));
        owner.gameMode.changeGameModeForPlayer(GameType.SURVIVAL); holder.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        owner.getInventory().clearContent(); holder.getInventory().clearContent();
        var state = WorldState.get(level.getServer());
        for (var actor : List.of(owner, holder)) for (var entry : state.ledger.owned(actor.getUUID())) {
            state.cycle(entry.id(), null);
            if (entry.weapon() != null) state.ledger.release(actor.getUUID(), entry.id());
            state.ledger.withdraw(actor.getUUID(), entry.id());
        }
        var card = ModDefinitions.INSTANCE.catalog().card("yagens_attributes:galvanized_chamber", 10);
        UUID id = UUID.randomUUID(), free = UUID.randomUUID();
        for (UUID mod : List.of(id, free)) state.ledger.add(new ModLedger.Entry(mod, owner.getUUID(), null, false, card.bonuses(), card.info(), -1));
        var bow = new ItemStack(Items.BOW); UUID weapon = Components.identify(bow);
        holder.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, bow);
        state.ledger.install(owner.getUUID(), id, weapon, ModInfo.Category.BOW, 8, 2, 30, false);
        var before = state.ledger.entry(id); var shot = RangedShooting.snapshot(holder, bow); close(1.8, shot.multishot());
        check(state.ledger.bonuses(weapon).size() == 1); check(state.ledger.bonuses(weapon, "minecraft:bow").size() == 1);
        state.setDirty(false);
        PanelService.handle(holder, new PanelRequest(2010, PanelRequest.STORAGE, 0, PanelRequest.RELEASE, null, id));
        check(before.equals(state.ledger.entry(id)) && !state.isDirty());
        PanelService.handle(owner, new PanelRequest(2011, PanelRequest.STORAGE, 0, PanelRequest.RELEASE, null, id));
        var after = state.ledger.entry(id);
        check(after.owner().equals(owner.getUUID()) && after.weapon() == null && after.slot() == -1);
        check(after.info().equals(before.info()) && after.bonuses().equals(before.bonuses()) && after.shared() == before.shared());
        check(state.isDirty() && state.ledger.installed(weapon).isEmpty());
        check(state.ledger.bonuses(weapon).isEmpty() && state.ledger.bonuses(weapon, "minecraft:bow").isEmpty());
        close(1, RangedShooting.snapshot(holder, bow).multishot()); close(1.8, shot.multishot());
        var groups = ModInventory.browse(state.ledger.owned(owner.getUUID()), ModInventory.Query.DEFAULT, null, e -> e.info().name());
        check(groups.size() == 1 && groups.get(0).quantity() == 2 && groups.get(0).available() == 2);
        var restored = WorldState.load(state.save(new CompoundTag(), level.registryAccess()), level.registryAccess());
        check(restored.ledger.entry(id).equals(after) && restored.ledger.installed(weapon).isEmpty());
        state.ledger.install(owner.getUUID(), id, weapon, ModInfo.Category.BOW, 8, 2, 30, false);
        state.ledger.share(owner.getUUID(), id, true);
        PanelService.handle(holder, new PanelRequest(2012, PanelRequest.STORAGE, 0, PanelRequest.RELEASE, null, id));
        check(state.ledger.entry(id).weapon().equals(weapon));
        state.ledger.uninstall(holder.getUUID(), id, weapon, true);
        state.ledger.install(holder.getUUID(), id, weapon, ModInfo.Category.BOW, 8, 2, 30, false);
        PanelService.handle(owner, new PanelRequest(2013, PanelRequest.STORAGE, 0, PanelRequest.RELEASE, null, id));
        check(state.ledger.entry(id).owner().equals(holder.getUUID()) && state.ledger.entry(id).weapon().equals(weapon));
        try {
            var source = holder.createCommandSourceStack().withPermission(0);
            int result = level.getServer().getCommands().getDispatcher().execute("ya release " + id, source);
            check(result == 1 && state.ledger.entry(id).weapon() == null);
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException exception) { throw new AssertionError(exception); }
        var current = state.ledger.entry(id); state.setDirty(false);
        PanelService.handle(holder, new PanelRequest(2014, PanelRequest.STORAGE, 0, PanelRequest.RELEASE, null, id));
        check(current.equals(state.ledger.entry(id)) && !state.isDirty());
        check(!new PanelRequest(2015, PanelRequest.STORAGE, 0, PanelRequest.RELEASE, weapon, id).valid());
        check(owner.getMainHandItem().isEmpty() && holder.getMainHandItem() == bow);
        report.addProperty("remote_release_checks", checks); return checks;
    }
    private static void check(boolean value) { checks++; if (!value) throw new AssertionError("Remote release runtime " + checks); }
    private static void close(double expected, double actual) { check(Math.abs(expected - actual) < 1e-6); }
}
