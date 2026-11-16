package yagen.waitmydawn.yagens_attributes.setup;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.data.*;
import yagen.waitmydawn.yagens_attributes.registries.*;

import java.util.*;

public final class ModTransactions {
    private ModTransactions() {
    }

    public static ItemStack stack(String type, int level) {
        var stack = new ItemStack(ModItems.MOD.get());
        stack.set(Components.MOD_DATA.get(), new ModItemData(ResourceLocation.parse(type), level));
        return stack;
    }

    public static void deposit(ServerPlayer player, ItemStack stack) {
        var data = stack.get(Components.MOD_DATA.get());
        if (!stack.is(ModItems.MOD.get()) || data == null) throw new IllegalArgumentException("Mod has no definition");
        var card = card(stack, ModDefinitions.INSTANCE.catalog());
        var state = WorldState.get(player.server);
        int amount = stack.getCount();
        if (amount < 1 || amount > stack.getMaxStackSize())
            throw new IllegalArgumentException("Invalid Mod stack size");
        var entries = new ArrayList<ModLedger.Entry>();
        for (int i = 0; i < amount; i++)
            entries.add(new ModLedger.Entry(UUID.randomUUID(), player.getUUID(), null, false, card.bonuses(), card.info(), -1));
        for (var entry : entries) state.ledger.add(entry);
        stack.shrink(amount);
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        player.displayClientMessage(net.minecraft.network.chat.Component.translatable("gui.yagens_attributes.mods_deposited", amount), true);
    }

    public static void withdraw(ServerPlayer player, WorldState state, UUID mod) {
        if (state.cycle(mod) != null) throw new IllegalArgumentException("gui.yagens_attributes.cycle_choose_first");
        var entry = state.ledger.entry(mod);
        if (!entry.info().defined()) throw new IllegalArgumentException("Custom Mods have no physical item template");
        if (!entry.owner().equals(player.getUUID()) || entry.weapon() != null)
            throw new IllegalArgumentException("Only owned, uninstalled Mods can be withdrawn");
        var item = entry.info().riven() == null ? stack(entry.info().details().type(), entry.info().level()) : RivenService.stack(entry.info().riven(), entry.info().level());
        var inventory = player.getInventory();
        int target = -1;
        for (int i = 0; i < inventory.items.size(); i++) {
            var current = inventory.items.get(i);
            if (current.isEmpty() || ItemStack.isSameItemSameComponents(current, item) && current.getCount() < Math.min(current.getMaxStackSize(), inventory.getMaxStackSize())) {
                target = i;
                break;
            }
        }
        if (target < 0) throw new IllegalArgumentException("Inventory is full");
        state.ledger.withdraw(player.getUUID(), mod);
        var current = inventory.items.get(target);
        if (current.isEmpty()) inventory.items.set(target, item);
        else current.grow(1);
        inventory.setChanged();
        player.containerMenu.broadcastChanges();
    }
    public static long recycle(ServerPlayer player, WorldState state, UUID mod, long amount, boolean all) {
        var first = state.ledger.entry(mod);
        if (!first.owner().equals(player.getUUID()) || first.weapon() != null || state.cycle(mod) != null)
            throw new IllegalArgumentException("gui.yagens_attributes.recycle_denied");
        var catalog = ModDefinitions.INSTANCE.catalog();
        var quality = catalog.qualities().get(first.info().rarity());
        if (quality == null) throw new IllegalArgumentException("gui.yagens_attributes.recycle_denied");
        long each = ModRecycling.value(quality.baseEndo(), first.info().level());
        var eligible = state.ledger.owned(player.getUUID()).stream().filter(entry -> entry.weapon() == null && state.cycle(entry.id()) == null
                && ModInventory.sameVariant(entry, first)).sorted(Comparator.comparing(entry -> entry.id().toString())).toList();
        long count = all ? eligible.size() : amount;
        if (count < 1 || count > eligible.size()) throw new IllegalArgumentException("gui.yagens_attributes.recycle_amount_invalid");
        final long total, balance;
        try { total = Math.multiplyExact(each, count); balance = Math.addExact(state.endo(player.getUUID()), total); }
        catch (ArithmeticException exception) { throw new IllegalArgumentException("gui.yagens_attributes.recycle_overflow", exception); }
        // Validation and overflow checks precede all mutations; both entries and currency share one SavedData file.
        for (int i = 0; i < count; i++) state.ledger.withdraw(player.getUUID(), eligible.get(i).id());
        state.endo(player.getUUID(), balance);
        return total;
    }

    public static void upgrade(ServerPlayer player, WorldState state, UUID mod, UUID heldWeapon) {
        if (state.cycle(mod) != null) throw new IllegalArgumentException("gui.yagens_attributes.cycle_choose_first");
        var entry = state.ledger.entry(mod);
        if (!entry.info().defined())
            throw new IllegalArgumentException("Custom Mods do not have an upgrade definition");
        var current = ModDefinitions.INSTANCE.catalog().card(entry.info(), entry.info().level());
        var next = ModDefinitions.INSTANCE.catalog().card(current.info(), current.level() + 1);
        long cost = player.isCreative() ? 0 : current.info().details().upgradeCost(), balance = state.endo(player.getUUID());
        if (balance < cost) throw new IllegalArgumentException("Insufficient Endo");
        if (entry.weapon() != null && !entry.weapon().equals(heldWeapon))
            throw new IllegalArgumentException("Hold the installed weapon to upgrade this Mod");
        int capacity = entry.weapon() == null ? 0 : Components.capacity(player.getMainHandItem(), state.progress(entry.weapon()));
        var profile = CombatData.WEAPONS.weapon(player.getMainHandItem());
        if (entry.weapon() != null && !WeaponEligibility.operable(player.getMainHandItem(), profile))
            throw new IllegalArgumentException("Held item has no operable weapon profile");
        var slots = profile == null ? List.<ModInfo.Polarity>of() : WeaponPolarityData.profile(player.getMainHandItem(), profile, state.ledger).slots();
        state.ledger.upgrade(player.getUUID(), mod, next.info(), next.bonuses(), capacity, player.isCreative(), heldWeapon, slots);
        if (cost > 0) state.endo(player.getUUID(), balance - cost);
    }

    public static void polarize(ServerPlayer player, WorldState state, UUID expectedWeapon, int slot, ModInfo.Polarity polarity) {
        var held = player.getMainHandItem();
        var profile = CombatData.WEAPONS.weapon(held);
        UUID actual = held.get(Components.WEAPON_ID.get());
        if (!WeaponEligibility.operable(held, profile))
            throw new IllegalArgumentException("gui.yagens_attributes.polarize_invalid");
        PanelService.validateWeapon(expectedWeapon, actual);
        int materialSlot = -1;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++)
            if (player.getInventory().getItem(i).is(ModItems.FORMA.get())) {
                materialSlot = i;
                break;
            }
        if (!player.isCreative() && materialSlot < 0)
            throw new IllegalArgumentException("gui.yagens_attributes.polarize_no_forma");
        var effective = WeaponPolarityData.profile(held, profile, state.ledger);
        PolarityRules.validateForma(state.progress(actual), effective.slots(), slot, polarity, player.isCreative());
        state.ledger.validatePolarization(player.getUUID(), actual);
        var next = held.getOrDefault(Components.WEAPON_POLARITIES.get(), WeaponPolarityData.EMPTY).with(slot, polarity);
        if (!player.isCreative()) state.ledger.clearForPolarization(player.getUUID(), actual);
        held.set(Components.WEAPON_POLARITIES.get(), next);
        if (!player.isCreative()) state.progress(actual, WeaponProgress.ZERO);
        if (!player.isCreative()) player.getInventory().getItem(materialSlot).shrink(1);
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, new yagen.waitmydawn.yagens_attributes.network.ProgressPayload(actual, state.progress(actual)));
    }
    public static ModCatalog.Card card(ItemStack stack, ModCatalog catalog) {
        var data = stack.get(Components.MOD_DATA.get());
        if (data == null) throw new IllegalArgumentException("Missing Mod data");
        var roll = stack.get(Components.RIVEN_ROLL.get());
        if (roll == null) return catalog.card(data.type().toString(), data.level());
        if (!data.type().toString().equals(RivenRules.TYPE)) throw new IllegalArgumentException("Riven payload requires the Riven card type");
        return catalog.rivenCard(RivenService.current(roll, CombatData.WEAPONS.profiles()), data.level());
    }
    public static void drop(ServerPlayer player, WorldState state, UUID mod, boolean stack) {
        var first = state.ledger.entry(mod);
        if (!first.owner().equals(player.getUUID()) || first.weapon() != null || !first.info().defined() || state.cycle(mod) != null)
            throw new IllegalArgumentException("gui.yagens_attributes.gesture_denied");
        var items = state.ledger.owned(player.getUUID()).stream().filter(entry -> entry.weapon() == null && ModInventory.sameVariant(entry, first) && state.cycle(entry.id()) == null).sorted(Comparator.comparing(entry -> !entry.id().equals(mod))).limit(stack ? 64 : 1).toList();
        var physical = first.info().riven() == null ? stack(first.info().details().type(), first.info().level()) : RivenService.stack(first.info().riven(), first.info().level());
        physical.setCount(items.size());
        for (var entry : items) state.ledger.withdraw(player.getUUID(), entry.id());
        try {
            var dropped = player.drop(physical, false);
            if (dropped == null || player.serverLevel().getEntity(dropped.getUUID()) != dropped) throw new IllegalArgumentException("gui.yagens_attributes.drop_failed");
        } catch (RuntimeException exception) {
            for (var entry : items) state.ledger.add(entry);
            throw exception;
        }
    }
}
