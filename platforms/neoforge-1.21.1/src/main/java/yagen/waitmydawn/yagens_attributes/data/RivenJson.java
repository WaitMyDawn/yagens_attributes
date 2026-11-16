package yagen.waitmydawn.yagens_attributes.data;

import com.google.gson.*;
import yagen.waitmydawn.yagens_attributes.core.*;
import java.util.*;

public final class RivenJson {
    private RivenJson() {}
    public static JsonObject rules(RivenRules rules) {
        var json = ModJson.GSON.toJsonTree(rules).getAsJsonObject();
        json.addProperty("description", "Full-rank bases use disposition 1, layout factor 1 and quality 0.5. Values are fractions except combo duration in seconds. Weapon selection is uniform; disposition only changes strength. Other-mod profiles default to disposition 0.");
        return json;
    }
    public static RivenRules rules(JsonObject object) {
        JsonData.fields(object, "baseCapacity", "maxLevel", "variationMin", "variationMax", "layouts", "traits", "texture", "description");
        var layouts = new ArrayList<RivenRules.Factors>();
        for (var entry : object.getAsJsonArray("layouts")) {
            var value = entry.getAsJsonObject(); JsonData.fields(value, "positiveCount", "negativeCount", "positive", "negative");
            layouts.add(new RivenRules.Factors(integer(value, "positiveCount"), integer(value, "negativeCount"), number(value, "positive"), number(value, "negative")));
        }
        var traits = new LinkedHashMap<String, RivenRules.Trait>();
        for (var entry : object.getAsJsonObject("traits").entrySet()) {
            var value = entry.getValue().getAsJsonObject();
            JsonData.fields(value, "stat", "operation", "basis", "applicability", "meleeBase", "rangedBase", "positive", "negative", "physicalMinimum", "description");
            var scope = EnumSet.noneOf(ModInfo.Category.class);
            for (var category : value.getAsJsonArray("applicability")) scope.add(ModInfo.Category.valueOf(category.getAsString().toUpperCase(Locale.ROOT)));
            traits.put(entry.getKey(), new RivenRules.Trait(value.get("stat").getAsString(), ModDefinition.Operation.valueOf(value.get("operation").getAsString().toUpperCase(Locale.ROOT)),
                    ModDefinition.Basis.valueOf(value.get("basis").getAsString().toUpperCase(Locale.ROOT)), scope, number(value, "meleeBase"), number(value, "rangedBase"),
                    bool(value, "positive"), bool(value, "negative"), number(value, "physicalMinimum"), value.get("description").getAsString()));
        }
        return new RivenRules(integer(object, "baseCapacity"), integer(object, "maxLevel"), number(object, "variationMin"), number(object, "variationMax"), layouts, traits, object.get("texture").getAsString());
    }
    public static String roll(RivenRoll roll) { return ModJson.GSON.toJson(roll); }
    public static RivenRoll roll(String json) { return roll(JsonParser.parseString(json).getAsJsonObject()); }
    public static RivenRoll roll(JsonObject object) {
        JsonData.fields(object, "weapon", "category", "polarity", "disposition", "lines", "cycles");
        var lines = new ArrayList<RivenRoll.Line>();
        if (object.getAsJsonArray("lines").size() > 4) throw new IllegalArgumentException("Too many Riven lines");
        for (var entry : object.getAsJsonArray("lines")) {
            var line = entry.getAsJsonObject(); JsonData.fields(line, "trait", "quality", "negative");
            lines.add(new RivenRoll.Line(line.get("trait").getAsString(), number(line, "quality"), bool(line, "negative")));
        }
        return new RivenRoll(object.get("weapon").getAsString(), ModInfo.Category.valueOf(object.get("category").getAsString().toUpperCase(Locale.ROOT)),
                ModInfo.Polarity.valueOf(object.get("polarity").getAsString().toUpperCase(Locale.ROOT)), number(object, "disposition"), lines, object.has("cycles") ? integer(object, "cycles") : 0);
    }
    private static double number(JsonObject object, String key) { return JsonData.number(object, key, Double.NaN); }
    private static int integer(JsonObject object, String key) {
        double value = number(object, key); if (value != (int) value) throw new IllegalArgumentException("Expected integer " + key); return (int) value;
    }
    private static boolean bool(JsonObject object, String key) {
        var value = object.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean()) throw new IllegalArgumentException("Expected boolean " + key);
        return value.getAsBoolean();
    }
}
