package net.lghast.elemenix.network.ejector;

import net.lghast.elemenix.common.system.menu.EjectorMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record EjectorFilterPayload(int filterIndex) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<EjectorFilterPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("elemenix", "ejector_filter"));

    public static final StreamCodec<FriendlyByteBuf, EjectorFilterPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, EjectorFilterPayload::filterIndex,
                    EjectorFilterPayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(EjectorFilterPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof EjectorMenu menu) {
                menu.setFilterIndex(payload.filterIndex());
            }
        });
    }
}
