package yagen.waitmydawn.yagens_attributes.data;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import yagen.waitmydawn.yagens_attributes.core.Bonus;
import yagen.waitmydawn.yagens_attributes.core.ModInfo;
import yagen.waitmydawn.yagens_attributes.core.ModLedger;
import yagen.waitmydawn.yagens_attributes.core.Stats;
import yagen.waitmydawn.yagens_attributes.registries.Components;
import yagen.waitmydawn.yagens_attributes.registries.RangedAttachments;
import yagen.waitmydawn.yagens_attributes.setup.ConditionalModService;
import yagen.waitmydawn.yagens_attributes.setup.RangedShooting;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

final class ArgonScopeRuntimeChecks {
    private ArgonScopeRuntimeChecks() {}

    static int run(ServerLevel level) {
        var world = WorldState.get(level.getServer());
        var player = FakePlayerFactory.get(level, new GameProfile(UUID.fromString("71dd5293-fea9-49b9-b126-70ae9ac6b569"), "[ArgonChecks]"));
        var bow = new ItemStack(Items.BOW);
        var otherBow = new ItemStack(Items.BOW);
        var bowId = Components.identify(bow);
        var target = Objects.requireNonNull(EntityType.ZOMBIE.create(level));
        target.setNoAi(true);
        target.setNoGravity(true);
        target.setPos(8.5, 220, 8.5);
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);
        target.setHealth(1000);
        level.getChunk(0, 0);
        require(level.addFreshEntity(target), "The native bow target entered the level");
        player.setPos(8.5, 220, 6.5);
        level.addNewPlayer(player);
        var rank = ModDefinitions.INSTANCE.catalog().card("yagens_attributes:argon_scope", 5);
        var mod = UUID.randomUUID();
        world.ledger.add(new ModLedger.Entry(mod, player.getUUID(), null, false, rank.bonuses(), rank.info(), -1));
        world.ledger.install(player.getUUID(), mod, bowId, ModInfo.Category.BOW, 8, 0, 30, true);
        player.getInventory().setItem(8, new ItemStack(Items.ARROW, 64));
        var buffs = player.getData(RangedAttachments.KILL_BUFFS);
        var clock = level.getServer().getWorldData().overworldData();
        long start = level.getGameTime();
        try {
            double bodyY = target.getY() + .65;
            var bodyShot = single(fire(level, player, bow, target, bodyY));
            var initial = RangedShooting.shot(bodyShot);
            double base = initial.profile().criticalChance();
            equal(base, chance(initial), "Argon Scope has no static bonus");
            hit(bodyShot, target, bodyY);
            require(target.getHealth() < target.getMaxHealth() && buffs.counters.isEmpty(), "Body damage does not activate Argon Scope");

            double headY = target.getEyeY();
            var trigger = single(fire(level, player, bow, target, headY));
            var triggerShot = RangedShooting.shot(trigger);
            player.setItemInHand(InteractionHand.MAIN_HAND, otherBow);
            hit(trigger, target, headY);
            equal(base, chance(triggerShot), "A shot cannot benefit from its own proc");
            require(buffs.counters.size() == 1 && buffs.counters.keySet().iterator().next().weapon().equals(bowId),
                    "Weak-point impact after a swap belongs to the firing bow");

            var unrelated = single(fire(level, player, otherBow, target, bodyY));
            equal(base, chance(RangedShooting.shot(unrelated)), "Another bow cannot borrow the buff");
            unrelated.discard();

            world.bonus(player.getUUID(), Stats.MULTISHOT, new Bonus(0, 1, List.of(), 0));
            clock.setGameTime(start + 60);
            var volley = fire(level, player, bow, target, headY);
            require(volley.size() == 2, "Native bow fire produces both projectiles, actual count " + volley.size());
            for (var arrow : volley) {
                var shot = RangedShooting.shot(arrow);
                equal(base * 2.35, chance(shot), "Every projectile shares the launch-time critical chance");
                equal(1, shot.scale(), "Multishot damage shares are preserved");
            }
            ConditionalModService.tick(new PlayerTickEvent.Post(player));
            require(buffs.displayed.size() == 1 && buffs.displayed.getFirst().deadlines().equals(List.of(start + 180)),
                    "Weapon buff panel publishes the active deadline");
            hit(volley.getFirst(), target, headY);
            ConditionalModService.tick(new PlayerTickEvent.Post(player));
            require(buffs.displayed.getFirst().deadlines().equals(List.of(start + 240)),
                    "Another weak point hit refreshes the one stack");

            clock.setGameTime(start + 240);
            ConditionalModService.tick(new PlayerTickEvent.Post(player));
            require(buffs.displayed.isEmpty(), "Expired Argon Scope disappears from the weapon buff panel");
            var airborne = volley.getLast();
            equal(base * 2.35, chance(RangedShooting.shot(airborne)), "An airborne arrow retains the active bonus after expiry");
            hit(airborne, target, bodyY);
            world.bonus(player.getUUID(), Stats.MULTISHOT, Bonus.NONE);
            var expired = single(fire(level, player, bow, target, bodyY));
            equal(base, chance(RangedShooting.shot(expired)), "A shot fired after expiry has base critical chance");
            expired.discard();
            return 14 + galvanized(level, player, target, otherBow, bow, start + 300);
        } finally {
            clock.setGameTime(start);
            buffs.counters.clear();
            world.bonus(player.getUUID(), Stats.MULTISHOT, Bonus.NONE);
            world.ledger.uninstall(player.getUUID(), mod, bowId, false);
            world.ledger.withdraw(player.getUUID(), mod);
            arrows(level, player, target).forEach(Arrow::discard);
            target.discard();
            player.discard();
        }
    }

    private static int galvanized(ServerLevel level, ServerPlayer player, Zombie target, ItemStack bow, ItemStack otherBow, long start) {
        var world = WorldState.get(level.getServer());
        var bowId = Components.identify(bow);
        var rank = ModDefinitions.INSTANCE.catalog().card("yagens_attributes:galvanized_argon_scope", 10);
        var mod = UUID.randomUUID();
        world.ledger.add(new ModLedger.Entry(mod, player.getUUID(), null, false, rank.bonuses(), rank.info(), -1));
        world.ledger.install(player.getUUID(), mod, bowId, ModInfo.Category.BOW, 8, 0, 30, true);
        var buffs = player.getData(RangedAttachments.KILL_BUFFS);
        var clock = level.getServer().getWorldData().overworldData();
        try {
            clock.setGameTime(start);
            var first = single(fire(level, player, bow, target, target.getEyeY()));
            var firstShot = RangedShooting.shot(first);
            double base = firstShot.profile().criticalChance();
            equal(base, chance(firstShot), "The galvanized scope starts without a static bonus");
            hit(first, target, target.getEyeY());
            var afterHit = single(fire(level, player, bow, target, target.getEyeY()));
            equal(base * 2.2, chance(RangedShooting.shot(afterHit)), "A nonlethal weak point hit gives only the hit bonus");
            afterHit.discard();

            clock.setGameTime(start + 20);
            killWithBow(level, player, bow, otherBow, false);
            ConditionalModService.tick(new PlayerTickEvent.Post(player));
            require(buffs.counters.size() == 1, "A body-shot kill cannot add the weak point kill bonus");
            killWithBow(level, player, bow, otherBow, true);
            clock.setGameTime(start + 40);
            killWithBow(level, player, bow, otherBow, true);

            var afterKills = single(fire(level, player, bow, target, target.getEyeY()));
            equal(base * 3, chance(RangedShooting.shot(afterKills)), "Two weak point kills stack with the hit bonus on their firing bow");
            afterKills.discard();
            ConditionalModService.tick(new PlayerTickEvent.Post(player));
            require(buffs.displayed.size() == 2, "Hit and kill bonuses appear as separate buff entries");
            require(buffs.displayed.get(1).deadlines().equals(List.of(start + 260, start + 280)),
                    "Each kill keeps its own expiry instead of refreshing previous kills");

            clock.setGameTime(start + 260);
            var afterExpiry = single(fire(level, player, bow, target, target.getEyeY()));
            equal(base * 2.6, chance(RangedShooting.shot(afterExpiry)), "The first kill stack drops independently");
            afterExpiry.discard();
            clock.setGameTime(start + 280);
            var expired = single(fire(level, player, bow, target, target.getEyeY()));
            equal(base, chance(RangedShooting.shot(expired)), "Both bonuses expire after their final deadlines");
            expired.discard();
            return 8;
        } finally {
            buffs.counters.clear();
            world.ledger.uninstall(player.getUUID(), mod, bowId, false);
            world.ledger.withdraw(player.getUUID(), mod);
        }
    }

    private static void killWithBow(ServerLevel level, ServerPlayer player, ItemStack bow, ItemStack swap, boolean weakPoint) {
        var target = Objects.requireNonNull(EntityType.ZOMBIE.create(level));
        target.setNoAi(true);
        target.setNoGravity(true);
        target.setPos(11.5, 220, 8.5);
        target.setHealth(1);
        require(level.addFreshEntity(target), "The lethal shot target entered the level");
        try {
            double height = weakPoint ? target.getEyeY() : target.getY() + .65;
            var arrow = single(fire(level, player, bow, target, height));
            player.setItemInHand(InteractionHand.MAIN_HAND, swap);
            hit(arrow, target, height);
            require(target.isDeadOrDying(), "The native arrow killed the target");
        } finally {
            target.discard();
        }
    }

    private static List<Arrow> fire(ServerLevel level, ServerPlayer player, ItemStack bow, Zombie target, double height) {
        player.setPos(target.getX(), height - player.getEyeHeight() + .1, target.getZ() - 2);
        player.setXRot(0);
        player.setYRot(0);
        player.setItemInHand(InteractionHand.MAIN_HAND, bow);
        var existing = arrows(level, player, target);
        player.startUsingItem(InteractionHand.MAIN_HAND);
        Items.BOW.releaseUsing(bow, level, player, Items.BOW.getUseDuration(bow, player) - 20);
        player.stopUsingItem();
        return arrows(level, player, target).stream().filter(arrow -> !existing.contains(arrow)).toList();
    }

    private static List<Arrow> arrows(ServerLevel level, ServerPlayer player, Zombie target) {
        return level.getEntitiesOfClass(Arrow.class, new AABB(target.blockPosition()).inflate(6), arrow -> arrow.getOwner() == player);
    }

    private static Arrow single(List<Arrow> arrows) {
        require(arrows.size() == 1, "Native bow fire creates one arrow");
        return arrows.getFirst();
    }

    private static void hit(Arrow arrow, Zombie target, double height) {
        arrow.setPos(target.getX(), height, target.getZ() - 2);
        arrow.setDeltaMovement(new Vec3(0, 0, 3));
        arrow.tick();
        arrow.discard();
    }

    private static double chance(ShotData shot) {
        return Stats.resolve(shot.profile().criticalChance(), Stats.CRITICAL_CHANCE, shot.sources());
    }

    private static void equal(double expected, double actual, String description) {
        require(Math.abs(expected - actual) < 1e-8, description + ": expected " + expected + ", got " + actual);
    }

    private static void require(boolean value, String description) {
        if (!value) throw new AssertionError(description);
    }
}
