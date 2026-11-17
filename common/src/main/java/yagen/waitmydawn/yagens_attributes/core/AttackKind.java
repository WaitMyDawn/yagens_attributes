package yagen.waitmydawn.yagens_attributes.core;

import java.util.Locale;
import java.util.UUID;

/** Attack mode belongs to a hit, since one weapon may support both melee and ranged attacks. */
public enum AttackKind {
    UNKNOWN, MELEE, RANGED;

    public String key() { return name().toLowerCase(Locale.ROOT); }
    public static AttackKind parse(String value) { return valueOf(value.toUpperCase(Locale.ROOT)); }
    public void validateWeapon(UUID weapon) {
        if ((weapon != null) != (this != UNKNOWN)) throw new IllegalArgumentException("Attack kind and weapon identity disagree");
    }
    public boolean credits(ConditionalModifier.Trigger trigger) {
        return this == MELEE && trigger == ConditionalModifier.Trigger.MELEE_KILLS
                || this == RANGED && trigger == ConditionalModifier.Trigger.RANGED_KILLS;
    }
}
