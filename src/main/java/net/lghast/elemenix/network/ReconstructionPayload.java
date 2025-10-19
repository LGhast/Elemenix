package net.lghast.elemenix.network;

import net.lghast.elemenix.common.system.menu.AnalyzerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ReconstructionPayload(ResourceLocation itemId, boolean shiftClick) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ReconstructionPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("elemenix", "reconstruction"));

    public static final StreamCodec<FriendlyByteBuf, ReconstructionPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, ReconstructionPayload::itemId,
                    ByteBufCodecs.BOOL, ReconstructionPayload::shiftClick,
                    ReconstructionPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ReconstructionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player &&
                    player.containerMenu instanceof AnalyzerMenu menu) {
                menu.reconstructItem(payload.itemId(), payload.shiftClick());
            }
        });
    }
}
