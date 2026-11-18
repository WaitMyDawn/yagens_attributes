package yagen.waitmydawn.yagens_attributes.data;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import yagen.waitmydawn.yagens_attributes.api.WeakPointHitEvent;
import yagen.waitmydawn.yagens_attributes.core.AttackKind;
import yagen.waitmydawn.yagens_attributes.registries.Components;
import yagen.waitmydawn.yagens_attributes.registries.RangedAttachments;
import yagen.waitmydawn.yagens_attributes.setup.RangedShooting;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

final class WeakPointRuntimeChecks {
    private WeakPointRuntimeChecks() {}

    static int run(ServerLevel level) {
        var definitions = WeakPointDefinitions.INSTANCE;
        var humanoid = definitions.regions(EntityType.ZOMBIE);
        require(humanoid.size() == 1 && humanoid.getFirst().id().equals("head"), "Zombie inherits humanoid head");
        require(humanoid.equals(definitions.regions(EntityType.ZOMBIFIED_PIGLIN)), "Zombified piglin inherits the same region");
        var villager = definitions.regions(EntityType.VILLAGER);
        require(villager.size() == 1 && !humanoid.equals(villager), "Exact villager head replaces inherited head");
        require(definitions.regions(EntityType.SLIME).isEmpty(), "An unconfigured entity has no regions");

        level.getChunk(0, 0);
        var shooter = FakePlayerFactory.get(level, new GameProfile(UUID.fromString("2bf56de5-e3c2-4a32-b381-9431e342d189"), "[PointChecks]"));
        shooter.setPos(8.5, 220, 3.5);
        var bow = new ItemStack(Items.BOW);
        var target = spawn(level, EntityType.ZOMBIE);
        var hits = new ArrayList<WeakPointHitEvent>();
        Consumer<WeakPointHitEvent> listener = event -> {
            if (event.target() == target) hits.add(event);
        };
        Consumer<ProjectileImpactEvent> cancel = event -> {
            if (event.getProjectile().getOwner() == shooter) event.setCanceled(true);
        };
        NeoForge.EVENT_BUS.addListener(listener);
        try {
            var head = target.getEyePosition();
            var arrow = new Arrow(level, shooter, new ItemStack(Items.ARROW), bow);
            launch(arrow, shooter, bow, head.add(0, 0, -2), new Vec3(0, 0, 3));
            require(hits.size() == 1, "Native bow head contact emits one confirmed event");
            var hit = hits.getFirst();
            require(hit.attacker() == shooter && hit.source().getDirectEntity() == arrow && hit.region().equals("head")
                    && hit.weapon().equals(bow.get(Components.WEAPON_ID.get())) && hit.attackKind() == AttackKind.RANGED
                    && hit.damage() > 0 && Math.abs(hit.location().z - target.getBoundingBox().minZ) < 1e-5,
                    "Weak point event preserves native attribution and first-surface contact");

            reset(target);
            var crossbow = new ItemStack(Items.CROSSBOW);
            var torso = target.position().add(0, 0.6, -2);
            launch(new Arrow(level, shooter, new ItemStack(Items.ARROW), crossbow), shooter, crossbow, torso, new Vec3(0, 0, 3));
            require(target.getHealth() < target.getMaxHealth() && hits.size() == 1, "Crossbow torso damage is ordinary damage");

            reset(target);
            var trident = new ItemStack(Items.TRIDENT);
            launch(new ThrownTrident(level, shooter, trident), shooter, trident, head.add(0, 0, -2), new Vec3(0, 0, 3));
            require(hits.size() == 2 && hits.getLast().weapon().equals(trident.get(Components.WEAPON_ID.get())),
                    "Thrown trident follows the native weak-point path");

            reset(target);
            var wall = BlockPos.containing(head.add(0, 0, -1));
            var original = level.getBlockState(wall);
            try {
                level.setBlockAndUpdate(wall, Blocks.STONE.defaultBlockState());
                launch(new Arrow(level, shooter, new ItemStack(Items.ARROW), bow), shooter, bow, head.add(0, 0, -2), new Vec3(0, 0, 3));
                require(target.getHealth() == target.getMaxHealth() && hits.size() == 2, "Block obstruction prevents a weak point hit");
            } finally {
                level.setBlockAndUpdate(wall, original);
            }

            NeoForge.EVENT_BUS.addListener(cancel);
            try {
                launch(new Arrow(level, shooter, new ItemStack(Items.ARROW), bow), shooter, bow, head.add(0, 0, -2), new Vec3(0, 0, 3));
                require(target.getHealth() == target.getMaxHealth() && hits.size() == 2, "Cancelled native projectile impact emits no event");
            } finally {
                NeoForge.EVENT_BUS.unregister(cancel);
            }
        } finally {
            NeoForge.EVENT_BUS.unregister(listener);
            target.discard();
        }

        var ghast = spawn(level, EntityType.GHAST);
        try {
            ghast.yBodyRot = 90;
            var rightEye = ghast.getEyePosition().add(0, 0, ghast.getBbWidth() * .20);
            var regions = definitions.regions(ghast.getType());
            var front = WeakPointGeometry.match(ghast, rightEye.add(-5, 0, 0), rightEye.add(5, 0, 0), regions);
            require(front != null && front.region().equals("right_eye"), "Ghast eye weak points rotate with body yaw");
            require(WeakPointGeometry.match(ghast, rightEye.add(5, 0, 0), rightEye.add(-5, 0, 0), regions) == null,
                    "A rear hit cannot hit the opposite-face eye");
        } finally {
            ghast.discard();
        }
        return 12;
    }

    private static <T extends Mob> T spawn(ServerLevel level, EntityType<T> type) {
        var entity = Objects.requireNonNull(type.create(level));
        entity.setNoAi(true);
        entity.setNoGravity(true);
        entity.setPos(8.5, 220, 8.5);
        require(level.addFreshEntity(entity), "Test target enters the level");
        return entity;
    }

    private static void launch(AbstractArrow arrow, ServerPlayer player, ItemStack weapon, Vec3 position, Vec3 velocity) {
        arrow.setData(RangedAttachments.SHOT, Objects.requireNonNull(RangedShooting.snapshot(player, weapon, AttackKind.RANGED)));
        arrow.setPos(position);
        arrow.setDeltaMovement(velocity);
        arrow.tick();
        arrow.discard();
    }

    private static void reset(LivingEntity entity) {
        entity.setHealth(entity.getMaxHealth());
        entity.invulnerableTime = 0;
    }

    private static void require(boolean value, String description) {
        if (!value) throw new AssertionError(description);
    }
}
