package net.lghast.elemenix.network.registrar;

import net.lghast.elemenix.client.misc.RegistrationStageToast;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public record RegistrationStageToastPayload(int count) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RegistrationStageToastPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("elemenix", "registration_stage_toast"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RegistrationStageToastPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    RegistrationStageToastPayload::count,
                    RegistrationStageToastPayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RegistrationStageToastPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> RegistrationStageToast.show(payload.count()));
    }
}
