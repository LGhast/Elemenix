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

public record BurnerInsertBeforePayload(int selectedIndex, int targetIndex) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<BurnerInsertBeforePayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath("elemenix", "burner_insert_before")
            );

    public static final StreamCodec<FriendlyByteBuf, BurnerInsertBeforePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, BurnerInsertBeforePayload::selectedIndex,
                    ByteBufCodecs.VAR_INT, BurnerInsertBeforePayload::targetIndex,
                    BurnerInsertBeforePayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(BurnerInsertBeforePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player &&
                    player.containerMenu instanceof BurnerMenu menu) {
                menu.insertBefore(payload.selectedIndex(), payload.targetIndex());
            }
        });
    }
}
