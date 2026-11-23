package net.lghast.elemenix.network.scan;

import net.lghast.elemenix.common.system.misc.ScanHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public record RequestContainerScanPayload(BlockPos pos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RequestContainerScanPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("elemenix", "request_container_scan"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestContainerScanPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    RequestContainerScanPayload::pos,
                    RequestContainerScanPayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequestContainerScanPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                boolean hasParseable = false;
                BlockEntity be = player.level().getBlockEntity(payload.pos());
                if (be instanceof BaseContainerBlockEntity container) {
                    hasParseable = ScanHelper.hasParseableItem(container);
                }
                context.reply(new ResponseContainerScanPayload(payload.pos(), hasParseable));
            }
        });
    }
}
