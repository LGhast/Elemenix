package net.lghast.elemenix.network;

import net.lghast.elemenix.common.system.menu.AnalyzerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DeconstructionPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DeconstructionPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("elemenix", "deconstruction"));

    public static final StreamCodec<FriendlyByteBuf, DeconstructionPayload> STREAM_CODEC =
            StreamCodec.unit(new DeconstructionPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DeconstructionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player &&
                    player.containerMenu instanceof AnalyzerMenu menu) {
                menu.decomposeItem();
            }
        });
    }
}
