package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;

/** Explicit vanilla assignments; uncertain entities intentionally retain neutral life material. */
public final class BuiltinMaterials {
    public record Material(List<String> entities, Map<DamageType, Double> multipliers) {
        public Material { entities = List.copyOf(entities); multipliers = Map.copyOf(multipliers); }
    }
    private BuiltinMaterials() {}
    private static List<String> entities(String ids) { return Arrays.stream(ids.split(" ")).map(id -> "minecraft:" + id).toList(); }
    private static Map<DamageType, Double> factors(double... values) {
        // Values follow the stable DamageType declaration order, including wire-compatible physical ordinals.
        if (values.length != DamageType.values().length) throw new IllegalArgumentException("Missing material values");
        var result = new EnumMap<DamageType, Double>(DamageType.class);
        for (var type : DamageType.values()) result.put(type, values[type.ordinal()]);
        return Map.copyOf(result);
    }
    public static Map<String, Material> life() {
        var result = new LinkedHashMap<String, Material>();
        result.put("flesh", new Material(entities("player zombie husk drowned zombie_villager zombified_piglin zoglin hoglin zombie_horse giant piglin piglin_brute villager wandering_trader pillager vindicator evoker illusioner witch ravager cow pig sheep chicken wolf cat ocelot fox rabbit horse donkey mule llama trader_llama camel goat panda polar_bear armadillo sniffer bat parrot bee spider cave_spider silverfish turtle frog tadpole axolotl cod salmon tropical_fish pufferfish dolphin squid glow_squid"), factors(1,1,1,1,1,1,1,1,.75,1,1.5,1.25,.75)));
        result.put("bone", new Material(entities("skeleton stray bogged wither_skeleton skeleton_horse wither"), factors(1.5,.75,.75,1,1,.75,.5,1.25,1,1.25,.5,.5,1)));
        result.put("ghost", new Material(entities("ghast vex"), factors(.5,.5,.5,1.25,1,1.25,.5,.75,1.5,.75,.5,.5,1.25)));
        result.put("plant", new Material(entities("mooshroom"), factors(1,1,1.5,1.25,1.5,1,.75,1,1,1,1,.75,.75)));
        result.put("stone", new Material(entities("shulker"), factors(1.5,.75,.5,1,.75,.75,.25,1.5,1,1.25,.25,.25,.5)));
        result.put("metal", new Material(entities("iron_golem"), factors(.75,1.25,.75,1,1,1.25,.5,1,1.5,1.5,.5,.5,1.5)));
        result.put("mucus", new Material(entities("slime"), factors(.75,.75,.75,1.5,1.25,1,.75,1.25,1,1,1.25,1.25,.75)));
        result.put("pyrophobic", new Material(entities("snow_golem"), factors(1,1,1,.5,2,1,1,1.5,1,1,1,1,1)));
        return Collections.unmodifiableMap(result);
    }
    public static Map<DamageType, Double> shield() { return factors(1.5,.8,.75,1,.75,1.25,0,1,1,.75,1,1,2); }
}
