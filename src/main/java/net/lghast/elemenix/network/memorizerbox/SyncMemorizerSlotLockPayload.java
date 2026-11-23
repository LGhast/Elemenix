package net.lghast.elemenix.network.memorizerbox;

import net.lghast.elemenix.common.system.datacomponent.MemorizerSlotLock;
import net.lghast.elemenix.common.system.menu.MemorizerBoxMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public record SyncMemorizerSlotLockPayload(boolean enabled, MemorizerSlotLock lock) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncMemorizerSlotLockPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("elemenix", "sync_memorizer_box_style_lock"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncMemorizerSlotLockPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    SyncMemorizerSlotLockPayload::enabled,
                    MemorizerSlotLock.STREAM_CODEC,
                    SyncMemorizerSlotLockPayload::lock,
                    SyncMemorizerSlotLockPayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncMemorizerSlotLockPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.containerMenu instanceof MemorizerBoxMenu menu) {
                menu.setStyleLock(payload.enabled(), payload.lock());
            }
        });
    }
}
