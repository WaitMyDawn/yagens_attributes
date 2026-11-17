package yagen.waitmydawn.yagens_attributes.data;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.network.*;
import yagen.waitmydawn.yagens_attributes.setup.*;
import java.io.IOException;
import java.util.*;

public final class RangedContracts {
    private static int checks;
    private RangedContracts() {}
    public static void run() {
        var profile = JsonData.weapon(JsonParser.parseString("{\"category\":\"bow\",\"damage_distribution\":{\"puncture\":1},\"fire_rate\":2,\"multishot\":1.5}").getAsJsonObject());
        equal(2D, profile.fireRate()); equal(1.5, profile.multishot()); equal(profile, JsonData.weapon(JsonData.weaponJson(profile)));
        var defaults = JsonData.weapon(JsonParser.parseString("{\"damage_distribution\":{\"impact\":1}}").getAsJsonObject());
        equal(1D, defaults.fireRate()); equal(1D, defaults.multishot());
        var modifier = new Bonus(0, .5, List.of(1.2), 0);
        var mutable = new HashMap<>(Map.of(Stats.MULTISHOT, modifier));
        var shot = new ShotData(profile, List.of(mutable), .75, true); mutable.clear();
        equal(modifier, shot.sources().get(0).get(Stats.MULTISHOT)); equal(shot, ShotData.decode(shot.encode()));
        equal(shot, ShotData.CODEC.parse(JsonOps.INSTANCE, ShotData.CODEC.encodeStart(JsonOps.INSTANCE, shot).getOrThrow()).getOrThrow());
        equal(ShotData.EMPTY, ShotData.decode(ShotData.EMPTY.encode()));
        var targetRule = new ConditionalModifier(Stats.DIRECT_DAMAGE, .8, ConditionalModifier.Condition.STATUS_TYPES);
        var deferred = new ShotData(profile, shot.sources(), .75, true, UUID.randomUUID(), List.of(targetRule));
        equal(deferred, ShotData.decode(deferred.encode()));
        equal(deferred.targetModifiers(), deferred.projectile(.5, false).targetModifiers());
        equal(deferred, ShotData.CODEC.parse(JsonOps.INSTANCE, ShotData.CODEC.encodeStart(JsonOps.INSTANCE, deferred).getOrThrow()).getOrThrow());
        var nativeSource = new ShotData(profile, shot.sources(), .75, true, UUID.randomUUID(), List.of(targetRule), UUID.randomUUID(), AttackKind.MELEE);
        equal(nativeSource, ShotData.decode(nativeSource.encode()));
        equal(nativeSource.owner(), nativeSource.projectile(.25, true).owner());
        equal(AttackKind.MELEE, nativeSource.projectile(.25, true).attackKind());
        equal(nativeSource.weapon(), nativeSource.baseMultishot(3).weapon());
        equal(3D, nativeSource.baseMultishot(3).profile().multishot());
        equal(nativeSource.owner(), nativeSource.baseMultishot(3).owner());
        var schema3 = JsonParser.parseString(nativeSource.encode()).getAsJsonObject(); schema3.addProperty("schema", 3);
        equal(AttackKind.RANGED, ShotData.decode(schema3.toString()).attackKind()); equal(null, ShotData.decode(schema3.toString()).owner());
        var legacyShot = JsonParser.parseString(deferred.encode()).getAsJsonObject();
        legacyShot.addProperty("schema", 2); legacyShot.remove("target_modifiers");
        equal(List.of(), ShotData.decode(legacyShot.toString()).targetModifiers());
        legacyShot.addProperty("schema", 1); legacyShot.remove("weapon");
        equal(null, ShotData.decode(legacyShot.toString()).weapon());
        rejects(() -> new ShotData(profile, List.of(), 1, false, null,
                BuiltinMods.catalog().card("yagens_attributes:galvanized_gaze_of_pain", 10).info().details().conditional()));
        rejects(() -> new ShotData(profile, List.of(), Double.NaN, false));
        equal(true, ShotData.CODEC.parse(JsonOps.INSTANCE, new com.google.gson.JsonPrimitive("{\"schema\":2}")).isError());
        var tag = new CompoundTag(); tag.putUUID("UUID", UUID.randomUUID()); tag.put("Passengers", new ListTag()); tag.putString("CustomName", "unchanged"); tag.putDouble("damage", 7);
        var template = RangedShooting.cloneTemplate(tag); equal(false, template.hasUUID("UUID")); equal(false, template.contains("Passengers")); equal("unchanged", template.getString("CustomName")); equal(7D, template.getDouble("damage"));
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        try {
            CombatCodecs.profile(buffer, profile); equal(profile, CombatCodecs.profile(buffer));
            var request = new ChargeRequest(7, ResourceLocation.parse("sample:odd_caster"), UUID.randomUUID(), InteractionHand.OFF_HAND);
            var packet = new ChargePayload(request, .5); ChargePayload.CODEC.encode(buffer, packet); equal(packet, ChargePayload.CODEC.decode(buffer));
        } finally { buffer.release(); }
        rejects(() -> new ChargePayload(new ChargeRequest(0, ResourceLocation.parse("test:tool"), null, InteractionHand.MAIN_HAND), Double.NaN));
        var resolved = PanelService.resolve(profile, List.of(Map.of(Stats.FIRE_RATE, new Bonus(0, .5, List.of(2D), 0))));
        equal(6D, resolved.get(Stats.FIRE_RATE)); equal(1.5, resolved.get(Stats.MULTISHOT));
        equal(6, yagen.waitmydawn.yagens_attributes.client.StatText.base(profile).size());
        equal(4, yagen.waitmydawn.yagens_attributes.client.StatText.base(defaults).size());
        var bow = BuiltinCombat.weapons().get("minecraft:bow");
        var sword = BuiltinCombat.weapons().get("minecraft:diamond_sword");
        var core = List.of(Stats.CRITICAL_CHANCE, Stats.CRITICAL_DAMAGE, Stats.STATUS_CHANCE, Stats.STATUS_DURATION);
        equal(core, yagen.waitmydawn.yagens_attributes.client.StatText.visibleKeys(bow, Map.of()));
        equal(core, yagen.waitmydawn.yagens_attributes.client.StatText.visibleKeys(sword, Map.of()));
        equal(4, yagen.waitmydawn.yagens_attributes.client.StatText.base(bow).size());
        equal(true, yagen.waitmydawn.yagens_attributes.client.StatText.visibleKeys(bow, Map.of(Stats.MULTISHOT, 3D)).contains(Stats.MULTISHOT));
        equal(true, yagen.waitmydawn.yagens_attributes.client.StatText.visibleKeys(bow, Map.of(Stats.FIRE_RATE, .8)).contains(Stats.FIRE_RATE));
        equal(true, yagen.waitmydawn.yagens_attributes.client.StatText.visibleKeys(sword, Map.of(Stats.ATTACK_SPEED, 1.3, Stats.MELEE_RANGE, 1D,
                Stats.COMBO_DURATION, 7D, Stats.COMBO_COUNT_CHANCE, 2D)).containsAll(List.of(Stats.ATTACK_SPEED, Stats.MELEE_RANGE, Stats.COMBO_DURATION, Stats.COMBO_COUNT_CHANCE)));
        var triple = BuiltinCombat.weaponDefinitions().get("twilightforest:triple_bow").profile();
        equal(true, yagen.waitmydawn.yagens_attributes.client.StatText.visibleKeys(triple, Map.of()).contains(Stats.MULTISHOT));
        equal(false, yagen.waitmydawn.yagens_attributes.client.StatText.visibleKeys(triple, Map.of(Stats.MULTISHOT, 1D)).contains(Stats.MULTISHOT));
        bytecode();
        System.out.println("Passed " + checks + " ranged snapshot/wire/bytecode contract checks");
    }
    private static ClassNode read(String name) {
        try (var stream = RangedContracts.class.getResourceAsStream("/net/minecraft/" + name + ".class")) {
            if (stream == null) throw new AssertionError("Missing target class " + name);
            var node = new ClassNode(); new ClassReader(stream).accept(node, 0); return node;
        } catch (IOException exception) { throw new IllegalStateException(exception); }
    }
    private static void bytecode() {
        var living = read("world/entity/LivingEntity");
        var hurt = living.methods.stream().filter(method -> method.name.equals("hurt") && method.desc.equals("(Lnet/minecraft/world/damagesource/DamageSource;F)Z")).findFirst().orElseThrow();
        int reads = 0, writes = 0, previous = 0;
        for (var instruction : hurt.instructions) if (instruction instanceof FieldInsnNode field && field.owner.equals("net/minecraft/world/entity/LivingEntity")) {
            if (field.name.equals("invulnerableTime") && field.getOpcode() == Opcodes.GETFIELD) reads++;
            if (field.name.equals("invulnerableTime") && field.getOpcode() == Opcodes.PUTFIELD) writes++;
            if (field.name.equals("lastHurt") && field.getOpcode() == Opcodes.PUTFIELD) previous++;
        }
        equal(1, reads); equal(1, writes); equal(2, previous);
        var weapon = read("world/item/ProjectileWeaponItem");
        equal(true, weapon.methods.stream().anyMatch(method -> method.name.equals("shoot") && method.desc.equals("(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;Ljava/util/List;FFZLnet/minecraft/world/entity/LivingEntity;)V")));
        var trident = read("world/item/TridentItem"); int spawn = 0;
        for (var method : trident.methods) if (method.name.equals("releaseUsing")) for (var instruction : method.instructions)
            if (instruction instanceof MethodInsnNode invoke && invoke.owner.equals("net/minecraft/world/level/Level") && invoke.name.equals("addFreshEntity") && invoke.desc.equals("(Lnet/minecraft/world/entity/Entity;)Z")) spawn++;
        equal(1, spawn);
        int ammunition = 0;
        for (var method : weapon.methods) if (method.name.equals("draw")) for (var instruction : method.instructions)
            if (instruction instanceof MethodInsnNode invoke && invoke.owner.equals("net/minecraft/world/item/enchantment/EnchantmentHelper") && invoke.name.equals("processProjectileCount") && invoke.desc.equals("(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/Entity;I)I")) ammunition++;
        equal(1, ammunition);
        equal(true, read("world/item/CrossbowItem").methods.stream().anyMatch(method -> method.name.equals("performShooting") && method.desc.equals("(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;FFLnet/minecraft/world/entity/LivingEntity;)V")));
        for (String name : List.of("AbstractArrow", "ThrownTrident")) equal(true, read("world/entity/projectile/" + name).methods.stream().anyMatch(method -> method.name.equals("tryPickup") && method.desc.equals("(Lnet/minecraft/world/entity/player/Player;)Z")));
        equal(true, read("world/entity/projectile/AbstractArrow").methods.stream().anyMatch(method -> method.name.equals("getPickupItem") && method.desc.equals("()Lnet/minecraft/world/item/ItemStack;")));
        var stack = read("world/item/ItemStack");
        for (String signature : List.of(
                "use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;",
                "useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;",
                "releaseUsing(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V",
                "onUseTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V",
                "hurtEnemy(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/player/Player;)Z"))
            equal(true, stack.methods.stream().anyMatch(method -> (method.name + method.desc).equals(signature)));
        var server = read("server/level/ServerLevel"); int ticks = 0;
        for (var method : server.methods) if (method.name.equals("tickNonPassenger")) for (var instruction : method.instructions)
            if (instruction instanceof MethodInsnNode invoke && invoke.owner.equals("net/minecraft/world/entity/Entity") && invoke.name.equals("tick") && invoke.desc.equals("()V")) ticks++;
        equal(1, ticks);
        equal(true, server.methods.stream().anyMatch(method -> method.name.equals("addFreshEntity") && method.desc.equals("(Lnet/minecraft/world/entity/Entity;)Z")));
        equal(true, read("world/item/ItemCooldowns").methods.stream().anyMatch(method -> method.name.equals("addCooldown") && method.desc.equals("(Lnet/minecraft/world/item/Item;I)V")));
    }
    private static void equal(Object a, Object b) { if (!Objects.equals(a, b)) throw new AssertionError(a + " != " + b); checks++; }
    private static void rejects(Runnable action) { try { action.run(); } catch (RuntimeException expected) { checks++; return; } throw new AssertionError("Expected rejection"); }
}
