package yagen.waitmydawn.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import yagen.waitmydawn.YagensAttributes;

public class NetworkHandler {

    public static void onRegister(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(YagensAttributes.MODID)
                .versioned("1.0")
                .optional();

        registrar.playToClient(
                SyncComboPacket.TYPE,
                SyncComboPacket.STREAM_CODEC,
                SyncComboPacket::handle
        );

        registrar.playToClient(
                SyncPreShootCountPacket.TYPE,
                SyncPreShootCountPacket.STREAM_CODEC,
                SyncPreShootCountPacket::handle
        );

        registrar.playToClient(
                DamageNumberPacket.TYPE,
                DamageNumberPacket.STREAM_CODEC,
                DamageNumberPacket::handle
        );

        registrar.playToClient(
                ElectricityPacket.TYPE,
                ElectricityPacket.STREAM_CODEC,
                ElectricityPacket::handle
        );

        registrar.playToClient(
                HeatPacket.TYPE,
                HeatPacket.STREAM_CODEC,
                HeatPacket::handle
        );

        registrar.playToClient(
                BlastPacket.TYPE,
                BlastPacket.STREAM_CODEC,
                BlastPacket::handle
        );

        registrar.playToClient(
                ColdPacket.TYPE,
                ColdPacket.STREAM_CODEC,
                ColdPacket::handle
        );

        registrar.playToClient(
                ToxinPacket.TYPE,
                ToxinPacket.STREAM_CODEC,
                ToxinPacket::handle
        );

        registrar.playToClient(
                GasPacket.TYPE,
                GasPacket.STREAM_CODEC,
                GasPacket::handle
        );

        registrar.playToClient(
                SyncMissionDataPacket.TYPE,
                SyncMissionDataPacket.STREAM_CODEC,
                SyncMissionDataPacket::handle
        );

        registrar.playToServer(
                AirBrakePacket.TYPE,
                AirBrakePacket.STREAM_CODEC,
                AirBrakePacket::handle
        );

        registrar.playToServer(
                BulletJumpPacket.TYPE,
                BulletJumpPacket.STREAM_CODEC,
                BulletJumpPacket::handle
        );

        registrar.playToServer(
                AddNourishEffectPacket.TYPE,
                AddNourishEffectPacket.STREAM_CODEC,
                AddNourishEffectPacket::handle
        );

        registrar.playToServer(
                AddBladeStormEffectPacket.TYPE,
                AddBladeStormEffectPacket.STREAM_CODEC,
                AddBladeStormEffectPacket::handle
        );

        registrar.playToServer(
                SendBladeStormTargetPacket.TYPE,
                SendBladeStormTargetPacket.STREAM_CODEC,
                SendBladeStormTargetPacket::handle
        );

        registrar.playToServer(
                ExecuteBladeStormPacket.TYPE,
                ExecuteBladeStormPacket.STREAM_CODEC,
                ExecuteBladeStormPacket::handle
        );
    }
}