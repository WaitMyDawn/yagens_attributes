package yagen.waitmydawn.yagens_attributes.datagen;

import com.google.common.hash.Hashing;
import com.google.gson.*;
import net.minecraft.data.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.core.ui.ModAnimations;
import yagen.waitmydawn.yagens_attributes.data.*;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class ModDataProvider implements DataProvider {
    private final Path root;
    private final ResourceManager existingClientResources;

    public ModDataProvider(PackOutput output, ResourceManager existingClientResources) {
        root = output.getOutputFolder();
        this.existingClientResources = existingClientResources;
    }

    public static void gather(GatherDataEvent event) {
        event.getGenerator().addProvider(true, new ModDataProvider(event.getGenerator().getPackOutput(), event.getResourceManager(PackType.CLIENT_RESOURCES)));
    }

    @Override
    public String getName() {
        return "Yagen combat and Mod defaults";
    }

    private static JsonObject object(String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }

    private void json(CachedOutput output, String path, JsonElement value) throws IOException {
        bytes(output, path, (ModJson.GSON.toJson(value) + "\n").getBytes(StandardCharsets.UTF_8));
    }

    private void bytes(CachedOutput output, String path, byte[] bytes) throws IOException {
        output.writeIfNeeded(root.resolve(path), bytes, Hashing.sha1().hashBytes(bytes));
    }

    private static String resource(String id, String folder) {
        int separator = id.indexOf(':');
        return "data/" + id.substring(0, separator) + "/" + folder + "/" + id.substring(separator + 1) + ".json";
    }

    private static JsonObject model(String texture) {
        var value = object("{\"parent\":\"minecraft:item/generated\"}");
        var textures = new JsonObject();
        textures.addProperty("layer0", texture);
        value.add("textures", textures);
        return value;
    }

    private void placeholderIfMissing(CachedOutput output, String texture, byte[] placeholder) throws IOException {
        var id = ResourceLocation.parse(texture);
        var png = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "textures/" + id.getPath() + ".png");
        // GatherDataEvent excludes the generating mod's pack, so previous generated PNGs cannot suppress fallbacks.
        if (existingClientResources.getResource(png).isEmpty())
            bytes(output, "assets/" + png.getNamespace() + "/" + png.getPath(), placeholder);
    }

    private void animatedArt(CachedOutput output, String id, ModDefinition definition) throws IOException {
        var visual = definition.style().visual();
        if (visual == ModStyle.Visual.NONE) return;
        var source = ResourceLocation.parse(definition.texture());
        var location = ResourceLocation.fromNamespaceAndPath(source.getNamespace(), "textures/" + source.getPath() + ".png");
        var resource = existingClientResources.getResource(location).orElseThrow(() -> new IOException("Missing animated Mod subject: " + location));
        BufferedImage original;
        try (var input = resource.open()) { original = ImageIO.read(input); }
        if (original == null || original.getWidth() != 32 || original.getHeight() != 32) throw new IOException("Animated Mod subject must be 32x32: " + location);
        int[] pixels = original.getRGB(0, 0, 32, 32, null, 0, 32);
        var animation = new BufferedImage(32, 32 * ModAnimations.frames(visual), BufferedImage.TYPE_INT_ARGB);
        for (int frame = 0; frame < ModAnimations.frames(visual); frame++)
            animation.setRGB(0, frame * 32, 32, 32, ModAnimations.frame(visual, pixels, 32, 32, frame), 0, 32);
        var png = new ByteArrayOutputStream(); ImageIO.write(animation, "PNG", png);
        var texture = ResourceLocation.parse(definition.style().texture(id, definition.texture()));
        String path = "assets/" + texture.getNamespace() + "/textures/" + texture.getPath() + ".png";
        bytes(output, path, png.toByteArray());
        json(output, path + ".mcmeta", object("{\"animation\":{\"frametime\":" + ModAnimations.frameTicks(visual) + ",\"interpolate\":false}}"));
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        try {
            var catalog = BuiltinMods.catalog();
            json(output, "assets/yagens_attributes/particles/status_blood.json",
                    object("{\"textures\":[\"yagens_attributes:status/blood_splat\"]}"));
            for (var entry : BuiltinMaterials.life().entrySet()) {
                var material = new JsonObject(); material.addProperty("priority", 0);
                var entities = new JsonArray(); entry.getValue().entities().forEach(entities::add); material.add("entities", entities);
                material.add("entity_tags", new JsonArray()); var factors = new JsonObject();
                for (var type : DamageType.values()) factors.addProperty(type.key(), entry.getValue().multipliers().get(type));
                material.add("multipliers", factors);
                json(output, "data/yagens_attributes/health_materials/" + entry.getKey() + ".json", material);
            }
            var shield = new JsonObject(); var factors = new JsonObject();
            for (var type : DamageType.values()) factors.addProperty(type.key(), BuiltinMaterials.shield().get(type));
            shield.add("multipliers", factors); shield.addProperty("description", "Applies only to absorption/shield loss. Toxin bypasses shields; life material never modifies shield demand. Missing damage types default to 1.");
            json(output, "data/yagens_attributes/shield_materials/default.json", shield);
            var image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
            int[] pixels = new int[1024];
            Arrays.fill(pixels, 0xFFFFFFFF);
            image.setRGB(0, 0, 32, 32, pixels, 0, 32);
            var png = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", png);
            byte[] placeholder = png.toByteArray();
            placeholderIfMissing(output, "yagens_attributes:item/mod", placeholder);
            placeholderIfMissing(output, "yagens_attributes:item/orokin_catalyst", placeholder);
            json(output, "assets/yagens_attributes/models/item/mod.json", model("yagens_attributes:item/mod"));
            // Item models share the block atlas, whose default directories do not include arbitrary texture paths.
            var atlas = new JsonObject();
            var sources = new JsonArray();
            var textures = new TreeSet<String>();
            textures.add("yagens_attributes:item/mod");
            textures.add("yagens_attributes:item/unknown_riven");
            textures.add(catalog.rivenRules().texture());
            catalog.definitions().values().forEach(definition -> textures.add(definition.texture()));
            catalog.definitions().forEach((id, definition) -> textures.add(definition.style().texture(id, definition.texture())));
            for (String texture : textures) {
                var source = new JsonObject();
                source.addProperty("type", "minecraft:single");
                source.addProperty("resource", texture);
                sources.add(source);
            }
            atlas.add("sources", sources);
            json(output, "data/yagens_attributes/riven_rules/default.json", RivenJson.rules(catalog.rivenRules()));
            json(output, "assets/yagens_attributes/models/item/mods/riven.json", model(catalog.rivenRules().texture()));
            for (String pool : List.of("melee", "ranged")) {
                String item = "unknown_" + pool + "_riven";
                json(output, "assets/yagens_attributes/models/item/" + item + ".json", model("yagens_attributes:item/unknown_riven"));
                json(output, "data/yagens_attributes/loot_table/items/" + item + ".json", object("{\"type\":\"minecraft:generic\",\"pools\":[{\"rolls\":1,\"entries\":[{\"type\":\"minecraft:item\",\"name\":\"yagens_attributes:" + item + "\"}]}]}"));
            }
            json(output, "assets/minecraft/atlases/blocks.json", atlas);
            for (var entry : catalog.definitions().entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
                var id = entry.getKey();
                String path = id.substring(id.indexOf(':') + 1);
                json(output, resource(id, "mods"), ModJson.definition(entry.getValue()));
                placeholderIfMissing(output, entry.getValue().texture(), placeholder);
                animatedArt(output, id, entry.getValue());
                json(output, "assets/yagens_attributes/models/item/mods/" + path + ".json", model(entry.getValue().style().texture(id, entry.getValue().texture())));
                var loot = object("{\"type\":\"minecraft:generic\",\"pools\":[{\"rolls\":1,\"entries\":[{\"type\":\"minecraft:item\",\"name\":\"yagens_attributes:mod\"}]}]}");
                var function = object("{\"function\":\"minecraft:set_components\"}");
                var components = new JsonObject();
                var data = new JsonObject();
                data.addProperty("type", id);
                data.addProperty("level", 0);
                components.add("yagens_attributes:mod_data", data);
                function.add("components", components);
                var functions = new JsonArray();
                functions.add(function);
                loot.getAsJsonArray("pools").get(0).getAsJsonObject().getAsJsonArray("entries").get(0).getAsJsonObject().add("functions", functions);
                json(output, "data/yagens_attributes/loot_table/mods/" + path + ".json", loot);
            }
            for (var value : catalog.qualities().values())
                json(output, "data/yagens_attributes/mod_rarities/" + value.type().key() + ".json", ModJson.quality(value));
            for (var value : catalog.polarities().values())
                json(output, "data/yagens_attributes/mod_polarities/" + value.type().key() + ".json", ModJson.polarity(value));
            for (var entry : BuiltinCombat.weaponDefinitions().entrySet()) {
                var profile = JsonData.weaponJson(entry.getValue().profile());
                if (entry.getValue().derived()) profile.remove("base_damage");
                json(output, resource(entry.getKey(), "weapon_stats"), profile);
            }
            var defaults = new JsonObject();
            defaults.addProperty("schema_version", 1);
            var classes = new JsonObject();
            BuiltinCombat.templates().entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
                var profile = JsonData.weaponJson(entry.getValue().profile());
                if (entry.getValue().derived()) profile.remove("base_damage");
                classes.add(entry.getKey(), profile);
            });
            defaults.add("classes", classes);
            json(output, "data/yagens_attributes/weapon_defaults/classes.json", defaults);
            for (var type : DamageType.values()) {
                var damage = object("{\"scaling\":\"never\",\"exhaustion\":0.0,\"effects\":\"hurt\"}");
                damage.addProperty("message_id", "yagens_attributes." + type.key());
                json(output, "data/yagens_attributes/damage_type/" + type.key() + "_status.json", damage);
            }
            for (var entry : BuiltinCombat.tags().entrySet()) {
                var tag = new JsonObject();
                tag.addProperty("replace", false);
                var values = new JsonArray();
                entry.getValue().forEach(values::add);
                tag.add("values", values);
                json(output, resource(entry.getKey(), "tags"), tag);
            }
            for (var entry : VanillaWeakPoints.files().entrySet())
                json(output, entry.getKey(), entry.getValue());
            for (String item : List.of("endo", "forma", "kuva", "orokin_catalyst"))
                json(output, "assets/yagens_attributes/models/item/" + item + ".json", model("yagens_attributes:item/" + item));
            var font = new JsonObject();
            var providers = new JsonArray();
            for (var type : DamageType.values()) {
                var provider = new JsonObject();
                provider.addProperty("type", "bitmap");
                provider.addProperty("file", "yagens_attributes:status/" + type.key() + "_status.png");
                provider.addProperty("ascent", 7);
                provider.addProperty("height", 8);
                var chars = new JsonArray();
                chars.add(String.valueOf((char) (0xE000 + type.ordinal())));
                provider.add("chars", chars);
                providers.add(provider);
            }
            font.add("providers", providers);
            json(output, "assets/yagens_attributes/font/damage_icons.json", font);
            var polarityProviders = new JsonArray();
            for (var polarity : ModInfo.Polarity.values()) {
                var glyph = PolarityTextures.export(Path.of(System.getProperty("yagens_attributes.polarityInput")), polarity);
                var data = new ByteArrayOutputStream();
                ImageIO.write(glyph, "PNG", data);
                bytes(output, "assets/yagens_attributes/textures/polarity/" + polarity.key() + ".png", data.toByteArray());
                var provider = object("{\"type\":\"bitmap\",\"ascent\":7,\"height\":8}");
                provider.addProperty("file", "yagens_attributes:polarity/" + polarity.key() + ".png");
                var chars = new JsonArray();
                chars.add(polarity.symbol());
                provider.add("chars", chars);
                polarityProviders.add(provider);
            }
            var polarityFont = new JsonObject();
            polarityFont.add("providers", polarityProviders);
            json(output, "assets/yagens_attributes/font/polarity_icons.json", polarityFont);
            var cycleImage = PolarityTextures.glyph(Path.of(System.getProperty("yagens_attributes.polarityInput")).getParent().resolve("ui/cycle.png"));
            var cycleBytes = new ByteArrayOutputStream(); ImageIO.write(cycleImage, "PNG", cycleBytes);
            bytes(output, "assets/yagens_attributes/textures/gui/cycle.png", cycleBytes.toByteArray());
            json(output, "assets/yagens_attributes/font/mod_symbols.json", object("{\"providers\":[{\"type\":\"bitmap\",\"file\":\"yagens_attributes:gui/cycle.png\",\"ascent\":7,\"height\":8,\"chars\":[\"\\ue109\"]}]}"));
            json(output, "assets/minecraft/font/default.json", object("{\"providers\":[{\"type\":\"reference\",\"id\":\"yagens_attributes:polarity_icons\"},{\"type\":\"reference\",\"id\":\"yagens_attributes:mod_symbols\"}]}"));
            for (String language : List.of("en_us", "zh_cn")) {
                var input = Path.of(System.getProperty("yagens_attributes.datagenInput"), "lang", language + ".json");
                var translations = JsonParser.parseString(Files.readString(input, StandardCharsets.UTF_8)).getAsJsonObject();
                json(output, "assets/yagens_attributes/lang/" + language + ".json", translations);
            }
            return CompletableFuture.completedFuture(null);
        } catch (Exception exception) {
            return CompletableFuture.failedFuture(exception);
        }
    }
}
