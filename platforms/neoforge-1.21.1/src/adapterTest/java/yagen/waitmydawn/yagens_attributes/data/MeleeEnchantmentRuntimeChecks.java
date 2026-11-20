package yagen.waitmydawn.yagens_attributes.data;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import yagen.waitmydawn.yagens_attributes.core.Bonus;
import yagen.waitmydawn.yagens_attributes.core.Combat;
import yagen.waitmydawn.yagens_attributes.core.DamageType;
import yagen.waitmydawn.yagens_attributes.core.ModLedger;
import yagen.waitmydawn.yagens_attributes.core.Stats;
import yagen.waitmydawn.yagens_attributes.registries.Components;
import yagen.waitmydawn.yagens_attributes.setup.CombatEvents;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

final class MeleeEnchantmentRuntimeChecks {
    private MeleeEnchantmentRuntimeChecks() {}

    static int run(ServerLevel level) throws ReflectiveOperationException {
        var clock = LivingEntity.class.getDeclaredField("attackStrengthTicker");
        clock.setAccessible(true);
        var player = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "[EnchantChecks]"));
        level.getChunk(0, 0);
        player.setPos(8.5, 220, 7.5);
        level.addNewPlayer(player);
        player.setOnGround(true);
        var world = WorldState.get(level.getServer());
        var sword = new ItemStack(Items.DIAMOND_SWORD);
        var weapon = Components.identify(sword);
        var profile = CombatData.WEAPONS.weapon(sword);
        var mod = UUID.randomUUID();
        var damageMod = ModDefinitions.INSTANCE.catalog().card("yagens_attributes:universal_nanocube", 5);
        world.ledger.add(new ModLedger.Entry(mod, player.getUUID(), null, false, damageMod.bonuses(), damageMod.info(), -1));
        world.ledger.install(player.getUUID(), mod, weapon, profile.category(), 8, 0, 30, true);
        var heat = Map.of("damage.heat", new Bonus(0, 0, List.of(), 0, 0, .5));
        heat.forEach((key, bonus) -> world.bonus(player.getUUID(), key, bonus));
        world.bonus(player.getUUID(), Stats.CRITICAL_CHANCE, new Bonus(0, -1, List.of(), 0));
        world.bonus(player.getUUID(), Stats.STATUS_CHANCE, new Bonus(0, -1, List.of(), 0));
        player.setItemInHand(InteractionHand.MAIN_HAND, sword);
        player.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(7);
        double base = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        var bonuses = List.of(damageMod.bonuses(), heat);
        var incoming = new IdentityHashMap<LivingEntity, Float>();
        var proportions = new IdentityHashMap<LivingEntity, Map<DamageType, Double>>();
        Consumer<LivingIncomingDamageEvent> observeDamage = event -> {
            if (event.getSource().getDirectEntity() == player) incoming.put(event.getEntity(), event.getAmount());
        };
        Consumer<LivingDamageEvent.Pre> observeComponents = event -> {
            if (event.getSource().getDirectEntity() == player) proportions.put(event.getEntity(), CombatEvents.components(event.getSource(), event.getEntity()));
        };
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, observeDamage);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, observeComponents);
        try {
            for (var test : List.of(
                    new Case(Enchantments.SHARPNESS, EntityType.COW, 1.5),
                    new Case(Enchantments.SMITE, EntityType.ZOMBIE, 5),
                    new Case(Enchantments.SMITE, EntityType.COW, 0),
                    new Case(Enchantments.BANE_OF_ARTHROPODS, EntityType.SPIDER, 5),
                    new Case(Enchantments.BANE_OF_ARTHROPODS, EntityType.COW, 0))) {
                var enchanted = sword.copy();
                enchanted.enchant(level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(test.enchantment()), 2);
                player.setItemInHand(InteractionHand.MAIN_HAND, enchanted);
                var preview = MeleeEnchantments.preview(enchanted);
                equal(test.enchantment() == Enchantments.SHARPNESS ? 1.5 : 5, preview.getFirst().amount(), "Full-charge preview");
                var target = target(level, test.victim(), 8.5);
                try {
                    clock.setInt(player, 100);
                    player.attack(target);
                    expectHit(target, base, test.bonus(), profile, bonuses, incoming, proportions);
                } finally { target.discard(); }
            }

            var sharp = sword.copy();
            sharp.enchant(level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.SHARPNESS), 2);
            player.setItemInHand(InteractionHand.MAIN_HAND, sharp);
            var partial = target(level, EntityType.COW, 8.5);
            try {
                clock.setInt(player, 3);
                double charge = player.getAttackStrengthScale(.5F);
                player.attack(partial);
                expectHit(partial, base * (.2 + .8 * charge * charge), 1.5 * charge, profile, bonuses, incoming, proportions);
            } finally { partial.discard(); }

            var smite = sword.copy();
            smite.enchant(level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.SMITE), 2);
            player.setItemInHand(InteractionHand.MAIN_HAND, smite);
            var primary = target(level, EntityType.COW, 8.5);
            var swept = target(level, EntityType.ZOMBIE, 9.1);
            try {
                clock.setInt(player, 100);
                player.attack(primary);
                expectHit(primary, base, 0, profile, bonuses, incoming, proportions);
                double sweepBase = 1 + player.getAttributeValue(Attributes.SWEEPING_DAMAGE_RATIO) * base;
                expectHit(swept, sweepBase, 5, profile, bonuses, incoming, proportions);
            } finally {
                swept.discard();
                primary.discard();
            }
            return 8;
        } finally {
            NeoForge.EVENT_BUS.unregister(observeDamage);
            NeoForge.EVENT_BUS.unregister(observeComponents);
            world.ledger.uninstall(player.getUUID(), mod, weapon, false);
            world.ledger.withdraw(player.getUUID(), mod);
            heat.keySet().forEach(key -> world.bonus(player.getUUID(), key, Bonus.NONE));
            world.bonus(player.getUUID(), Stats.CRITICAL_CHANCE, Bonus.NONE);
            world.bonus(player.getUUID(), Stats.STATUS_CHANCE, Bonus.NONE);
            player.discard();
        }
    }

    private record Case(ResourceKey<Enchantment> enchantment, EntityType<? extends Mob> victim, double bonus) {}

    private static Mob target(ServerLevel level, EntityType<? extends Mob> type, double x) {
        var target = Objects.requireNonNull(type.create(level));
        target.setPos(x, 220, 8.5);
        target.setNoAi(true);
        target.setNoGravity(true);
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
        target.setHealth(100);
        if (!level.addFreshEntity(target)) throw new AssertionError("Fixture target failed to enter the world");
        return target;
    }

    private static void expectHit(LivingEntity target, double base, double bonus, yagen.waitmydawn.yagens_attributes.core.WeaponProfile profile,
                                  List<Map<String, Bonus>> sources, Map<LivingEntity, Float> incoming,
                                  Map<LivingEntity, Map<DamageType, Double>> observed) {
        var components = Combat.components(base, profile, sources);
        double total = components.values().stream().mapToDouble(Double::doubleValue).sum();
        equal(total + bonus, incoming.getOrDefault(target, -1F), "Native attack with flat enchantment damage against " + target.getType());
        var actual = observed.getOrDefault(target, Map.of());
        for (var type : DamageType.values())
            equal(components.get(type) * (1 + bonus / total), actual.getOrDefault(type, -1D), "Proportional " + type + " damage");
    }

    private static void equal(double expected, double actual, String context) {
        if (Math.abs(expected - actual) > 1e-4) throw new AssertionError(context + ": expected " + expected + ", got " + actual);
    }
}
