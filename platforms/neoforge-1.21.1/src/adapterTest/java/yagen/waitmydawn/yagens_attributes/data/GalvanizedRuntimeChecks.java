package yagen.waitmydawn.yagens_attributes.data;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.api.*;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.registries.*;
import yagen.waitmydawn.yagens_attributes.setup.*;
import java.util.*;

@EventBusSubscriber(modid = YagensAttributes.MODID, value = Dist.DEDICATED_SERVER)
public final class GalvanizedRuntimeChecks {
    private static final Map<LivingEntity, Float> AMOUNTS = new IdentityHashMap<>();
    private static final Map<LivingEntity, StatusContext> CONTEXTS = new IdentityHashMap<>();
    private static final List<Cow> TARGETS = new ArrayList<>();
    private static int checks;
    private GalvanizedRuntimeChecks() {}
    @SubscribeEvent(priority = EventPriority.LOWEST) public static void incoming(LivingIncomingDamageEvent event) {
        if (TARGETS.contains(event.getEntity())) AMOUNTS.put(event.getEntity(), event.getAmount());
    }
    @SubscribeEvent(priority = EventPriority.LOWEST) public static void proc(StatusProcEvent event) {
        if (TARGETS.contains(event.target())) CONTEXTS.put(event.target(), event.context());
    }
    static int run(ServerLevel level, JsonObject report) {
        checks = 0;
        var player = FakePlayerFactory.get(level, new GameProfile(UUID.fromString("f2997d39-0779-40aa-9c08-674a1cbd8541"), "[GalvanizedChecks]"));
        player.setHealth(player.getMaxHealth()); player.setPos(160, 100, 160);
        var state = WorldState.get(player.server);
        level.addNewPlayer(player);
        try {
            ranged(level, player, state);
            melee(level, player, state);
            statusKills(level, player, state);
        } finally {
            for (var target : TARGETS) target.discard(); TARGETS.clear(); AMOUNTS.clear(); CONTEXTS.clear();
            for (var entry : state.ledger.owned(player.getUUID())) {
                if (entry.weapon() != null) state.ledger.uninstall(player.getUUID(), entry.id(), entry.weapon(), false);
                state.ledger.withdraw(player.getUUID(), entry.id());
            }
            player.getData(RangedAttachments.KILL_BUFFS).counters.clear();
            for (var stat : List.of(Stats.CRITICAL_CHANCE, Stats.STATUS_CHANCE)) state.bonus(player.getUUID(), stat, Bonus.NONE);
            player.discard();
        }
        report.addProperty("galvanized_runtime_checks", checks);
        return checks;
    }
    private static void ranged(ServerLevel level, ServerPlayer player, WorldState state) {
        var bow = new ItemStack(Items.BOW); player.setItemInHand(InteractionHand.MAIN_HAND, bow);
        UUID weapon = Components.identify(bow); var profile = CombatData.WEAPONS.weapon(bow);
        state.bonus(player.getUUID(), Stats.CRITICAL_CHANCE, new Bonus(-profile.criticalChance(), 0, List.of(), 0));
        state.bonus(player.getUUID(), Stats.STATUS_CHANCE, new Bonus(-profile.statusChance(), 0, List.of(), 1));
        UUID mod = install(player, state, bow, "galvanized_gaze_of_pain", 10, 0);
        install(player, state, bow, "universal_nanocube", 5, 1);
        var empty = Objects.requireNonNull(RangedShooting.snapshot(player, bow));
        check(empty.targetModifiers().isEmpty());
        for (int i = 0; i < 2; i++) {
            var victim = target(level);
            hit(level, player, bow, Objects.requireNonNull(RangedShooting.snapshot(player, bow)), victim, 10000, false);
            check(!victim.isAlive());
        }
        var snapshot = ShotData.decode(Objects.requireNonNull(RangedShooting.snapshot(player, bow)).encode());
        check(snapshot.targetModifiers().size() == 1);
        close(.8, snapshot.targetModifiers().get(0).addition());
        var target = target(level);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200));
        check(ConditionalModService.targetSources(snapshot.targetModifiers(), target).isEmpty());
        for (int i = 0; i < 3; i++) StatusApi.apply(target, yagen.waitmydawn.yagens_attributes.core.DamageType.CORROSIVE, player.getUUID(), 1, 120);
        StatusApi.apply(target, yagen.waitmydawn.yagens_attributes.core.DamageType.IMPACT, player.getUUID(), 1, 120);
        StatusApi.apply(target, yagen.waitmydawn.yagens_attributes.core.DamageType.COLD, player.getUUID(), 1, 120);
        check(StatusApi.distinctTypes(target) == 3);
        state.ledger.uninstall(player.getUUID(), mod, weapon, false);
        player.getData(RangedAttachments.KILL_BUFFS).counters.clear();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_AXE));
        hit(level, player, bow, snapshot.projectile(.75, true), target, 10, false);
        close(32.25, AMOUNTS.get(target));
        var context = Objects.requireNonNull(CONTEXTS.get(target));
        close(14.25, context.base(yagen.waitmydawn.yagens_attributes.core.DamageType.SLASH));
        check(weapon.equals(context.rangedWeapon()));
        var different = target(level);
        StatusApi.apply(different, yagen.waitmydawn.yagens_attributes.core.DamageType.IMPACT, player.getUUID(), 1, 120);
        hit(level, player, bow, snapshot.projectile(.75, true), different, 10, false);
        close(20.25, AMOUNTS.get(different));
        var explosion = target(level);
        StatusApi.apply(explosion, yagen.waitmydawn.yagens_attributes.core.DamageType.IMPACT, player.getUUID(), 1, 120);
        hit(level, player, bow, snapshot.projectile(.75, true), explosion, 10, true);
        close(14.25, AMOUNTS.get(explosion));
    }
    private static void melee(ServerLevel level, ServerPlayer player, WorldState state) {
        var sword = new ItemStack(Items.DIAMOND_SWORD); player.setItemInHand(InteractionHand.MAIN_HAND, sword);
        var profile = CombatData.WEAPONS.weapon(sword);
        state.bonus(player.getUUID(), Stats.CRITICAL_CHANCE, new Bonus(-profile.criticalChance(), 0, List.of(), 0));
        state.bonus(player.getUUID(), Stats.STATUS_CHANCE, new Bonus(-profile.statusChance(), 0, List.of(), 0));
        install(player, state, sword, "galvanized_bone_finger_box", 10, 0);
        for (int i = 0; i < 5; i++) {
            var victim = target(level); check(victim.hurt(level.damageSources().playerAttack(player), 10000)); check(!victim.isAlive());
            var sources = ConditionalModService.sources(player, sword, ComboService.baseSources(player, sword), null);
            close(2 * (1 + .3 * Math.min(4, i + 1)), Stats.resolve(2, Stats.CRITICAL_DAMAGE, sources));
        }
        var other = new ItemStack(Items.DIAMOND_SWORD); Components.identify(other);
        close(2, Stats.resolve(2, Stats.CRITICAL_DAMAGE, ConditionalModService.sources(player, other, List.of(), null)));
    }
    private static void statusKills(ServerLevel level, ServerPlayer player, WorldState state) {
        var original = new ItemStack(Items.DIAMOND_SWORD); var held = new ItemStack(Items.DIAMOND_SWORD);
        UUID originalWeapon = Components.identify(original); Components.identify(held);
        UUID steel = install(player, state, original, "galvanized_bone_finger_box", 10, 0);
        UUID fury = install(player, state, original, "berserker_fury", 5, 1);
        install(player, state, held, "galvanized_bone_finger_box", 10, 0);
        install(player, state, held, "berserker_fury", 5, 1);
        player.setItemInHand(InteractionHand.MAIN_HAND, held);
        int position = 200;
        for (var type : List.of(yagen.waitmydawn.yagens_attributes.core.DamageType.SLASH,
                yagen.waitmydawn.yagens_attributes.core.DamageType.TOXIN, yagen.waitmydawn.yagens_attributes.core.DamageType.HEAT,
                yagen.waitmydawn.yagens_attributes.core.DamageType.ELECTRICITY, yagen.waitmydawn.yagens_attributes.core.DamageType.GAS,
                yagen.waitmydawn.yagens_attributes.core.DamageType.BLAST)) {
            player.getData(RangedAttachments.KILL_BUFFS).counters.clear();
            var victim = target(level); victim.setPos(position, 100, 200); position += 16;
            var context = new StatusContext(Map.of(type, 10000D), 0, originalWeapon, AttackKind.MELEE);
            check(StatusApi.apply(victim, type, player.getUUID(), context, 120));
            if (victim.isAlive()) {
                var layer = StatusApi.layers(victim).get(0);
                check(layer.weapon().equals(originalWeapon) && layer.attackKind() == AttackKind.MELEE);
            }
            finishStatus(level, victim);
            close(2.6, value(player, original, Stats.CRITICAL_DAMAGE, 2));
            close(1.35, value(player, original, Stats.ATTACK_SPEED, 1));
            close(2, value(player, held, Stats.CRITICAL_DAMAGE, 2));
            close(1, value(player, held, Stats.ATTACK_SPEED, 1));
        }
        player.getData(RangedAttachments.KILL_BUFFS).counters.clear();
        var host = target(level); host.setPos(320, 100, 200);
        var gas = yagen.waitmydawn.yagens_attributes.core.DamageType.GAS;
        check(StatusApi.apply(host, gas, player.getUUID(), new StatusContext(Map.of(gas, 100D), 0, originalWeapon, AttackKind.MELEE), 120));
        check(host.hurt(level.damageSources().generic(), 10000));
        var cloud = level.getEntitiesOfClass(AreaEffectCloud.class, host.getBoundingBox().inflate(3)).stream().findFirst().orElseThrow();
        try {
            var victim = target(level); victim.setPos(320, 100, 200); victim.setHealth(1);
            for (int tick = 0; tick < 40 && victim.isAlive(); tick++) StatusEffects.tick(new EntityTickEvent.Post(cloud));
            check(!victim.isAlive());
            close(2.6, value(player, original, Stats.CRITICAL_DAMAGE, 2));
        } finally { cloud.discard(); }

        var trident = new ItemStack(Items.TRIDENT); UUID tridentId = Components.identify(trident);
        install(player, state, trident, "galvanized_bone_finger_box", 10, 0);
        install(player, state, trident, "galvanized_chamber", 10, 1);
        var slash = yagen.waitmydawn.yagens_attributes.core.DamageType.SLASH;
        for (var kind : List.of(AttackKind.MELEE, AttackKind.RANGED)) {
            player.getData(RangedAttachments.KILL_BUFFS).counters.clear();
            var victim = target(level); victim.setPos(352, 100, 200);
            check(StatusApi.apply(victim, slash, player.getUUID(), new StatusContext(Map.of(slash, 10000D), 0, tridentId, kind), 120));
            finishStatus(level, victim);
            close(kind == AttackKind.MELEE ? 2.6 : 2, value(player, trident, Stats.CRITICAL_DAMAGE, 2));
            close(kind == AttackKind.RANGED ? 2.1 : 1.8, value(player, trident, Stats.MULTISHOT, 1));
        }
        player.getData(RangedAttachments.KILL_BUFFS).counters.clear();
        var removed = target(level); removed.setPos(384, 100, 200);
        check(StatusApi.apply(removed, slash, player.getUUID(), new StatusContext(Map.of(slash, 10000D), 0, originalWeapon, AttackKind.MELEE), 120));
        state.ledger.uninstall(player.getUUID(), steel, originalWeapon, false);
        state.ledger.uninstall(player.getUUID(), fury, originalWeapon, false);
        finishStatus(level, removed); check(player.getData(RangedAttachments.KILL_BUFFS).counters.isEmpty());
        var unknown = target(level); unknown.setPos(416, 100, 200);
        check(StatusApi.apply(unknown, slash, player.getUUID(), 10000, 120));
        finishStatus(level, unknown); check(player.getData(RangedAttachments.KILL_BUFFS).counters.isEmpty());
    }
    private static double value(ServerPlayer player, ItemStack weapon, String stat, double base) {
        return Stats.resolve(base, stat, ConditionalModService.sources(player, weapon, ComboService.baseSources(player, weapon), null));
    }
    private static void finishStatus(ServerLevel level, Cow target) {
        for (int tick = 0; tick < 130 && target.isAlive(); tick++) {
            StatusEffects.tick(new EntityTickEvent.Post(target));
            StatusEffects.endTick(new ServerTickEvent.Post(() -> true, level.getServer()));
        }
        check(!target.isAlive());
    }
    private static UUID install(ServerPlayer player, WorldState state, ItemStack weapon, String type, int rank, int slot) {
        var card = ModDefinitions.INSTANCE.catalog().card("yagens_attributes:" + type, rank); UUID id = UUID.randomUUID();
        state.ledger.add(new ModLedger.Entry(id, player.getUUID(), null, false, card.bonuses(), card.info(), -1));
        state.ledger.install(player.getUUID(), id, Components.identify(weapon), CombatData.WEAPONS.weapon(weapon).category(), 8, slot, 30, true);
        return id;
    }
    private static Cow target(ServerLevel level) {
        var target = new Cow(EntityType.COW, level); target.setPos(162, 100, 160); target.setNoAi(true);
        Objects.requireNonNull(target.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(1000); target.setHealth(1000);
        level.addFreshEntity(target); TARGETS.add(target); return target;
    }
    private static void hit(ServerLevel level, ServerPlayer player, ItemStack bow, ShotData shot, Cow target, float amount, boolean explosion) {
        var arrow = new Arrow(level, player, new ItemStack(Items.ARROW), bow); arrow.setData(RangedAttachments.SHOT, shot);
        var source = explosion ? new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.EXPLOSION), arrow, player)
                : level.damageSources().arrow(arrow, player);
        check(target.hurt(source, amount));
    }
    private static void check(boolean value) { checks++; if (!value) throw new AssertionError("Galvanized runtime check " + checks); }
    private static void close(double expected, double actual) { checks++; if (Math.abs(expected - actual) > 1e-4) throw new AssertionError(expected + " != " + actual); }
}
