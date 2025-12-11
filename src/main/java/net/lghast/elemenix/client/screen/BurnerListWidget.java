package net.lghast.elemenix.client.screen;

import net.lghast.elemenix.common.system.menu.BurnerMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.client.gui.screens.Screen.hasShiftDown;

@OnlyIn(Dist.CLIENT)
public class BurnerListWidget extends MemoryListWidget {
    public interface ItemClickListener {
        void onItemClick(int itemIndex, boolean shiftClick);
    }

    private ItemClickListener itemClickListener;
    private final BurnerMenu menu;

    public BurnerListWidget(int x, int y, int width, int height, BurnerMenu menu) {
        super(x, y, width, height);
        this.menu = menu;
    }

    public void setOnItemClick(ItemClickListener listener) {
        this.itemClickListener = listener;
    }


    @Override
    protected boolean isMenuWrong() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return true;
        return !(minecraft.player.containerMenu instanceof BurnerMenu);
    }

    @Override
    protected void updateItemList() {
        List<ResourceLocation> currentMemories = menu.getPrimaryMemories();

        if (currentMemories.equals(this.items)) {
            return;
        }

        this.items = new ArrayList<>(currentMemories);
        this.itemStacks.clear();

        for (ResourceLocation itemId : items) {
            Item item = BuiltInRegistries.ITEM.get(itemId);
            ItemStack stack = new ItemStack(item);
            itemStacks.add(stack.isEmpty() ? ItemStack.EMPTY : stack);
        }

        if (onPageChange != null) {
            onPageChange.run();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY)) return false;

        int relativeY = (int) (mouseY - getY());
        int displayIndex = relativeY / ITEM_HEIGHT;

        if (displayIndex >= 0 && displayIndex < ITEMS_PER_PAGE) {
            int itemIndex = currentPage * ITEMS_PER_PAGE + displayIndex;

            if (itemIndex >= 0 && itemIndex < items.size()) {
                if (itemClickListener != null) {
                    boolean shiftClick = hasShiftDown();
                    itemClickListener.onItemClick(itemIndex, shiftClick);
                    this.needsUpdate = true;
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
