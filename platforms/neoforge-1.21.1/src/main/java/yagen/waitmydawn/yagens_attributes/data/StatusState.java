package yagen.waitmydawn.yagens_attributes.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import yagen.waitmydawn.yagens_attributes.core.DamageType;
import yagen.waitmydawn.yagens_attributes.core.AttackKind;
import java.util.*;

/** Authoritative proc layers own their sources and clocks independently of vanilla effects. */
public final class StatusState {
    // Layer may initialize before its enclosing state; primitive codecs must not initialize the outer state codec.
    private static final class LayerCodecs {
        private static final Codec<AttackKind> ATTACK_KIND = Codec.STRING.comapFlatMap(value -> {
            try { return DataResult.success(AttackKind.parse(value)); }
            catch (IllegalArgumentException exception) { return DataResult.error(() -> "Unknown attack kind: " + value); }
        }, AttackKind::key);
        private static final Codec<DamageType> TYPE = Codec.STRING.comapFlatMap(value -> {
            try { return DataResult.success(DamageType.parse(value)); }
            catch (IllegalArgumentException exception) { return DataResult.error(() -> "Unknown status type: " + value); }
        }, DamageType::key);
        private static final Codec<UUID> OWNER = Codec.STRING.comapFlatMap(value -> {
            try { return DataResult.success(UUID.fromString(value)); }
            catch (IllegalArgumentException exception) { return DataResult.error(() -> "Invalid status owner UUID"); }
        }, UUID::toString);
    }
    public record Layer(DamageType type, UUID owner, double base, int remaining, int pulse, int age, int criticalTier, UUID weapon, AttackKind attackKind) {
        public Layer(DamageType type, UUID owner, double base, int remaining, int pulse, int age, int criticalTier, UUID weapon) {
            this(type, owner, base, remaining, pulse, age, criticalTier, weapon, weapon == null ? AttackKind.UNKNOWN : AttackKind.RANGED);
        }
        public Layer(DamageType type, UUID owner, double base, int remaining, int pulse, int age, int criticalTier) { this(type, owner, base, remaining, pulse, age, criticalTier, null); }
        public Layer(DamageType type, UUID owner, double base, int remaining, int pulse, int age) { this(type, owner, base, remaining, pulse, age, 0); }
        public Layer {
            Objects.requireNonNull(type); Objects.requireNonNull(owner);
            Objects.requireNonNull(attackKind).validateWeapon(weapon);
            if (!Double.isFinite(base) || base < 0 || base > Float.MAX_VALUE || remaining < 1 || remaining > 144000 || pulse < 0 || pulse > 20 || age < 0 || age > 144000 || criticalTier < 0 || criticalTier > 100)
                throw new IllegalArgumentException("Invalid status layer");
        }
        public static final Codec<Layer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                LayerCodecs.TYPE.fieldOf("type").forGetter(Layer::type),
                LayerCodecs.OWNER.fieldOf("owner").forGetter(Layer::owner),
                Codec.doubleRange(0, Float.MAX_VALUE).fieldOf("base").forGetter(Layer::base),
                Codec.intRange(1, 144000).fieldOf("remaining").forGetter(Layer::remaining),
                Codec.intRange(0, 20).fieldOf("pulse").forGetter(Layer::pulse),
                Codec.intRange(0, 144000).fieldOf("age").forGetter(Layer::age),
                Codec.intRange(0, 100).optionalFieldOf("critical_tier", 0).forGetter(Layer::criticalTier),
                LayerCodecs.OWNER.optionalFieldOf("weapon").forGetter(layer -> Optional.ofNullable(layer.weapon())),
                LayerCodecs.ATTACK_KIND.optionalFieldOf("attack_kind").forGetter(layer -> Optional.of(layer.attackKind()))
        ).apply(instance, (type, owner, base, remaining, pulse, age, tier, weapon, kind) -> new Layer(type, owner, base, remaining, pulse, age, tier, weapon.orElse(null),
                kind.orElse(weapon.isPresent() ? AttackKind.RANGED : AttackKind.UNKNOWN))));
        public Layer tick() { return new Layer(type, owner, base, remaining - 1, pulse <= 1 ? 20 : pulse - 1, Math.min(144000, age + 1), criticalTier, weapon, attackKind); }
        public yagen.waitmydawn.yagens_attributes.api.StatusContext childContext(DamageType child, double childBase) {
            return new yagen.waitmydawn.yagens_attributes.api.StatusContext(Map.of(child, childBase), criticalTier, weapon, attackKind);
        }
    }
    public static final Codec<StatusState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Layer.CODEC.listOf(0, 8192).fieldOf("layers").forGetter((StatusState value) -> value.layers),
            Codec.intRange(0, 72000).fieldOf("frozen").forGetter(value -> value.frozen),
            Codec.intRange(0, 20).optionalFieldOf("impact_stun", 0).forGetter(value -> value.impactStun),
            Codec.intRange(0, 60).optionalFieldOf("electric_stun", 0).forGetter(value -> value.electricStun),
            Codec.intRange(0, 80).optionalFieldOf("heat_panic", 0).forGetter(value -> value.heatPanic),
            Codec.doubleRange(0, .5).fieldOf("heat_strip").forGetter(value -> value.heatStrip)
    ).apply(instance, StatusState::new));
    public final List<Layer> layers;
    public int frozen, impactStun, electricStun, heatPanic;
    public double heatStrip;
    public UUID confusedTarget;
    public boolean dirty;
    // Transient network baseline; persistence contains only authoritative gameplay state.
    public int[] markerCounts = new int[DamageType.values().length];
    public boolean markerFrozen;
    public StatusState() { this(List.of(), 0, 0, 0, 0, 0); }
    private StatusState(List<Layer> layers, int frozen, int impactStun, int electricStun, int heatPanic, double heatStrip) {
        this.layers = new ArrayList<>(layers); this.frozen = frozen; this.impactStun = impactStun; this.electricStun = electricStun; this.heatPanic = heatPanic; this.heatStrip = heatStrip;
        dirty = !empty();
    }
    public int count(DamageType type) { int total = 0; for (var layer : layers) if (layer.type == type) total++; return total; }
    public int duration(DamageType type) { int result = 0; for (var layer : layers) if (layer.type == type) result = Math.max(result, layer.remaining); return result; }
    public boolean stunned() { return impactStun > 0 || electricStun > 0 || heatPanic > 0; }
    public boolean empty() { return layers.isEmpty() && frozen == 0 && !stunned() && heatStrip == 0; }
    public void clear(DamageType type) {
        layers.removeIf(layer -> layer.type() == type);
        if (type == DamageType.COLD) frozen = 0;
        if (type == DamageType.HEAT) { heatStrip = 0; heatPanic = 0; }
        if (type == DamageType.IMPACT) impactStun = 0;
        if (type == DamageType.ELECTRICITY) electricStun = 0;
        if (type == DamageType.RADIATION) confusedTarget = null;
        dirty = true;
    }
    public List<Layer> advance(boolean freezeCold) {
        List<Layer> due = null;
        for (int i = layers.size() - 1; i >= 0; i--) {
            var layer = layers.get(i);
            if (freezeCold && layer.type() == DamageType.COLD) continue;
            boolean damage = switch (layer.type()) { case SLASH, HEAT, ELECTRICITY, TOXIN, GAS -> layer.pulse() <= 1; case BLAST -> layer.remaining() == 1; default -> false; };
            if (damage) {
                if (due == null) due = new ArrayList<>();
                due.add(layer);
            }
            if (layer.remaining() <= 1) {
                layers.remove(i);
                dirty = true;
            } else {
                layers.set(i, layer.tick());
            }
        }
        return due == null ? List.of() : due;
    }
}
