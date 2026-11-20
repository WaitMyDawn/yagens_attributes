package yagen.waitmydawn.yagens_attributes.data;

import com.google.gson.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.core.*;
import java.util.*;

public final class WeaponProfiles extends SimplePreparableReloadListener<WeaponProfiles.Prepared> {
    public record Prepared(Map<ResourceLocation, JsonElement> exact, Map<ResourceLocation, JsonElement> defaults,
                           Map<ResourceLocation, JsonElement> attacks) {
        public Prepared(Map<ResourceLocation, JsonElement> exact, Map<ResourceLocation, JsonElement> defaults) {
            this(exact, defaults, Map.of());
        }
    }
    public record Rule(WeaponProfile profile, boolean deriveDamage) {
        public WeaponProfile resolve(double nativeDamage) {
            return deriveDamage ? new WeaponProfile(profile.criticalChance(), profile.criticalDamage(), profile.statusChance(),
                    profile.statusDuration(), profile.distribution(), nativeDamage, profile.category(), profile.slots(),
                    profile.fireRate(), profile.multishot(), profile.rivenDisposition()) : profile;
        }
    }
    public record Attack(WeaponProfile profile, AttackKind kind, List<DamageType> forcedProcs) {}
    private record AttackRule(WeaponProfile stats, boolean overrideDuration, AttackKind kind, List<DamageType> forcedProcs) {
        Attack resolve(WeaponProfile base) {
            return new Attack(new WeaponProfile(stats.criticalChance(), stats.criticalDamage(), stats.statusChance(),
                    overrideDuration ? stats.statusDuration() : base.statusDuration(), stats.distribution(), base.baseDamage(),
                    base.category(), base.slots(), base.fireRate(), base.multishot(), base.rivenDisposition()), kind, forcedProcs);
        }
    }
    private record Snapshot(Map<Item, WeaponProfile> byItem, Map<ResourceLocation, WeaponProfile> byId,
                            Map<ResourceLocation, Map<ResourceLocation, Attack>> attacks) {}
    private volatile Snapshot snapshot = new Snapshot(Map.of(), Map.of(), Map.of());

    @Override protected Prepared prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, JsonElement> exact = new HashMap<>(), defaults = new HashMap<>(), attacks = new HashMap<>();
        SimpleJsonResourceReloadListener.scanDirectory(manager, "weapon_stats", new Gson(), exact);
        SimpleJsonResourceReloadListener.scanDirectory(manager, "weapon_defaults", new Gson(), defaults);
        SimpleJsonResourceReloadListener.scanDirectory(manager, "weapon_attacks", new Gson(), attacks);
        return new Prepared(Map.copyOf(exact), Map.copyOf(defaults), Map.copyOf(attacks));
    }

    public static Map<WeaponFamilies.Family, Rule> defaults(Map<ResourceLocation, JsonElement> documents) {
        var result = new EnumMap<WeaponFamilies.Family, Rule>(WeaponFamilies.Family.class);
        for (var entry : documents.entrySet()) {
            try {
                JsonObject root = entry.getValue().getAsJsonObject();
                JsonData.fields(root, "schema_version", "classes");
                if (JsonData.number(root, "schema_version", 0) != 1) throw new IllegalArgumentException("Unsupported defaults schema");
                var classes = root.getAsJsonObject("classes");
                for (var object : classes.entrySet()) {
                    var family = WeaponFamilies.Family.valueOf(object.getKey().toUpperCase(Locale.ROOT));
                    JsonObject definition = object.getValue().getAsJsonObject().deepCopy();
                    if (!definition.has("category")) definition.addProperty("category", family.category().key());
                    if (result.put(family, rule(definition)) != null) throw new IllegalArgumentException("Duplicate family default: " + family);
                }
            } catch (RuntimeException exception) { throw new IllegalArgumentException("Invalid weapon defaults " + entry.getKey(), exception); }
        }
        return Map.copyOf(result);
    }

    public static Rule rule(JsonObject definition) {
        return new Rule(JsonData.weapon(definition), !definition.has("base_damage"));
    }

    @Override protected void apply(Prepared prepared, ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, Rule> exact = new HashMap<>();
        prepared.exact().forEach((id, json) -> {
            try { exact.put(id, rule(json.getAsJsonObject())); }
            catch (RuntimeException exception) { throw new IllegalArgumentException("Invalid weapon profile " + id, exception); }
        });
        var defaults = defaults(prepared.defaults());
        Map<Item, WeaponProfile> byItem = new IdentityHashMap<>();
        Map<ResourceLocation, WeaponProfile> byId = new HashMap<>();
        exact.forEach((id, rule) -> { if (!rule.deriveDamage()) byId.put(id, rule.profile()); });
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            var rule = exact.get(id);
            if (rule == null) rule = WeaponFamilies.match(item.getClass()).map(defaults::get).orElse(null);
            if (rule == null) continue;
            WeaponProfile profile;
            try { profile = rule.resolve(rule.deriveDamage() ? nativeDamage(item.getDefaultInstance()) : 0); }
            catch (RuntimeException exception) {
                YagensAttributes.LOGGER.warn("Cannot resolve weapon baseline for {}; set an explicit positive base_damage", id, exception);
                continue;
            }
            byItem.put(item, profile); byId.put(id, profile);
        }
        var attacks = attacks(prepared.attacks(), byId);
        snapshot = new Snapshot(Collections.unmodifiableMap(byItem), Map.copyOf(byId), attacks);
        YagensAttributes.LOGGER.info("Loaded {} exact weapon profiles, {} class defaults, {} resolved items, {} items with attack profiles",
                exact.size(), defaults.size(), byItem.size(), attacks.size());
    }

    static Map<ResourceLocation, Map<ResourceLocation, Attack>> attacks(Map<ResourceLocation, JsonElement> documents,
                                                                         Map<ResourceLocation, WeaponProfile> weapons) {
        var result = new HashMap<ResourceLocation, Map<ResourceLocation, Attack>>();
        documents.forEach((item, json) -> {
            var modes = new HashMap<ResourceLocation, Attack>();
            WeaponProfile base = weapons.get(item);
            try {
                var root = json.getAsJsonObject();
                JsonData.fields(root, "attacks");
                root.getAsJsonObject("attacks").entrySet().forEach(entry -> {
                    ResourceLocation attackId = ResourceLocation.parse(entry.getKey());
                    AttackRule rule = attackRule(entry.getValue().getAsJsonObject());
                    if (base != null && modes.put(attackId, rule.resolve(base)) != null)
                        throw new IllegalArgumentException("Duplicate attack " + attackId);
                });
            } catch (RuntimeException exception) {
                throw new IllegalArgumentException("Invalid weapon attacks " + item, exception);
            }
            if (!modes.isEmpty()) result.put(item, Map.copyOf(modes));
        });
        return Map.copyOf(result);
    }

    private static AttackRule attackRule(JsonObject object) {
        JsonData.fields(object, "kind", "critical_chance", "critical_damage", "status_chance", "status_duration",
                "damage_distribution", "forced_procs");
        var kind = AttackKind.parse(object.get("kind").getAsString());
        if (kind == AttackKind.UNKNOWN) throw new IllegalArgumentException("Attack kind must be melee or ranged");
        var forced = EnumSet.noneOf(DamageType.class);
        if (object.has("forced_procs")) for (var type : object.getAsJsonArray("forced_procs")) {
            if (!forced.add(DamageType.parse(type.getAsString()))) throw new IllegalArgumentException("Duplicate forced proc " + type);
        }
        var stats = new WeaponProfile(JsonData.number(object, "critical_chance", Double.NaN),
                JsonData.number(object, "critical_damage", Double.NaN), JsonData.number(object, "status_chance", Double.NaN),
                JsonData.number(object, "status_duration", 0), JsonData.damage(object.getAsJsonObject("damage_distribution")));
        return new AttackRule(stats, object.has("status_duration"), kind, List.copyOf(forced));
    }

    private static double nativeDamage(ItemStack stack) {
        var supplied = yagen.waitmydawn.yagens_attributes.compat.NativeWeapons.baseline(stack);
        if (supplied.isPresent()) return supplied.getAsDouble();
        double[] sums = {0, 0}; List<Double> factors = new ArrayList<>();
        stack.getAttributeModifiers().forEach(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            if (!attribute.equals(Attributes.ATTACK_DAMAGE)) return;
            switch (modifier.operation()) {
                case ADD_VALUE -> sums[0] += modifier.amount();
                case ADD_MULTIPLIED_BASE -> sums[1] += modifier.amount();
                case ADD_MULTIPLIED_TOTAL -> factors.add(1 + modifier.amount());
            }
        });
        // Profiles use the vanilla player's unequipped attack baseline; real hits retain their native pipeline.
        return Bonus.evaluate(1, List.of(new Bonus(sums[0], sums[1], factors, 0)));
    }
    public WeaponProfile weapon(ItemStack stack) { return stack.isEmpty() ? null : snapshot.byItem().get(stack.getItem()); }
    public Map<ResourceLocation, WeaponProfile> profiles() { return snapshot.byId(); }
    public Attack attack(ResourceLocation item, ResourceLocation attack) {
        return snapshot.attacks().getOrDefault(item, Map.of()).get(attack);
    }
}
