package net.lghast.elemenix.network.terminal;

import net.lghast.elemenix.common.system.menu.AnalysisTerminalMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record TerminalPotionReconstructPayload(ResourceLocation itemId, ResourceLocation potionId, boolean shiftClick) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<TerminalPotionReconstructPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("elemenix", "terminal_potion_reconstruct"));

    public static final StreamCodec<FriendlyByteBuf, TerminalPotionReconstructPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, TerminalPotionReconstructPayload::itemId,
                    ResourceLocation.STREAM_CODEC, TerminalPotionReconstructPayload::potionId,
                    ByteBufCodecs.BOOL, TerminalPotionReconstructPayload::shiftClick,
                    TerminalPotionReconstructPayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(TerminalPotionReconstructPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player
                    && player.containerMenu instanceof AnalysisTerminalMenu menu) {
                menu.reconstructPotion(payload.itemId(), payload.potionId(), payload.shiftClick());
            }
        });
    }
}
