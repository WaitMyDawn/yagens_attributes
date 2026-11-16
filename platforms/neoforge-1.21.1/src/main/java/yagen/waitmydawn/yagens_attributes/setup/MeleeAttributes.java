package yagen.waitmydawn.yagens_attributes.setup;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.*;
import yagen.waitmydawn.yagens_attributes.core.*;
import java.util.*;

public final class MeleeAttributes {
    private static final ResourceLocation SPEED = ResourceLocation.fromNamespaceAndPath("yagens_attributes", "mod_attack_speed");
    private static final ResourceLocation RANGE = ResourceLocation.fromNamespaceAndPath("yagens_attributes", "mod_melee_range");
    private static final Set<ServerPlayer> INVALID = Collections.newSetFromMap(new WeakHashMap<>());
    private MeleeAttributes() {}
    public static void update(ServerPlayer player, List<Map<String, Bonus>> sources) {
        double speed = 0, range = 0;
        try {
            speed = Stats.resolve(1, Stats.ATTACK_SPEED, sources) - 1;
            range = Stats.resolve(0, Stats.MELEE_RANGE, sources);
            INVALID.remove(player);
        } catch (IllegalArgumentException exception) {
            speed = 0; range = 0;
            if (INVALID.add(player)) yagen.waitmydawn.yagens_attributes.YagensAttributes.LOGGER.warn("Rejected melee attribute modifiers for {}: {}", player.getUUID(), exception.getMessage());
        }
        apply(player, Attributes.ATTACK_SPEED, SPEED, speed, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        apply(player, Attributes.ENTITY_INTERACTION_RANGE, RANGE, range, AttributeModifier.Operation.ADD_VALUE);
    }
    private static void apply(ServerPlayer player, Holder<Attribute> attribute, ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        var instance = player.getAttribute(attribute); if (instance == null) return;
        var previous = instance.getModifier(id);
        if (previous != null && previous.amount() == amount && previous.operation() == operation) return;
        if (previous != null) instance.removeModifier(id);
        if (amount != 0) instance.addTransientModifier(new AttributeModifier(id, amount, operation));
    }
}
