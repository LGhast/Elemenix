package net.lghast.elemenix.network.memorizerbox;

import net.lghast.elemenix.common.content.item.MemorizerBoxItem;
import net.lghast.elemenix.common.util.ModUtils;
import net.lghast.elemenix.register.content.ModItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public record CollectMemorizersPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CollectMemorizersPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("elemenix", "collect_memorizers"));

    public static final StreamCodec<FriendlyByteBuf, CollectMemorizersPayload> STREAM_CODEC =
            StreamCodec.unit(new CollectMemorizersPayload());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CollectMemorizersPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ItemStack box = ModUtils.findItemInCuriosSlot(player, "memorizer_box", ModItems.MEMORIZER_BOX.asItem());
                if (!box.isEmpty()) {
                    MemorizerBoxItem.collectMemorizers(player, box);
                }
            }
        });
    }
}
