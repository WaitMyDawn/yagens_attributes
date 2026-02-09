package yagen.waitmydawn.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.config.ServerConfigs;
import yagen.waitmydawn.registries.MobEffectRegistry;

public record AddBladeStormEffectPacket() implements CustomPacketPayload {
    public static final Type<AddBladeStormEffectPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "add_blade_storm"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AddBladeStormEffectPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                    },
                    buf -> new AddBladeStormEffectPacket()
            );

    public static void handle(AddBladeStormEffectPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            player.addEffect(new MobEffectInstance(MobEffectRegistry.BLADE_STORM, ServerConfigs.MOD_WARFRAME_BLADE_STORM_DURATION.get() * 20, 0));
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}