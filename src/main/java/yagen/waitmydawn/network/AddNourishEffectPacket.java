package yagen.waitmydawn.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.registries.MobEffectRegistry;

public record AddNourishEffectPacket() implements CustomPacketPayload {
    private static final int DURATION = 600;
    public static final Type<AddNourishEffectPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "add_nourish"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AddNourishEffectPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {},
                    buf -> new AddNourishEffectPacket()
            );

    public static void handle(AddNourishEffectPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            player.addEffect(new MobEffectInstance(MobEffectRegistry.NOURISH, DURATION, 0));
            player.getPersistentData().putInt("YANourishLeft", DURATION);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}