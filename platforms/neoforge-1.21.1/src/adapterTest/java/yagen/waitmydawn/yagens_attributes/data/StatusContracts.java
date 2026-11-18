package yagen.waitmydawn.yagens_attributes.data;

import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.network.*;
import java.util.*;

public final class StatusContracts {
    private static int checks;
    private StatusContracts() {}
    public static void run() {
        var shield = new ShieldState();
        shield.observe(4); check(!shield.armed); check(!shield.consume(4));
        shield.naturalGain(0, 0); check(!shield.armed);
        shield.naturalGain(0, .1); check(shield.armed); shield.observe(16); close(16, shield.before);
        check(shield.consume(16)); check(!shield.armed); shield.observe(16); check(!shield.consume(16));
        shield.naturalGain(16, 16); check(!shield.armed); shield.naturalGain(1, 2); check(shield.armed);
        var restored = ShieldState.CODEC.parse(JsonOps.INSTANCE, ShieldState.CODEC.encodeStart(JsonOps.INSTANCE, shield).getOrThrow()).getOrThrow();
        check(restored.armed); close(shield.before, restored.before); check(restored.gate == shield.gate);
        var state = new StatusState(); UUID first = UUID.randomUUID(), second = UUID.randomUUID();
        var critical = new StatusState.Layer(DamageType.SLASH, first, 50, 120, 20, 0, 4);
        check(critical.tick().criticalTier() == 4);
        check(StatusState.Layer.CODEC.parse(JsonOps.INSTANCE, StatusState.Layer.CODEC.encodeStart(JsonOps.INSTANCE, critical).getOrThrow()).getOrThrow().equals(critical));
        UUID weapon = UUID.randomUUID();
        var melee = new StatusState.Layer(DamageType.HEAT, first, 50, 120, 20, 0, 3, weapon, AttackKind.MELEE);
        var meleeJson = StatusState.Layer.CODEC.encodeStart(JsonOps.INSTANCE, melee).getOrThrow();
        check(StatusState.Layer.CODEC.parse(JsonOps.INSTANCE, meleeJson).getOrThrow().equals(melee));
        check(melee.tick().weapon().equals(weapon) && melee.tick().attackKind() == AttackKind.MELEE);
        var legacy = meleeJson.getAsJsonObject().deepCopy(); legacy.remove("attack_kind");
        check(StatusState.Layer.CODEC.parse(JsonOps.INSTANCE, legacy).getOrThrow().attackKind() == AttackKind.RANGED);
        legacy.remove("weapon");
        var unownedWeapon = StatusState.Layer.CODEC.parse(JsonOps.INSTANCE, legacy).getOrThrow();
        check(unownedWeapon.attackKind() == AttackKind.UNKNOWN && unownedWeapon.weapon() == null);
        legacy.addProperty("attack_kind", "invalid"); check(StatusState.Layer.CODEC.parse(JsonOps.INSTANCE, legacy).isError());
        var context = new yagen.waitmydawn.yagens_attributes.api.StatusContext(Map.of(DamageType.SLASH, 50D), 3, weapon, AttackKind.MELEE);
        check(context.weapon().equals(weapon) && context.rangedWeapon() == null && context.attackKind() == AttackKind.MELEE);
        var rangedContext = new yagen.waitmydawn.yagens_attributes.api.StatusContext(context.bases(), 3, weapon);
        check(rangedContext.attackKind() == AttackKind.RANGED && rangedContext.rangedWeapon().equals(weapon));
        var copied = new StatusState(); copied.layers.add(melee);
        var copy = StatusState.CODEC.parse(JsonOps.INSTANCE, StatusState.CODEC.encodeStart(JsonOps.INSTANCE, copied).getOrThrow()).getOrThrow();
        check(copy.layers.get(0).weapon().equals(weapon) && copy.layers.get(0).attackKind() == AttackKind.MELEE);
        state.layers.add(new StatusState.Layer(DamageType.SLASH, first, 10, 120, 20, 0));
        state.layers.add(new StatusState.Layer(DamageType.SLASH, second, 20, 40, 20, 0));
        state.layers.add(new StatusState.Layer(DamageType.COLD, first, 0, 120, 20, 0));
        int firstPulses = 0, secondPulses = 0;
        for (int tick = 1; tick <= 120; tick++) for (var layer : state.advance(false)) {
            check(tick % 20 == 0); if (layer.owner().equals(first)) firstPulses++; else secondPulses++;
        }
        check(firstPulses == 6); check(secondPulses == 2); check(state.empty());
        state.layers.add(new StatusState.Layer(DamageType.COLD, first, 0, 120, 20, 0));
        state.frozen = 60; state.advance(true); check(state.duration(DamageType.COLD) == 120);
        state.layers.add(new StatusState.Layer(DamageType.BLAST, second, 40, 30, 20, 0));
        int explosions = 0;
        for (int tick = 1; tick <= 30; tick++) for (var layer : state.advance(true)) { check(tick == 30); explosions++; }
        check(explosions == 1);
        state.heatStrip = .5;
        var json = StatusState.CODEC.encodeStart(JsonOps.INSTANCE, state).getOrThrow();
        var decoded = StatusState.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
        check(state.layers.equals(decoded.layers)); check(decoded.frozen == 60); close(.5, decoded.heatStrip);
        decoded.heatPanic = 80; decoded.electricStun = 60; decoded.clear(DamageType.ELECTRICITY);
        check(decoded.electricStun == 0 && decoded.heatPanic == 80 && decoded.dirty);
        decoded.clear(DamageType.HEAT); check(decoded.heatPanic == 0 && decoded.heatStrip == 0);
        decoded.clear(DamageType.COLD); check(decoded.empty());
        var bad = json.getAsJsonObject().deepCopy(); bad.addProperty("frozen", -1); check(StatusState.CODEC.parse(JsonOps.INSTANCE, bad).isError());
        bad = json.getAsJsonObject().deepCopy(); bad.getAsJsonArray("layers").get(0).getAsJsonObject().addProperty("type", "unknown"); check(StatusState.CODEC.parse(JsonOps.INSTANCE, bad).isError());
        bad = json.getAsJsonObject().deepCopy(); bad.getAsJsonArray("layers").get(0).getAsJsonObject().addProperty("owner", "invalid"); check(StatusState.CODEC.parse(JsonOps.INSTANCE, bad).isError());
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        try {
            var packet = new ShieldPayload(8); ShieldPayload.CODEC.encode(buffer, packet); check(packet.equals(ShieldPayload.CODEC.decode(buffer)));
        } finally { buffer.release(); }
        var living = read("world/entity/LivingEntity");
        try (var stream = StatusContracts.class.getResourceAsStream("/yagen/waitmydawn/yagens_attributes/mixin/ArmorFormulaMixin.class")) {
            var node = new ClassNode(); new ClassReader(Objects.requireNonNull(stream)).accept(node, 0);
            var annotations = new ArrayList<AnnotationNode>();
            if (node.visibleAnnotations != null) annotations.addAll(node.visibleAnnotations);
            if (node.invisibleAnnotations != null) annotations.addAll(node.invisibleAnnotations);
            var mixin = annotations.stream().filter(annotation -> annotation.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;")).findFirst().orElseThrow();
            int index = mixin.values.indexOf("priority"); check(index >= 0 && mixin.values.get(index + 1).equals(2000));
        } catch (java.io.IOException failure) { throw new IllegalStateException(failure); }
        check(living.methods.stream().anyMatch(method -> method.name.equals("getMaxAbsorption") && method.desc.equals("()F")));
        check(living.methods.stream().anyMatch(method -> method.name.equals("setAbsorptionAmount") && method.desc.equals("(F)V")));
        check(living.fields.stream().anyMatch(field -> field.name.equals("damageContainers") && field.desc.equals("Ljava/util/Stack;")));
        var absorption = read("world/effect/AbsorptionMobEffect");
        check(absorption.methods.stream().anyMatch(method -> method.name.equals("onEffectStarted") && method.desc.equals("(Lnet/minecraft/world/entity/LivingEntity;I)V")));
        var gui = read("client/gui/Gui"); var render = gui.methods.stream().filter(method -> method.name.equals("renderHearts")).findFirst().orElseThrow();
        int calls = 0; for (var instruction : render.instructions) if (instruction instanceof MethodInsnNode method && method.name.equals("renderHeart")) {
            check(method.desc.equals("(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Gui$HeartType;IIZZZ)V")); calls++;
        }
        check(calls == 4);
        var health = gui.methods.stream().filter(method -> method.name.equals("renderHealthLevel")).findFirst().orElseThrow();
        check(java.util.stream.StreamSupport.stream(health.instructions.spliterator(), false).anyMatch(instruction -> instruction instanceof MethodInsnNode method && method.owner.equals("net/minecraft/world/entity/player/Player") && method.name.equals("getAbsorptionAmount") && method.desc.equals("()F")));
        var armor = living.methods.stream().filter(method -> method.name.equals("getDamageAfterArmorAbsorb")).findFirst().orElseThrow();
        check(java.util.stream.StreamSupport.stream(armor.instructions.spliterator(), false).anyMatch(instruction -> instruction instanceof MethodInsnNode method && method.owner.equals("net/minecraft/world/damagesource/CombatRules") && method.name.equals("getDamageAfterAbsorb") && method.desc.equals("(Lnet/minecraft/world/entity/LivingEntity;FLnet/minecraft/world/damagesource/DamageSource;FF)F")));
        check(read("world/entity/Mob").methods.stream().anyMatch(method -> method.name.equals("serverAiStep") && method.desc.equals("()V")));
        System.out.println("Passed " + checks + " shield/status state, pulse, wire and mapped-target checks");
    }
    private static ClassNode read(String name) {
        try (var stream = StatusContracts.class.getResourceAsStream("/net/minecraft/" + name + ".class")) {
            var node = new ClassNode(); new ClassReader(Objects.requireNonNull(stream)).accept(node, 0); return node;
        } catch (java.io.IOException exception) { throw new IllegalStateException(exception); }
    }
    private static void check(boolean value) { checks++; if (!value) throw new AssertionError("Status contract failed at " + checks); }
    private static void close(double expected, double actual) { check(Math.abs(expected - actual) < 1e-6); }
}
