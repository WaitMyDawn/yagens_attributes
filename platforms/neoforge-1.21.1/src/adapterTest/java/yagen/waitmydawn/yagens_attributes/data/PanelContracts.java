package yagen.waitmydawn.yagens_attributes.data;

import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import yagen.waitmydawn.yagens_attributes.client.ClientProfiles;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.network.*;
import yagen.waitmydawn.yagens_attributes.render.DamageNumberTransform;
import yagen.waitmydawn.yagens_attributes.setup.PanelService;
import java.util.*;

public final class PanelContracts {
    private static int checks;
    private PanelContracts() {}

    public static void run() {
        transforms(); profiles(); packets(); permissionsAndStats(); endo(); release(); filters();
        System.out.println("Passed " + checks + " panel/network/coordinate contract checks");
    }

    private static void filters() {
        var filter = new ModFilter(List.of(List.of(ModFilter.Tag.RARE, ModFilter.Tag.LEGENDARY), List.of(ModFilter.Tag.AVAILABLE), List.of(ModFilter.Tag.LEVELED)));
        var query = new ModInventory.Query("unusual", ModInfo.Category.RANGED, ModInventory.Order.QUANTITY, filter);
        var request = new PanelRequest(91, PanelRequest.STORAGE, 4, PanelRequest.REFRESH, null, null, -1, query, "zh_cn");
        var buffer = buffer();
        try { PanelRequest.CODEC.encode(buffer, request); equal(request, PanelRequest.CODEC.decode(buffer)); equal(0, buffer.readableBytes()); }
        finally { buffer.release(); }
        for (int invalid : List.of(-1, 13, Integer.MAX_VALUE)) {
            var bad = buffer(); try { bad.writeVarInt(invalid); rejects(() -> PanelRequest.readFilter(bad)); } finally { bad.release(); }
        }
        for (int invalid : List.of(0, -1, 13, Integer.MAX_VALUE)) {
            var bad = buffer(); try { bad.writeVarInt(1).writeVarInt(invalid); rejects(() -> PanelRequest.readFilter(bad)); } finally { bad.release(); }
        }
        var duplicate = buffer();
        try { duplicate.writeVarInt(1).writeVarInt(2).writeEnum(ModFilter.Tag.RARE).writeEnum(ModFilter.Tag.RARE); rejects(() -> PanelRequest.readFilter(duplicate)); }
        finally { duplicate.release(); }
        var unknown = buffer();
        try { unknown.writeVarInt(1).writeVarInt(1).writeVarInt(999); rejects(() -> PanelRequest.readFilter(unknown)); }
        finally { unknown.release(); }
    }

    private static void release() {
        UUID mod = UUID.randomUUID();
        var request = new PanelRequest(31, PanelRequest.STORAGE, 0, PanelRequest.RELEASE, null, mod);
        equal(true, request.valid());
        var buffer = buffer();
        try { PanelRequest.CODEC.encode(buffer, request); equal(request, PanelRequest.CODEC.decode(buffer)); }
        finally { buffer.release(); }
        equal(false, new PanelRequest(31, PanelRequest.STORAGE, 0, PanelRequest.RELEASE, UUID.randomUUID(), mod).valid());
        equal(false, new PanelRequest(31, PanelRequest.WEAPON, 0, PanelRequest.RELEASE, null, mod).valid());
        equal(false, new PanelRequest(31, PanelRequest.STORAGE, 0, PanelRequest.RELEASE, null, null).valid());
        equal(false, new PanelRequest(31, PanelRequest.STORAGE, 0, PanelRequest.RELEASE, null, mod, 0, ModInventory.Query.DEFAULT, "en_us").valid());
    }

    private static void endo() {
        var buffer = buffer();
        try {
            var request = new PanelRequest(23, PanelRequest.STORAGE, 0, PanelRequest.WITHDRAW_ENDO, null, null, -1, ModInventory.Query.DEFAULT, "zh_cn", Long.MAX_VALUE);
            equal(true, request.valid()); PanelRequest.CODEC.encode(buffer, request); equal(request, PanelRequest.CODEC.decode(buffer));
            PanelPayload.writeEndo(buffer, Long.MAX_VALUE, 2304); equal(new PanelPayload.EndoView(Long.MAX_VALUE, 2304), PanelPayload.readEndo(buffer));
        } finally { buffer.release(); }
        rejects(() -> new PanelPayload.EndoView(-1, 1)); rejects(() -> new PanelPayload.EndoView(1, -1));
        buffer = buffer();
        try {
            buffer.writeVarLong(-1).writeVarInt(0);
            var invalid = buffer;
            rejects(() -> PanelPayload.readEndo(invalid));
        } finally { buffer.release(); }
    }

    private static WeaponProfile weapon() { return new WeaponProfile(.15, 2, .2, 6, Map.of(DamageType.SLASH, .7, DamageType.PUNCTURE, .3), 10, ModInfo.Category.SWORD, Collections.nCopies(8, ModInfo.Polarity.NONE)); }
    private static RegistryFriendlyByteBuf buffer() { return new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY); }

    private static void transforms() {
        Vec3[] cameras = {Vec3.ZERO, new Vec3(103.2, 72.5, -58.75), new Vec3(-18_000_000.25, -61.5, 20_000_000.5)};
        Quaternionf[] rotations = {new Quaternionf(), new Quaternionf().rotationYXZ(.73F, -.24F, 0), new Quaternionf().rotationYXZ(-2.1F, .64F, .1F)};
        for (Vec3 camera : cameras) for (Quaternionf rotation : rotations) {
            Vec3 anchor = camera.add(2.25, 1.75, -4.5);
            Quaternionf viewRotation = rotation.conjugate(new Quaternionf());
            Matrix4f finalPose = new Matrix4f().rotation(viewRotation).mul(DamageNumberTransform.billboard(anchor, camera, rotation, .2F, 0));
            Vector3f expected = new Vector3f(2.25F, 1.95F, -4.5F).rotate(viewRotation);
            vector(expected, finalPose.transformPosition(new Vector3f()));
            vector(new Vector3f(.025F, 0, 0), finalPose.transformDirection(new Vector3f(1, 0, 0)));
            vector(new Vector3f(0, -.025F, 0), finalPose.transformDirection(new Vector3f(0, 1, 0)));
        }
    }

    private static void profiles() {
        var irregular = ResourceLocation.parse("sample:tools/odd_blade_7");
        var unrelated = ResourceLocation.parse("another:flute");
        ClientProfiles.clear();
        ClientProfiles.accept(new ProfilePayload(true, true, Map.of(irregular, weapon())));
        equal(weapon(), ClientProfiles.get(irregular));
        ClientProfiles.accept(new ProfilePayload(true, false, Map.of(unrelated, weapon())));
        equal(weapon(), ClientProfiles.get(irregular)); equal(null, ClientProfiles.get(unrelated));
        ClientProfiles.accept(new ProfilePayload(false, true, Map.of()));
        equal(null, ClientProfiles.get(irregular)); equal(weapon(), ClientProfiles.get(unrelated));
        ClientProfiles.clear(); equal(null, ClientProfiles.get(unrelated));
        ClientProfiles.accept(new ProfilePayload(true, true, Map.of())); equal(null, ClientProfiles.get(irregular));
    }

    private static void packets() {
        UUID owner = UUID.randomUUID(), id = UUID.randomUUID(), target = UUID.randomUUID();
        var request = new PanelRequest(17, PanelRequest.WEAPON, 2, PanelRequest.UNINSTALL, target, id);
        var buffer = buffer();
        try { PanelRequest.CODEC.encode(buffer, request); equal(request, PanelRequest.CODEC.decode(buffer)); }
        finally { buffer.release(); }
        var damage = new DamagePayload(192, new Vec3(8.25, 64.5, -12.75), owner, 12.5F, 4, 13);
        buffer = buffer();
        try { DamagePayload.CODEC.encode(buffer, damage); equal(damage, DamagePayload.CODEC.decode(buffer)); }
        finally { buffer.release(); }
        var resource = ResourceLocation.parse("sample:irregular/tool");
        buffer = buffer();
        try {
            ProfilePayload.CODEC.encode(buffer, new ProfilePayload(true, true, Map.of(resource, weapon())));
            var decoded = ProfilePayload.CODEC.decode(buffer);
            equal(true, decoded.first()); equal(true, decoded.last());
            close(.15, decoded.profiles().get(resource).criticalChance()); close(.7, decoded.profiles().get(resource).distribution().get(DamageType.SLASH));
            close(10, decoded.profiles().get(resource).baseDamage()); equal(ModInfo.Category.SWORD, decoded.profiles().get(resource).category()); equal(8, decoded.profiles().get(resource).slots().size());
        } finally { buffer.release(); }
        var entry = new ModLedger.Entry(id, owner, target, true, Map.of("damage.cold", new Bonus(2, .5, List.of(1.5, 1.2), 1)));
        var summary = PanelPayload.ModView.of(entry).bonuses();
        buffer = buffer();
        try {
            CombatCodecs.bonuses(buffer, summary); equal(summary, CombatCodecs.bonuses(buffer));
        } finally { buffer.release(); }
        buffer = buffer();
        try {
            var metadata = new ModInfo("奇异曲线", ModInfo.Category.TRIDENT, ModInfo.Rarity.LEGENDARY, ModInfo.Polarity.UMBRA);
            var mod = PanelPayload.ModView.of(new ModLedger.Entry(UUID.randomUUID(), owner, target, true, entry.bonuses(), metadata, 7), "奇异曲线", 4, 2);
            PanelPayload.writeMods(buffer, List.of(mod)); equal(List.of(mod), PanelPayload.readMods(buffer, 24));
        } finally { buffer.release(); }
        buffer = buffer();
        try {
            var refresh = new PanelRequest(1, PanelRequest.STORAGE, 0, PanelRequest.REFRESH, null, null);
            PanelRequest.CODEC.encode(buffer, refresh); equal(refresh, PanelRequest.CODEC.decode(buffer));
        } finally { buffer.release(); }
        buffer = buffer();
        try {
            buffer.writeBoolean(true).writeBoolean(true).writeVarInt(ProfilePayload.CHUNK_SIZE + 1);
            RegistryFriendlyByteBuf malformed = buffer;
            rejects(() -> ProfilePayload.CODEC.decode(malformed));
        } finally { buffer.release(); }
        // Display summaries cannot overflow even when a source's independent product would.
        var view = PanelPayload.ModifierView.of(new Bonus(0, 0, Collections.nCopies(32, Double.MAX_VALUE), 0));
        equal(true, view.multiply().contains("…")); equal(true, view.multiply().length() <= 256);
        buffer = buffer();
        try {
            var inspect = new TooltipPayload(resource, target, new WeaponInspectionData(Map.of("critical_damage", 2D), new WeaponProgress(7, 3), 9, true));
            TooltipPayload.CODEC.encode(buffer, inspect); equal(inspect, TooltipPayload.CODEC.decode(buffer));
        } finally { buffer.release(); }
        buffer = buffer();
        try {
            var request2 = new TooltipRequest(0, 17, resource, target);
            TooltipRequest.CODEC.encode(buffer, request2); equal(request2, TooltipRequest.CODEC.decode(buffer));
            var progress = new ProgressPayload(target, new WeaponProgress(30, 0));
            ProgressPayload.CODEC.encode(buffer, progress); equal(progress, ProgressPayload.CODEC.decode(buffer));
        } finally { buffer.release(); }
        equal("2x", yagen.waitmydawn.yagens_attributes.client.StatText.value("critical_damage", 2));
        var lines = yagen.waitmydawn.yagens_attributes.client.StatText.composition(weapon(), Map.of("damage.blast", 5D, "damage.heat", 2D));
        equal(4, lines.size()); equal(true, lines.get(0).getString().endsWith(": 5"));
        equal((char) (0xE000 + DamageType.BLAST.ordinal()), lines.get(0).getString().charAt(0));
    }

    private static void permissionsAndStats() {
        UUID weapon = UUID.randomUUID(); PanelService.validateWeapon(weapon, weapon);
        UUID id = UUID.randomUUID();
        rejects(() -> PanelService.validateWeapon(weapon, UUID.randomUUID()));
        rejects(() -> PanelService.validateWeapon(null, weapon));
        equal(false, new PanelRequest(-1, 0, 0, 0, null, null).valid());
        equal(false, new PanelRequest(1, 2, 0, 0, null, null).valid());
        equal(false, new PanelRequest(1, 0, -1, 0, null, null).valid());
        equal(false, new PanelRequest(1, 0, 0, 9, null, null).valid());
        equal(true, new PanelRequest(1, PanelRequest.WEAPON, 0, PanelRequest.MOVE, weapon, id, 3, ModInventory.Query.DEFAULT, "en_us").valid());
        equal(false, new PanelRequest(1, PanelRequest.WEAPON, 0, PanelRequest.MOVE, weapon, id).valid());
        var sources = List.of(Map.of("critical_chance", new Bonus(0, .2, List.of(1.5), 0)),
                Map.of("critical_chance", new Bonus(0, .4, List.of(1.2), 0), "damage.cold", new Bonus(5, 0, List.of(), 0)));
        var resolved = PanelService.resolve(weapon(), sources);
        close(.432, resolved.get("critical_chance")); close(5, resolved.get("damage.cold")); close(7, resolved.get("damage.slash"));
    }

    private static void vector(Vector3f expected, Vector3f actual) {
        if (expected.distance(actual) > .0001F) throw new AssertionError(expected + " != " + actual);
        checks++;
    }
    private static void close(double expected, double actual) {
        if (Math.abs(expected - actual) > 1e-9) throw new AssertionError(expected + " != " + actual);
        checks++;
    }
    private static void equal(Object expected, Object actual) {
        if (!Objects.equals(expected, actual)) throw new AssertionError(expected + " != " + actual);
        checks++;
    }
    private static void rejects(Runnable action) {
        try { action.run(); } catch (RuntimeException expected) { checks++; return; }
        throw new AssertionError("Expected rejection");
    }
}
