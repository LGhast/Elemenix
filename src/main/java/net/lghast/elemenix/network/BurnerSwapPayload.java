package net.lghast.elemenix.network;

import net.lghast.elemenix.common.system.menu.BurnerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record BurnerSwapPayload(int index1, int index2) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<BurnerSwapPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath("elemenix", "burner_swap")
            );

    public static final StreamCodec<FriendlyByteBuf, BurnerSwapPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, BurnerSwapPayload::index1,
                    ByteBufCodecs.VAR_INT, BurnerSwapPayload::index2,
                    BurnerSwapPayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(BurnerSwapPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player &&
                    player.containerMenu instanceof BurnerMenu menu) {
                menu.swapItems(payload.index1(), payload.index2());
            }
        });
    }
}
