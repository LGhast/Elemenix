package net.lghast.elemenix.network.terminal;

import net.lghast.elemenix.common.system.menu.AnalysisTerminalMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public record TerminalFlagsPayload(boolean allowDeconstruct, boolean allowMemory, boolean allowTransfer)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<TerminalFlagsPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("elemenix", "terminal_flags"));

    public static final StreamCodec<FriendlyByteBuf, TerminalFlagsPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeBoolean(payload.allowDeconstruct());
                buf.writeBoolean(payload.allowMemory());
                buf.writeBoolean(payload.allowTransfer());
            },
            buf -> new TerminalFlagsPayload(buf.readBoolean(), buf.readBoolean(), buf.readBoolean())
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(TerminalFlagsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player
                    && player.containerMenu instanceof AnalysisTerminalMenu menu) {
                menu.setFlags(payload.allowDeconstruct(), payload.allowMemory(), payload.allowTransfer());
            }
        });
    }
}
