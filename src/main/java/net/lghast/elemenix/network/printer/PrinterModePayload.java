package net.lghast.elemenix.network.printer;

import net.lghast.elemenix.common.system.menu.PrinterMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PrinterModePayload(int mode) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PrinterModePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("elemenix", "printer_mode"));

    public static final StreamCodec<FriendlyByteBuf, PrinterModePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, PrinterModePayload::mode,
                    PrinterModePayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PrinterModePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof PrinterMenu menu) {
                menu.setMode(payload.mode());
            }
        });
    }
}
