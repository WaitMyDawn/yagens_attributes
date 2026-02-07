package yagen.waitmydawn.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.config.ClientConfigs;

import java.util.UUID;

public record HeadShotSoundPacket() implements CustomPacketPayload {
    public static final Type<HeadShotSoundPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "headshot_sound"));

    public static final StreamCodec<RegistryFriendlyByteBuf, HeadShotSoundPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                    },
                    buf -> new HeadShotSoundPacket()
            );

    public static void handle(HeadShotSoundPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!ClientConfigs.IF_HEADSHOT_SOUND.get()) return;
            var player = Minecraft.getInstance().player;
            if (player != null) {
                player.playSound(SoundEvents.ARROW_HIT_PLAYER,
                        ClientConfigs.HEADSHOT_VOLUME.get().floatValue(),
                        ClientConfigs.HEADSHOT_PITCH.get().floatValue());
            }
        });
    }

    @Override
    public Type<HeadShotSoundPacket> type() {
        return TYPE;
    }
}