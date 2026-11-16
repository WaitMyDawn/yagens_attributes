package yagen.waitmydawn.yagens_attributes.data;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.network.PanelRequest;
import java.util.*;
import static yagen.waitmydawn.yagens_attributes.core.ModInfo.Polarity.*;

public final class PolarityContracts {
    private static int checks;
    private PolarityContracts() {}
    public static void run() {
        var original = WeaponPolarityData.EMPTY.with(7, MADURAI).with(0, NARAMON);
        var updated = original.with(7, VAZARIN);
        equal(MADURAI, original.resolve(List.of(NONE), 1).get(7));
        equal(VAZARIN, updated.resolve(List.of(NONE), 1).get(7));
        equal(NARAMON, updated.resolve(Collections.nCopies(8, UMBRA), 8).get(0));
        equal(UMBRA, updated.resolve(Collections.nCopies(8, UMBRA), 8).get(1));
        equal(List.of(new WeaponPolarityData.Slot(0, NARAMON), new WeaponPolarityData.Slot(7, VAZARIN)), updated.overrides());
        var nbt = WeaponPolarityData.CODEC.encodeStart(NbtOps.INSTANCE, updated).getOrThrow();
        equal(updated, WeaponPolarityData.CODEC.parse(NbtOps.INSTANCE, nbt).getOrThrow());
        for (var json : List.of("[{\"slot\":0,\"polarity\":\"madurai\"},{\"slot\":0,\"polarity\":\"vazarin\"}]", "[{\"slot\":64,\"polarity\":\"naramon\"}]", "[{\"slot\":-1,\"polarity\":\"naramon\"}]", "[{\"slot\":0,\"polarity\":\"unknown\"}]"))
            equal(true, WeaponPolarityData.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).error().isPresent());
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        try {
            WeaponPolarityData.STREAM_CODEC.encode(buffer, updated); equal(updated, WeaponPolarityData.STREAM_CODEC.decode(buffer));
            var request = new PanelRequest(31, PanelRequest.WEAPON, 0, PanelRequest.POLARIZE, UUID.randomUUID(), null, 7, ModInventory.Query.DEFAULT, "zh_cn", 0, MADURAI);
            equal(true, request.valid()); PanelRequest.CODEC.encode(buffer, request); equal(request, PanelRequest.CODEC.decode(buffer));
            buffer.writeVarInt(65);
            try { WeaponPolarityData.STREAM_CODEC.decode(buffer); throw new AssertionError("Expected oversized component rejection"); }
            catch (IllegalArgumentException expected) { checks++; }
        } finally { buffer.release(); }
        equal(false, new PanelRequest(1, PanelRequest.STORAGE, 0, PanelRequest.POLARIZE, UUID.randomUUID(), null, 0, ModInventory.Query.DEFAULT, "zh_cn", 0, MADURAI).valid());
        equal(false, new PanelRequest(1, PanelRequest.WEAPON, 0, PanelRequest.POLARIZE, UUID.randomUUID(), null, 0, ModInventory.Query.DEFAULT, "zh_cn", 0, UMBRA).valid());
        equal(false, new PanelRequest(1, PanelRequest.WEAPON, 0, PanelRequest.POLARIZE, UUID.randomUUID(), UUID.randomUUID(), 0, ModInventory.Query.DEFAULT, "zh_cn", 0, MADURAI).valid());
        System.out.println("Passed " + checks + " weapon polarity component/network contract checks");
    }
    private static void equal(Object expected, Object actual) { checks++; if (!Objects.equals(expected, actual)) throw new AssertionError(expected + " != " + actual); }
}
