package yagen.waitmydawn.yagens_attributes.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.core.WeakPointRegion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public final class WeakPointDefinitions extends SimplePreparableReloadListener<WeakPointDefinitions.Rules> {
    public static final WeakPointDefinitions INSTANCE = new WeakPointDefinitions();

    private Rules rules = new Rules(List.of(), Map.of());
    private Map<ResourceLocation, List<WeakPointRegion>> profiles = Map.of();
    private boolean dirty = true;

    record Rules(List<Template> templates, Map<ResourceLocation, EntityRule> entities) {}
    private record Template(ResourceLocation id, int priority, List<ResourceLocation> types,
                            List<TagKey<EntityType<?>>> tags, List<WeakPointRegion> regions) {
        boolean matches(ResourceLocation key, EntityType<?> type) {
            return types.contains(key) || tags.stream().anyMatch(type::is);
        }
    }
    private record EntityRule(boolean replace, List<String> remove, List<WeakPointRegion> regions) {}

    private WeakPointDefinitions() {}

    @Override
    protected Rules prepare(ResourceManager manager, ProfilerFiller profiler) {
        var templates = new ArrayList<Template>();
        resources(manager, "weak_points/templates").forEach((id, json) -> {
            try {
                JsonData.fields(json, "priority", "entity_types", "entity_tags", "regions");
                var types = strings(json, "entity_types").stream().map(ResourceLocation::parse).toList();
                List<TagKey<EntityType<?>>> tags = strings(json, "entity_tags").stream()
                        .map(tag -> TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse(tag))).toList();
                double priority = JsonData.number(json, "priority", 0);
                if (priority != (int) priority || types.isEmpty() && tags.isEmpty())
                    throw new IllegalArgumentException("Expected an integer priority and an explicit entity type or tag");
                templates.add(new Template(id, (int) priority, types, tags, regions(json)));
            } catch (RuntimeException exception) {
                throw new IllegalArgumentException("Invalid weak point template " + id, exception);
            }
        });
        templates.sort(Comparator.comparingInt(Template::priority).thenComparing(Template::id));
        var entities = new TreeMap<ResourceLocation, EntityRule>();
        resources(manager, "weak_points/entities").forEach((id, json) -> {
            try {
                JsonData.fields(json, "replace", "remove", "regions");
                var remove = strings(json, "remove");
                remove.forEach(WeakPointRegion::validateId);
                boolean replace = false;
                if (json.has("replace")) {
                    var flag = json.getAsJsonPrimitive("replace");
                    if (!flag.isBoolean()) throw new IllegalArgumentException("Expected boolean replace");
                    replace = flag.getAsBoolean();
                }
                entities.put(id, new EntityRule(replace, remove, regions(json)));
            } catch (RuntimeException exception) {
                throw new IllegalArgumentException("Invalid weak point definition for " + id, exception);
            }
        });
        return new Rules(List.copyOf(templates), Map.copyOf(entities));
    }

    @Override
    protected void apply(Rules next, ResourceManager manager, ProfilerFiller profiler) {
        rules = next;
        invalidate();
    }

    public void invalidate() {
        dirty = true;
    }

    public List<WeakPointRegion> regions(EntityType<?> type) {
        return profiles().getOrDefault(BuiltInRegistries.ENTITY_TYPE.getKey(type), List.of());
    }

    public Map<ResourceLocation, List<WeakPointRegion>> profiles() {
        if (!dirty) return profiles;
        dirty = false;
        // Tags are rebound after reload listeners apply. Resolve against the final server tags on demand.
        var next = new TreeMap<ResourceLocation, List<WeakPointRegion>>();
        for (var key : BuiltInRegistries.ENTITY_TYPE.keySet()) {
            var merged = new TreeMap<String, WeakPointRegion>();
            var type = BuiltInRegistries.ENTITY_TYPE.get(key);
            var exact = rules.entities().get(key);
            if (exact == null || !exact.replace()) {
                for (var template : rules.templates()) {
                    if (template.matches(key, type)) template.regions().forEach(region -> merged.put(region.id(), region));
                }
            }
            if (exact != null) {
                exact.remove().forEach(merged::remove);
                exact.regions().forEach(region -> merged.put(region.id(), region));
            }
            if (merged.size() > WeakPointRegion.MAX_PER_ENTITY) {
                YagensAttributes.LOGGER.error("Ignoring weak points for {}: maximum of {} regions exceeded.", key, WeakPointRegion.MAX_PER_ENTITY);
                continue;
            }
            if (!merged.isEmpty()) next.put(key, List.copyOf(merged.values()));
        }
        profiles = Map.copyOf(next);
        return profiles;
    }

    private static Map<ResourceLocation, JsonObject> resources(ResourceManager manager, String directory) {
        var converter = FileToIdConverter.json(directory);
        var result = new TreeMap<ResourceLocation, JsonObject>();
        converter.listMatchingResources(manager).forEach((path, resource) -> {
            try (var reader = resource.openAsReader()) {
                result.put(converter.fileToId(path), JsonParser.parseReader(reader).getAsJsonObject());
            } catch (IOException | RuntimeException exception) {
                throw new IllegalArgumentException("Invalid weak point resource " + path + " in " + resource.sourcePackId(), exception);
            }
        });
        return result;
    }

    private static List<WeakPointRegion> regions(JsonObject json) {
        if (!json.has("regions")) return List.of();
        var map = json.getAsJsonObject("regions");
        if (map.size() > WeakPointRegion.MAX_PER_ENTITY) throw new IllegalArgumentException("Too many weak point regions");
        var result = new ArrayList<WeakPointRegion>();
        for (var entry : map.entrySet()) {
            var value = entry.getValue().getAsJsonObject();
            JsonData.fields(value, "anchor", "rotation", "min", "max", "state", "part");
            var anchor = WeakPointRegion.Anchor.valueOf(key(value, "anchor", "feet"));
            var rotation = WeakPointRegion.Rotation.valueOf(key(value, "rotation", "body"));
            var state = WeakPointRegion.State.valueOf(key(value, "state", "always"));
            var part = WeakPointRegion.Part.valueOf(key(value, "part", "body"));
            result.add(new WeakPointRegion(entry.getKey(), anchor, rotation, point(value, "min"), point(value, "max"), state, part));
        }
        return List.copyOf(result);
    }

    private static String key(JsonObject json, String key, String fallback) {
        return (json.has(key) ? string(json.get(key)) : fallback).toUpperCase(Locale.ROOT);
    }

    private static List<String> strings(JsonObject json, String name) {
        var result = new ArrayList<String>();
        if (json.has(name)) json.getAsJsonArray(name).forEach(value -> result.add(string(value)));
        if (Set.copyOf(result).size() != result.size()) throw new IllegalArgumentException("Duplicate entry in " + name);
        return List.copyOf(result);
    }

    private static String string(JsonElement json) {
        if (!json.isJsonPrimitive() || !json.getAsJsonPrimitive().isString()) throw new IllegalArgumentException("Expected string");
        return json.getAsString();
    }

    private static WeakPointRegion.Point point(JsonObject json, String key) {
        var array = json.getAsJsonArray(key);
        if (array == null || array.size() != 3) throw new IllegalArgumentException("Expected three coordinates in " + key);
        for (var element : array) {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber())
                throw new IllegalArgumentException("Expected numeric coordinates in " + key);
        }
        return new WeakPointRegion.Point(array.get(0).getAsDouble(), array.get(1).getAsDouble(), array.get(2).getAsDouble());
    }
}
