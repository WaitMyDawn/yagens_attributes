package yagen.waitmydawn.yagens_attributes.data;

import com.google.gson.*;
import net.minecraft.server.packs.resources.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.core.*;
import java.util.*;

public final class ModDefinitions extends SimplePreparableReloadListener<ModCatalog> {
    public static final ModDefinitions INSTANCE = new ModDefinitions();
    private volatile ModCatalog catalog = ModCatalog.EMPTY;
    private ModDefinitions() {}
    public ModCatalog catalog() { return catalog; }
    @Override protected ModCatalog prepare(ResourceManager manager, ProfilerFiller profiler) {
        var object = new JsonObject(); var mods = new JsonObject(); var qualities = new JsonArray(); var polarities = new JsonArray();
        resources(manager, "mods").forEach((id, value) -> mods.add(id.toString(), value));
        resources(manager, "mod_rarities").values().forEach(qualities::add);
        resources(manager, "mod_polarities").values().forEach(polarities::add);
        object.add("mods", mods); object.add("qualities", qualities); object.add("polarities", polarities);
        var rivenResources = resources(manager, "riven_rules");
        if (rivenResources.size() != 1) throw new IllegalArgumentException("Expected one Riven rules resource");
        object.add("rivens", rivenResources.values().iterator().next());
        var result = ModJson.catalog(object);
        if (ModJson.GSON.toJson(ModJson.catalog(result)).length() > 250000) throw new IllegalArgumentException("Mod catalog exceeds synchronization limit");
        return result;
    }
    private static Map<ResourceLocation, JsonObject> resources(ResourceManager manager, String folder) {
        var result = new TreeMap<ResourceLocation, JsonObject>();
        manager.listResources(folder, path -> path.getPath().endsWith(".json")).forEach((path, resource) -> {
            var id = ResourceLocation.fromNamespaceAndPath(path.getNamespace(), path.getPath().substring(folder.length() + 1, path.getPath().length() - 5));
            try (var reader = resource.openAsReader()) { result.put(id, JsonParser.parseReader(reader).getAsJsonObject()); }
            catch (Exception exception) { throw new IllegalArgumentException("Invalid Mod resource " + path, exception); }
        }); return result;
    }
    @Override protected void apply(ModCatalog prepared, ResourceManager manager, ProfilerFiller profiler) {
        catalog = prepared;
        YagensAttributes.LOGGER.info("Loaded {} Mod definitions and {} rank variants", prepared.definitions().size(), prepared.creative().size());
    }
}
