package net.lghast.elemenix.network.outlet;

import net.lghast.elemenix.common.system.menu.OutletMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record OutletPercentPayload(int percent) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OutletPercentPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("elemenix", "outlet_percent"));

    public static final StreamCodec<FriendlyByteBuf, OutletPercentPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, OutletPercentPayload::percent,
                    OutletPercentPayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OutletPercentPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof OutletMenu menu) {
                menu.setOutputPercent(payload.percent());
            }
        });
    }
}
