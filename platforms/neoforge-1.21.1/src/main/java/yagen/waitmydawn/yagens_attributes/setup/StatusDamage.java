package yagen.waitmydawn.yagens_attributes.setup;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.core.DamageType;
import java.util.*;

/** Typed secondary damage cannot re-enter weapon resolution or generate random status procs. */
public final class StatusDamage extends DamageSource {
    private static final Map<DamageType, ResourceKey<net.minecraft.world.damagesource.DamageType>> KEYS = new EnumMap<>(DamageType.class);
    private static final Map<ResourceKey<net.minecraft.world.damagesource.DamageType>, DamageType> TYPES = new HashMap<>();
    static { for (var type : DamageType.values()) {
        var key = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, type.key() + "_status"));
        KEYS.put(type, key); TYPES.put(key, type);
    } }
    public final DamageType type;
    public final int criticalTier;
    public final UUID weapon;
    private final double[] armorPortions;
    private StatusDamage(ServerLevel level, DamageType type, UUID owner, double[] armorPortions, int criticalTier, UUID weapon) {
        super(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(KEYS.get(type)), null, owner(level, owner));
        this.type = type; this.armorPortions = armorPortions; this.criticalTier = criticalTier;
        this.weapon = weapon;
    }
    @Override public ItemStack getWeaponItem() { return null; }
    public int portionCount() { return armorPortions.length; }
    public double portion(int index) { return armorPortions[index]; }
    @Override public Component getLocalizedDeathMessage(LivingEntity target) {
        String key = "death.attack." + getMsgId();
        return getEntity() == null ? Component.translatable(key, target.getDisplayName())
                : Component.translatable(key + ".player", target.getDisplayName(), getEntity().getDisplayName());
    }
    public static DamageType damageType(DamageSource source) { return source instanceof StatusDamage damage ? damage.type : TYPES.get(source.typeHolder().unwrapKey().orElse(null)); }
    public static boolean isSecondary(DamageSource source) { return damageType(source) != null; }
    public static net.minecraft.world.entity.Entity owner(ServerLevel level, UUID id) {
        var entity = level.getEntity(id); return entity == null ? level.getServer().getPlayerList().getPlayer(id) : entity;
    }
    public static boolean bypassesShields(DamageSource source) { return damageType(source) == DamageType.TOXIN; }
    public static void hurt(LivingEntity target, DamageType type, UUID owner, double amount) {
        hurt(target, type, owner, amount, new double[0], 0, null);
    }
    public static void hurtBatch(LivingEntity target, DamageType type, UUID owner, List<Double> amounts) {
        hurtBatch(target, type, owner, amounts, 0);
    }
    public static void hurtBatch(LivingEntity target, DamageType type, UUID owner, List<Double> amounts, int criticalTier) {
        hurtBatch(target, type, owner, amounts, criticalTier, null);
    }
    public static void hurtBatch(LivingEntity target, DamageType type, UUID owner, List<Double> amounts, int criticalTier, UUID weapon) {
        for (double amount : amounts) if (!Double.isFinite(amount) || amount < 0) throw new IllegalArgumentException("Invalid status damage portion");
        double total = amounts.stream().mapToDouble(Double::doubleValue).sum();
        double[] portions = type == DamageType.HEAT || amounts.size() < 2 ? new double[0] : amounts.stream().mapToDouble(Double::doubleValue).toArray();
        hurt(target, type, owner, total, portions, criticalTier, weapon);
    }
    private static void hurt(LivingEntity target, DamageType type, UUID owner, double amount, double[] portions, int criticalTier, UUID weapon) {
        if (!Double.isFinite(amount) || amount < 0 || criticalTier < 0 || criticalTier > 100) throw new IllegalArgumentException("Invalid status damage");
        if (!target.isAlive() || amount <= 0 || !(target.level() instanceof ServerLevel level)) return;
        var attacker = owner(level, owner);
        if (target instanceof net.minecraft.world.entity.player.Player player && !target.getUUID().equals(owner)) {
            if (attacker == null) return;
            if (attacker instanceof net.minecraft.world.entity.player.Player source
                    && (!level.getServer().isPvpAllowed() || !source.canHarmPlayer(player))) return;
        }
        target.hurt(new StatusDamage(level, type, owner, portions, criticalTier, weapon), (float) Math.min(Float.MAX_VALUE, amount));
    }
}
