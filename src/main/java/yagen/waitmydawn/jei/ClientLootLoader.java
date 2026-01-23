package yagen.waitmydawn.jei;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModFileInfo;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.util.SupportedMod;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ClientLootLoader {
    public record LootInfo(
            ResourceLocation tableId,
            int common, int uncommon, int rare, int legendary,
            int minLevel, int maxLevel,
            float baseChance,
            float minRolls, float maxRolls
    ) {
    }

    private record RollRange(float min, float max) {
    }

    public static List<LootInfo> scanAll() {
        List<LootInfo> results = new ArrayList<>();
        IModFileInfo modFile = ModList.get().getModFileById(YagensAttributes.MODID);
        if (modFile == null) return results;
        Path root = modFile.getFile().findResource("data", YagensAttributes.MODID, "loot_table");

        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".json"))
                    .forEach(path -> {
                        try (BufferedReader reader = Files.newBufferedReader(path)) {
                            JsonObject rootJson = JsonParser.parseReader(reader).getAsJsonObject();
                            String relativePath = root.relativize(path).toString();
                            relativePath = relativePath.replace("\\", "/").replace(".json", "");
                            ResourceLocation tableId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, relativePath);
                            String tableName = tableId.getPath();
                            int start = tableName.lastIndexOf("/") + 1;
                            int end = tableName.indexOf("_additional");
                            String id="minecraft";
                            if (start < end)
                                id = tableName.substring(start, end);
                            if (id.equals(SupportedMod.DUNGEONS_ARISE.getValue())) {
                                if (!ModList.get().isLoaded(SupportedMod.DUNGEONS_ARISE.getValue()))
                                    return;
                            } else if (id.equals(SupportedMod.TWILIGHT_FOREST.getValue())) {
                                if (!ModList.get().isLoaded(SupportedMod.TWILIGHT_FOREST.getValue()))
                                    return;
                            }
                            findFunctionInJson(rootJson, tableId, results);
                        } catch (Exception e) {
                            YagensAttributes.LOGGER.error("Failed to parse: {}", path, e);
                        }
                    });
        } catch (Exception e) {
            YagensAttributes.LOGGER.error("Failed to walk loot table directory", e);
        }

        YagensAttributes.LOGGER.info("Client Loot Scan finished. Found {} entries.", results.size());
        return results;
    }

    private static void findFunctionInJson(JsonObject obj, ResourceLocation tableId, List<LootInfo> results) {
        if (obj.has("pools")) {
            JsonArray pools = obj.getAsJsonArray("pools");
            for (JsonElement pool : pools) {
                if (pool.isJsonObject()) {
                    JsonObject poolObj = pool.getAsJsonObject();
                    float poolChance = parseChance(poolObj);
                    RollRange rolls = new RollRange(1.0f, 1.0f);
                    if (poolObj.has("rolls")) {
                        rolls = parseRolls(poolObj.get("rolls"));
                    }
                    if (poolObj.has("entries")) {
                        JsonArray entries = poolObj.getAsJsonArray("entries");
                        for (JsonElement entry : entries) {
                            if (entry.isJsonObject()) {
                                processEntry(entry.getAsJsonObject(), tableId, results, poolChance, rolls);
                            }
                        }
                    }
                }
            }
        }
    }

    private static float parseChance(JsonObject obj) {
        if (!obj.has("conditions")) return 1.0f;
        JsonArray conditions = obj.getAsJsonArray("conditions");
        float chance = 1.0f;
        for (JsonElement elem : conditions) {
            if (elem.isJsonObject()) {
                JsonObject cond = elem.getAsJsonObject();
                if (cond.has("condition") &&
                        cond.get("condition").getAsString().equals("minecraft:random_chance")) {

                    if (cond.has("chance")) {
                        chance *= cond.get("chance").getAsFloat();
                    }
                }
            }
        }
        return chance;
    }

    private static void processEntry(JsonObject entryObj, ResourceLocation tableId, List<LootInfo> results, float parentChance, RollRange rolls) {
        float currentChance = parentChance * parseChance(entryObj);
        if (entryObj.has("functions")) {
            JsonArray functions = entryObj.getAsJsonArray("functions");
            for (JsonElement func : functions) {
                if (func.isJsonObject()) {
                    processFunction(func.getAsJsonObject(), tableId, results, currentChance, rolls);
                }
            }
        }

        if (entryObj.has("children")) {
            JsonArray children = entryObj.getAsJsonArray("children");
            for (JsonElement child : children) {
                if (child.isJsonObject()) {
                    processEntry(child.getAsJsonObject(), tableId, results, currentChance, rolls);
                }
            }
        }
    }

    private static void processFunction(JsonObject func, ResourceLocation tableId, List<LootInfo> results, float finalChance, RollRange rolls) {
        if (func.has("function") &&
                func.get("function").getAsString().equals("yagens_attributes:randomize_mod")) {

            int common = getInt(func, "common_weight", 50);
            int uncommon = getInt(func, "uncommon_weight", 40);
            int rare = getInt(func, "rare_weight", 10);
            int legendary = getInt(func, "legendary_weight", 0);
            int minLvl = getInt(func, "level_percent_inf", 0);
            int maxLvl = getInt(func, "level_percent_sup", 50);

            results.add(new LootInfo(tableId, common, uncommon, rare, legendary, minLvl, maxLvl, finalChance, rolls.min, rolls.max));
        }
    }

    private static RollRange parseRolls(JsonElement json) {
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber()) {
            float val = json.getAsFloat();
            return new RollRange(val, val);
        }

        if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();

            if (obj.has("min") && obj.has("max")) {
                float min = obj.get("min").isJsonPrimitive() ? obj.get("min").getAsFloat() : 1.0f;
                float max = obj.get("max").isJsonPrimitive() ? obj.get("max").getAsFloat() : 1.0f;
                return new RollRange(min, max);
            }

            if (obj.has("value")) {
                float val = obj.get("value").getAsFloat();
                return new RollRange(val, val);
            }
        }

        return new RollRange(1.0f, 1.0f);
    }

    private static int getInt(JsonObject obj, String key, int def) {
        return obj.has(key) ? obj.get(key).getAsInt() : def;
    }
}