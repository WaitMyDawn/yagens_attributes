package yagen.waitmydawn.yagens_attributes.setup;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.*;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.config.ServerConfigs;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.data.*;
import yagen.waitmydawn.yagens_attributes.registries.*;
import java.util.*;
import java.util.function.IntSupplier;

public final class RangedShooting {
    private RangedShooting() {}
    public static boolean supported(ItemStack weapon) { return weapon.getItem() instanceof ProjectileWeaponItem || weapon.getItem() instanceof TridentItem
            || yagen.waitmydawn.yagens_attributes.compat.NativeWeapons.ranged(weapon); }
    public static boolean handles(LivingEntity shooter, ItemStack weapon) {
        return shooter instanceof ServerPlayer && ServerConfigs.ENABLE_COMBAT.get() && supported(weapon) && CombatData.WEAPONS.weapon(weapon) != null;
    }
    public static ShotData snapshot(LivingEntity shooter, ItemStack weapon) {
        if (!(shooter instanceof ServerPlayer player) || !handles(shooter, weapon)) return null;
        return snapshot(player, weapon, AttackKind.RANGED);
    }
    public static ShotData snapshot(ServerPlayer player, ItemStack weapon, AttackKind kind) {
        if (!ServerConfigs.ENABLE_COMBAT.get()) return null;
        var profile = CombatData.WEAPONS.weapon(weapon); if (profile == null) return null;
        var state = WorldState.get(player.server);
        List<Map<String, Bonus>> sources = new ArrayList<>();
        UUID id = WeaponEligibility.operable(weapon, profile) ? Components.identify(weapon) : null;
        if (id != null) sources.addAll(state.ledger.bonuses(id, net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(weapon.getItem()).toString(), profile.category())); sources.add(state.bonuses(player.getUUID()));
        try {
            if (kind == AttackKind.MELEE && id != null) sources = new ArrayList<>(ComboService.sources(player, id, sources));
            sources = new ArrayList<>(ConditionalModService.sources(player, weapon, sources, null, kind == AttackKind.RANGED));
            var result = new ShotData(profile, sources, 1, false, id, ConditionalModService.targetModifiers(player, weapon),
                    player.getUUID(), id == null ? AttackKind.UNKNOWN : kind); result.multishot(); result.fireRate(); return result;
        } catch (IllegalArgumentException exception) {
            YagensAttributes.LOGGER.warn("Invalid ranged stats for {}: {}", player.getUUID(), exception.getMessage()); return null;
        }
    }
    public static ShotData shot(Entity entity) {
        return entity != null && entity.hasData(RangedAttachments.SHOT.get()) ? entity.getData(RangedAttachments.SHOT.get()) : null;
    }
    public static boolean rangedDamage(DamageSource source) {
        if (!ServerConfigs.ENABLE_COMBAT.get() || !(source.getEntity() instanceof ServerPlayer)) return false;
        var shot = shot(source.getDirectEntity());
        if (shot == null) shot = yagen.waitmydawn.yagens_attributes.compat.NativeAttacks.source(source);
        if (shot != null && shot.profile() != null) return shot.attackKind() == AttackKind.RANGED;
        ItemStack weapon = source.getWeaponItem();
        return source.is(DamageTypeTags.IS_PROJECTILE) && weapon != null && !weapon.isEmpty() && supported(weapon) && CombatData.WEAPONS.weapon(weapon) != null;
    }
    @FunctionalInterface public interface Factory { Projectile create(ItemStack ammo); }
    @FunctionalInterface public interface Aim { void shoot(Projectile projectile); }
    public static boolean shoot(ServerLevel level, LivingEntity shooter, InteractionHand hand, ItemStack weapon, List<ItemStack> ammunition,
                                Factory factory, Aim aim, IntSupplier durability) {
        var snapshot = snapshot(shooter, weapon); if (snapshot == null) return false;
        var volley = RangedStats.volley(snapshot.multishot(), shooter.getRandom()::nextDouble);
        if (volley.count() == 0 || ammunition.stream().noneMatch(stack -> !stack.isEmpty())) return true;
        ItemStack ammo = ammunition.stream().filter(stack -> !stack.isEmpty()).findFirst().orElseThrow();
        Projectile original = factory.create(ammo); aim.shoot(original);
        spawn(level, original, snapshot, volley);
        weapon.hurtAndBreak(durability.getAsInt(), shooter, LivingEntity.getSlotForHand(hand));
        return true;
    }
    public static boolean spawn(ServerLevel level, Projectile original, ShotData snapshot, RangedStats.Volley volley) {
        List<Projectile> candidates = new ArrayList<>(); candidates.add(original);
        if (volley.count() > 1) try {
            // Serialize native projectile state once. Fresh instances retain their own UUID and runtime entity ID.
            CompoundTag template = cloneTemplate(original.saveWithoutId(new CompoundTag()));
            for (int i = 1; i < volley.count(); i++) {
                try {
                    Entity created = original.getType().create(level);
                    if (!(created instanceof Projectile copy)) break;
                    copy.load(template.copy()); copy.setDeltaMovement(original.getDeltaMovement());
                    copySyncedState(original, copy);
                    if (copy instanceof AbstractArrow arrow) arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
                    candidates.add(copy);
                } catch (RuntimeException exception) {
                    YagensAttributes.LOGGER.warn("Cannot copy projectile {} for multishot", original.getType(), exception); break;
                }
            }
        } catch (RuntimeException exception) { YagensAttributes.LOGGER.warn("Cannot serialize projectile {} for multishot", original.getType(), exception); }
        List<Projectile> spawned = new ArrayList<>();
        double planned = snapshot.multishot() / candidates.size();
        for (int i = 0; i < candidates.size(); i++) {
            Projectile projectile = candidates.get(i);
            projectile.setData(RangedAttachments.SHOT.get(), snapshot.projectile(planned, i != 0));
            if (level.addFreshEntity(projectile)) spawned.add(projectile);
        }
        if (!spawned.isEmpty()) {
            double actual = snapshot.multishot() / spawned.size();
            for (Projectile projectile : spawned) {
                var prior = shot(projectile); projectile.setData(RangedAttachments.SHOT.get(), prior.projectile(actual, prior.virtual()));
            }
        }
        return !spawned.isEmpty();
    }
    public static CompoundTag cloneTemplate(CompoundTag tag) { tag.remove("UUID"); tag.remove("Passengers"); return tag; }
    public static void copySyncedState(Entity original, Entity copy) {
        if (original.getType() != copy.getType()) throw new IllegalArgumentException("Projectile copy type mismatch");
        var values = original.getEntityData().getNonDefaultValues();
        if (values == null) return;
        var copied = new ArrayList<net.minecraft.network.syncher.SynchedEntityData.DataValue<?>>();
        for (var value : values) copied.add(copyValue(value));
        copy.getEntityData().assignValues(copied);
    }
    private static <T> net.minecraft.network.syncher.SynchedEntityData.DataValue<T> copyValue(net.minecraft.network.syncher.SynchedEntityData.DataValue<T> value) {
        return new net.minecraft.network.syncher.SynchedEntityData.DataValue<>(value.id(), value.serializer(), value.serializer().copy(value.value()));
    }
}
