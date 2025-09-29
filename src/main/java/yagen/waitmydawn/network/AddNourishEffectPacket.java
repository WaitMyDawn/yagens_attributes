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

public record AddNourishEffectPacket(int ticks) implements CustomPacketPayload {

    public static final Type<AddNourishEffectPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "add_nourish"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AddNourishEffectPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> buf.writeVarInt(pkt.ticks),
                    buf -> new AddNourishEffectPacket(buf.readVarInt())
            );

    public static void handle(AddNourishEffectPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer)ctx.player();
            if (player != null) {
                player.addEffect(new MobEffectInstance(MobEffectRegistry.NOURISH, pkt.ticks, 0));
//                setNourishTime(player,600);
                player.getPersistentData().putInt("YANourishLeft", pkt.ticks);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}