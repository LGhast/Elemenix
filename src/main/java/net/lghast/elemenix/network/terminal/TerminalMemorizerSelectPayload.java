package net.lghast.elemenix.network.terminal;

import net.lghast.elemenix.common.system.menu.AnalysisTerminalMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public record TerminalMemorizerSelectPayload(int memorizerIndex) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<TerminalMemorizerSelectPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("elemenix", "terminal_disk_select"));

    public static final StreamCodec<FriendlyByteBuf, TerminalMemorizerSelectPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, TerminalMemorizerSelectPayload::memorizerIndex,
                    TerminalMemorizerSelectPayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(TerminalMemorizerSelectPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player
                    && player.containerMenu instanceof AnalysisTerminalMenu menu) {
                menu.setSelectedMemorizer(payload.memorizerIndex());
            }
        });
    }
}
