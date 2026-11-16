package yagen.waitmydawn.yagens_attributes.core.ui;

import java.util.*;

/** Two finite rejection pulses, scoped to the weapon that was being edited. */
public final class ConflictFlash {
    public static final int ON_MILLIS = 160, PERIOD_MILLIS = 320, DURATION_MILLIS = 640;
    private UUID weapon;
    private Set<UUID> participants = Set.of();
    private long started;
    public void start(UUID weapon, Collection<UUID> participants, long now) {
        this.weapon = Objects.requireNonNull(weapon); this.participants = Set.copyOf(participants); started = now;
    }
    public boolean active(UUID currentWeapon, long now) {
        return !participants.isEmpty() && Objects.equals(weapon, currentWeapon) && now >= started && now - started < DURATION_MILLIS;
    }
    public boolean red(UUID currentWeapon, UUID mod, long now) {
        return active(currentWeapon, now) && participants.contains(mod) && (now - started) % PERIOD_MILLIS < ON_MILLIS;
    }
}
