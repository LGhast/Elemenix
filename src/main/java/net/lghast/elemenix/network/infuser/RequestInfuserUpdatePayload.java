package net.lghast.elemenix.network.infuser;

import net.lghast.elemenix.common.content.blockentity.InfuserBlockEntity;
import net.lghast.elemenix.common.content.item.StorageItem;
import net.lghast.elemenix.common.system.datacomponent.ElemenicStorage;
import net.lghast.elemenix.register.content.ModItems;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public record RequestInfuserUpdatePayload(GlobalPos pos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RequestInfuserUpdatePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("elemenix", "request_infuser_update"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestInfuserUpdatePayload> STREAM_CODEC =
            StreamCodec.composite(
                    GlobalPos.STREAM_CODEC,
                    RequestInfuserUpdatePayload::pos,
                    RequestInfuserUpdatePayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequestInfuserUpdatePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                var level = player.level();
                var blockEntity = level.getBlockEntity(payload.pos().pos());

                if (blockEntity instanceof InfuserBlockEntity infuser) {
                    ItemStack storageStack = infuser.getItem(0);
                    if (storageStack.is(ModItems.ELEMENIC_STORAGE)) {
                        ElemenicStorage storage = StorageItem.getOrCreateData(storageStack);
                        context.reply(new InfuserDataUpdatePayload(payload.pos(), storage.elemenix()));
                    }
                }
            }
        });
    }
}
