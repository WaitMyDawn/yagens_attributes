package yagen.waitmydawn.yagens_attributes.data;

import com.google.gson.*;
import yagen.waitmydawn.yagens_attributes.core.*;

import java.util.*;

public final class ModJson {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private ModJson() {
    }

    private static String string(JsonObject object, String key) {
        return object.get(key).getAsString();
    }

    private static int integer(JsonObject object, String key) {
        double value = JsonData.number(object, key, Double.NaN);
        if (!Double.isFinite(value) || value != (int) value)
            throw new IllegalArgumentException("Expected integer: " + key);
        return (int) value;
    }

    private static <T extends Enum<T>> T enumeration(Class<T> type, String value) {
        return Enum.valueOf(type, value.toUpperCase(Locale.ROOT));
    }

    public static JsonObject definition(ModDefinition value) {
        var result = new JsonObject();
        result.addProperty("schema_version", 1);
        result.addProperty("name", value.name());
        result.addProperty("rarity", value.rarity().key());
        result.addProperty("polarity", value.polarity().key());
        var capacity = new JsonObject();
        capacity.addProperty("base", value.baseCapacity());
        capacity.addProperty("per_level", value.capacityPerLevel());
        capacity.addProperty("description", "Mod capacity used at level 0 and additional capacity per level. Total capacity cost is base + per_level * level.");
        result.add("capacity", capacity);
        result.addProperty("max_level", value.maxLevel());
        result.addProperty("max_level_description", "Maximum inclusive Mod level. Level 0 is a usable Mod and every entry scales with (level + 1).");
        var scope = new JsonArray();
        value.applicability().stream().sorted().forEach(category -> scope.add(category.key()));
        result.add("applicability", scope);
        result.addProperty("texture", value.texture());
        if (!value.style().equals(ModStyle.DEFAULT)) result.add("style", style(value.style()));
        var entries = new JsonArray();
        for (var entry : value.entries()) {
            var item = new JsonObject();
            item.addProperty("stat", entry.stat());
            item.addProperty("operation", entry.operation().name().toLowerCase(Locale.ROOT));
            item.addProperty("value", entry.value());
            item.addProperty("basis", entry.basis().name().toLowerCase(Locale.ROOT));
            item.addProperty("description", entry.description());
            if (entry.condition().trigger() != ConditionalModifier.Trigger.ALWAYS) item.add("condition", condition(entry.condition()));
            entries.add(item);
        }
        result.add("entries", entries);
        return result;
    }

    public static ModDefinition definition(JsonObject object) {
        JsonData.fields(object, "schema_version", "name", "rarity", "polarity", "capacity", "max_level", "max_level_description", "applicability", "texture", "entries", "style");
        if (integer(object, "schema_version") != 1)
            throw new IllegalArgumentException("Unsupported Mod definition schema");
        var capacity = object.getAsJsonObject("capacity");
        JsonData.fields(capacity, "base", "per_level", "description");
        if (string(capacity, "description").isBlank() || string(object, "max_level_description").isBlank())
            throw new IllegalArgumentException("Missing numeric description");
        var scope = EnumSet.noneOf(ModInfo.Category.class);
        for (var category : object.getAsJsonArray("applicability"))
            scope.add(enumeration(ModInfo.Category.class, category.getAsString()));
        var entries = new ArrayList<ModDefinition.Effect>();
        for (var value : object.getAsJsonArray("entries")) {
            var entry = value.getAsJsonObject();
            JsonData.fields(entry, "stat", "operation", "value", "basis", "description", "condition");
            entries.add(new ModDefinition.Effect(string(entry, "stat"), entry.has("operation") ? enumeration(ModDefinition.Operation.class, string(entry, "operation")) : ModDefinition.Operation.ADDITION, JsonData.number(entry, "value", Double.NaN),
                    enumeration(ModDefinition.Basis.class, string(entry, "basis")), string(entry, "description"), entry.has("condition") ? condition(entry.getAsJsonObject("condition")) : ConditionalModifier.Condition.ALWAYS));
        }
        return new ModDefinition(string(object, "name"), enumeration(ModInfo.Rarity.class, string(object, "rarity")), enumeration(ModInfo.Polarity.class, string(object, "polarity")),
                integer(capacity, "base"), integer(capacity, "per_level"), integer(object, "max_level"), scope, string(object, "texture"), entries,
                object.has("style") ? style(object.getAsJsonObject("style")) : ModStyle.DEFAULT);
    }

    private static JsonObject style(ModStyle value) {
        var object = new JsonObject();
        object.addProperty("visual", value.visual().name().toLowerCase(Locale.ROOT));
        var groups = new JsonArray(); value.exclusiveGroups().stream().sorted().forEach(groups::add);
        object.add("exclusive_groups", groups);
        if (!value.scopedExclusiveGroups().isEmpty()) {
            var scoped = new JsonObject();
            value.scopedExclusiveGroups().entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
                var ids = new JsonArray(); entry.getValue().stream().sorted().forEach(ids::add); scoped.add(entry.getKey().key(), ids);
            });
            object.add("scoped_exclusive_groups", scoped);
        }
        return object;
    }
    private static ModStyle style(JsonObject object) {
        JsonData.fields(object, "visual", "exclusive_groups", "scoped_exclusive_groups");
        var groups = new HashSet<String>();
        for (var group : object.getAsJsonArray("exclusive_groups")) if (!groups.add(group.getAsString())) throw new IllegalArgumentException("Duplicate Mod exclusion group");
        var scoped = new EnumMap<ModInfo.Category, Set<String>>(ModInfo.Category.class);
        if (object.has("scoped_exclusive_groups")) object.getAsJsonObject("scoped_exclusive_groups").entrySet().forEach(entry -> {
            var ids = new HashSet<String>();
            for (var id : entry.getValue().getAsJsonArray()) if (!ids.add(id.getAsString())) throw new IllegalArgumentException("Duplicate scoped exclusion group");
            var category = enumeration(ModInfo.Category.class, entry.getKey());
            if (scoped.put(category, ids) != null) throw new IllegalArgumentException("Duplicate scoped category");
        });
        return new ModStyle(enumeration(ModStyle.Visual.class, string(object, "visual")), groups, scoped);
    }
    private static JsonObject condition(ConditionalModifier.Condition value) {
        var object = new JsonObject();
        object.addProperty("trigger", value.trigger().name().toLowerCase(Locale.ROOT));
        object.addProperty("duration_seconds", value.durationSeconds());
        object.addProperty("max_stacks", value.maxStacks());
        if (value.perTargetStatus()) object.addProperty("per_target_status", true);
        if (value.decay() != ConditionalModifier.Decay.ALL_AT_ONCE) object.addProperty("decay", value.decay().name().toLowerCase(Locale.ROOT));
        if (value.scaleDurationWithRank()) object.addProperty("scale_duration_with_rank", true);
        String description = value.trigger() == ConditionalModifier.Trigger.RANGED_WEAK_POINT_HITS
                ? "Confirmed ranged weak point hits build or refresh the weapon buff. The bonus applies at launch. scale_duration_with_rank multiplies the rank 0 duration by (rank + 1)."
                : (value.trigger() == ConditionalModifier.Trigger.RANGED_WEAK_POINT_KILLS
                    ? "Direct ranged weak point kills build the weapon buff. The bonus applies at launch. " : "")
                    + "Qualifying kills add a stack, capped at max_stacks. all_at_once refreshes all layers and expires together; one_at_a_time refreshes the next expiry then removes one layer per duration_seconds; independent gives each layer its own expiry and replaces the earliest expiry at capacity. per_target_status multiplies the active kill count by distinct independent target proc types before this hit, ignoring potion effects. "
                    + (value.scaleDurationWithRank() ? "Duration scales with (rank + 1). The stack cap does not scale." : "Duration and stack cap do not scale with rank.");
        object.addProperty("description", description);
        return object;
    }
    private static ConditionalModifier.Condition condition(JsonObject object) {
        JsonData.fields(object, "trigger", "duration_seconds", "max_stacks", "description", "per_target_status", "decay", "scale_duration_with_rank");
        if (string(object, "description").isBlank()) throw new IllegalArgumentException("Missing condition description");
        var scale = object.get("scale_duration_with_rank");
        if (scale != null && (!scale.isJsonPrimitive() || !scale.getAsJsonPrimitive().isBoolean()))
            throw new IllegalArgumentException("Expected boolean scale_duration_with_rank");
        return new ConditionalModifier.Condition(enumeration(ConditionalModifier.Trigger.class, string(object, "trigger")),
                JsonData.number(object, "duration_seconds", Double.NaN), integer(object, "max_stacks"),
                object.has("per_target_status") && object.get("per_target_status").getAsBoolean(),
                object.has("decay") ? enumeration(ConditionalModifier.Decay.class, string(object, "decay")) : ConditionalModifier.Decay.ALL_AT_ONCE,
                scale != null && scale.getAsBoolean());
    }

    public static JsonObject conditional(ConditionalModifier value) {
        var object = new JsonObject();
        object.addProperty("stat", value.stat()); object.addProperty("addition", value.addition());
        object.add("condition", condition(value.condition()));
        return object;
    }
    public static ConditionalModifier conditional(JsonObject object) {
        JsonData.fields(object, "stat", "addition", "condition");
        return new ConditionalModifier(string(object, "stat"), JsonData.number(object, "addition", Double.NaN), condition(object.getAsJsonObject("condition")));
    }

    public static JsonObject quality(ModCatalog.Quality value) {
        var object = new JsonObject();
        object.addProperty("id", value.type().key());
        object.addProperty("name", value.name());
        object.addProperty("color", String.format(Locale.ROOT, "#%06X", value.color()));
        object.addProperty("base_endo", value.baseEndo());
        object.addProperty("order", value.order());
        object.addProperty("description", value.description());
        return object;
    }

    public static ModCatalog.Quality quality(JsonObject value) {
        JsonData.fields(value, "id", "name", "color", "base_endo", "order", "description");
        String color = string(value, "color");
        if (!color.matches("#[0-9a-fA-F]{6}")) throw new IllegalArgumentException("Expected RGB hex color");
        return new ModCatalog.Quality(enumeration(ModInfo.Rarity.class, string(value, "id")), string(value, "name"), Integer.parseInt(color.substring(1), 16), integer(value, "base_endo"), integer(value, "order"), string(value, "description"));
    }

    public static JsonObject polarity(ModCatalog.Polarity value) {
        var object = new JsonObject();
        object.addProperty("id", value.type().key());
        object.addProperty("name", value.name());
        object.addProperty("symbol", value.symbol());
        return object;
    }

    public static ModCatalog.Polarity polarity(JsonObject value) {
        JsonData.fields(value, "id", "name", "symbol");
        return new ModCatalog.Polarity(enumeration(ModInfo.Polarity.class, string(value, "id")), string(value, "name"), string(value, "symbol"));
    }

    public static JsonObject catalog(ModCatalog catalog) {
        var result = new JsonObject();
        var mods = new JsonObject();
        var qualities = new JsonArray();
        var polarities = new JsonArray();
        catalog.definitions().entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> mods.add(entry.getKey(), definition(entry.getValue())));
        catalog.qualities().values().stream().sorted(Comparator.comparing(value -> value.type().key())).forEach(value -> qualities.add(quality(value)));
        catalog.polarities().values().stream().sorted(Comparator.comparing(value -> value.type().key())).forEach(value -> polarities.add(polarity(value)));
        result.add("mods", mods);
        result.add("qualities", qualities);
        result.add("polarities", polarities);
        if (catalog.rivenRules() != null) result.add("rivens", RivenJson.rules(catalog.rivenRules()));
        return result;
    }

    public static ModCatalog catalog(JsonObject object) {
        JsonData.fields(object, "mods", "qualities", "polarities", "rivens");
        var mods = new LinkedHashMap<String, ModDefinition>();
        object.getAsJsonObject("mods").entrySet().forEach(entry -> mods.put(entry.getKey(), definition(entry.getValue().getAsJsonObject())));
        var qualities = new EnumMap<ModInfo.Rarity, ModCatalog.Quality>(ModInfo.Rarity.class);
        for (var value : object.getAsJsonArray("qualities")) {
            var quality = quality(value.getAsJsonObject());
            if (qualities.put(quality.type(), quality) != null) throw new IllegalArgumentException("Duplicate quality");
        }
        var polarities = new EnumMap<ModInfo.Polarity, ModCatalog.Polarity>(ModInfo.Polarity.class);
        for (var value : object.getAsJsonArray("polarities")) {
            var polarity = polarity(value.getAsJsonObject());
            if (polarities.put(polarity.type(), polarity) != null)
                throw new IllegalArgumentException("Duplicate polarity");
        }
        if (qualities.size() != ModInfo.Rarity.values().length || polarities.size() != ModInfo.Polarity.values().length)
            throw new IllegalArgumentException("Incomplete Mod metadata");
        return new ModCatalog(mods, qualities, polarities, object.has("rivens") ? RivenJson.rules(object.getAsJsonObject("rivens")) : null);
    }

    public static String details(ModInfo.Details value) {
        if (value == null) return "";
        var object = GSON.toJsonTree(value).getAsJsonObject();
        object.add("style", style(value.style()));
        var conditional = new JsonArray();
        for (var entry : value.conditional()) {
            var item = new JsonObject(); item.addProperty("stat", entry.stat()); item.addProperty("addition", entry.addition());
            item.add("condition", condition(entry.condition())); conditional.add(item);
        }
        object.add("conditional", conditional);
        return GSON.toJson(object);
    }

    public static ModInfo.Details details(String json) {
        if (json.isEmpty()) return null;
        // Reconstruct through the record constructor so malformed saved or network values cannot skip validation.
        var value = JsonParser.parseString(json).getAsJsonObject();
        var scope = EnumSet.noneOf(ModInfo.Category.class);
        for (var category : value.getAsJsonArray("applicability"))
            scope.add(enumeration(ModInfo.Category.class, category.getAsString()));
        var conditional = new ArrayList<ConditionalModifier>();
        if (value.has("conditional")) for (var element : value.getAsJsonArray("conditional")) {
            var item = element.getAsJsonObject();
            conditional.add(new ConditionalModifier(string(item, "stat"), JsonData.number(item, "addition", Double.NaN), condition(item.getAsJsonObject("condition"))));
        }
        return new ModInfo.Details(string(value, "type"), integer(value, "level"), integer(value, "maxLevel"), scope, string(value, "texture"), string(value, "symbol"), integer(value, "color"), integer(value, "order"), value.get("upgradeCost").getAsLong(),
                value.has("riven") && !value.get("riven").isJsonNull() ? RivenJson.roll(value.getAsJsonObject("riven")) : null,
                value.has("style") ? style(value.getAsJsonObject("style")) : ModStyle.DEFAULT, conditional);
    }
}
