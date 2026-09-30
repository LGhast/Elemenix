package net.lghast.elemenix.common.content.item;

import net.lghast.elemenix.common.system.menu.AnalysisTerminalMenu;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.utils.ModUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class AnalysisTerminalItem extends Item {
    public AnalysisTerminalItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        ItemStack analyzerStack = ModUtils.findItemInCuriosSlot(player, "analyzer", ModItems.ELEMENIC_ANALYZER.asItem());
        ItemStack boxStack = ModUtils.findItemInCuriosSlot(player, "memorizer_box", ModItems.MEMORIZER_BOX.asItem());
        if (analyzerStack.isEmpty() || boxStack.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable("message.elemenix.analysis_terminal.requires_equipment"), true);
            return InteractionResultHolder.fail(stack);
        }
        AnalyzerItem.getOrCreateUuid(analyzerStack);
        MemorizerBoxItem.createUuid(boxStack);

        int lockedSlot = usedHand == InteractionHand.MAIN_HAND ? player.getInventory().selected : Inventory.SLOT_OFFHAND;

        if (player instanceof ServerPlayer serverPlayer) {
            final int slot = lockedSlot;
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (windowId, playerInventory, playerEntity) ->
                            new AnalysisTerminalMenu(windowId, playerInventory, analyzerStack, boxStack, slot),
                    Component.translatable("gui.elemenix.analysis_terminal")
            ));
        }
        return InteractionResultHolder.success(stack);
    }
}
