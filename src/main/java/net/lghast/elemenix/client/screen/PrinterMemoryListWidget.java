package net.lghast.elemenix.client.screen;

import net.lghast.elemenix.common.system.menu.PrinterMenu;
import net.lghast.elemenix.network.printer.PrinterDisableItemPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class PrinterMemoryListWidget extends MemoryListWidget {
    private final List<String> keys = new ArrayList<>();

    public PrinterMemoryListWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    protected boolean isMenuWrong() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return true;
        return !(minecraft.player.containerMenu instanceof PrinterMenu);
    }

    @Override
    protected String getEmptyText() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.containerMenu instanceof PrinterMenu menu) {
            if (menu.getMemorizerItem().isEmpty()) {
                return Component.translatable("gui.elemenix.no_memorizer").getString();
            }
        }
        return Component.translatable("gui.elemenix.no_resolved_items").getString();
    }

    @Override
    protected void updateItemList() {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return;
        if (!(player.containerMenu instanceof PrinterMenu menu)) return;

        List<String> newKeys = new ArrayList<>();
        List<ItemStack> newStacks = new ArrayList<>();

        List<String> memoryKeys = menu.getMemoryKeys();
        if (memoryKeys != null) {
            for (String key : memoryKeys) {
                newKeys.add(key);
                newStacks.add(menu.createEntryStack(key));
            }
        }

        boolean changed = !newKeys.equals(this.keys);
        if (changed) {
            currentPage = 0;
        }

        this.keys.clear();
        this.itemIds.clear();
        this.potionIds.clear();
        this.displayStacks.clear();
        this.keys.addAll(newKeys);
        this.displayStacks.addAll(newStacks);

        if (onPageChange != null) {
            onPageChange.run();
        }
    }

    @Override
    protected int getItemNameColor(int index, ItemStack stack) {
        if (index >= 0 && index < keys.size()) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null && minecraft.player.containerMenu instanceof PrinterMenu menu) {
                if (menu.isDisabled(keys.get(index))) {
                    return 0xFF3333;
                }
            }
        }
        return super.getItemNameColor(index, stack);
    }

    @Nullable
    public String getHoveredItemKey(double mouseX, double mouseY) {
        if (!isMouseOver(mouseX, mouseY)) return null;
        int relativeY = (int) (mouseY - getY());
        int displayIndex = relativeY / ITEM_HEIGHT;
        if (displayIndex >= 0 && displayIndex < ITEMS_PER_PAGE) {
            int itemIndex = currentPage * ITEMS_PER_PAGE + displayIndex;
            if (itemIndex >= 0 && itemIndex < keys.size()) {
                return keys.get(itemIndex);
            }
        }
        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        String hoveredKey = getHoveredItemKey(mouseX, mouseY);
        if (hoveredKey != null) {
            PacketDistributor.sendToServer(new PrinterDisableItemPayload(hoveredKey));
            return true;
        }
        return false;
    }
}
