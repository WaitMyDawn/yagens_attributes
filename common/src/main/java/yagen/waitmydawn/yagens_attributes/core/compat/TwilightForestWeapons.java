package yagen.waitmydawn.yagens_attributes.core.compat;

import yagen.waitmydawn.yagens_attributes.core.*;
import java.util.*;
import static yagen.waitmydawn.yagens_attributes.core.DamageType.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.Category.*;
import static yagen.waitmydawn.yagens_attributes.core.compat.WeaponDefinitions.*;

public final class TwilightForestWeapons {
    private TwilightForestWeapons() {}
    public static Map<String, BuiltinCombat.Template> definitions(Map<String, WeaponProfile> vanilla) {
        var result = new LinkedHashMap<String, BuiltinCombat.Template>();
        inherit(result, "twilightforest:ironwood_sword", vanilla.get("minecraft:diamond_sword"));
        inherit(result, "twilightforest:ironwood_axe", vanilla.get("minecraft:diamond_axe"));
        inherit(result, "twilightforest:steeleaf_sword", vanilla.get("minecraft:diamond_sword"));
        inherit(result, "twilightforest:steeleaf_axe", vanilla.get("minecraft:diamond_axe"));
        nativeWeapon(result, "twilightforest:block_and_chain", .22, 2.2, .24, .95, MELEE, physical(.85, .05, .10));
        nativeWeapon(result, "twilightforest:diamond_minotaur_axe", .22, 2.2, .20, 1.05, AXE, physical(.20, .10, .70));
        nativeWeapon(result, "twilightforest:fiery_sword", .20, 2, .24, 1, SWORD, mixed(HEAT, .60, .04, .18, .18));
        nativeWeapon(result, "twilightforest:giant_sword", .24, 2.4, .16, 1.10, SWORD, physical(.45, .05, .50));
        nativeWeapon(result, "twilightforest:glass_sword", .25, 2.4, .12, 1, SWORD, physical(.05, .45, .50));
        nativeWeapon(result, "twilightforest:gold_minotaur_axe", .18, 2, .18, 1.25, AXE, physical(.20, .10, .70));
        nativeWeapon(result, "twilightforest:ice_sword", .18, 2, .26, 1, SWORD, mixed(COLD, .60, .04, .18, .18));
        nativeWeapon(result, "twilightforest:knightmetal_axe", .20, 2.1, .16, 1.15, AXE, physical(.20, .10, .70));
        nativeWeapon(result, "twilightforest:knightmetal_sword", .18, 2, .18, 1.20, SWORD, physical(.10, .45, .45));
        nativeWeapon(result, "twilightforest:ender_bow", .40, 2, .20, 1.05, BOW, physical(.05, .75, .20));
        nativeWeapon(result, "twilightforest:ice_bow", .30, 2, .30, .90, BOW, mixed(COLD, .60, .04, .28, .08));
        nativeWeapon(result, "twilightforest:seeker_bow", .35, 2, .25, .90, BOW, physical(.10, .70, .20));
        nativeWeapon(result, "twilightforest:triple_bow", .30, 2, .25, .80, BOW, physical(.10, .70, .20), 3);
        return Map.copyOf(result);
    }
}
