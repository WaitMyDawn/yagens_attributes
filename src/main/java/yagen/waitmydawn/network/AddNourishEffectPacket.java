package yagen.waitmydawn.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.attribute.YAttributes;
import yagen.waitmydawn.config.ServerConfigs;
import yagen.waitmydawn.registries.MobEffectRegistry;

public record AddNourishEffectPacket() implements CustomPacketPayload {
    public static final Type<AddNourishEffectPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "add_nourish"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AddNourishEffectPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                    },
                    buf -> new AddNourishEffectPacket()
            );

    public static void handle(AddNourishEffectPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            int duration = (int) (20 * ServerConfigs.MOD_WARFRAME_NOURISH_DURATION.get() * player.getAttributeValue(YAttributes.ABILITY_DURATION));
            player.addEffect(new MobEffectInstance(MobEffectRegistry.NOURISH, duration, 0));
            player.getPersistentData().putInt("YANourishLeft", duration);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}