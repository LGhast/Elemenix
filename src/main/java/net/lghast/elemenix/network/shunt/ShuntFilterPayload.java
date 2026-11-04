package net.lghast.elemenix.network.shunt;

import net.lghast.elemenix.common.system.menu.ShuntMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record ShuntFilterPayload(int filterIndex) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ShuntFilterPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("elemenix", "shunt_filter"));

    public static final StreamCodec<FriendlyByteBuf, ShuntFilterPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, ShuntFilterPayload::filterIndex,
                    ShuntFilterPayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ShuntFilterPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof ShuntMenu menu) {
                menu.setFilterIndex(payload.filterIndex());
            }
        });
    }
}
