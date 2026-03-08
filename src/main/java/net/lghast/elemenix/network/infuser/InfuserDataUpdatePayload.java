package net.lghast.elemenix.network.infuser;

import net.lghast.elemenix.client.misc.ClientInfuserDataCache;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public record InfuserDataUpdatePayload(GlobalPos pos, long[] elemenixStorage) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<InfuserDataUpdatePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("elemenix", "infuser_data_update"));

    public static final StreamCodec<RegistryFriendlyByteBuf, long[]> LONG_ARRAY_STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public long @NotNull [] decode(RegistryFriendlyByteBuf buf) {
                    int length = buf.readVarInt();
                    long[] array = new long[length];
                    for (int i = 0; i < length; i++) {
                        array[i] = buf.readLong();
                    }
                    return array;
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, long[] array) {
                    buf.writeVarInt(array.length);
                    for (long value : array) {
                        buf.writeLong(value);
                    }
                }
            };

    public static final StreamCodec<RegistryFriendlyByteBuf, InfuserDataUpdatePayload> STREAM_CODEC =
            StreamCodec.composite(
                    GlobalPos.STREAM_CODEC,
                    InfuserDataUpdatePayload::pos,
                    LONG_ARRAY_STREAM_CODEC,
                    InfuserDataUpdatePayload::elemenixStorage,
                    InfuserDataUpdatePayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(InfuserDataUpdatePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientInfuserDataCache.updateCache(payload.pos(), payload.elemenixStorage());
        });
    }
}