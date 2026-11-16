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
    public record Prepared(Map<ResourceLocation, JsonElement> exact, Map<ResourceLocation, JsonElement> defaults) {}
    public record Rule(WeaponProfile profile, boolean deriveDamage) {}
    private record Snapshot(Map<Item, WeaponProfile> byItem, Map<ResourceLocation, WeaponProfile> byId) {}
    private volatile Snapshot snapshot = new Snapshot(Map.of(), Map.of());

    @Override protected Prepared prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, JsonElement> exact = new HashMap<>(), defaults = new HashMap<>();
        SimpleJsonResourceReloadListener.scanDirectory(manager, "weapon_stats", new Gson(), exact);
        SimpleJsonResourceReloadListener.scanDirectory(manager, "weapon_defaults", new Gson(), defaults);
        return new Prepared(Map.copyOf(exact), Map.copyOf(defaults));
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
                    var rule = new Rule(JsonData.weapon(definition), !definition.has("base_damage"));
                    if (result.put(family, rule) != null) throw new IllegalArgumentException("Duplicate family default: " + family);
                }
            } catch (RuntimeException exception) { throw new IllegalArgumentException("Invalid weapon defaults " + entry.getKey(), exception); }
        }
        return Map.copyOf(result);
    }

    @Override protected void apply(Prepared prepared, ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, WeaponProfile> exact = new HashMap<>();
        prepared.exact().forEach((id, json) -> {
            try { exact.put(id, JsonData.weapon(json.getAsJsonObject())); }
            catch (RuntimeException exception) { throw new IllegalArgumentException("Invalid weapon profile " + id, exception); }
        });
        var defaults = defaults(prepared.defaults());
        Map<Item, WeaponProfile> byItem = new IdentityHashMap<>();
        Map<ResourceLocation, WeaponProfile> byId = new HashMap<>(exact);
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            WeaponProfile profile = exact.get(id);
            if (profile == null) {
                var rule = WeaponFamilies.match(item.getClass()).map(defaults::get).orElse(null);
                if (rule == null) continue;
                profile = rule.profile();
                if (rule.deriveDamage()) {
                    double damage;
                    try { damage = nativeDamage(item.getDefaultInstance()); }
                    catch (RuntimeException exception) {
                        YagensAttributes.LOGGER.warn("Cannot inspect native weapon attributes for {}; an explicit profile is required", id, exception);
                        continue;
                    }
                    if (!Double.isFinite(damage) || damage <= 0) {
                        YagensAttributes.LOGGER.warn("Cannot derive a positive weapon baseline for {}; an explicit profile is required", id); continue;
                    }
                    profile = new WeaponProfile(profile.criticalChance(), profile.criticalDamage(), profile.statusChance(), profile.statusDuration(), profile.distribution(), damage, profile.category(), profile.slots(), profile.fireRate(), profile.multishot(), profile.rivenDisposition());
                }
            }
            byItem.put(item, profile); byId.put(id, profile);
        }
        snapshot = new Snapshot(Collections.unmodifiableMap(byItem), Map.copyOf(byId));
        YagensAttributes.LOGGER.info("Loaded {} exact weapon profiles, {} class defaults, {} resolved items", exact.size(), defaults.size(), byItem.size());
    }

    private static double nativeDamage(ItemStack stack) {
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
}
