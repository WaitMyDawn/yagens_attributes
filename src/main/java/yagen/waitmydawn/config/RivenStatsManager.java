package yagen.waitmydawn.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;
import yagen.waitmydawn.api.mods.RivenUniqueInfo;

import java.util.*;

public class RivenStatsManager extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private static final String ROOT_FOLDER = "riven_unique_info";
    private static final String DAMAGE_TYPE_FOLDER = "damagetype";
    private static final String ATTRIBUTE_FOLDER = "attribute";

    private static final Comparator<String> MOD_PRIORITY = (id1, id2) -> {
        if (id1.equals(id2)) return 0;
        if ("yagens_attributes".equals(id1)) return -1;
        if ("yagens_attributes".equals(id2)) return 1;

        if ("minecraft".equals(id1)) return -1;
        if ("minecraft".equals(id2)) return 1;

        return id1.compareTo(id2);
    };

    public RivenStatsManager() {
        super(GSON, ROOT_FOLDER);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        RivenUniqueInfo.reset();

        // Key: "melee/positive" -> Value: Map<ModId, List<FileEntry>>
        Map<String, Map<String, List<PendingFile>>> attributeBuffer = new HashMap<>();

        object.forEach((location, json) -> {
            try {
                // location.getPath() : melee/positive/damagetype/slash
                String fullPath = location.getPath();
                String[] parts = fullPath.split("/");

                if (parts.length < 3) return;

                String category = parts[0]; // melee or projectile
                String polarity = parts[1]; // positive or negative
                String type = parts[2];     // damagetype or attribute

                List<RivenStatConfig.Entry> entries = RivenStatConfig.CODEC.parse(JsonOps.INSTANCE, json)
                        .getOrThrow(IllegalStateException::new);

                List<RivenUniqueInfo> targetList = getTargetList(category, polarity);
                if (targetList == null) {
                    LOGGER.warn("Unknown Riven Category/Polarity: {}/{}", category, polarity);
                    return;
                }

                if (DAMAGE_TYPE_FOLDER.equals(type)) {
                    String damageName = parts[parts.length - 1];
                    for (RivenStatConfig.Entry entry : entries) {
                        // Key: tooltip.yagens_attributes.slash_addition
                        String key = "tooltip.yagens_attributes." + damageName + "_" + entry.operation();
                        targetList.add(new RivenUniqueInfo(key, entry.weight(), entry.baseValue()));
                    }
                } else if (ATTRIBUTE_FOLDER.equals(type)) {
                    // melee/positive/attribute/<modid>/<filename>
                    if (parts.length < 5) return;

                    String modId = parts[3];
                    String fileName = parts[4];
                    String listKey = category + "/" + polarity;

                    if (!modId.equals("minecraft") && !modId.equals("yagens_attributes") && !ModList.get().isLoaded(modId))
                        return;

                    attributeBuffer.computeIfAbsent(listKey, k -> new HashMap<>())
                            .computeIfAbsent(modId, k -> new ArrayList<>())
                            .add(new PendingFile(fileName, entries));

                    if (!"yagens_attributes".equals(modId) && !"minecraft".equals(modId)) {
                        RivenUniqueInfo.ATTRIBUTES_MODID.add(modId);
                    }
                }

            } catch (Exception e) {
                LOGGER.error("Failed to load Riven JSON: {}", location, e);
            }
        });

        processAttributes(attributeBuffer);

        LOGGER.info("Riven Stats Reloaded. MeleePos: {}, MeleeNeg: {}, ProjPos: {}, ProjNeg: {}",
                RivenUniqueInfo.MELEE_POSITIVE.size(), RivenUniqueInfo.MELEE_NEGATIVE.size(),
                RivenUniqueInfo.PROJECTILE_POSITIVE.size(), RivenUniqueInfo.PROJECTILE_NEGATIVE.size());
    }

    private void processAttributes(Map<String, Map<String, List<PendingFile>>> buffer) {
        buffer.forEach((listKey, modMap) -> {
            String[] keys = listKey.split("/");
            List<RivenUniqueInfo> targetList = getTargetList(keys[0], keys[1]);
            if (targetList == null) return;

            List<String> sortedMods = modMap.keySet().stream()
                    .sorted(MOD_PRIORITY)
                    .toList();

            for (String modId : sortedMods) {
                List<PendingFile> files = modMap.get(modId);
                files.sort(Comparator.comparing(f -> f.fileName));

                for (PendingFile file : files) {
                    String rawFileName = file.fileName;
                    String finalPathName = rawFileName;

                    if (!modId.equals("yagens_attributes") && !modId.equals("minecraft"))
                        finalPathName = modId + "." + rawFileName;

                    for (RivenStatConfig.Entry entry : file.entries) {
                        // Key: tooltips.<modid>.<final_name>_<operation>
                        String translationKey = "tooltips.yagens_attributes." + finalPathName + "_" + entry.operation();
                        targetList.add(new RivenUniqueInfo(translationKey, entry.weight(), entry.baseValue()));
                    }
                }
            }
        });
    }

    private List<RivenUniqueInfo> getTargetList(String category, String polarity) {
        if ("melee".equals(category)) {
            return "positive".equals(polarity) ? RivenUniqueInfo.MELEE_POSITIVE : RivenUniqueInfo.MELEE_NEGATIVE;
        } else if ("projectile".equals(category)) {
            return "positive".equals(polarity) ? RivenUniqueInfo.PROJECTILE_POSITIVE : RivenUniqueInfo.PROJECTILE_NEGATIVE;
        }
        return null;
    }

    private record PendingFile(String fileName, List<RivenStatConfig.Entry> entries) {
    }
}