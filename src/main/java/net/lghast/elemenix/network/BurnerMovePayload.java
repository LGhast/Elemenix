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

public record BurnerMovePayload(int itemIndex, int direction) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<BurnerMovePayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath("elemenix", "burner_move")
            );

    public static final StreamCodec<FriendlyByteBuf, BurnerMovePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, BurnerMovePayload::itemIndex,
                    ByteBufCodecs.VAR_INT, BurnerMovePayload::direction,
                    BurnerMovePayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(BurnerMovePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player &&
                    player.containerMenu instanceof BurnerMenu menu) {
                menu.moveItem(payload.itemIndex(), payload.direction());
            }
        });
    }
}
