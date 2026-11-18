package yagen.waitmydawn.yagens_attributes.data;

import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.core.ui.*;
import yagen.waitmydawn.yagens_attributes.network.StatusMarkersPayload;
import yagen.waitmydawn.yagens_attributes.text.ModText;
import javax.imageio.ImageIO;
import java.util.*;

public final class MarkerContracts {
    private static int checks;
    private MarkerContracts() {}
    public static void run() throws Exception {
        var counts = new EnumMap<DamageType, Integer>(DamageType.class);
        for (var type : DamageType.values()) counts.put(type, type.ordinal() + 1);
        var payload = new StatusMarkersPayload(42, UUID.randomUUID(), counts, true);
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        try { StatusMarkersPayload.CODEC.encode(buffer, payload); check(payload.equals(StatusMarkersPayload.CODEC.decode(buffer))); }
        finally { buffer.release(); }
        counts.clear(); check(payload.counts().size() == 13);
        check(new StatusMarkersPayload(42, payload.entityUuid(), Map.of()).counts().isEmpty());
        rejects(() -> new StatusMarkersPayload(-1, UUID.randomUUID(), Map.of()));
        rejects(() -> new StatusMarkersPayload(1, UUID.randomUUID(), Map.of(DamageType.GAS, 0)));
        rejects(() -> new StatusMarkersPayload(1, UUID.randomUUID(), Map.of(DamageType.GAS, 8193)));
        rejects(() -> new StatusMarkersPayload(1, UUID.randomUUID(), Map.of(DamageType.GAS, 3), true));
        buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        try {
            buffer.writeVarInt(1).writeUUID(UUID.randomUUID()).writeBoolean(false).writeVarInt(2);
            buffer.writeEnum(DamageType.COLD).writeVarInt(1).writeEnum(DamageType.COLD).writeVarInt(2);
            var malformed = buffer; rejects(() -> StatusMarkersPayload.CODEC.decode(malformed));
        } finally { buffer.release(); }
        var rows = StatusMarkerLayout.rows(payload.counts(), 3);
        check(rows.size() == 5); check(rows.get(4).size() == 1);
        check(rows.stream().flatMap(List::stream).map(StatusMarkerLayout.Entry::type).toList().equals(DamageType.DISPLAY_ORDER));
        check(StatusMarkerLayout.rows(Map.of(), 3).isEmpty());
        for (int columns : List.of(1, 3, 13)) {
            var grid = StatusMarkerLayout.rows(payload.counts(), columns);
            check(grid.stream().allMatch(row -> row.size() <= columns));
            check(StatusMarkerLayout.rowY(grid.size() - 1, grid.size()) == -9);
        }
        check(Math.abs(StatusMarkerLayout.anchorHeight(2, .2) - 2.4) < 1e-9);
        check(Math.abs(StatusMarkerLayout.anchorHeight(.5, .2) - .6) < 1e-9);
        rejects(() -> StatusMarkerLayout.rows(payload.counts(), 0)); rejects(() -> StatusMarkerLayout.rows(payload.counts(), 14));
        var metadata = ModText.metadata(BuiltinMods.catalog().creative().get(0).info());
        check(metadata.getSiblings().stream().noneMatch(component -> component.getString().contains("polarity.yagens_attributes")));
        var scope = ModText.scope(BuiltinMods.catalog().card("yagens_attributes:multishot", 0).info());
        check(scope.getString().contains(" ")); check(!scope.getString().contains("\n"));
        var universal = BuiltinMods.catalog().polarities().get(ModInfo.Polarity.OMNI);
        check(universal.equals(ModJson.polarity(ModJson.polarity(universal))));
        var weapon = new WeaponProfile(0, 2, 0, 6, Map.of(DamageType.IMPACT, 1D), 1, ModInfo.Category.SWORD, List.of(ModInfo.Polarity.OMNI, ModInfo.Polarity.UMBRA));
        check(weapon.equals(JsonData.weapon(JsonData.weaponJson(weapon))));
        buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        try { yagen.waitmydawn.yagens_attributes.network.CombatCodecs.profile(buffer, weapon); check(weapon.equals(yagen.waitmydawn.yagens_attributes.network.CombatCodecs.profile(buffer))); }
        finally { buffer.release(); }
        for (var polarity : ModInfo.Polarity.values()) {
            check(PolarityGlyphs.type(polarity.symbol().charAt(0)) == polarity);
            try (var input = MarkerContracts.class.getResourceAsStream("/assets/yagens_attributes/textures/polarity/" + polarity.key() + ".png")) {
                var image = ImageIO.read(Objects.requireNonNull(input)); check(image.getWidth() == 32 && image.getHeight() == 32);
                boolean ink = false, transparency = false;
                for (int y = 0; y < 32; y++) for (int x = 0; x < 32; x++) { ink |= image.getRGB(x, y) == -1; transparency |= image.getRGB(x, y) == 0; }
                check(ink && transparency);
            }
        }
        System.out.println("Passed " + checks + " status marker layout/wire and Mod metadata/glyph checks");
    }
    private static void check(boolean condition) { checks++; if (!condition) throw new AssertionError("Marker contract failed at " + checks); }
    private static void rejects(Runnable action) { checks++; try { action.run(); } catch (IllegalArgumentException expected) { return; } throw new AssertionError("Expected rejection"); }
}
