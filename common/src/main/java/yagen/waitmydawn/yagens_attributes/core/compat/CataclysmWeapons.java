package yagen.waitmydawn.yagens_attributes.core.compat;

import yagen.waitmydawn.yagens_attributes.core.*;
import java.util.*;
import static yagen.waitmydawn.yagens_attributes.core.DamageType.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.Category.*;
import static yagen.waitmydawn.yagens_attributes.core.compat.WeaponDefinitions.*;

public final class CataclysmWeapons {
    private CataclysmWeapons() {}
    public static Map<String, BuiltinCombat.Template> definitions(Map<String, WeaponProfile> vanilla) {
        var result = new LinkedHashMap<String, BuiltinCombat.Template>();
        inherit(result, "cataclysm:black_steel_sword", vanilla.get("minecraft:diamond_sword"));
        inherit(result, "cataclysm:black_steel_axe", vanilla.get("minecraft:diamond_axe"));
        nativeWeapon(result, "cataclysm:ancient_spear", .20, 2, .20, .90, TRIDENT, physical(.10, .75, .15));
        nativeWeapon(result, "cataclysm:astrape", .26, 2.2, .14, .70, TRIDENT, mixed(ELECTRICITY, .60, .05, .30, .05));
        nativeWeapon(result, "cataclysm:athame", .20, 1.8, .28, 1.25, SWORD, Map.of(SLASH, 1D));
        nativeWeapon(result, "cataclysm:brontes", .24, 2.4, .18, .70, MACE, Map.of(ELECTRICITY, .60, IMPACT, .40));
        nativeWeapon(result, "cataclysm:ceraunus", .32, 2.2, .05, .70, TRIDENT, mixed(ELECTRICITY, .50, .30, .05, .15));
        nativeWeapon(result, "cataclysm:coral_bardiche", .20, 2, .10, 1.10, TRIDENT, physical(.10, .30, .60));
        nativeWeapon(result, "cataclysm:coral_spear", .18, 2, .24, 1.20, TRIDENT, physical(.20, .70, .10));
        nativeWeapon(result, "cataclysm:gauntlet_of_bulwark", .22, 2.2, .12, .75, MELEE, Map.of(HEAT, .60, IMPACT, .40));
        nativeWeapon(result, "cataclysm:gauntlet_of_guard", .24, 2, .05, .85, MELEE, Map.of(MAGNETIC, .40, IMPACT, .60));
        nativeWeapon(result, "cataclysm:gauntlet_of_maelstrom", .20, 2, .14, .75, MELEE, Map.of(MAGNETIC, .50, IMPACT, .30, SLASH, .20));
        nativeWeapon(result, "cataclysm:infernal_forge", .24, 2.2, .20, .85, MACE, Map.of(HEAT, .50, IMPACT, .50));
        nativeWeapon(result, "cataclysm:khopesh", .18, 2, .18, 1.25, SWORD, physical(.05, .20, .75));
        nativeWeapon(result, "cataclysm:meat_shredder", .05, 2, .30, .90, MELEE, physical(.05, .10, .85));
        nativeWeapon(result, "cataclysm:soul_render", .16, 2, .24, .75, MELEE, mixed(RADIATION, .60, .05, .05, .30));
        nativeWeapon(result, "cataclysm:the_annihilator", .28, 2.5, .12, .70, MACE, physical(.80, .10, .10));
        nativeWeapon(result, "cataclysm:the_immolator", .24, 2.5, .20, .70, MACE, mixed(HEAT, .60, .35, 0, .05));
        nativeWeapon(result, "cataclysm:the_incinerator", .25, 2, .20, .70, SWORD, mixed(HEAT, .60, .05, .05, .30));
        nativeWeapon(result, "cataclysm:tidal_claws", .20, 2, .30, .80, MELEE, physical(.10, .45, .45));
        nativeWeapon(result, "cataclysm:void_forge", .24, 2.2, .26, .75, MACE, Map.of(MAGNETIC, .60, IMPACT, .40));
        nativeWeapon(result, "cataclysm:cursed_bow", .32, 2.2, .24, .80, BOW, mixed(RADIATION, .40, .05, .45, .10), 3);
        nativeWeapon(result, "cataclysm:laser_gatling", .16, 1.8, .20, .85, RANGED, Map.of(RADIATION, .70, PUNCTURE, .30));
        nativeWeapon(result, "cataclysm:void_assault_shoulder_weapon", .26, 2.2, .28, .75, RANGED, Map.of(BLAST, .50, MAGNETIC, .50));
        nativeWeapon(result, "cataclysm:wither_assault_shoulder_weapon", .24, 2.2, .24, .85, RANGED, Map.of(BLAST, .60, TOXIN, .40));
        nativeWeapon(result, "cataclysm:wrath_of_the_desert", .30, 2.2, .30, .70, BOW, Map.of(BLAST, .45, IMPACT, .35, SLASH, .20), 3);
        return Map.copyOf(result);
    }
}
