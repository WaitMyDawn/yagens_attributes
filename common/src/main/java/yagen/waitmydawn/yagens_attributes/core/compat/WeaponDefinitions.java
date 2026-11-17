package yagen.waitmydawn.yagens_attributes.core.compat;

import yagen.waitmydawn.yagens_attributes.core.*;
import java.util.*;

final class WeaponDefinitions {
    private WeaponDefinitions() {}
    static Map<DamageType, Double> physical(double impact, double puncture, double slash) {
        return Map.of(DamageType.IMPACT, impact, DamageType.PUNCTURE, puncture, DamageType.SLASH, slash);
    }
    static Map<DamageType, Double> mixed(DamageType type, double share, double impact, double puncture, double slash) {
        return Map.of(type, share, DamageType.IMPACT, impact, DamageType.PUNCTURE, puncture, DamageType.SLASH, slash);
    }
    static void nativeWeapon(Map<String, BuiltinCombat.Template> target, String id, double chance, double critical,
                             double status, double disposition, ModInfo.Category category, Map<DamageType, Double> damage) {
        nativeWeapon(target, id, chance, critical, status, disposition, category, damage, 1);
    }
    static void nativeWeapon(Map<String, BuiltinCombat.Template> target, String id, double chance, double critical,
                             double status, double disposition, ModInfo.Category category, Map<DamageType, Double> damage, double multishot) {
        target.put(id, new BuiltinCombat.Template(new WeaponProfile(chance, critical, status, 6, damage, 1, category,
                Collections.nCopies(8, ModInfo.Polarity.NONE), 1, multishot, disposition), true));
    }
    static void inherit(Map<String, BuiltinCombat.Template> target, String id, WeaponProfile reference) {
        target.put(id, new BuiltinCombat.Template(reference, true));
    }
}
