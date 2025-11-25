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
                SyncMissionDataPacket.TYPE,
                SyncMissionDataPacket.STREAM_CODEC,
                SyncMissionDataPacket::handle
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