package yagen.waitmydawn.yagens_attributes.data;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.network.CombatCodecs;
import java.util.*;

public final class RivenContracts {
    private static int checks;
    private RivenContracts() {}
    public static void run() throws Exception {
        var catalog = BuiltinMods.catalog();
        check(RivenJson.rules(RivenJson.rules(catalog.rivenRules())).equals(catalog.rivenRules()));
        try (var stream = RivenContracts.class.getResourceAsStream("/data/yagens_attributes/riven_rules/default.json")) {
            check(RivenJson.rules(JsonParser.parseReader(new java.io.InputStreamReader(Objects.requireNonNull(stream), java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject()).equals(catalog.rivenRules()));
        }
        var random = new Random(31); var roll = catalog.rivenRules().roll(RivenRoll.Pool.MELEE, BuiltinCombat.weapons(), random::nextInt, random::nextDouble);
        check(RivenJson.roll(RivenJson.roll(roll)).equals(roll));
        var encoded = RivenItemData.CODEC.encodeStart(JsonOps.INSTANCE, roll).getOrThrow();
        check(RivenItemData.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow().equals(roll));
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        try {
            RivenItemData.STREAM_CODEC.encode(buffer, roll); check(RivenItemData.STREAM_CODEC.decode(buffer).equals(roll));
            var profile = BuiltinCombat.weapons().get("minecraft:diamond_sword"); CombatCodecs.profile(buffer, profile); check(CombatCodecs.profile(buffer).equals(profile));
            var info = catalog.rivenCard(roll, 8).info(); check(ModJson.details(ModJson.details(info.details())).equals(info.details()));
        } finally { buffer.release(); }
        var card = catalog.rivenCard(roll, 8); var id = UUID.randomUUID(); var owner = UUID.randomUUID(); var weapon = UUID.randomUUID();
        var state = new WorldState(); state.ledger.add(new ModLedger.Entry(id, owner, weapon, true, card.bonuses(), card.info(), 0));
        var tag = state.save(new CompoundTag(), null); check(tag.getInt("schema") == 7);
        var loaded = WorldState.load(tag, null); check(loaded.ledger.all().equals(state.ledger.all()));
        var profiles = new HashMap<ResourceLocation, WeaponProfile>();
        BuiltinCombat.weapons().forEach((key, value) -> profiles.put(ResourceLocation.parse(key), value));
        loaded.refreshCatalog(catalog, Map.copyOf(profiles));
        var previous = loaded.ledger.entry(id).bonuses();
        var target = ResourceLocation.parse(roll.weapon()); double disposition = profiles.get(target).rivenDisposition();
        profiles.put(target, profiles.get(target).withDisposition(.5)); loaded.refreshCatalog(catalog, Map.copyOf(profiles));
        var after = loaded.ledger.entry(id); check(after.owner().equals(owner) && after.weapon().equals(weapon));
        check(after.info().riven().lines().equals(roll.lines()));
        for (var entry : previous.entrySet()) {
            var next = after.bonuses().get(entry.getKey()); close(entry.getValue().addition() * .5 / disposition, next.addition());
            close(entry.getValue().addin() * .5 / disposition, next.addin()); close(entry.getValue().inputAddition() * .5 / disposition, next.inputAddition());
        }
        var corrupt = JsonParser.parseString(RivenJson.roll(roll)).getAsJsonObject(); corrupt.getAsJsonArray("lines").get(0).getAsJsonObject().addProperty("quality", 2);
        rejects(() -> RivenJson.roll(corrupt));
        var old = state.save(new CompoundTag(), null); old.putInt("schema", 5); check(WorldState.load(old, null).isDirty());
        System.out.println("Passed " + checks + " Riven resource, component, persistence and reload checks");
    }
    private static void check(boolean value) { checks++; if (!value) throw new AssertionError("Riven adapter mismatch"); }
    private static void close(double expected, double actual) { check(Math.abs(expected - actual) < 1e-9); }
    private static void rejects(Runnable action) { checks++; try { action.run(); } catch (IllegalArgumentException expected) { return; } throw new AssertionError("Expected invalid Riven payload rejection"); }
}
