package yagen.waitmydawn.yagens_attributes.data;

import com.google.gson.*;
import yagen.waitmydawn.yagens_attributes.core.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class ModNames {
    private static final Map<String, Map<String, String>> LANGUAGES = Map.of("en_us", load("en_us"), "zh_cn", load("zh_cn"));
    private ModNames() {}
    private static Map<String, String> load(String language) {
        try (var stream = ModNames.class.getResourceAsStream("/assets/yagens_attributes/lang/" + language + ".json")) {
            if (stream == null) throw new IllegalStateException("Missing bundled language: " + language);
            var object = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            Map<String, String> result = new HashMap<>();
            object.entrySet().forEach(entry -> result.put(entry.getKey(), entry.getValue().getAsString()));
            return Map.copyOf(result);
        } catch (IOException exception) { throw new IllegalStateException(exception); }
    }
    public static String name(String language, ModLedger.Entry entry) {
        if (entry.info().riven() != null) {
            String result = translate(language, entry.info().name()) + " · " + entry.info().riven().weapon();
            return result.length() > 80 ? result.substring(0, 79) + "…" : result;
        }
        if (!entry.info().name().isBlank()) return entry.info().defined() ? LANGUAGES.getOrDefault(language, LANGUAGES.get("en_us")).getOrDefault(entry.info().name(), entry.info().name()) : entry.info().name();
        String stat = Stats.KEYS.stream().filter(entry.bonuses()::containsKey).findFirst().orElse(Stats.DAMAGE);
        return LANGUAGES.getOrDefault(language, LANGUAGES.get("en_us")).getOrDefault("stat.yagens_attributes." + stat, stat);
    }
    public static String translate(String language, String key) {
        return LANGUAGES.getOrDefault(language, LANGUAGES.get("en_us")).getOrDefault(key, key);
    }
}
