package yagen.waitmydawn.yagens_attributes.data;

import java.util.*;
import yagen.waitmydawn.yagens_attributes.core.Combo;

/** Session-only chains belong to a wielder and weapon identity; holstering does not pause expiry. */
public final class ComboState {
    public final Map<UUID, Combo.Counter> counters = new HashMap<>();
    public UUID displayedWeapon;
    public Combo.Counter displayedCounter;
    public Combo.Rules displayedRules;
}
