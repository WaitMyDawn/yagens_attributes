package yagen.waitmydawn.yagens_attributes.data;

import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import yagen.waitmydawn.yagens_attributes.network.ComboPayload;
import java.util.*;

public final class ComboContracts {
    private static int checks;
    private ComboContracts() {}
    public static void run() {
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        try {
            for (var snapshot : List.of(new ComboPayload(null, 0, 0, 0, 20, 12), new ComboPayload(UUID.randomUUID(), 220, 99, 100, 20, 12), new ComboPayload(UUID.randomUUID(), 21, 1, 500, 7, 4))) {
                ComboPayload.CODEC.encode(buffer, snapshot); check(snapshot.equals(ComboPayload.CODEC.decode(buffer)));
            }
        } finally { buffer.release(); }
        rejects(() -> new ComboPayload(null, 1, 1, 100, 20, 12));
        rejects(() -> new ComboPayload(UUID.randomUUID(), 221, 1, 100, 20, 12));
        rejects(() -> new ComboPayload(UUID.randomUUID(), 20, 101, 100, 20, 12));
        rejects(() -> new ComboPayload(UUID.randomUUID(), 0, 1, 100, 20, 12));
        rejects(() -> new ComboPayload(UUID.randomUUID(), 20, 0, 100, 20, 12));
        System.out.println("Passed " + checks + " combo payload bounds/round-trip checks");
    }
    private static void check(boolean value) { checks++; if (!value) throw new AssertionError("Combo wire mismatch"); }
    private static void rejects(Runnable action) { checks++; try { action.run(); } catch (IllegalArgumentException expected) { return; } throw new AssertionError("Expected rejection"); }
}
