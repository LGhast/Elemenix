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
public record TransformerTogglePayload(int option) implements CustomPacketPayload {
    public static final int OPTION_TRANSFORM = 0;
    public static final int OPTION_ENRICH = 1;

    public static final CustomPacketPayload.Type<TransformerTogglePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("elemenix", "transformer_toggle"));

    public static final StreamCodec<FriendlyByteBuf, TransformerTogglePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, TransformerTogglePayload::option,
                    TransformerTogglePayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(TransformerTogglePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof TransformerMenu menu) {
                switch (payload.option()) {
                    case OPTION_TRANSFORM -> menu.toggleTransform();
                    case OPTION_ENRICH -> menu.toggleEnrich();
                    default -> { }
                }
            }
        });
    }
}
