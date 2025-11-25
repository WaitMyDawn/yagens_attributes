package yagen.waitmydawn.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mission.MissionData;
import yagen.waitmydawn.api.mission.ClientMissionData;

public record SyncMissionDataPacket(
        ResourceLocation taskId,
        String missionType,
        int missionLevel,
        int progress,
        int maxProgress,
        int summonCount,
        double distance,
        double missionRange,
        double x, double y, double z,
        boolean completed
) implements CustomPacketPayload {

    public static final Type<SyncMissionDataPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "sync_mission_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncMissionDataPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeResourceLocation(pkt.taskId);
                        buf.writeUtf(pkt.missionType);
                        buf.writeInt(pkt.missionLevel);
                        buf.writeInt(pkt.progress);
                        buf.writeInt(pkt.maxProgress);
                        buf.writeInt(pkt.summonCount);
                        buf.writeDouble(pkt.distance);
                        buf.writeDouble(pkt.missionRange);
                        buf.writeDouble(pkt.x);
                        buf.writeDouble(pkt.y);
                        buf.writeDouble(pkt.z);
                        buf.writeBoolean(pkt.completed);
                    },
                    buf -> new SyncMissionDataPacket(
                            buf.readResourceLocation(),
                            buf.readUtf(),
                            buf.readInt(),
                            buf.readInt(),
                            buf.readInt(),
                            buf.readInt(),
                            buf.readDouble(),
                            buf.readDouble(),
                            buf.readDouble(),
                            buf.readDouble(),
                            buf.readDouble(),
                            buf.readBoolean()
                    )
            );

    public static void handle(SyncMissionDataPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientMissionData.update(pkt);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}