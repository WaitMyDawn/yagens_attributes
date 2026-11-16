package yagen.waitmydawn.yagens_attributes.api;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import yagen.waitmydawn.yagens_attributes.core.ArmorPenetration;

/** Fired once per armor evaluation in custom modes, before the curve and native armor-effectiveness enchantments. */
public final class ArmorPenetrationEvent extends Event {
    private final LivingEntity target;
    private final DamageSource source;
    private ArmorPenetration penetration = ArmorPenetration.NONE;
    public ArmorPenetrationEvent(LivingEntity target, DamageSource source) { this.target = target; this.source = source; }
    public LivingEntity target() { return target; }
    public DamageSource source() { return source; }
    public ArmorPenetration penetration() { return penetration; }
    public void add(ArmorPenetration value) { penetration = penetration.plus(value); }
}
