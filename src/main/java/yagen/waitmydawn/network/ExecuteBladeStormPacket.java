package yagen.waitmydawn.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.registries.MobEffectRegistry;
import yagen.waitmydawn.util.BladeStormTargets;

public class ExecuteBladeStormPacket implements CustomPacketPayload {

    public static final ExecuteBladeStormPacket INSTANCE = new ExecuteBladeStormPacket();

    public static final Type<ExecuteBladeStormPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "execute_blade_storm"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ExecuteBladeStormPacket> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    public static void handle(ExecuteBladeStormPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer sp = (ServerPlayer) ctx.player();
            if (!sp.hasEffect(MobEffectRegistry.BLADE_STORM)) return;

            BladeStormTargets.execute(sp);
            sp.removeEffect(MobEffectRegistry.BLADE_STORM);

//            try {
//                ServerPlayer sp = (ServerPlayer) ctx.player();
//                if (sp == null) return;
//
//                if (!sp.hasEffect(MobEffectRegistry.BLADE_STORM)) {
//                    return;
//                }
//
//                BladeStormTargets.execute(sp);
//                sp.removeEffect(MobEffectRegistry.BLADE_STORM);
//
//            } catch (Exception e) {
//                YagensAttributes.LOGGER.error("ExecuteBladeStormPacket Error: ", e);
//            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}