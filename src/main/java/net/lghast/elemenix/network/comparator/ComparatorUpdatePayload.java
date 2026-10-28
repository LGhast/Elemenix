package net.lghast.elemenix.network.comparator;

import net.lghast.elemenix.common.system.menu.ElemenicComparatorMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record ComparatorUpdatePayload(int target, int rule, int threshold, int unit) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ComparatorUpdatePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("elemenix", "comparator_update"));

    public static final StreamCodec<FriendlyByteBuf, ComparatorUpdatePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, ComparatorUpdatePayload::target,
                    ByteBufCodecs.VAR_INT, ComparatorUpdatePayload::rule,
                    ByteBufCodecs.VAR_INT, ComparatorUpdatePayload::threshold,
                    ByteBufCodecs.VAR_INT, ComparatorUpdatePayload::unit,
                    ComparatorUpdatePayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ComparatorUpdatePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof ElemenicComparatorMenu menu) {
                menu.update(payload.target(), payload.rule(), payload.threshold(), payload.unit());
            }
        });
    }
}
