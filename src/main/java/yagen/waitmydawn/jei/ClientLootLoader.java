package yagen.waitmydawn.jei;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModFileInfo;
import yagen.waitmydawn.YagensAttributes;

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
            int minLevel, int maxLevel
    ) {}

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
                    if (poolObj.has("entries")) {
                        JsonArray entries = poolObj.getAsJsonArray("entries");
                        for (JsonElement entry : entries) {
                            if (entry.isJsonObject()) {
                                processEntry(entry.getAsJsonObject(), tableId, results);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void processEntry(JsonObject entryObj, ResourceLocation tableId, List<LootInfo> results) {
        if (entryObj.has("functions")) {
            JsonArray functions = entryObj.getAsJsonArray("functions");
            for (JsonElement func : functions) {
                if (func.isJsonObject()) {
                    processFunction(func.getAsJsonObject(), tableId, results);
                }
            }
        }

        if (entryObj.has("children")) {
            JsonArray children = entryObj.getAsJsonArray("children");
            for (JsonElement child : children) {
                if (child.isJsonObject()) {
                    processEntry(child.getAsJsonObject(), tableId, results);
                }
            }
        }
    }

    private static void processFunction(JsonObject func, ResourceLocation tableId, List<LootInfo> results) {
        if (func.has("function") &&
                func.get("function").getAsString().equals("yagens_attributes:randomize_mod")) {

            int common = getInt(func, "common_weight", 50);
            int uncommon = getInt(func, "uncommon_weight", 40);
            int rare = getInt(func, "rare_weight", 10);
            int legendary = getInt(func, "legendary_weight", 0);
            int minLvl = getInt(func, "level_percent_inf", 0);
            int maxLvl = getInt(func, "level_percent_sup", 50);

            results.add(new LootInfo(tableId, common, uncommon, rare, legendary, minLvl, maxLvl));
        }
    }

    private static int getInt(JsonObject obj, String key, int def) {
        return obj.has(key) ? obj.get(key).getAsInt() : def;
    }
}