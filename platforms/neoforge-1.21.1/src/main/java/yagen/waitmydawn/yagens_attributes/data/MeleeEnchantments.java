package yagen.waitmydawn.yagens_attributes.data;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.effects.AddValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class MeleeEnchantments {
    public enum Kind {
        SHARPNESS(Enchantments.SHARPNESS), SMITE(Enchantments.SMITE), BANE_OF_ARTHROPODS(Enchantments.BANE_OF_ARTHROPODS);

        private final ResourceKey<Enchantment> key;
        Kind(ResourceKey<Enchantment> key) { this.key = key; }
        public ResourceKey<Enchantment> key() { return key; }
    }

    private static final Set<ResourceKey<Enchantment>> ADDOUT = Set.of(
            Enchantments.SHARPNESS, Enchantments.SMITE, Enchantments.BANE_OF_ARTHROPODS);
    private static final Kind[] DISPLAY_ORDER = Kind.values();
    private static final ThreadLocal<Measurement> MEASURING = new ThreadLocal<>();
    private static final ThreadLocal<Impact> IMPACT = new ThreadLocal<>();

    public record Preview(Kind kind, double amount) {}
    public record Bonus(Entity target, DamageSource source, double amount) {}
    private record Impact(Entity target, DamageSource source, double nativeDamage, double bonus) {}

    private static final class Measurement {
        final Entity target;
        final DamageSource source;
        double bonus;

        Measurement(Entity target, DamageSource source) {
            this.target = target;
            this.source = source;
        }
    }

    private MeleeEnchantments() {}

    public static boolean present(ItemStack stack) {
        return stack.getEnchantments().keySet().stream()
                .anyMatch(holder -> holder.unwrapKey().filter(ADDOUT::contains).isPresent());
    }

    public static List<Preview> preview(ItemStack stack) {
        var enchantments = stack.getEnchantments();
        if (enchantments.isEmpty()) return List.of();
        var result = new ArrayList<Preview>();
        for (var kind : DISPLAY_ORDER) for (var entry : enchantments.entrySet()) {
            if (!entry.getKey().is(kind.key())) continue;
            double amount = 0;
            for (var effect : entry.getKey().value().getEffects(EnchantmentEffectComponents.DAMAGE)) {
                if (effect.effect() instanceof AddValue add) amount += add.value().calculate(entry.getIntValue());
            }
            if (Double.isFinite(amount) && amount > 0) result.add(new Preview(kind, amount));
        }
        return List.copyOf(result);
    }

    public static float measure(Entity target, DamageSource source, Supplier<Float> original, Consumer<Bonus> result) {
        var previous = MEASURING.get();
        var measurement = new Measurement(target, source);
        MEASURING.set(measurement);
        try {
            float damage = original.get();
            if (Double.isFinite(measurement.bonus) && measurement.bonus > 0)
                result.accept(new Bonus(target, source, measurement.bonus));
            return damage;
        } finally {
            if (previous == null) MEASURING.remove();
            else MEASURING.set(previous);
        }
    }

    public static boolean measures(ServerLevel level, Enchantment enchantment, Entity target, DamageSource source) {
        var measurement = MEASURING.get();
        return measurement != null && measurement.target == target && measurement.source == source
                && level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getResourceKey(enchantment).filter(ADDOUT::contains).isPresent();
    }

    public static void record(double amount) {
        MEASURING.get().bonus += amount;
    }

    public static boolean duringImpact(Bonus bonus, Entity target, DamageSource source, float amount, float strength, BooleanSupplier original) {
        if (bonus == null || bonus.target() != target || bonus.source() != source || !(amount > 0) || !(strength > 0))
            return original.getAsBoolean();
        var receiver = WeakPointGeometry.receiver(target);
        if (receiver == null) return original.getAsBoolean();
        var previous = IMPACT.get();
        IMPACT.set(new Impact(receiver, source, amount, bonus.amount() * strength));
        try {
            return original.getAsBoolean();
        } finally {
            if (previous == null) IMPACT.remove();
            else IMPACT.set(previous);
        }
    }

    public static double take(Entity target, DamageSource source, double damage) {
        var impact = IMPACT.get();
        if (impact == null || impact.target() != target || impact.source() != source) return 0;
        IMPACT.remove();
        // Native target scaling and earlier incoming-damage listeners affect the delivered hit proportionally.
        return Math.min(damage, impact.bonus() * (damage / impact.nativeDamage()));
    }
}
