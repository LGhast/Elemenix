package net.lghast.elemenix.network.registrar;

import net.lghast.elemenix.client.cache.ClientRegistrarCache;
import net.lghast.elemenix.common.system.datacomponent.PotionMemoryData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("unused")
public record ResponseRegistrarSyncPayload(List<ResourceLocation> items, List<PotionMemoryData.Entry> potions, boolean developerMode)
        implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ResponseRegistrarSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("elemenix", "response_registrar_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ResponseRegistrarSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()),
                    ResponseRegistrarSyncPayload::items,
                    PotionMemoryData.Entry.STREAM_CODEC.apply(ByteBufCodecs.list()),
                    ResponseRegistrarSyncPayload::potions,
                    ByteBufCodecs.BOOL,
                    ResponseRegistrarSyncPayload::developerMode,
                    ResponseRegistrarSyncPayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ResponseRegistrarSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientRegistrarCache.update(payload.items(), payload.potions());
            ClientRegistrarCache.setDeveloperMode(payload.developerMode());
        });
    }
}
