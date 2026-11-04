package net.lghast.elemenix.network.scan;

import net.lghast.elemenix.client.misc.ClientScanCache;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public record ResponseContainerScanPayload(BlockPos pos, boolean hasParseable) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ResponseContainerScanPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("elemenix", "response_container_scan"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ResponseContainerScanPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    ResponseContainerScanPayload::pos,
                    ByteBufCodecs.BOOL,
                    ResponseContainerScanPayload::hasParseable,
                    ResponseContainerScanPayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ResponseContainerScanPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientScanCache.update(payload.pos(), payload.hasParseable()));
    }
}
