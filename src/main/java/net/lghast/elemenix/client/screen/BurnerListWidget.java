package net.lghast.elemenix.client.screen;

import net.lghast.elemenix.common.system.datacomponent.PotionMemoryData;
import net.lghast.elemenix.common.system.menu.BurnerMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
        if (isMismatchMode()) {
            this.itemIds.clear();
            this.potionIds.clear();
            this.displayStacks.clear();
            if (onPageChange != null) {
                onPageChange.run();
            }
            return;
        }

        List<ResourceLocation> newItemIds = new ArrayList<>();
        List<ResourceLocation> newPotionIds = new ArrayList<>();
        List<ItemStack> newStacks = new ArrayList<>();

        if (menu.isPrimaryPotionMemorizer()) {
            for (PotionMemoryData.Entry entry : menu.getPrimaryPotionMemories()) {
                newItemIds.add(entry.itemId());
                newPotionIds.add(entry.potionId());
                newStacks.add(createPotionStack(entry.itemId(), entry.potionId()));
            }
        } else {
            for (ResourceLocation id : menu.getPrimaryMemories()) {
                newItemIds.add(id);
                newPotionIds.add(null);
                newStacks.add(new ItemStack(BuiltInRegistries.ITEM.get(id)));
            }
        }

        this.itemIds.clear();
        this.potionIds.clear();
        this.displayStacks.clear();
        this.itemIds.addAll(newItemIds);
        this.potionIds.addAll(newPotionIds);
        this.displayStacks.addAll(newStacks);

        if (onPageChange != null) {
            onPageChange.run();
        }
    }

    private boolean isMismatchMode() {
        BurnerMode mode = menu.getMode();
        return (mode == BurnerMode.COPY || mode == BurnerMode.CONCATENATE) && menu.isMemorizerMismatched();
    }

    @Override
    protected String getEmptyText() {
        if (isMismatchMode()) {
            return Component.translatable("gui.elemenix.memorizer_type_mismatch").getString();
        }
        return super.getEmptyText();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY)) return false;

        int relativeY = (int) (mouseY - getY());
        int displayIndex = relativeY / ITEM_HEIGHT;

        if (displayIndex >= 0 && displayIndex < ITEMS_PER_PAGE) {
            int itemIndex = currentPage * ITEMS_PER_PAGE + displayIndex;

            if (itemIndex >= 0 && itemIndex < itemIds.size()) {
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
