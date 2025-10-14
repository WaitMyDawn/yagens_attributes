package yagen.waitmydawn.api.attribute;

import java.util.Map;

public class DamageBonusTable {
    public static final Map<HealthMaterialType, Map<DamageType, Float>> TABLE = Map.of(
            HealthMaterialType.BONE, Map.of(
                    DamageType.IMPACT, 1.2f,
                    DamageType.PUNCTURE, 0.8f,
                    DamageType.SLASH, 0.9f,
                    DamageType.COLD, 0.8f,
                    DamageType.CORROSIVE, 1.4f,
                    DamageType.MAGNETIC, 1.2f,
                    DamageType.VIRAL, 0.8f
            ),
            HealthMaterialType.FLESH, Map.of(
                    DamageType.IMPACT, 0.8f,
                    DamageType.SLASH, 1.2f,
                    DamageType.HEAT, 1.2f,
                    DamageType.TOXIN, 1.2f,
                    DamageType.VIRAL, 1.6f
            ),
            HealthMaterialType.METAL, Map.of(
                    DamageType.ELECTRICITY, 1.2f,
                    DamageType.BLAST, 1.2f,
                    DamageType.MAGNETIC, 1.2f,
                    DamageType.CORROSIVE, 1.5f,
                    DamageType.TOXIN, 0.6f,
                    DamageType.GAS, 0.6f,
                    DamageType.VIRAL, 0.8f
            ),
            HealthMaterialType.ARTHROPOD, Map.of(
                    DamageType.HEAT, 1.2f,
                    DamageType.VIRAL, 1.2f,
                    DamageType.SLASH,1.2f,
                    DamageType.PUNCTURE,0.8f
            ),
            HealthMaterialType.GHOST, Map.of(
                    DamageType.SLASH,0.8f,
                    DamageType.PUNCTURE,0.8f,
                    DamageType.IMPACT,0.8f,
                    DamageType.HEAT, 1.2f,
                    DamageType.MAGNETIC,1.5f
            ),
            HealthMaterialType.ELEMENT, Map.of(
                    DamageType.SLASH,0.8f,
                    DamageType.PUNCTURE,0.8f,
                    DamageType.IMPACT,1.2f,
                    DamageType.ELECTRICITY, 1.2f,
                    DamageType.MAGNETIC,1.2f
            ),
            HealthMaterialType.PLANT, Map.of(
                    DamageType.SLASH,1.2f,
                    DamageType.IMPACT,0.8f,
                    DamageType.HEAT, 1.4f,
                    DamageType.COLD, 0.9f,
                    DamageType.ELECTRICITY, 0.9f,
                    DamageType.VIRAL, 1.2f,
                    DamageType.MAGNETIC,0.8f,
                    DamageType.RADIATION,0.8f
            )
    );

    public static float getBonus(HealthMaterialType type, DamageType dmgType) {
        return TABLE.getOrDefault(type, Map.of()).getOrDefault(dmgType, 1f);
    }
}