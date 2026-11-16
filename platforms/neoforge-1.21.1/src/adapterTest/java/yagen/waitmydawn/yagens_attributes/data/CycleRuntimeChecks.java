package yagen.waitmydawn.yagens_attributes.data;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.setup.*;
import yagen.waitmydawn.yagens_attributes.registries.*;
import java.util.*;

final class CycleRuntimeChecks {
    private static int checks;
    static int run(ServerLevel level, JsonObject report) {
        var player = FakePlayerFactory.get(level, new GameProfile(UUID.fromString("819a640d-8318-4f98-a0de-5f115bfec6e6"), "[CycleChecks]"));
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL); player.setPos(8, 100, 8); player.getInventory().clearContent(); player.getInventory().selected = 0;
        var state = WorldState.get(player.server); UUID owner = player.getUUID();
        for (var old : state.ledger.owned(owner)) { state.cycle(old.id(), null); if (old.weapon() != null) state.ledger.uninstall(owner, old.id(), old.weapon(), false); state.ledger.withdraw(owner, old.id()); }
        state.kuva(owner, 0); player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.KUVA.get(), 7));
        check(player.gameMode.useItem(player, level, player.getMainHandItem(), InteractionHand.MAIN_HAND).consumesAction());
        check(state.kuva(owner) == 7 && player.getMainHandItem().isEmpty());
        state.endo(owner, 99); EndoTransactions.withdraw(player, state, 3, true);
        check(state.kuva(owner) == 4 && state.endo(owner) == 99 && player.getInventory().getItem(0).getCount() == 3);
        for (int i = 0; i < 36; i++) player.getInventory().setItem(i, new ItemStack(Items.STONE, 64));
        rejects(() -> EndoTransactions.withdraw(player, state, 1, true)); check(state.kuva(owner) == 4);
        player.getInventory().clearContent();
        var catalog = ModDefinitions.INSTANCE.catalog(); var random = new Random(52);
        var roll = catalog.rivenRules().roll(RivenRoll.Pool.MELEE, RivenService.weapons(), random::nextInt, random::nextDouble);
        var card = catalog.rivenCard(roll, 2); UUID id = UUID.randomUUID();
        state.ledger.add(new ModLedger.Entry(id, owner, null, false, card.bonuses(), card.info(), -1)); state.kuva(owner, 0);
        rejects(() -> RivenService.cycle(player, state, id)); check(state.ledger.entry(id).info().riven().cycles() == 0 && state.cycle(id) == null);
        state.kuva(owner, 1000); long balance = 1000;
        for (int i = 1; i <= 18; i++) {
            var before = state.ledger.entry(id).info().riven(); RivenService.cycle(player, state, id); balance -= Math.min(16, i);
            var pending = state.cycle(id);
            check(pending != null && pending.cycles() == i && pending.polarity() == before.polarity() && pending.weapon().equals(before.weapon()));
            check(state.kuva(owner) == balance && state.ledger.entry(id).info().riven().lines().equals(before.lines()));
            rejects(() -> RivenService.cycle(player, state, id));
            check(state.kuva(owner) == balance);
            if (i == 1) {
                var loaded = WorldState.load(state.save(new net.minecraft.nbt.CompoundTag(), level.registryAccess()), level.registryAccess());
                check(loaded.cycle(id).equals(pending) && loaded.kuva(owner) == balance);
                rejects(() -> ModTransactions.withdraw(player, state, id)); rejects(() -> ModTransactions.upgrade(player, state, id, null));
                rejects(() -> ModTransactions.drop(player, state, id, true));
                RivenService.choose(player, loaded, id, false); check(loaded.cycle(id) == null && loaded.kuva(owner) == balance);
                var currentView = yagen.waitmydawn.yagens_attributes.network.PanelPayload.ModView.of(state.ledger.entry(id));
                var candidateCard = catalog.rivenCard(pending, 2);
                var candidateView = yagen.waitmydawn.yagens_attributes.network.PanelPayload.ModView.of(new ModLedger.Entry(id, owner, null, false, candidateCard.bonuses(), candidateCard.info(), -1));
                var payload = new yagen.waitmydawn.yagens_attributes.network.PanelPayload(99, 0, 0, 1, 1, net.minecraft.network.chat.Component.empty(), null, null, Map.of(), List.of(currentView), List.of(), 0, 1,
                        ItemStack.EMPTY, WeaponProgress.ZERO, 0, false, 99, 20, "", balance, 40, true, false, true, List.of(currentView, candidateView));
                var buffer = new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), level.registryAccess());
                try {
                    yagen.waitmydawn.yagens_attributes.network.PanelPayload.CODEC.encode(buffer, payload);
                    var decoded = yagen.waitmydawn.yagens_attributes.network.PanelPayload.CODEC.decode(buffer);
                    check(decoded.kuva() == balance && decoded.pendingCycle() && decoded.hasRiven() && decoded.cycleViews().equals(payload.cycleViews()));
                } finally { buffer.release(); }
            }
            boolean replace = i % 2 == 0; RivenService.choose(player, state, id, replace);
            var chosen = state.ledger.entry(id).info();
            check(chosen.level() == 2 && chosen.riven().cycles() == i && chosen.riven().lines().equals(replace ? pending.lines() : before.lines()));
            check(state.cycle(id) == null && state.kuva(owner) == balance);
        }
        ModTransactions.withdraw(player, state, id); var physical = player.getInventory().getItem(0);
        check(physical.get(Components.RIVEN_ROLL.get()).cycles() == 18); ModTransactions.deposit(player, physical);
        check(state.ledger.owned(owner).get(0).info().riven().cycles() == 18);
        var standard = catalog.card("yagens_attributes:universal_nanocube", 0);
        var ids = new ArrayList<UUID>();
        for (int i = 0; i < 70; i++) { UUID mod = UUID.randomUUID(); ids.add(mod); state.ledger.add(new ModLedger.Entry(mod, owner, null, false, standard.bonuses(), standard.info(), -1)); }
        var area = new AABB(player.blockPosition()).inflate(24);
        var existing = new HashSet<UUID>(); level.getEntitiesOfClass(ItemEntity.class, area).forEach(entity -> existing.add(entity.getUUID()));
        try {
            ModTransactions.drop(player, state, ids.get(0), false);
            check(state.ledger.owned(owner).stream().filter(entry -> entry.info().equals(standard.info())).count() == 69);
            ModTransactions.drop(player, state, ids.get(1), true);
            check(state.ledger.owned(owner).stream().filter(entry -> entry.info().equals(standard.info())).count() == 5);
            int dropped = level.getEntitiesOfClass(ItemEntity.class, area).stream().filter(entity -> !existing.contains(entity.getUUID())).mapToInt(entity -> entity.getItem().getCount()).sum();
            check(dropped == 65);
        } finally { level.getEntitiesOfClass(ItemEntity.class, area).stream().filter(entity -> !existing.contains(entity.getUUID())).forEach(ItemEntity::discard); }
        var foreign = FakePlayerFactory.get(level, new GameProfile(UUID.fromString("2bb423f0-9312-4f27-a58a-4b6335f59e7a"), "[CycleForeign]"));
        UUID rivenId = state.ledger.owned(owner).stream().filter(entry -> entry.info().riven() != null).findFirst().orElseThrow().id();
        rejects(() -> RivenService.cycle(foreign, state, rivenId));
        report.addProperty("cycle_kuva_drop_checks", checks); return checks;
    }
    private static void check(boolean value) { checks++; if (!value) throw new AssertionError("Cycle/Kuva/drop runtime check " + checks); }
    private static void rejects(Runnable action) { checks++; try { action.run(); } catch (IllegalArgumentException expected) { return; } throw new AssertionError("Expected transaction rejection"); }
}
