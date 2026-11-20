package yagen.waitmydawn.yagens_attributes.data;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import yagen.waitmydawn.yagens_attributes.core.Bonus;
import yagen.waitmydawn.yagens_attributes.core.ConditionalModifier;
import yagen.waitmydawn.yagens_attributes.core.ModLedger;
import yagen.waitmydawn.yagens_attributes.core.Stats;
import yagen.waitmydawn.yagens_attributes.registries.Components;
import yagen.waitmydawn.yagens_attributes.registries.RangedAttachments;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

final class GatlingWeakPointRuntimeChecks {
    private GatlingWeakPointRuntimeChecks() {}

    static int run(ServerLevel level) {
        var item = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse("cataclysm:laser_gatling")).orElseThrow();
        level.getChunk(0, 0);
        var shooter = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "[LaserChecks]"));
        shooter.setPos(8.5, 220, 4.5);
        level.addNewPlayer(shooter);
        var gun = new ItemStack(item);
        var weapon = Components.identify(gun);
        var world = WorldState.get(level.getServer());
        world.bonus(shooter.getUUID(), Stats.FIRE_RATE, new Bonus(0, 1, List.of(), 0));
        world.bonus(shooter.getUUID(), Stats.STATUS_CHANCE, new Bonus(0, -1, List.of(), 0));
        var buffs = shooter.getData(RangedAttachments.KILL_BUFFS);
        var mod = UUID.randomUUID();
        var catalog = ModDefinitions.INSTANCE.catalog();
        var profile = CombatData.WEAPONS.weapon(gun);
        var ordinary = catalog.card("yagens_attributes:argon_scope", 5);
        var galvanized = catalog.card("yagens_attributes:galvanized_argon_scope", 10);
        try {
            world.ledger.add(new ModLedger.Entry(mod, shooter.getUUID(), null, false, ordinary.bonuses(), ordinary.info(), -1));
            world.ledger.install(shooter.getUUID(), mod, weapon, profile.category(), 8, 0, 30, true);
            try {
                var body = target(level);
                try {
                    fire(level, shooter, gun, body, body.getY() + .65, 3);
                    require(body.getHealth() < body.getMaxHealth() && buffs.counters.isEmpty(), "Torso damage cannot activate the scope");
                } finally { body.discard(); }
                var head = target(level);
                try {
                    fire(level, shooter, gun, head, head.getEyeY(), 3);
                    require(head.getHealth() < head.getMaxHealth() && buffs.counters.size() == 1
                            && buffs.counters.keySet().iterator().next().weapon().equals(weapon), "Native laser head hit activates Argon Scope on the firing weapon");
                } finally { head.discard(); }
                buffs.counters.clear();
                var graze = target(level);
                try {
                    fire(level, shooter, gun, graze, graze.getBoundingBox().maxY + .12, 4);
                    require(graze.getHealth() < graze.getMaxHealth() && buffs.counters.isEmpty(), "The collision margin alone is insufficient for a weak point hit");
                } finally { graze.discard(); }
            } finally {
                buffs.counters.clear();
                world.ledger.uninstall(shooter.getUUID(), mod, weapon, false);
                world.ledger.withdraw(shooter.getUUID(), mod);
            }
            world.ledger.add(new ModLedger.Entry(mod, shooter.getUUID(), null, false, galvanized.bonuses(), galvanized.info(), -1));
            world.ledger.install(shooter.getUUID(), mod, weapon, profile.category(), 8, 0, 30, true);
            try {
                var blocked = target(level);
                var wall = BlockPos.containing(blocked.getEyePosition().add(0, 0, -2));
                var original = level.getBlockState(wall);
                try {
                    level.setBlockAndUpdate(wall, Blocks.OBSIDIAN.defaultBlockState());
                    fire(level, shooter, gun, blocked, blocked.getEyeY(), 3);
                    require(blocked.getHealth() == blocked.getMaxHealth() && buffs.counters.isEmpty(), "A wall blocks laser weak point credit");
                } finally {
                    level.setBlockAndUpdate(wall, original);
                    blocked.discard();
                }
                var canceled = target(level);
                Consumer<ProjectileImpactEvent> cancel = event -> {
                    if (event.getProjectile().getOwner() == shooter) event.setCanceled(true);
                };
                NeoForge.EVENT_BUS.addListener(cancel);
                try {
                    fire(level, shooter, gun, canceled, canceled.getEyeY(), 3);
                    require(canceled.getHealth() == canceled.getMaxHealth() && buffs.counters.isEmpty(), "A canceled laser impact grants no buff");
                } finally {
                    NeoForge.EVENT_BUS.unregister(cancel);
                    canceled.discard();
                }
                var lethal = target(level);
                lethal.setHealth(1);
                try {
                    fire(level, shooter, gun, lethal, lethal.getEyeY(), 4);
                    require(lethal.isDeadOrDying(), "The native laser kills the head-shot target");
                    require(buffs.counters.keySet().stream().allMatch(key -> key.weapon().equals(weapon)), "Laser buffs belong to the weapon that fired");
                    require(buffs.counters.keySet().stream().map(key -> key.modifier().condition().trigger()).collect(Collectors.toSet()).equals(
                            Set.of(ConditionalModifier.Trigger.RANGED_WEAK_POINT_HITS, ConditionalModifier.Trigger.RANGED_WEAK_POINT_KILLS)),
                            "A lethal laser head hit activates both galvanized scope buffs");
                } finally { lethal.discard(); }
            } finally {
                buffs.counters.clear();
                world.ledger.uninstall(shooter.getUUID(), mod, weapon, false);
                world.ledger.withdraw(shooter.getUUID(), mod);
            }
            return 8;
        } finally {
            world.bonus(shooter.getUUID(), Stats.FIRE_RATE, Bonus.NONE);
            world.bonus(shooter.getUUID(), Stats.STATUS_CHANCE, Bonus.NONE);
            shooter.discard();
        }
    }

    private static Zombie target(ServerLevel level) {
        var target = Objects.requireNonNull(EntityType.ZOMBIE.create(level));
        target.setNoAi(true);
        target.setNoGravity(true);
        target.setPos(8.5, 220, 8.5);
        require(level.addFreshEntity(target), "The laser target entered the level");
        return target;
    }

    private static void fire(ServerLevel level, ServerPlayer shooter, ItemStack gun, Zombie target, double height, double distance) {
        var beams = new ArrayList<Projectile>();
        Consumer<EntityJoinLevelEvent> track = event -> {
            if (event.getEntity() instanceof Projectile beam && beam.getOwner() == shooter) beams.add(beam);
        };
        shooter.setPos(target.getX(), height - shooter.getEyeHeight() * .8F, target.getZ() - distance);
        shooter.setYRot(0);
        shooter.setYHeadRot(0);
        shooter.setXRot(0);
        shooter.setItemInHand(InteractionHand.MAIN_HAND, gun);
        NeoForge.EVENT_BUS.addListener(track);
        try {
            shooter.startUsingItem(InteractionHand.MAIN_HAND);
            gun.onUseTick(level, shooter, 20);
            shooter.releaseUsingItem();
            require(beams.size() == 1, "The Gatling fires a native beam");
            var beam = beams.getFirst();
            shooter.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            for (int tick = 0; tick < 24 && !beam.isRemoved() && !target.isDeadOrDying(); tick++) beam.tick();
        } finally {
            NeoForge.EVENT_BUS.unregister(track);
            beams.forEach(Projectile::discard);
        }
    }

    private static void require(boolean value, String description) {
        if (!value) throw new AssertionError(description);
    }
}
