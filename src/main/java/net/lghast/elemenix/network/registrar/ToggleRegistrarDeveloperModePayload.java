package net.lghast.elemenix.network.registrar;

import net.lghast.elemenix.common.content.item.MemoryRegistrarItem;
import net.lghast.elemenix.common.system.attachment.RegistrarCollection;
import net.lghast.elemenix.register.system.ModAttachments;
import net.lghast.elemenix.utils.misc.ModUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@SuppressWarnings("unused")
public record ToggleRegistrarDeveloperModePayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ToggleRegistrarDeveloperModePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("elemenix", "toggle_registrar_developer_mode"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleRegistrarDeveloperModePayload> STREAM_CODEC =
            StreamCodec.unit(new ToggleRegistrarDeveloperModePayload());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ToggleRegistrarDeveloperModePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && ModUtils.isDeveloper(player)) {
                ItemStack stack = player.getMainHandItem();
                if (stack.getItem() instanceof MemoryRegistrarItem) {
                    MemoryRegistrarItem.setDeveloperMode(stack, !MemoryRegistrarItem.isDeveloperMode(stack));
                    RegistrarCollection collection = player.getData(ModAttachments.REGISTRAR_COLLECTION);
                    PacketDistributor.sendToPlayer(player, new ResponseRegistrarSyncPayload(
                            new ArrayList<>(collection.items()), new ArrayList<>(collection.potions()),
                            MemoryRegistrarItem.isDeveloperMode(stack)));
                }
            }
        });
    }
}
