package yagen.waitmydawn.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.util.BladeStormTargets;

public record SendBladeStormTargetPacket(int targetId) implements CustomPacketPayload {
    public static final Type<SendBladeStormTargetPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "get_blade_storm_target"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SendBladeStormTargetPacket> STREAM_CODEC =
            StreamCodec.of(
                    (b, p) -> b.writeInt(p.targetId),
                    b -> new SendBladeStormTargetPacket(b.readInt())
            );

    public static void handle(SendBladeStormTargetPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer sp = (ServerPlayer) ctx.player();
            Entity entity = sp.serverLevel().getEntity(pkt.targetId);
            if (entity instanceof LivingEntity living &&
                    living.level() == sp.level() &&
                    sp.distanceTo(living) <= 20) {
                BladeStormTargets.add(sp, living);
                sp.displayClientMessage(Component.literal("Locked"), true);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
