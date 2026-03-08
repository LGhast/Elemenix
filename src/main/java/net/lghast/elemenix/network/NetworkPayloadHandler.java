package net.lghast.elemenix.network;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.network.analyzer.DeconstructionPayload;
import net.lghast.elemenix.network.analyzer.ReconstructionPayload;
import net.lghast.elemenix.network.burner.*;
import net.lghast.elemenix.network.infuser.InfuserDataUpdatePayload;
import net.lghast.elemenix.network.infuser.RequestInfuserUpdatePayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = Elemenics.MOD_ID)
public class NetworkPayloadHandler {

    @SubscribeEvent
    public static void registerPayloadHandlers(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1.0");

        registrar.playToServer(
                DeconstructionPayload.TYPE,
                DeconstructionPayload.STREAM_CODEC,
                DeconstructionPayload::handle
        );

        registrar.playToServer(
                ReconstructionPayload.TYPE,
                ReconstructionPayload.STREAM_CODEC,
                ReconstructionPayload::handle
        );

        registrar.playToServer(
                OpenMemorizerBoxPayload.TYPE,
                OpenMemorizerBoxPayload.STREAM_CODEC,
                OpenMemorizerBoxPayload::handle
        );

        registrar.playToServer(
                BurnerMovePayload.TYPE,
                BurnerMovePayload.STREAM_CODEC,
                BurnerMovePayload::handle
        );

        registrar.playToServer(
                BurnerDeletePayload.TYPE,
                BurnerDeletePayload.STREAM_CODEC,
                BurnerDeletePayload::handle
        );

        registrar.playToServer(
                BurnerSwapPayload.TYPE,
                BurnerSwapPayload.STREAM_CODEC,
                BurnerSwapPayload::handle
        );

        registrar.playToServer(
                BurnerActionPayload.TYPE,
                BurnerActionPayload.STREAM_CODEC,
                BurnerActionPayload::handle
        );

        registrar.playToServer(
                BurnerInsertBeforePayload.TYPE,
                BurnerInsertBeforePayload.STREAM_CODEC,
                BurnerInsertBeforePayload::handle
        );

        registrar.playToServer(
                RequestInfuserUpdatePayload.TYPE,
                RequestInfuserUpdatePayload.STREAM_CODEC,
                RequestInfuserUpdatePayload::handle
        );

        registrar.playToClient(
                InfuserDataUpdatePayload.TYPE,
                InfuserDataUpdatePayload.STREAM_CODEC,
                InfuserDataUpdatePayload::handle
        );

        registrar.playToClient(
                SyncModStartedPayload.TYPE,
                SyncModStartedPayload.STREAM_CODEC,
                SyncModStartedPayload::handle
        );
    }
}
