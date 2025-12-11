package net.lghast.elemenix.network;

import net.lghast.elemenix.client.screen.BurnerMode;
import net.lghast.elemenix.common.system.menu.BurnerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record BurnerActionPayload(BurnerMode mode) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<BurnerActionPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath("elemenix", "burner_action")
            );

    public static final StreamCodec<FriendlyByteBuf, BurnerActionPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public @NotNull BurnerActionPayload decode(FriendlyByteBuf buf) {
                    int ordinal = buf.readVarInt();
                    BurnerMode[] values = BurnerMode.values();
                    if (ordinal >= 0 && ordinal < values.length) {
                        return new BurnerActionPayload(values[ordinal]);
                    }
                    return new BurnerActionPayload(BurnerMode.MOVE_UP);
                }

                @Override
                public void encode(FriendlyByteBuf buf, BurnerActionPayload payload) {
                    buf.writeVarInt(payload.mode().ordinal());
                }
            };

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(BurnerActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player &&
                    player.containerMenu instanceof BurnerMenu menu) {

                BurnerMode mode = payload.mode();

                if (mode == BurnerMode.COPY) {
                    menu.copyMemories();
                } else if (mode == BurnerMode.CONCATENATE) {
                    menu.concatenateMemories();
                }
            }
        });
    }
}
