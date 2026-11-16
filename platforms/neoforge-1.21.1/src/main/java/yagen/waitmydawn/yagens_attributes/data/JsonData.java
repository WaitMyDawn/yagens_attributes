package yagen.waitmydawn.yagens_attributes.data;

import com.google.gson.*;
import yagen.waitmydawn.yagens_attributes.core.*;
import java.util.*;

public final class JsonData {
    private static final Gson GSON = new Gson();
    private JsonData() {}

    public static void fields(JsonObject object, String... allowed) {
        var keys = Set.of(allowed);
        for (String key : object.keySet()) if (!keys.contains(key)) throw new IllegalArgumentException("Unknown field: " + key);
    }

    public static double number(JsonObject object, String key, double fallback) {
        if (!object.has(key)) return fallback;
        JsonPrimitive value = object.getAsJsonPrimitive(key);
        if (!value.isNumber()) throw new IllegalArgumentException("Expected number: " + key);
        double result = value.getAsDouble();
        if (!Double.isFinite(result)) throw new IllegalArgumentException("Non-finite value: " + key);
        return result;
    }

    public static Map<DamageType, Double> damage(JsonObject object) {
        var result = new EnumMap<DamageType, Double>(DamageType.class);
        for (String key : object.keySet()) {
            DamageType type = DamageType.valueOf(key.toUpperCase(Locale.ROOT));
            double value = number(object, key, 0);
            if (value < 0) throw new IllegalArgumentException("Negative damage value: " + key);
            result.put(type, value);
        }
        return Map.copyOf(result);
    }

    public static WeaponProfile weapon(JsonObject object) {
        fields(object, "critical_chance", "critical_damage", "status_chance", "status_duration", "damage_distribution", "base_damage", "category", "mod_slots", "fire_rate", "multishot", "riven_disposition");
        List<ModInfo.Polarity> slots = new ArrayList<>();
        if (object.has("mod_slots")) for (JsonElement value : object.getAsJsonArray("mod_slots")) slots.add(ModInfo.Polarity.valueOf(value.getAsString().toUpperCase(Locale.ROOT)));
        else slots.addAll(Collections.nCopies(8, ModInfo.Polarity.NONE));
        return new WeaponProfile(number(object, "critical_chance", 0), number(object, "critical_damage", 2),
                number(object, "status_chance", 0), number(object, "status_duration", 6), damage(object.getAsJsonObject("damage_distribution")), number(object, "base_damage", 1),
                object.has("category") ? ModInfo.Category.valueOf(object.get("category").getAsString().toUpperCase(Locale.ROOT)) : ModInfo.Category.WEAPON, slots,
                number(object, "fire_rate", 1), number(object, "multishot", 1), number(object, "riven_disposition", 0));
    }

    public static JsonObject weaponJson(WeaponProfile profile) {
        JsonObject result = new JsonObject();
        result.addProperty("critical_chance", profile.criticalChance()); result.addProperty("critical_damage", profile.criticalDamage());
        result.addProperty("status_chance", profile.statusChance()); result.addProperty("status_duration", profile.statusDuration());
        result.addProperty("base_damage", profile.baseDamage()); result.addProperty("category", profile.category().key());
        result.addProperty("fire_rate", profile.fireRate()); result.addProperty("multishot", profile.multishot());
        result.addProperty("riven_disposition", profile.rivenDisposition());
        JsonObject distribution = new JsonObject();
        for (var type : DamageType.values()) if (profile.distribution().containsKey(type)) distribution.addProperty(type.key(), profile.distribution().get(type));
        result.add("damage_distribution", distribution);
        JsonArray slots = new JsonArray(); profile.slots().forEach(polarity -> slots.add(polarity.key())); result.add("mod_slots", slots);
        return result;
    }

    public static String encodeBonuses(Map<String, Bonus> bonuses) { return GSON.toJson(bonuses); }

    public static Map<String, Bonus> decodeBonuses(String json) {
        var result = new LinkedHashMap<String, Bonus>();
        JsonObject object = JsonParser.parseString(json).getAsJsonObject();
        for (String key : object.keySet()) {
            if (!Stats.KEYS.contains(key)) throw new IllegalArgumentException("Unknown stat: " + key);
            JsonObject value = object.getAsJsonObject(key);
            fields(value, "addin", "addition", "multiply", "addout", "inputAddin", "inputAddition");
            var factors = new ArrayList<Double>();
            if (value.has("multiply")) for (JsonElement factor : value.getAsJsonArray("multiply")) factors.add(factor.getAsDouble());
            result.put(key, new Bonus(number(value, "addin", 0), number(value, "addition", 0), factors, number(value, "addout", 0), number(value, "inputAddin", 0), number(value, "inputAddition", 0)));
        }
        return Collections.unmodifiableMap(result);
    }
}
