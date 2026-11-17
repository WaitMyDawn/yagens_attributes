package yagen.waitmydawn.yagens_attributes.core.compat;

import yagen.waitmydawn.yagens_attributes.core.*;

import java.util.*;

/**
 * Loader-free weapon definitions; melee baselines resolve from registered item attributes at reload.
 */
public final class IronsSpellbooksWeapons {
    private IronsSpellbooksWeapons() {
    }

    public static Map<String, BuiltinCombat.Template> definitions(Map<String, WeaponProfile> vanilla) {
        var sword = vanilla.get("minecraft:diamond_sword");
        var netherite = vanilla.get("minecraft:netherite_sword");
        var crossbow = vanilla.get("minecraft:crossbow");
        var result = new LinkedHashMap<String, BuiltinCombat.Template>();
        melee(result, "amethyst_rapier", sword, .18, 1.8, .28, 1, Map.of(DamageType.PUNCTURE, 1D));
        result.put("irons_spellbooks:autoloader_crossbow", new BuiltinCombat.Template(copy(crossbow,
                crossbow.criticalChance() - .05, crossbow.criticalDamage(), crossbow.statusChance() - .05,
                crossbow.rivenDisposition(), crossbow.distribution(), crossbow.baseDamage()), false));
        melee(result, "boreal_blade", sword, .15, 2, .25, .8, elemental(DamageType.COLD));
        melee(result, "claymore", sword, sword.criticalChance(), sword.criticalDamage(), sword.statusChance(), 1, sword.distribution());
        melee(result, "decrepit_scythe", sword, .22, 2, .15, .9, sword.distribution());
        melee(result, "hellrazor", sword, .25, 2, .16, .8, elemental(DamageType.HEAT));
        melee(result, "keeper_flamberge", sword, .18, 2, .15, .9, sword.distribution());
        melee(result, "legionnaire_flamberge", sword, .2, 2, .15, .85, elemental(DamageType.HEAT));
        melee(result, "magehunter", netherite, netherite.criticalChance(), netherite.criticalDamage(), netherite.statusChance(),
                netherite.rivenDisposition() - .05, netherite.distribution());
        melee(result, "misery", netherite, netherite.criticalChance(), netherite.criticalDamage(), netherite.statusChance(), 1, elemental(DamageType.TOXIN));
        melee(result, "spellbreaker", netherite, netherite.criticalChance(), netherite.criticalDamage(), netherite.statusChance(), .85, elemental(DamageType.MAGNETIC));
        melee(result, "twilight_gale", sword, .22, 2.2, .2, .7,
                Map.of(DamageType.ELECTRICITY, .6, DamageType.SLASH, .05, DamageType.PUNCTURE, .3, DamageType.IMPACT, .05));
        return Map.copyOf(result);
    }

    private static Map<DamageType, Double> elemental(DamageType type) {
        return Map.of(type, .6, DamageType.SLASH, .18, DamageType.PUNCTURE, .18, DamageType.IMPACT, .04);
    }

    private static void melee(Map<String, BuiltinCombat.Template> target, String path, WeaponProfile reference,
                              double chance, double critical, double status, double disposition, Map<DamageType, Double> distribution) {
        target.put("irons_spellbooks:" + path, new BuiltinCombat.Template(copy(reference, chance, critical, status, disposition, distribution, 1), true));
    }

    private static WeaponProfile copy(WeaponProfile reference, double chance, double critical, double status,
                                      double disposition, Map<DamageType, Double> distribution, double base) {
        return new WeaponProfile(chance, critical, status, reference.statusDuration(), distribution, base,
                reference.category(), reference.slots(), reference.fireRate(), reference.multishot(), disposition);
    }
}
