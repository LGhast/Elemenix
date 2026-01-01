package net.lghast.elemenix.network;

import net.lghast.elemenix.common.system.menu.MemorizerBoxMenu;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.utils.ModUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public record OpenMemorizerBoxPayload() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OpenMemorizerBoxPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("elemenix", "open_memorizer_box"));

    public static final StreamCodec<FriendlyByteBuf, OpenMemorizerBoxPayload> STREAM_CODEC =
            StreamCodec.unit(new OpenMemorizerBoxPayload());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenMemorizerBoxPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ItemStack boxStack = ModUtils.findItemInPlayerInventory(player, ModItems.MEMORIZER_BOX.asItem());
                if (!boxStack.isEmpty()) {
                    player.openMenu(new SimpleMenuProvider(
                            (windowId, playerInventory, playerEntity) ->
                                    new MemorizerBoxMenu(windowId, playerInventory, boxStack),
                            Component.translatable("gui.elemenix.memorizer_box")
                    ));
                }
            }
        });
    }
}
