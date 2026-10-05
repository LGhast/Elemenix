package net.lghast.elemenix.network.transformer;

import net.lghast.elemenix.common.system.menu.TransformerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public record TransformerSwitchPagePayload(int page) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<TransformerSwitchPagePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("elemenix", "transformer_switch_page"));

    public static final StreamCodec<FriendlyByteBuf, TransformerSwitchPagePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, TransformerSwitchPagePayload::page,
                    TransformerSwitchPagePayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(TransformerSwitchPagePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof TransformerMenu menu) {
                menu.switchPage(payload.page());
            }
        });
    }
}
