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
                DamageNumberPacket.TYPE,
                DamageNumberPacket.STREAM_CODEC,
                DamageNumberPacket::handle
        );

        registrar.playToServer(
                AddNourishEffectPacket.TYPE,
                AddNourishEffectPacket.STREAM_CODEC,
                AddNourishEffectPacket::handle
        );

        registrar.playToServer(
                SendBladeStormTargetPacket.TYPE,
                SendBladeStormTargetPacket.STREAM_CODEC,
                SendBladeStormTargetPacket::handle
        );
    }
}