package net.lghast.elemenix.network.analyzer;

import net.lghast.elemenix.common.system.menu.AnalyzerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PotionReconstructionPayload(ResourceLocation itemId, ResourceLocation potionId, boolean shiftClick) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PotionReconstructionPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("elemenix", "potion_reconstruction"));

    public static final StreamCodec<FriendlyByteBuf, PotionReconstructionPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, PotionReconstructionPayload::itemId,
                    ResourceLocation.STREAM_CODEC, PotionReconstructionPayload::potionId,
                    ByteBufCodecs.BOOL, PotionReconstructionPayload::shiftClick,
                    PotionReconstructionPayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PotionReconstructionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player &&
                    player.containerMenu instanceof AnalyzerMenu menu) {
                menu.reconstructPotion(payload.itemId(), payload.potionId(), payload.shiftClick());
            }
        });
    }
}
