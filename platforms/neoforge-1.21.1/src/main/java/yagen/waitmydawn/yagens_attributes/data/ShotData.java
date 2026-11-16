package yagen.waitmydawn.yagens_attributes.data;

import com.google.gson.*;
import com.mojang.serialization.*;
import yagen.waitmydawn.yagens_attributes.core.*;
import java.util.*;

/** Immutable launch-time stats are shared in memory and serialized only when a projectile is saved. */
public record ShotData(WeaponProfile profile, List<Map<String, Bonus>> sources, double scale, boolean virtual, UUID weapon) {
    public ShotData(WeaponProfile profile, List<Map<String, Bonus>> sources, double scale, boolean virtual) { this(profile, sources, scale, virtual, null); }
    public static final ShotData EMPTY = new ShotData(null, List.of(), 1, false);
    public static final Codec<ShotData> CODEC = Codec.STRING.comapFlatMap(value -> {
        try { return DataResult.success(decode(value)); }
        catch (RuntimeException exception) { return DataResult.error(() -> "Invalid ranged shot: " + exception.getMessage()); }
    }, ShotData::encode);
    public ShotData {
        sources = sources.stream().map(source -> Collections.unmodifiableMap(new LinkedHashMap<>(source))).toList();
        if (sources.size() > 129 || !Double.isFinite(scale) || scale < 0 || scale > RangedStats.LIMIT) throw new IllegalArgumentException("Invalid shot snapshot");
        for (var source : sources) if (!Stats.KEYS.containsAll(source.keySet())) throw new IllegalArgumentException("Unknown shot stat");
        if (profile == null && (!sources.isEmpty() || scale != 1 || virtual)) throw new IllegalArgumentException("Invalid empty shot");
    }
    public ShotData projectile(double factor, boolean additional) { return new ShotData(profile, sources, factor, additional, weapon); }
    public double multishot() { return RangedStats.value(profile.multishot(), Stats.MULTISHOT, sources); }
    public double fireRate() { return RangedStats.value(profile.fireRate(), Stats.FIRE_RATE, sources); }
    public String encode() {
        JsonObject root = new JsonObject(); root.addProperty("schema", 2);
        if (weapon != null) root.addProperty("weapon", weapon.toString());
        root.add("profile", profile == null ? JsonNull.INSTANCE : JsonData.weaponJson(profile));
        JsonArray modifiers = new JsonArray(); sources.forEach(source -> modifiers.add(JsonParser.parseString(JsonData.encodeBonuses(source))));
        root.add("sources", modifiers); root.addProperty("scale", scale); root.addProperty("virtual", virtual);
        return root.toString();
    }
    public static ShotData decode(String json) {
        var root = JsonParser.parseString(json).getAsJsonObject(); JsonData.fields(root, "schema", "profile", "sources", "scale", "virtual", "weapon");
        if (JsonData.number(root, "schema", 0) != 1 && JsonData.number(root, "schema", 0) != 2) throw new IllegalArgumentException("Unsupported shot schema");
        List<Map<String, Bonus>> sources = new ArrayList<>();
        for (JsonElement source : root.getAsJsonArray("sources")) sources.add(JsonData.decodeBonuses(source.toString()));
        return new ShotData(root.get("profile").isJsonNull() ? null : JsonData.weapon(root.getAsJsonObject("profile")), sources, JsonData.number(root, "scale", 1), root.get("virtual").getAsBoolean(), root.has("weapon") ? UUID.fromString(root.get("weapon").getAsString()) : null);
    }
}
