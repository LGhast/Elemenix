package net.lghast.elemenix.network.memorizerbox;

import net.lghast.elemenix.common.system.datacomponent.MemorizerSlotLock;
import net.lghast.elemenix.common.system.menu.MemorizerBoxMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public record ToggleMemorizerSlotLockPayload(boolean enabled) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ToggleMemorizerSlotLockPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("elemenix", "toggle_memorizer_box_style_lock"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleMemorizerSlotLockPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    ToggleMemorizerSlotLockPayload::enabled,
                    ToggleMemorizerSlotLockPayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ToggleMemorizerSlotLockPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof MemorizerBoxMenu menu) {
                boolean enabled = payload.enabled();
                MemorizerSlotLock lock = enabled ? menu.captureStyleLock() : MemorizerSlotLock.EMPTY;
                menu.setStyleLock(enabled, lock);
                PacketDistributor.sendToPlayer(player, new SyncMemorizerSlotLockPayload(enabled, lock));
            }
        });
    }
}
