package net.lghast.elemenix.network.open;

import net.lghast.elemenix.common.content.item.AnalyzerItem;
import net.lghast.elemenix.common.system.menu.AnalyzerMenu;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.register.system.ModStats;
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
public record OpenAnalyzerPayload() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OpenAnalyzerPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("elemenix", "open_analyzer"));

    public static final StreamCodec<FriendlyByteBuf, OpenAnalyzerPayload> STREAM_CODEC =
            StreamCodec.unit(new OpenAnalyzerPayload());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenAnalyzerPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ItemStack analyzerStack = ModUtils.findItemInCuriosSlot(
                        player,
                        "analyzer",
                        ModItems.ELEMENIC_ANALYZER.asItem()
                );

                if (!analyzerStack.isEmpty()) {
                    AnalyzerItem.getOrCreateUuid(analyzerStack);
                    player.awardStat(ModStats.OPEN_ANALYZER.get());
                    player.openMenu(new SimpleMenuProvider(
                            (windowId, playerInventory, playerEntity) ->
                                    new AnalyzerMenu(windowId, playerInventory, analyzerStack),
                            Component.translatable("gui.elemenix.analyzer")
                    ));
                }
            }
        });
    }
}
