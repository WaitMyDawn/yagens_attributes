package yagen.waitmydawn.yagens_attributes.data;

import com.google.gson.*;
import com.mojang.serialization.*;
import yagen.waitmydawn.yagens_attributes.core.*;
import java.util.*;

/** Immutable launch-time stats are shared in memory and serialized only when a projectile is saved. */
public record ShotData(WeaponProfile profile, List<Map<String, Bonus>> sources, double scale, boolean virtual, UUID weapon,
                       List<ConditionalModifier> targetModifiers, UUID owner, AttackKind attackKind) {
    public ShotData(WeaponProfile profile, List<Map<String, Bonus>> sources, double scale, boolean virtual, UUID weapon, List<ConditionalModifier> targetModifiers) {
        this(profile, sources, scale, virtual, weapon, targetModifiers, null, weapon == null ? AttackKind.UNKNOWN : AttackKind.RANGED);
    }
    public ShotData(WeaponProfile profile, List<Map<String, Bonus>> sources, double scale, boolean virtual, UUID weapon) {
        this(profile, sources, scale, virtual, weapon, List.of());
    }
    public ShotData(WeaponProfile profile, List<Map<String, Bonus>> sources, double scale, boolean virtual) { this(profile, sources, scale, virtual, null); }
    public static final ShotData EMPTY = new ShotData(null, List.of(), 1, false);
    public static final Codec<ShotData> CODEC = Codec.STRING.comapFlatMap(value -> {
        try { return DataResult.success(decode(value)); }
        catch (RuntimeException exception) { return DataResult.error(() -> "Invalid ranged shot: " + exception.getMessage()); }
    }, ShotData::encode);
    public ShotData {
        Objects.requireNonNull(attackKind).validateWeapon(weapon);
        targetModifiers = List.copyOf(targetModifiers);
        if (targetModifiers.size() > 128 || targetModifiers.stream().anyMatch(modifier -> modifier.condition().trigger() != ConditionalModifier.Trigger.TARGET_STATUS_TYPES))
            throw new IllegalArgumentException("Invalid deferred target modifiers");
        if (sources.size() > 129 || !Double.isFinite(scale) || scale < 0 || scale > RangedStats.LIMIT) throw new IllegalArgumentException("Invalid shot snapshot");
        if (profile == null && (!sources.isEmpty() || !targetModifiers.isEmpty() || scale != 1 || virtual)) throw new IllegalArgumentException("Invalid empty shot");
        if (!(sources instanceof FrozenSources)) sources = new FrozenSources(sources);
    }
    public ShotData projectile(double factor, boolean additional) {
        return Double.compare(scale, factor) == 0 && virtual == additional
                ? this : new ShotData(profile, sources, factor, additional, weapon, targetModifiers, owner, attackKind);
    }

    private static final class FrozenSources extends AbstractList<Map<String, Bonus>> implements RandomAccess {
        private final List<Map<String, Bonus>> ordered;

        private FrozenSources(List<Map<String, Bonus>> sources) {
            var copies = new ArrayList<Map<String, Bonus>>(sources.size());
            for (var source : sources) {
                if (!Stats.KEYS.containsAll(source.keySet())) throw new IllegalArgumentException("Unknown shot stat");
                // Element fusion uses iteration order within each source.
                copies.add(Collections.unmodifiableMap(new LinkedHashMap<>(source)));
            }
            ordered = List.copyOf(copies);
        }

        @Override public Map<String, Bonus> get(int index) { return ordered.get(index); }
        @Override public int size() { return ordered.size(); }
        @Override public Iterator<Map<String, Bonus>> iterator() { return ordered.iterator(); }
    }
    public ShotData baseMultishot(double value) {
        var p = profile;
        return new ShotData(new WeaponProfile(p.criticalChance(), p.criticalDamage(), p.statusChance(), p.statusDuration(),
                p.distribution(), p.baseDamage(), p.category(), p.slots(), p.fireRate(), value, p.rivenDisposition()),
                sources, scale, virtual, weapon, targetModifiers, owner, attackKind);
    }
    public double multishot() { return RangedStats.value(profile.multishot(), Stats.MULTISHOT, sources); }
    public double fireRate() { return RangedStats.value(profile.fireRate(), Stats.FIRE_RATE, sources); }
    public String encode() {
        JsonObject root = new JsonObject(); root.addProperty("schema", 4);
        if (weapon != null) root.addProperty("weapon", weapon.toString());
        if (owner != null) root.addProperty("owner", owner.toString());
        root.addProperty("attack_kind", attackKind.key());
        root.add("profile", profile == null ? JsonNull.INSTANCE : JsonData.weaponJson(profile));
        JsonArray modifiers = new JsonArray(); sources.forEach(source -> modifiers.add(JsonParser.parseString(JsonData.encodeBonuses(source))));
        root.add("sources", modifiers); root.addProperty("scale", scale); root.addProperty("virtual", virtual);
        JsonArray target = new JsonArray(); targetModifiers.forEach(modifier -> target.add(ModJson.conditional(modifier))); root.add("target_modifiers", target);
        return root.toString();
    }
    public static ShotData decode(String json) {
        var root = JsonParser.parseString(json).getAsJsonObject(); JsonData.fields(root, "schema", "profile", "sources", "scale", "virtual", "weapon", "target_modifiers", "owner", "attack_kind");
        double schema = JsonData.number(root, "schema", 0);
        if (schema != 1 && schema != 2 && schema != 3 && schema != 4) throw new IllegalArgumentException("Unsupported shot schema");
        List<Map<String, Bonus>> sources = new ArrayList<>();
        for (JsonElement source : root.getAsJsonArray("sources")) sources.add(JsonData.decodeBonuses(source.toString()));
        List<ConditionalModifier> target = new ArrayList<>();
        if (root.has("target_modifiers")) for (var modifier : root.getAsJsonArray("target_modifiers")) target.add(ModJson.conditional(modifier.getAsJsonObject()));
        UUID weapon = root.has("weapon") ? UUID.fromString(root.get("weapon").getAsString()) : null;
        return new ShotData(root.get("profile").isJsonNull() ? null : JsonData.weapon(root.getAsJsonObject("profile")), sources,
                JsonData.number(root, "scale", 1), root.get("virtual").getAsBoolean(), weapon, target,
                schema >= 4 && root.has("owner") ? UUID.fromString(root.get("owner").getAsString()) : null,
                schema >= 4 && root.has("attack_kind") ? AttackKind.parse(root.get("attack_kind").getAsString()) : weapon == null ? AttackKind.UNKNOWN : AttackKind.RANGED);
    }
}
