package yagen.waitmydawn.yagens_attributes.setup;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.data.*;
import yagen.waitmydawn.yagens_attributes.item.UnknownRivenItem;
import yagen.waitmydawn.yagens_attributes.registries.Components;
import java.util.*;

public final class RivenService {
    private RivenService() {}
    public static Map<String, WeaponProfile> weapons() {
        var result = new TreeMap<String, WeaponProfile>();
        CombatData.WEAPONS.profiles().forEach((id, profile) -> {
            if (BuiltInRegistries.ITEM.containsKey(id) && id.equals(BuiltInRegistries.ITEM.getKey(BuiltInRegistries.ITEM.get(id)))
                    && WeaponEligibility.operable(BuiltInRegistries.ITEM.get(id).getDefaultInstance(), profile)) result.put(id.toString(), profile);
        });
        return Map.copyOf(result);
    }
    public static RivenRoll current(RivenRoll roll, Map<ResourceLocation, WeaponProfile> profiles) {
        var profile = profiles.get(ResourceLocation.parse(roll.weapon()));
        return roll.withDisposition(profile == null ? 0 : profile.rivenDisposition());
    }
    public static ItemStack stack(RivenRoll roll, int level) {
        var item = ModTransactions.stack(RivenRules.TYPE, level); item.set(Components.RIVEN_ROLL.get(), roll); return item;
    }
    public static void reveal(ServerPlayer player, InteractionHand hand, RivenRoll.Pool pool) {
        var held = player.getItemInHand(hand);
        if (!(held.getItem() instanceof UnknownRivenItem item) || item.pool() != pool || held.isEmpty()) throw new IllegalArgumentException("Invalid veiled Riven item");
        if (held.getCount() > 1 && player.getInventory().getFreeSlot() < 0) throw new IllegalArgumentException("gui.yagens_attributes.riven_inventory_full");
        var catalog = ModDefinitions.INSTANCE.catalog();
        if (catalog.rivenRules() == null) throw new IllegalArgumentException("Missing Riven rules");
        var roll = catalog.rivenRules().roll(pool, weapons(), player.getRandom()::nextInt, player.getRandom()::nextDouble);
        catalog.rivenCard(roll, 0);
        var revealed = stack(roll, 0);
        if (held.getCount() == 1) player.setItemInHand(hand, revealed);
        else { held.shrink(1); player.getInventory().add(revealed); }
        player.getInventory().setChanged(); player.containerMenu.broadcastChanges();
    }
    public static ModLedger.Entry editable(ServerPlayer player, WorldState state, UUID mod) {
        var entry = state.ledger.entry(mod);
        var held = player.getMainHandItem();
        if (entry.info().riven() == null || !entry.owner().equals(player.getUUID()) && !(entry.shared() && entry.weapon() != null))
            throw new IllegalArgumentException("gui.yagens_attributes.cycle_denied");
        if (entry.weapon() != null && (!entry.weapon().equals(held.get(Components.WEAPON_ID.get()))
                || !entry.info().compatibleItem(BuiltInRegistries.ITEM.getKey(held.getItem()).toString())))
            throw new IllegalArgumentException("gui.yagens_attributes.cycle_hold_weapon");
        return entry;
    }
    public static void cycle(ServerPlayer player, WorldState state, UUID mod) {
        var entry = editable(player, state, mod); var original = entry.info().riven();
        if (state.cycle(mod) != null) throw new IllegalArgumentException("gui.yagens_attributes.cycle_choose_first");
        if (original.cycles() == Integer.MAX_VALUE) throw new IllegalArgumentException("Cycle count limit reached");
        if (state.kuva(player.getUUID()) < original.cycleCost()) throw new IllegalArgumentException("gui.yagens_attributes.kuva_insufficient");
        var profile = CombatData.WEAPONS.profiles().get(ResourceLocation.parse(original.weapon()));
        if (profile == null || profile.category() != original.category()) throw new IllegalArgumentException("Missing matching Riven weapon profile");
        var catalog = ModDefinitions.INSTANCE.catalog();
        var candidate = catalog.rivenRules().cycle(original, profile, player.getRandom()::nextInt, player.getRandom()::nextDouble);
        catalog.rivenCard(candidate, entry.info().level());
        var current = catalog.rivenCard(original.withCycles(candidate.cycles()), entry.info().level());
        state.ledger.refresh(mod, current.info(), current.bonuses());
        state.cycle(mod, candidate); state.kuva(player.getUUID(), state.kuva(player.getUUID()) - original.cycleCost());
    }
    public static void choose(ServerPlayer player, WorldState state, UUID mod, boolean useNew) {
        var entry = editable(player, state, mod); var candidate = state.cycle(mod);
        if (candidate == null) throw new IllegalArgumentException("gui.yagens_attributes.cycle_no_candidate");
        if (useNew) {
            var card = ModDefinitions.INSTANCE.catalog().rivenCard(current(candidate, CombatData.WEAPONS.profiles()), entry.info().level());
            state.ledger.refresh(mod, card.info(), card.bonuses());
        }
        state.cycle(mod, null);
    }
}
