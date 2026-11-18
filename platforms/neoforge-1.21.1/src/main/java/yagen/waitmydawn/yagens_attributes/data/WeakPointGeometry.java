package yagen.waitmydawn.yagens_attributes.data;

import net.minecraft.world.entity.Entity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import yagen.waitmydawn.yagens_attributes.core.WeakPointRegion;

import java.util.List;

public final class WeakPointGeometry {
    public record Match(String region, Vec3 location) {}

    public record Box(Vec3 offset, float yaw, AABB bounds, Quaternionf frame) {
        private Vec3 local(Vec3 relative) {
            Vec3 position = relative.subtract(offset);
            if (frame != null) {
                var vector = frame.transformInverse(new Vector3d(position.x, position.y, position.z));
                position = new Vec3(vector.x, vector.y, vector.z);
            }
            return position.yRot(yaw * Mth.DEG_TO_RAD);
        }

        private Vec3 world(Vec3 local, Vec3 origin) {
            Vec3 position = local.yRot(-yaw * Mth.DEG_TO_RAD);
            if (frame != null) {
                var vector = frame.transform(new Vector3d(position.x, position.y, position.z));
                position = new Vec3(vector.x, vector.y, vector.z);
            }
            return position.add(offset).add(origin);
        }
    }

    private static final double EDGE = 1e-7;

    private WeakPointGeometry() {}

    public static LivingEntity receiver(Entity hit) {
        if (hit instanceof LivingEntity living) return living;
        return hit instanceof EnderDragonPart part ? part.parentMob : null;
    }

    private static Entity collider(LivingEntity entity, WeakPointRegion.Part part) {
        return switch (part) {
            case BODY -> entity;
            case DRAGON_HEAD -> entity instanceof EnderDragon dragon ? dragon.head : null;
        };
    }

    private static boolean active(LivingEntity entity, WeakPointRegion.State state) {
        return switch (state) {
            case ALWAYS -> true;
            case GOAT_HEAD_UP, GOAT_HEAD_DOWN -> entity instanceof Goat goat
                    && (goat.getRammingXHeadRot() >= 15 * Mth.DEG_TO_RAD) == (state == WeakPointRegion.State.GOAT_HEAD_DOWN);
            case PUFFERFISH_SMALL -> entity instanceof Pufferfish fish && fish.getPuffState() == 0;
            case PUFFERFISH_MEDIUM -> entity instanceof Pufferfish fish && fish.getPuffState() == 1;
            case PUFFERFISH_FULL -> entity instanceof Pufferfish fish && fish.getPuffState() == 2;
            case SHULKER_EXPOSED -> entity instanceof Shulker;
            case ARMADILLO_EXPOSED -> entity instanceof Armadillo armadillo && !armadillo.shouldHideInShell();
            case BAT_FLYING, BAT_HANGING -> entity instanceof Bat bat
                    && bat.isResting() == (state == WeakPointRegion.State.BAT_HANGING);
            case SQUID_MODEL -> entity instanceof Squid;
        };
    }

    public static Box box(LivingEntity entity, WeakPointRegion region, float partialTick) {
        Entity anchor = collider(entity, region.part());
        if (anchor == null || !active(entity, region.state())) return null;
        if (region.state() == WeakPointRegion.State.SHULKER_EXPOSED) return shulkerBox((Shulker) entity, region, partialTick);
        if (region.state() == WeakPointRegion.State.SQUID_MODEL) return squidBox((Squid) entity, region, partialTick);
        AABB collider = anchor.getBoundingBox();
        Vec3 position = entity.position();
        double y = collider.minY - position.y;
        if (region.anchor() == WeakPointRegion.Anchor.EYES) y = entity.getEyeHeight();
        var offset = new Vec3((collider.minX + collider.maxX) * 0.5 - position.x,
                y, (collider.minZ + collider.maxZ) * 0.5 - position.z);
        if (anchor != entity) {
            Vec3 motion = interpolated(anchor, partialTick).subtract(anchor.position())
                    .subtract(interpolated(entity, partialTick).subtract(position));
            offset = offset.add(motion);
        }
        float yaw = switch (region.rotation()) {
            case NONE -> 0;
            case BODY -> Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
            case HEAD -> Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);
        };
        return new Box(offset, yaw, scaled(region, collider.getXsize(), collider.getYsize(), collider.getZsize()), null);
    }

    private static Vec3 interpolated(Entity entity, float partialTick) {
        return new Vec3(Mth.lerp(partialTick, entity.xOld, entity.getX()), Mth.lerp(partialTick, entity.yOld, entity.getY()),
                Mth.lerp(partialTick, entity.zOld, entity.getZ()));
    }

    private static AABB scaled(WeakPointRegion region, double width, double height, double depth) {
        var min = region.min();
        var max = region.max();
        return new AABB(min.x() * width, min.y() * height, min.z() * depth, max.x() * width, max.y() * height, max.z() * depth);
    }

    private static double lidFloor(Shulker shulker, float partialTick) {
        double peek = shulker.getClientPeekAmount(partialTick);
        return (.75 - .5 * Math.cos(Math.PI * peek)) * shulker.getBbWidth();
    }

    private static Box shulkerBox(Shulker shulker, WeakPointRegion region, float partialTick) {
        var up = shulker.getAttachFace().getOpposite();
        double width = shulker.getBbWidth();
        var bounds = scaled(region, width, width, width);
        double visibleTop = Math.min(bounds.maxY, lidFloor(shulker, partialTick));
        if (visibleTop <= bounds.minY + EDGE) return null;
        var offset = new Vec3(0, width / 2, 0).subtract(Vec3.atLowerCornerOf(up.getNormal()).scale(width / 2));
        return new Box(offset, 0, new AABB(bounds.minX, bounds.minY, bounds.minZ, bounds.maxX, visibleTop, bounds.maxZ), up.getRotation());
    }

    private static Box squidBox(Squid squid, WeakPointRegion region, float partialTick) {
        // SquidRenderer tilts and rolls around a pivot half a block above its feet.
        float yaw = Mth.rotLerp(partialTick, squid.yBodyRotO, squid.yBodyRot);
        float pitch = Mth.lerp(partialTick, squid.xBodyRotO, squid.xBodyRot);
        float roll = Mth.lerp(partialTick, squid.zBodyRotO, squid.zBodyRot);
        var rotation = new Quaternionf().rotationY(-yaw * Mth.DEG_TO_RAD).rotateX(-pitch * Mth.DEG_TO_RAD)
                .rotateY(roll * Mth.DEG_TO_RAD);
        double pivot = squid.getScale() * .5;
        var moved = rotation.transform(new Vector3d(0, pivot, 0));
        var bounds = squid.getBoundingBox();
        return new Box(new Vec3(-moved.x, pivot - moved.y, -moved.z), 0,
                scaled(region, bounds.getXsize(), bounds.getYsize(), bounds.getZsize()), rotation);
    }

    public static Match match(LivingEntity target, Vec3 start, Vec3 end, List<WeakPointRegion> regions) {
        return match(target, target, start, end, regions);
    }

    public static Match match(Entity hit, LivingEntity target, Vec3 start, Vec3 end, List<WeakPointRegion> regions) {
        if (regions.isEmpty()) return null;
        Vec3 entry = contact(hit.getBoundingBox().inflate(EDGE), start, end);
        if (entry == null) return null;
        Vec3 origin = target.position();
        Vec3 relativeEntry = entry.subtract(origin);
        // A projectile stops at the first contact. A region on the opposite face must not count.
        for (var region : regions) {
            if (collider(target, region.part()) != hit) continue;
            var box = box(target, region, 1);
            if (box == null) continue;
            if (region.state() == WeakPointRegion.State.SHULKER_EXPOSED || region.state() == WeakPointRegion.State.SQUID_MODEL) {
                var localStart = box.local(start.subtract(origin));
                var localEnd = box.local(end.subtract(origin));
                Vec3 contact = contact(box.bounds().inflate(EDGE), localStart, localEnd);
                if (contact == null) continue;
                if (target instanceof Shulker shulker && shellObstructs(shulker, localStart, contact)) continue;
                if (target instanceof Squid && (localStart.z < box.bounds().minZ || localEnd.z >= localStart.z)) continue;
                return new Match(region.id(), box.world(contact, origin));
            }
            if (box.bounds().inflate(EDGE * 2).contains(box.local(relativeEntry))) return new Match(region.id(), entry);
        }
        return null;
    }

    private static boolean shellObstructs(Shulker shulker, Vec3 start, Vec3 head) {
        double width = shulker.getBbWidth(), half = width / 2;
        double lid = lidFloor(shulker, 1);
        var base = new AABB(-half, 0, -half, half, half, half);
        var top = new AABB(-half, lid, -half, half, lid + .75 * width, half);
        return contact(base, start, head) != null || contact(top, start, head) != null;
    }

    private static Vec3 contact(AABB box, Vec3 start, Vec3 end) {
        return box.contains(start) ? start : box.clip(start, end).orElse(null);
    }
}
