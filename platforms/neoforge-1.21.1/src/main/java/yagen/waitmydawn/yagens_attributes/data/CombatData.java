package yagen.waitmydawn.yagens_attributes.data;

import com.google.gson.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.*;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.LivingEntity;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.core.*;
import java.util.*;

public final class CombatData extends SimpleJsonResourceReloadListener {
    public static final WeaponProfiles WEAPONS = new WeaponProfiles();
    public static final CombatData MATERIALS = new CombatData();
    private List<Material> rules = List.of();
    private final Map<net.minecraft.world.entity.EntityType<?>, Map<DamageType, Double>> cache = new IdentityHashMap<>();

    private record Material(ResourceLocation id, int priority, List<ResourceLocation> entities,
                            List<TagKey<net.minecraft.world.entity.EntityType<?>>> tags, Map<DamageType, Double> multipliers) {
        boolean matches(net.minecraft.world.entity.EntityType<?> type) {
            return entities.contains(BuiltInRegistries.ENTITY_TYPE.getKey(type)) || tags.stream().anyMatch(type::is);
        }
    }

    private CombatData() { super(new Gson(), "health_materials"); }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> json, ResourceManager manager, ProfilerFiller profiler) {
        var nextMaterials = new ArrayList<Material>();
        for (var entry : json.entrySet()) {
            try {
                JsonObject object = entry.getValue().getAsJsonObject();
                JsonData.fields(object, "priority", "entities", "entity_tags", "multipliers");
                var entities = new ArrayList<ResourceLocation>();
                var tags = new ArrayList<TagKey<net.minecraft.world.entity.EntityType<?>>>();
                if (object.has("entities")) for (JsonElement id : object.getAsJsonArray("entities")) entities.add(ResourceLocation.parse(id.getAsString()));
                if (object.has("entity_tags")) for (JsonElement id : object.getAsJsonArray("entity_tags")) tags.add(TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse(id.getAsString())));
                double priority = JsonData.number(object, "priority", 0);
                if (priority != (int) priority) throw new IllegalArgumentException("Priority must be an integer");
                nextMaterials.add(new Material(entry.getKey(), (int) priority, List.copyOf(entities), List.copyOf(tags), JsonData.damage(object.getAsJsonObject("multipliers"))));
            } catch (RuntimeException exception) {
                // A bad reload must not publish a partially valid table.
                throw new IllegalArgumentException("Invalid combat resource " + entry.getKey(), exception);
            }
        }
        nextMaterials.sort(Comparator.comparingInt(Material::priority).reversed().thenComparing(rule -> rule.id().toString()));
        rules = List.copyOf(nextMaterials);
        invalidate();
        YagensAttributes.LOGGER.info("Loaded {} material definitions", json.size());
    }

    public Map<DamageType, Double> material(LivingEntity entity) {
        return cache.computeIfAbsent(entity.getType(), type -> {
            for (Material rule : rules) if (rule.matches(type)) return rule.multipliers();
            return Map.of();
        });
    }
    public void invalidate() { cache.clear(); }
}
