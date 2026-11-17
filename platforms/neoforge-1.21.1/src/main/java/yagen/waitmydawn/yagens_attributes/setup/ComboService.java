package yagen.waitmydawn.yagens_attributes.setup;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.data.*;
import yagen.waitmydawn.yagens_attributes.config.ServerConfigs;
import yagen.waitmydawn.yagens_attributes.network.ComboPayload;
import yagen.waitmydawn.yagens_attributes.registries.*;
import java.util.*;

@EventBusSubscriber(modid = YagensAttributes.MODID)
public final class ComboService {
    private ComboService() {}
    public static Combo.Rules rules() { return new Combo.Rules(ServerConfigs.COMBO_HITS_PER_TIER.get(), ServerConfigs.COMBO_MAX_TIER.get()); }
    private static long now(ServerPlayer player) { return player.server.overworld().getGameTime(); }
    public static boolean melee(ItemStack item) {
        var profile = CombatData.WEAPONS.weapon(item);
        return WeaponEligibility.operable(item, profile) && ModInfo.Category.MELEE.contains(profile.category());
    }
    public static List<Map<String, Bonus>> baseSources(ServerPlayer player, ItemStack weapon) {
        var world = WorldState.get(player.server); var result = new ArrayList<Map<String, Bonus>>();
        UUID id = weapon.get(Components.WEAPON_ID.get());
        var profile = CombatData.WEAPONS.weapon(weapon);
        if (id != null && profile != null) result.addAll(world.ledger.bonuses(id, net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(weapon.getItem()).toString(), profile.category()));
        result.add(world.bonuses(player.getUUID())); return List.copyOf(result);
    }
    public static int duration(List<Map<String, Bonus>> sources) { return Combo.duration(ServerConfigs.COMBO_DURATION.get(), sources); }
    public static int count(ServerPlayer player, UUID weapon) {
        if (!ServerConfigs.COMBO_ENABLED.get() || weapon == null || !player.hasData(RangedAttachments.COMBO)) return 0;
        return player.getData(RangedAttachments.COMBO).counters.getOrDefault(weapon, Combo.Counter.EMPTY).count(now(player), rules());
    }
    public static List<Map<String, Bonus>> sources(ServerPlayer player, UUID weapon, List<Map<String, Bonus>> sources) {
        return Combo.sources(sources, rules().tier(count(player, weapon)));
    }
    public static void hit(ServerPlayer player, UUID weapon, int duration) {
        hit(player, weapon, duration, Stats.resolve(1, Stats.COMBO_COUNT_CHANCE, baseSources(player, player.getMainHandItem())));
    }
    public static void hit(ServerPlayer player, UUID weapon, int duration, double chance) {
        if (!ServerConfigs.COMBO_ENABLED.get() || weapon == null || duration <= 0) return;
        var state = player.getData(RangedAttachments.COMBO); long now = now(player);
        state.counters.entrySet().removeIf(entry -> entry.getValue().remaining(now) == 0);
        if (!state.counters.containsKey(weapon) && state.counters.size() >= 64) {
            var oldest = state.counters.entrySet().stream().min(Comparator.comparingLong(entry -> entry.getValue().expires())).orElseThrow();
            state.counters.remove(oldest.getKey());
        }
        state.counters.put(weapon, state.counters.getOrDefault(weapon, Combo.Counter.EMPTY).hit(now, duration, rules(), Combo.gains(chance, player.getRandom()::nextDouble)));
        synchronize(player, state);
    }
    @SubscribeEvent(priority = EventPriority.LOWEST) public static void blocked(LivingShieldBlockEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !event.getBlocked() || event.getBlockedDamage() <= 0 || StatusDamage.isSecondary(event.getDamageSource())) return;
        var held = player.getMainHandItem(); if (!melee(held)) return;
        try { hit(player, Components.identify(held), duration(baseSources(player, held))); }
        catch (IllegalArgumentException exception) { YagensAttributes.LOGGER.debug("Rejected combo duration for {}: {}", player.getUUID(), exception.getMessage()); }
    }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var held = player.getMainHandItem();
        MeleeAttributes.update(player, player.isAlive() && melee(held) ? ConditionalModService.sources(player, held, baseSources(player, held), null) : List.of());
        if (!player.hasData(RangedAttachments.COMBO)) return;
        var state = player.getData(RangedAttachments.COMBO); long now = now(player);
        if (!ServerConfigs.COMBO_ENABLED.get()) state.counters.clear();
        else state.counters.entrySet().removeIf(entry -> entry.getValue().remaining(now) == 0);
        synchronize(player, state);
    }
    @SubscribeEvent public static void died(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && player.hasData(RangedAttachments.COMBO)) {
            var state = player.getData(RangedAttachments.COMBO); state.counters.clear(); synchronize(player, state);
        }
    }
    private static void synchronize(ServerPlayer player, ComboState state) {
        var held = player.getMainHandItem(); UUID weapon = melee(held) ? held.get(Components.WEAPON_ID.get()) : null;
        var counter = state.counters.getOrDefault(weapon, Combo.Counter.EMPTY); var rules = rules();
        if (Objects.equals(weapon, state.displayedWeapon) && counter.equals(state.displayedCounter) && rules.equals(state.displayedRules)) return;
        int count = counter.count(now(player), rules);
        PacketDistributor.sendToPlayer(player, new ComboPayload(weapon, count, count == 0 ? 0 : counter.remaining(now(player)), counter.duration(), rules.hitsPerTier(), rules.maxTier()));
        state.displayedWeapon = weapon; state.displayedCounter = counter; state.displayedRules = rules;
    }
}
