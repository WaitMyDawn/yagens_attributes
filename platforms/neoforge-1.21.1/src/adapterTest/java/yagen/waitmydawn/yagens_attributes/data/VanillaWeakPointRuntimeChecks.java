package yagen.waitmydawn.yagens_attributes.data;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import org.joml.Vector3d;
import yagen.waitmydawn.yagens_attributes.api.WeakPointHitEvent;
import yagen.waitmydawn.yagens_attributes.registries.RangedAttachments;
import yagen.waitmydawn.yagens_attributes.setup.RangedShooting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

final class VanillaWeakPointRuntimeChecks {
    private static final Vec3 ORIGIN = new Vec3(8.5, 220, 8.5);
    private int checks;

    private VanillaWeakPointRuntimeChecks() {}

    static int run(ServerLevel level) {
        var suite = new VanillaWeakPointRuntimeChecks();
        level.getChunk(0, 0);
        suite.fixed(level);
        suite.poses(level);
        suite.shulker(level);
        suite.dragon(level);
        return suite.checks;
    }

    private void fixed(ServerLevel level) {
        check(WeakPointDefinitions.INSTANCE.regions(EntityType.SLIME).isEmpty(), "Slime has no weak point");
        check(WeakPointDefinitions.INSTANCE.regions(EntityType.MAGMA_CUBE).isEmpty(), "Magma cube has no weak point");
        for (var type : List.of(EntityType.HORSE, EntityType.SKELETON_HORSE, EntityType.ZOMBIE_HORSE))
            check(front(mob(level, type), 0, .93, "head"), "Horse neck intercept is inside the native collider for " + type);
        for (var type : List.of(EntityType.GUARDIAN, EntityType.ELDER_GUARDIAN)) {
            var guardian = mob(level, type);
            check(front(guardian, 0, .52, "eye"), "Guardian's eye can be hit");
            check(!front(guardian, .34, .52, "eye"), "Guardian's cheek is outside the eye");
        }
        var wither = mob(level, EntityType.WITHER);
        check(front(wither, 0, .85, "head"), "Wither center head is a weak point");
        check(!front(wither, 1.3, .77, "head"), "Wither side heads are outside the center head");
        check(front(mob(level, EntityType.STRIDER), 0, .70, "head"), "Strider head is a weak point");
        for (var type : List.of(EntityType.SQUID, EntityType.GLOW_SQUID)) {
            var squid = mob(level, type);
            check(front(squid, -.25, .45, "left_eye") && front(squid, .25, .45, "right_eye"), "Squid has two separate eyes");
            check(!front(squid, 0, .45, "left_eye"), "The middle of the squid's face is outside its eyes");
            squid.xBodyRot = squid.xBodyRotO = -30;
            var region = regions(squid).getFirst();
            var eye = WeakPointGeometry.box(squid, region, 1);
            var local = eye.bounds().getCenter();
            var transformed = eye.frame().transform(new Vector3d(local.x, local.y, local.z));
            var forward = eye.frame().transform(new Vector3d(0, 0, 3));
            var center = squid.position().add(eye.offset()).add(transformed.x, transformed.y, transformed.z);
            var delta = new Vec3(forward.x, forward.y, forward.z);
            check(region.id().equals(match(squid, center.add(delta), center.subtract(delta))), "Squid eyes follow a swimming pose");
            check(match(squid, center.subtract(delta), center.add(delta)) == null, "A back shot does not reach a squid eye");
        }
    }

    private void poses(ServerLevel level) {
        var goat = mob(level, EntityType.GOAT);
        check(active(goat).equals(List.of("head_up")), "Goat starts with its head raised");
        level.broadcastEntityEvent(goat, (byte) 58);
        for (int i = 0; i < 20; i++) goat.aiStep();
        check(active(goat).equals(List.of("head_down")), "Native ram packet lowers the server weak point");
        level.broadcastEntityEvent(goat, (byte) 59);
        for (int i = 0; i < 10; i++) goat.aiStep();
        check(active(goat).equals(List.of("head_up")), "Finishing a ram restores the raised head");

        var bat = mob(level, EntityType.BAT);
        bat.setResting(false);
        check(front(bat, 0, .65, "flying_head"), "Flying bat has a higher head");
        bat.setResting(true);
        check(front(bat, 0, .41, "hanging_head") && match(bat, ORIGIN.add(0, .59, 3), ORIGIN.add(0, .59, -3)) == null,
                "Hanging bat exposes the lower head only");

        var fish = mob(level, EntityType.PUFFERFISH);
        var ids = List.of("face_small", "face_medium", "face_full");
        for (int state = 0; state < ids.size(); state++) {
            fish.setPuffState(state);
            check(active(fish).equals(List.of(ids.get(state))) && front(fish, 0, .55, ids.get(state)), "Pufferfish uses only its current size");
        }
        var armadillo = mob(level, EntityType.ARMADILLO);
        check(front(armadillo, 0, .38, "exposed_head"), "Uncurled armadillo exposes its head");
        armadillo.switchToState(Armadillo.ArmadilloState.SCARED);
        check(active(armadillo).isEmpty(), "Armadillo hides its head in the shell");
    }

    private void shulker(ServerLevel level) {
        var shulker = mob(level, EntityType.SHULKER);
        var supports = List.of(shulker.blockPosition().below(), shulker.blockPosition().west());
        var before = supports.stream().map(level::getBlockState).toList();
        supports.forEach(position -> level.setBlockAndUpdate(position, Blocks.STONE.defaultBlockState()));
        try {
            check(active(shulker).isEmpty(), "Closed shulker has no exposed head");
            peek(shulker, Direction.DOWN, 100);
            var center = shulker.position().add(0, .63, 0);
            check("exposed_head".equals(match(shulker, center.add(0, 0, 3), center.add(0, 0, -3))),
                    "Shot reaches the shulker's head through its open gap. Peek " + shulker.getClientPeekAmount(1)
                            + ", face " + shulker.getAttachFace() + ", active " + active(shulker) + ", collider " + shulker.getBoundingBox());
            check(match(shulker, center.add(0, 3, 0), center.add(0, -3, 0)) == null, "The raised lid can still obstruct a shot");
            check(match(shulker, center.add(0, -3, 0), center.add(0, 3, 0)) == null, "The base covers the head from below");
            check(match(shulker, center.add(.4, 0, 3), center.add(.4, 0, -3)) == null, "The gap beside the head does not count");
            peek(shulker, Direction.WEST, 100);
            center = shulker.position().add(.13, .5, 0);
            check("exposed_head".equals(match(shulker, center.add(0, 0, 3), center.add(0, 0, -3))), "The exposed gap rotates for wall-mounted shulkers");
            peek(shulker, Direction.WEST, 0);
            check(active(shulker).isEmpty(), "Closing covers a wall-mounted shulker's head again");
        } finally {
            for (int i = 0; i < supports.size(); i++) level.setBlockAndUpdate(supports.get(i), before.get(i));
            shulker.discard();
        }
    }

    private void dragon(ServerLevel level) {
        var dragon = mob(level, EntityType.ENDER_DRAGON);
        dragon.setSilent(true);
        var shooter = FakePlayerFactory.get(level, new GameProfile(UUID.fromString("9c843ddf-c029-4f1e-ab5e-bf44946dab88"), "[DragonPoint]"));
        shooter.setPos(ORIGIN.add(0, 0, -5));
        var events = new ArrayList<WeakPointHitEvent>();
        Consumer<WeakPointHitEvent> listener = event -> {
            if (event.target() == dragon) events.add(event);
        };
        dragon.head.setPos(ORIGIN.add(0, 1, 0));
        var neck = Arrays.stream(dragon.getParts()).filter(part -> part instanceof EnderDragonPart nativePart && nativePart.name.equals("neck"))
                .findFirst().orElseThrow();
        neck.setPos(ORIGIN.add(0, 1, 3));
        for (var part : dragon.getParts()) if (part != dragon.head && part != neck) part.setPos(ORIGIN.add(0, 1, 8));
        check(level.addFreshEntity(dragon), "Native multipart dragon enters the server");
        NeoForge.EVENT_BUS.addListener(listener);
        try {
            float health = dragon.getHealth();
            shoot(level, shooter, neck.position().add(0, .5, 2.25), new Vec3(0, 0, -3));
            check(health > dragon.getHealth() && events.isEmpty(), "A native arrow can hurt the dragon's neck without triggering weak point effects");
            dragon.invulnerableTime = 0;
            health = dragon.getHealth();
            shoot(level, shooter, dragon.head.position().add(0, .5, -2), new Vec3(0, 0, 3));
            check(health > dragon.getHealth() && events.size() == 1 && events.getFirst().region().equals("head"),
                    "The actual dragon head forwards a confirmed weapon hit and weak point event");
        } finally {
            NeoForge.EVENT_BUS.unregister(listener);
            dragon.discard();
        }
    }

    private static void shoot(ServerLevel level, ServerPlayer shooter, Vec3 start, Vec3 motion) {
        var bow = new ItemStack(Items.BOW);
        var arrow = new Arrow(level, shooter, new ItemStack(Items.ARROW), bow);
        arrow.setData(RangedAttachments.SHOT, Objects.requireNonNull(RangedShooting.snapshot(shooter, bow)));
        arrow.setPos(start);
        arrow.setDeltaMovement(motion);
        arrow.tick();
        arrow.discard();
    }

    private static void peek(Shulker shulker, Direction face, int amount) {
        var state = new CompoundTag();
        state.putByte("AttachFace", (byte) face.get3DDataValue());
        state.putByte("Peek", (byte) amount);
        state.putBoolean("NoAI", true);
        shulker.readAdditionalSaveData(state);
        for (int i = 0; i < 21; i++) shulker.tick();
    }

    private static <T extends Mob> T mob(ServerLevel level, EntityType<T> type) {
        var mob = Objects.requireNonNull(type.create(level));
        mob.setNoAi(true);
        mob.setNoGravity(true);
        mob.setPos(ORIGIN);
        return mob;
    }

    private static boolean front(LivingEntity entity, double horizontal, double height, String id) {
        var center = entity.position().add(horizontal * entity.getBbWidth(), height * entity.getBbHeight(), 0);
        return id.equals(match(entity, center.add(0, 0, 4), center.add(0, 0, -4)));
    }

    private static List<yagen.waitmydawn.yagens_attributes.core.WeakPointRegion> regions(LivingEntity entity) {
        return WeakPointDefinitions.INSTANCE.regions(entity.getType());
    }

    private static String match(LivingEntity entity, Vec3 start, Vec3 end) {
        var match = WeakPointGeometry.match(entity, start, end, regions(entity));
        return match == null ? null : match.region();
    }

    private static List<String> active(LivingEntity entity) {
        return regions(entity).stream().filter(region -> WeakPointGeometry.box(entity, region, 1) != null)
                .map(yagen.waitmydawn.yagens_attributes.core.WeakPointRegion::id).toList();
    }

    private void check(boolean value, String reason) {
        checks++;
        if (!value) throw new AssertionError(reason);
    }
}
