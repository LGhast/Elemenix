package net.lghast.elemenix.network.open;

import net.lghast.elemenix.common.system.menu.MemoryBurnerMenu;
import net.lghast.elemenix.common.util.ModUtils;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.register.system.ModStats;
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
public record OpenMemoryBurnerPayload() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OpenMemoryBurnerPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("elemenix", "open_memory_burner"));

    public static final StreamCodec<FriendlyByteBuf, OpenMemoryBurnerPayload> STREAM_CODEC =
            StreamCodec.unit(new OpenMemoryBurnerPayload());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenMemoryBurnerPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ItemStack burnerStack = ModUtils.findItemInCuriosSlot(
                        player,
                        "memory_burner",
                        ModItems.MEMORY_BURNER.asItem()
                );
                if (burnerStack.isEmpty()) {
                    return;
                }

                player.openMenu(new SimpleMenuProvider(
                        (windowId, playerInventory, playerEntity) ->
                                new MemoryBurnerMenu(windowId, playerInventory),
                        Component.translatable("gui.elemenix.memory_burner")
                ));
                player.awardStat(ModStats.MEMORY_BURNER_OPENS.get());
            }
        });
    }
}
