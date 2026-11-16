package yagen.waitmydawn.yagens_attributes.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Recharge eligibility persists through unloads so reconnecting cannot reset a spent gate. */
public final class ShieldState {
    public static final Codec<ShieldState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(0, 72000).fieldOf("delay").forGetter((ShieldState value) -> value.delay),
            Codec.intRange(0, 10).fieldOf("gate").forGetter(value -> value.gate),
            Codec.BOOL.fieldOf("armed").forGetter(value -> value.armed),
            Codec.doubleRange(0, Float.MAX_VALUE).fieldOf("before").forGetter(value -> value.before),
            Codec.doubleRange(0, Float.MAX_VALUE).optionalFieldOf("carry", 0D).forGetter(value -> value.carry)
    ).apply(instance, ShieldState::new));
    public int delay, gate;
    public boolean armed;
    public double before, carry;
    public ShieldState() {}
    public ShieldState(int delay, int gate, boolean armed, double before) {
        this(delay, gate, armed, before, 0);
    }
    public ShieldState(int delay, int gate, boolean armed, double before, double carry) {
        this.delay = delay; this.gate = gate; this.armed = armed; this.before = before;
        this.carry = carry;
    }
    public void naturalGain(double oldAmount, double newAmount) {
        if (newAmount > oldAmount) armed = true;
        observe(newAmount);
    }
    public void observe(double amount) { if (armed) before = amount; }
    public boolean consume(double amount) {
        if (!armed) return false;
        before = amount; armed = false;
        gate = yagen.waitmydawn.yagens_attributes.core.ShieldRules.gateTicks(amount);
        return true;
    }
}
