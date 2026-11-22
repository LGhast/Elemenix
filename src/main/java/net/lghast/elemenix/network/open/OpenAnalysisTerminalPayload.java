package net.lghast.elemenix.network.open;

import net.lghast.elemenix.common.content.item.AnalyzerItem;
import net.lghast.elemenix.common.content.item.MemorizerBoxItem;
import net.lghast.elemenix.common.system.menu.AnalysisTerminalMenu;
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
public record OpenAnalysisTerminalPayload() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OpenAnalysisTerminalPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("elemenix", "open_analysis_terminal"));

    public static final StreamCodec<FriendlyByteBuf, OpenAnalysisTerminalPayload> STREAM_CODEC =
            StreamCodec.unit(new OpenAnalysisTerminalPayload());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenAnalysisTerminalPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ItemStack terminalStack = ModUtils.findItemInCuriosSlot(
                        player,
                        "analysis_terminal",
                        ModItems.ANALYSIS_TERMINAL.asItem()
                );
                if (terminalStack.isEmpty()) {
                    return;
                }

                ItemStack analyzerStack = ModUtils.findItemInCuriosSlot(
                        player,
                        "analyzer",
                        ModItems.ELEMENIC_ANALYZER.asItem()
                );
                ItemStack boxStack = ModUtils.findItemInCuriosSlot(
                        player,
                        "memorizer_box",
                        ModItems.MEMORIZER_BOX.asItem()
                );
                if (analyzerStack.isEmpty() || boxStack.isEmpty()) {
                    player.displayClientMessage(
                            Component.translatable("message.elemenix.analysis_terminal.requires_equipment"), true);
                    return;
                }

                AnalyzerItem.getOrCreateUuid(analyzerStack);
                MemorizerBoxItem.createUuid(boxStack);
                player.openMenu(new SimpleMenuProvider(
                        (windowId, playerInventory, playerEntity) ->
                                new AnalysisTerminalMenu(windowId, playerInventory, analyzerStack, boxStack),
                        Component.translatable("gui.elemenix.analysis_terminal")
                ));
                player.awardStat(ModStats.ANALYSIS_TERMINAL_OPENS.get());
            }
        });
    }
}
