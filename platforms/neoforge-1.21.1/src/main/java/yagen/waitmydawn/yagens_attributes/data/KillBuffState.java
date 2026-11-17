package yagen.waitmydawn.yagens_attributes.data;

import java.util.*;
import yagen.waitmydawn.yagens_attributes.core.ConditionalModifier;

/** Session buffs belong to the wielder, installed instance and weapon; rank/config changes invalidate old stacks. */
public final class KillBuffState {
    public record Key(UUID weapon, UUID mod, int index, ConditionalModifier modifier) {}
    public final Map<Key, ConditionalModifier.Counter> counters = new HashMap<>();
    public UUID displayedWeapon;
    public List<yagen.waitmydawn.yagens_attributes.core.WeaponBuff> displayed;
}
