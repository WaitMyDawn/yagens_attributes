package yagen.waitmydawn.yagens_attributes.data;

import com.google.gson.*;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.network.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.imageio.ImageIO;

public final class ModContracts {
    private static int checks;
    private ModContracts() {}
    public static void run() {
        var expected = BuiltinMods.catalog(); var encoded = ModJson.catalog(expected); var decoded = ModJson.catalog(encoded);
        check(expected.creative().equals(decoded.creative()));
        for (var card : expected.creative()) check(card.info().details().equals(ModJson.details(ModJson.details(card.info().details()))));
        var conditionalJson = ModJson.definition(expected.definitions().get("yagens_attributes:berserker_fury"));
        conditionalJson.getAsJsonArray("entries").get(0).getAsJsonObject().getAsJsonObject("condition").addProperty("duration_seconds", 3);
        check(ModJson.definition(conditionalJson).conditional(5).get(0).condition().durationTicks() == 60);
        var disk = new JsonObject(); var definitions = new JsonObject(); var qualities = new JsonArray(); var polarities = new JsonArray();
        expected.definitions().forEach((id, value) -> definitions.add(id, resource("/data/yagens_attributes/mods/" + id.substring(id.indexOf(':') + 1) + ".json")));
        for (var type : ModInfo.Rarity.values()) qualities.add(resource("/data/yagens_attributes/mod_rarities/" + type.key() + ".json"));
        for (var type : ModInfo.Polarity.values()) polarities.add(resource("/data/yagens_attributes/mod_polarities/" + type.key() + ".json"));
        disk.add("mods", definitions); disk.add("qualities", qualities); disk.add("polarities", polarities);
        check(ModJson.catalog(disk).creative().equals(expected.creative()));
        var data = new ModItemData(ResourceLocation.parse("yagens_attributes:multishot"), 5);
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        try {
            ModItemData.STREAM_CODEC.encode(buffer, data); check(ModItemData.STREAM_CODEC.decode(buffer).equals(data));
            var packet = new ModCatalogPayload(ModJson.GSON.toJson(encoded)); ModCatalogPayload.CODEC.encode(buffer, packet); check(ModCatalogPayload.CODEC.decode(buffer).equals(packet));
            var details = expected.card("yagens_attributes:multishot", 3).info().details(); check(details.equals(ModJson.details(ModJson.details(details))));
            var bonuses = Map.of("damage.heat", PanelPayload.ModifierView.of(new Bonus(0, 0, List.of(), 0, .1, .9)));
            CombatCodecs.bonuses(buffer, bonuses); check(CombatCodecs.bonuses(buffer).equals(bonuses));
            var info = expected.card("yagens_attributes:heat_addition", 5).info();
            var entry = new ModLedger.Entry(UUID.randomUUID(), UUID.randomUUID(), null, false, expected.card("yagens_attributes:heat_addition", 5).bonuses(), info, -1);
            var view = PanelPayload.ModView.of(entry); PanelPayload.writeMods(buffer, List.of(view)); check(PanelPayload.readMods(buffer, 18).equals(List.of(view)));
        } finally { buffer.release(); }
        UUID owner = UUID.randomUUID(), weapon = UUID.randomUUID(), id = UUID.randomUUID();
        var state = new WorldState(); var card = expected.card("yagens_attributes:multishot", 0);
        state.ledger.add(new ModLedger.Entry(id, owner, weapon, true, card.bonuses(), card.info(), 2)); state.endo(owner, 93);
        var tag = state.save(new CompoundTag(), null); check(tag.getInt("schema") == 7);
        var restored = WorldState.load(tag, null); check(restored.ledger.all().equals(state.ledger.all())); check(restored.endo(owner) == 93);
        var changed = encoded.deepCopy(); changed.getAsJsonObject("mods").getAsJsonObject("yagens_attributes:multishot").getAsJsonArray("entries").get(0).getAsJsonObject().addProperty("value", .25);
        var changedCatalog = ModJson.catalog(changed); restored.refreshCatalog(changedCatalog);
        close(.25, restored.ledger.entry(id).bonuses().get(Stats.MULTISHOT).addition()); check(restored.ledger.entry(id).owner().equals(owner)); check(restored.ledger.entry(id).weapon().equals(weapon)); check(restored.ledger.entry(id).slot() == 2);
        var missing = encoded.deepCopy(); missing.getAsJsonObject("mods").remove("yagens_attributes:multishot"); restored.refreshCatalog(ModJson.catalog(missing));
        check(restored.ledger.entry(id).bonuses().isEmpty()); check(restored.ledger.entry(id).info().details().type().equals(card.id()));
        restored.refreshCatalog(expected); check(restored.ledger.entry(id).bonuses().equals(card.bonuses()));
        var invalid = encoded.deepCopy(); invalid.getAsJsonObject("mods").getAsJsonObject("yagens_attributes:multishot").getAsJsonArray("entries").get(0).getAsJsonObject().addProperty("operation", "unknown");
        rejects(() -> ModJson.catalog(invalid));
        var typo = encoded.deepCopy(); typo.getAsJsonObject("mods").getAsJsonObject("yagens_attributes:multishot").addProperty("max_levle", 8); rejects(() -> ModJson.catalog(typo));
        var negative = encoded.deepCopy(); negative.getAsJsonArray("qualities").get(0).getAsJsonObject().addProperty("base_endo", -1); rejects(() -> ModJson.catalog(negative));
        check(JsonData.decodeBonuses(JsonData.encodeBonuses(Map.of("damage.cold", new Bonus(0, 0, List.of(), 0, .15)))).get("damage.cold").inputAddin() == .15);
        check(JsonData.decodeBonuses(JsonData.encodeBonuses(Map.of("damage.cold", new Bonus(0, 0, List.of(), 0, 0, .3)))).get("damage.cold").inputAddition() == .3);
        check(JsonData.decodeBonuses("{\"damage.cold\":{\"inputAddin\":0.15}}").get("damage.cold").inputAddition() == 0);
        var defaultOperation = ModJson.definition(expected.definitions().get("yagens_attributes:heat_addition"));
        defaultOperation.getAsJsonArray("entries").get(0).getAsJsonObject().remove("operation");
        check(ModJson.definition(defaultOperation).entries().get(0).operation() == ModDefinition.Operation.ADDITION);
        var atlas = resource("/assets/minecraft/atlases/blocks.json").getAsJsonArray("sources");
        var atlasTextures = new HashSet<String>();
        for (var source : atlas) {
            var value = source.getAsJsonObject();
            check(value.get("type").getAsString().equals("minecraft:single"));
            check(atlasTextures.add(value.get("resource").getAsString()));
        }
        for (var definition : expected.definitions().entrySet()) {
            String path = definition.getKey().substring(definition.getKey().indexOf(':') + 1);
            var texture = ResourceLocation.parse(definition.getValue().texture());
            check(atlasTextures.contains(texture.toString()));
            String rendered = definition.getValue().style().texture(definition.getKey(), definition.getValue().texture());
            check(atlasTextures.contains(rendered));
            check(resource("/assets/yagens_attributes/models/item/mods/" + path + ".json").getAsJsonObject("textures").get("layer0").getAsString().equals(rendered));
            texture("/assets/" + texture.getNamespace() + "/textures/" + texture.getPath() + ".png", false);
            var loot = resource("/data/yagens_attributes/loot_table/mods/" + path + ".json");
            var item = loot.getAsJsonArray("pools").get(0).getAsJsonObject().getAsJsonArray("entries").get(0).getAsJsonObject();
            check(item.get("name").getAsString().equals("yagens_attributes:mod"));
            var component = item.getAsJsonArray("functions").get(0).getAsJsonObject().getAsJsonObject("components").getAsJsonObject("yagens_attributes:mod_data");
            check(component.get("type").getAsString().equals(definition.getKey())); check(component.get("level").getAsInt() == 0);
        }
        texture("/assets/yagens_attributes/textures/item/mod.png", true);
        System.out.println("Passed " + checks + " generated Mod data, save migration, reload, item/wire and texture checks");
    }
    private static JsonObject resource(String... paths) {
        for (var path : paths) try (var stream = ModContracts.class.getResourceAsStream(path)) {
            if (stream != null) return JsonParser.parseString(new String(stream.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (java.io.IOException exception) { throw new IllegalStateException(exception); }
        throw new AssertionError("Missing resource " + Arrays.toString(paths));
    }
    private static void texture(String path, boolean requirePlaceholder) {
        try (var stream = ModContracts.class.getResourceAsStream(path)) {
            var image = ImageIO.read(Objects.requireNonNull(stream)); check(image.getWidth() == 32 && image.getHeight() == 32);
            boolean visible = false, binaryAlpha = true, white = true;
            for (int y = 0; y < 32; y++) for (int x = 0; x < 32; x++) {
                int pixel = image.getRGB(x, y), alpha = pixel >>> 24;
                visible |= alpha > 0; binaryAlpha &= alpha == 0 || alpha == 255; white &= pixel == 0xFFFFFFFF;
            }
            // Shared legacy status icons may be translucent; binary-alpha constraints belong to authoring profiles.
            if (!(image.getColorModel().hasAlpha() && visible && (!requirePlaceholder || binaryAlpha && white)))
                throw new AssertionError("Invalid texture " + path + ": alpha=" + image.getColorModel().hasAlpha() + ", visible=" + visible + ", binary=" + binaryAlpha + ", placeholder=" + requirePlaceholder + ", white=" + white);
            check(true);
        } catch (java.io.IOException exception) { throw new IllegalStateException(exception); }
    }
    private static void close(double expected, double actual) { check(Math.abs(expected - actual) < 1e-8); }
    private static void check(boolean value) { checks++; if (!value) throw new AssertionError("Mod adapter contract failed at " + checks); }
    private static void rejects(Runnable action) { checks++; try { action.run(); } catch (IllegalArgumentException exception) { return; } throw new AssertionError("Expected invalid catalog rejection"); }
}
