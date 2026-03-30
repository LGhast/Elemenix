package net.lghast.elemenix.network;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.utils.elemenix.ElemenixInfo;
import net.lghast.elemenix.utils.recipe.RecipeHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncModStartedPayload(boolean started) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncModStartedPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath("elemenix", "sync_mod_started")
            );

    public static final StreamCodec<FriendlyByteBuf, SyncModStartedPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public @NotNull SyncModStartedPayload decode(FriendlyByteBuf buf) {
                    boolean started = buf.readBoolean();
                    return new SyncModStartedPayload(started);
                }

                @Override
                public void encode(FriendlyByteBuf buf, SyncModStartedPayload payload) {
                    buf.writeBoolean(payload.started());
                }
            };

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncModStartedPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Elemenics.started = payload.started();
            ElemenixInfo.clearCaches();
            RecipeHelper.clearCache();
        });
    }
}
