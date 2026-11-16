package yagen.waitmydawn.yagens_attributes.data;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.registries.*;
import yagen.waitmydawn.yagens_attributes.setup.*;
import yagen.waitmydawn.yagens_attributes.command.ModCommands;
import java.util.UUID;

final class RivenRuntimeChecks {
    private static int checks;
    private RivenRuntimeChecks() {}
    static int run(ServerLevel level, JsonObject report) {
        var player = FakePlayerFactory.get(level, new GameProfile(UUID.fromString("6cbaec08-1dbd-4da9-8dce-c5133b323bf4"), "[RivenChecks]"));
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL); player.getInventory().clearContent(); player.getInventory().selected = 0;
        for (int i = 1; i < 36; i++) player.getInventory().setItem(i, new ItemStack(Items.STONE, 64));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.UNKNOWN_MELEE_RIVEN.get(), 2));
        rejects(() -> RivenService.reveal(player, InteractionHand.MAIN_HAND, RivenRoll.Pool.MELEE));
        check(player.getMainHandItem().getCount() == 2);
        player.getInventory().setItem(1, ItemStack.EMPTY);
        check(player.gameMode.useItem(player, level, player.getMainHandItem(), InteractionHand.MAIN_HAND).consumesAction());
        check(player.getMainHandItem().getCount() == 1);
        var physical = player.getInventory().getItem(1); var roll = physical.get(Components.RIVEN_ROLL.get());
        check(physical.is(ModItems.MOD.get()) && roll != null && RivenRoll.Pool.MELEE.accepts(roll.category()));
        check(physical.get(Components.MOD_DATA.get()).level() == 0);
        var roundTrip = ItemStack.parse(level.registryAccess(), physical.save(level.registryAccess())).orElseThrow();
        check(ItemStack.isSameItemSameComponents(physical, roundTrip));
        var state = WorldState.get(level.getServer());
        for (var old : state.ledger.owned(player.getUUID())) if (old.weapon() == null) state.ledger.withdraw(player.getUUID(), old.id());
        player.getInventory().selected = 1;
        check(player.gameMode.useItem(player, level, physical, InteractionHand.MAIN_HAND).consumesAction());
        check(player.getMainHandItem().isEmpty()); check(state.ledger.owned(player.getUUID()).size() == 1);
        var entry = state.ledger.owned(player.getUUID()).get(0); check(entry.info().riven().equals(roll));
        ModTransactions.withdraw(player, state, entry.id()); check(state.ledger.owned(player.getUUID()).isEmpty());
        var returned = player.getMainHandItem(); check(roll.equals(returned.get(Components.RIVEN_ROLL.get())));
        ModTransactions.deposit(player, returned); entry = state.ledger.owned(player.getUUID()).get(0);
        var target = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(roll.weapon())));
        var weapon = Components.identify(target); state.progress(weapon, new WeaponProgress(30, 0));
        var wrong = new ItemStack(roll.weapon().equals("minecraft:diamond_sword") ? Items.DIAMOND_AXE : Items.DIAMOND_SWORD);
        player.setItemInHand(InteractionHand.MAIN_HAND, wrong); check(command(player, "ya install " + entry.id()) == 0);
        check(state.ledger.entry(entry.id()).weapon() == null);
        player.setItemInHand(InteractionHand.MAIN_HAND, target); check(command(player, "ya install " + entry.id()) == 1);
        check(weapon.equals(state.ledger.entry(entry.id()).weapon()));
        check(ModCommands.bonuses(player).stream().anyMatch(map -> !map.isEmpty()));
        wrong.set(Components.WEAPON_ID.get(), weapon); player.setItemInHand(InteractionHand.MAIN_HAND, wrong);
        check(ModCommands.bonuses(player).stream().allMatch(java.util.Map::isEmpty));
        player.setItemInHand(InteractionHand.MAIN_HAND, target); state.endo(player.getUUID(), 1000);
        long cost = entry.info().details().upgradeCost(); ModTransactions.upgrade(player, state, entry.id(), weapon);
        var upgraded = state.ledger.entry(entry.id());
        check(upgraded.info().level() == 1 && upgraded.info().riven().equals(roll)); check(state.endo(player.getUUID()) == 1000 - cost);
        player.gameMode.changeGameModeForPlayer(GameType.CREATIVE); state.endo(player.getUUID(), 0);
        ModTransactions.upgrade(player, state, entry.id(), weapon); check(state.ledger.entry(entry.id()).info().level() == 2 && state.endo(player.getUUID()) == 0);
        var restored = WorldState.load(state.save(new net.minecraft.nbt.CompoundTag(), level.registryAccess()), level.registryAccess());
        check(restored.ledger.entry(entry.id()).equals(state.ledger.entry(entry.id())));
        state.ledger.uninstall(player.getUUID(), entry.id(), weapon, false);
        UUID upgradedId = entry.id();
        rejects(() -> ModTransactions.withdraw(player, state, upgradedId));
        check(state.ledger.entry(upgradedId).info().level() == 2);
        player.getInventory().setItem(2, ItemStack.EMPTY);
        ModTransactions.withdraw(player, state, upgradedId);
        var result = player.getInventory().items.stream().filter(stack -> stack.get(Components.RIVEN_ROLL.get()) != null).findFirst().orElseThrow();
        check(result.get(Components.MOD_DATA.get()).level() == 2 && result.get(Components.RIVEN_ROLL.get()).equals(roll));
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL); player.getInventory().selected = 0;
        for (int i = 1; i < 36; i++) player.getInventory().setItem(i, new ItemStack(Items.STONE, 64));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.UNKNOWN_RANGED_RIVEN.get()));
        RivenService.reveal(player, InteractionHand.MAIN_HAND, RivenRoll.Pool.RANGED);
        var ranged = player.getMainHandItem().get(Components.RIVEN_ROLL.get());
        check(ranged != null && RivenRoll.Pool.RANGED.accepts(ranged.category()));
        check(ranged.weapon().equals("minecraft:bow") || ranged.weapon().equals("minecraft:crossbow"));
        report.addProperty("riven_checks", checks); return checks;
    }
    private static int command(ServerPlayer player, String command) {
        try { return player.server.getCommands().getDispatcher().execute(command, player.createCommandSourceStack()); }
        catch (com.mojang.brigadier.exceptions.CommandSyntaxException exception) { throw new AssertionError(exception); }
    }
    private static void check(boolean value) { checks++; if (!value) throw new AssertionError("Riven runtime check " + checks); }
    private static void rejects(Runnable action) { checks++; try { action.run(); } catch (IllegalArgumentException expected) { return; } throw new AssertionError("Expected reveal rejection"); }
}
