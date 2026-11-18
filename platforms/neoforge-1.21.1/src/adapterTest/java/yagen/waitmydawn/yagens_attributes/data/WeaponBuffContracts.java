package yagen.waitmydawn.yagens_attributes.data;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.network.WeaponBuffsPayload;
import java.util.*;

public final class WeaponBuffContracts {
    private static int checks;
    private WeaponBuffContracts() {}
    public static void run() {
        UUID weapon = UUID.randomUUID(), mod = UUID.randomUUID(), owner = UUID.randomUUID();
        var buff = new WeaponBuff(mod, 0, "unrelated:hourglass", 3, Stats.ATTACK_SPEED, .35, false, 200, 2,
                ConditionalModifier.Decay.INDEPENDENT, List.of(160L, 200L));
        var packet = new WeaponBuffsPayload(weapon, 100, List.of(buff));
        equal(2, buff.count(159)); equal(1, buff.count(160)); equal(0, buff.count(200));
        equal(.3F, buff.fraction(100)); equal(160L, buff.next(100));
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        try {
            WeaponBuffsPayload.CODEC.encode(buffer, packet); equal(packet, WeaponBuffsPayload.CODEC.decode(buffer));
            var empty = new WeaponBuffsPayload(null, 200, List.of()); WeaponBuffsPayload.CODEC.encode(buffer, empty); equal(empty, WeaponBuffsPayload.CODEC.decode(buffer));
            buffer.writeBoolean(false); buffer.writeVarLong(0); buffer.writeVarInt(257);
            rejects(() -> WeaponBuffsPayload.CODEC.decode(buffer));
        } finally { buffer.release(); }
        rejects(() -> new WeaponBuffsPayload(null, 100, List.of(buff)));
        rejects(() -> new WeaponBuffsPayload(weapon, 100, List.of(buff, buff)));
        rejects(() -> new WeaponBuffsPayload(weapon, 200, List.of(buff)));
        rejects(() -> new WeaponBuffsPayload(weapon, -1, List.of()));
        for (var kind : AttackKind.values()) {
            UUID sourceWeapon = kind == AttackKind.UNKNOWN ? null : weapon;
            var layer = new StatusState.Layer(DamageType.MAGNETIC, owner, 17, 120, 20, 0, 3, sourceWeapon, kind);
            var saved = StatusState.Layer.CODEC.parse(JsonOps.INSTANCE, StatusState.Layer.CODEC.encodeStart(JsonOps.INSTANCE, layer).getOrThrow()).getOrThrow();
            var context = saved.childContext(DamageType.ELECTRICITY, 4);
            equal(owner, saved.owner()); equal(sourceWeapon, context.weapon()); equal(kind, context.attackKind());
            equal(3, context.criticalTier()); equal(4D, context.base(DamageType.ELECTRICITY)); equal(0D, context.base(DamageType.MAGNETIC));
        }
        var catalog = BuiltinMods.catalog();
        equal("odd:unrecognized", ModCatalog.canonicalId("odd:unrecognized"));
        migration(catalog, "primed_winter_talisman", "primed_cold_addition", mod, owner, weapon);
        migration(catalog, "primed_shattered_venom_doll", "primed_toxin_addition", mod, owner, weapon);
        System.out.println("Passed " + checks + " weapon buff wire/deadline, derived status origin and Mod ID migration checks");
    }

    private static void migration(ModCatalog catalog, String oldName, String newName, UUID mod, UUID owner, UUID weapon) {
        String oldId = "yagens_attributes:" + oldName;
        var current = catalog.card("yagens_attributes:" + newName, 10);
        equal(false, catalog.definitions().containsKey(oldId));
        equal(current, catalog.card(oldId, 10));
        var item = new JsonObject();
        item.addProperty("type", oldId);
        item.addProperty("level", 10);
        var physical = ModItemData.CODEC.parse(JsonOps.INSTANCE, item).getOrThrow();
        equal(ResourceLocation.parse(current.id()), physical.type()); equal(10, physical.level());
        var details = ModJson.details(ModJson.details(current.info().details()).replace(current.id(), oldId));
        var legacy = new ModInfo("mod.yagens_attributes." + oldName, current.info().category(), current.info().rarity(),
                current.info().polarity(), current.info().capacityCost(), details);
        var state = new WorldState(); state.ledger.add(new ModLedger.Entry(mod, owner, weapon, true, current.bonuses(), legacy, 2));
        var restored = WorldState.load(state.save(new CompoundTag(), null), null); restored.refreshCatalog(catalog);
        var entry = restored.ledger.entry(mod);
        equal(current.info(), entry.info()); equal(owner, entry.owner());
        equal(weapon, entry.weapon()); equal(2, entry.slot()); equal(true, entry.shared()); equal(true, restored.isDirty());
    }
    private static void equal(Object expected, Object actual) { checks++; if (!Objects.equals(expected, actual)) throw new AssertionError(expected + " != " + actual); }
    private static void rejects(Runnable action) { try { action.run(); } catch (RuntimeException expected) { checks++; return; } throw new AssertionError("Expected rejection"); }
}
