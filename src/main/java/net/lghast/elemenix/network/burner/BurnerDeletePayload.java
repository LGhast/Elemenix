package net.lghast.elemenix.network.burner;

import net.lghast.elemenix.common.system.menu.BurnerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record BurnerDeletePayload(int itemIndex) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<BurnerDeletePayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath("elemenix", "burner_delete")
            );

    public static final StreamCodec<FriendlyByteBuf, BurnerDeletePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, BurnerDeletePayload::itemIndex,
                    BurnerDeletePayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(BurnerDeletePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player &&
                    player.containerMenu instanceof BurnerMenu menu) {
                menu.deleteItem(payload.itemIndex());
            }
        });
    }
}
