package net.lghast.elemenix.client.screen;

import net.lghast.elemenix.common.content.item.MemorizerItem;
import net.lghast.elemenix.common.system.datacomponent.MemoryData;
import net.lghast.elemenix.common.system.menu.MemorizerBoxMenu;
import net.lghast.elemenix.register.content.ModItems;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class MemorizerBoxScreen extends AbstractContainerScreen<MemorizerBoxMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("elemenix", "textures/gui/memorizer_box.png");

    private static final float ITEM_SCALE = 0.55f;
    private static final int GUI_WIDTH = 190;
    private static final int GUI_HEIGHT = 180;
    private static final int TITLE_X = 15;
    private static final int TITLE_Y = 6;
    private static final int ITEM_Y_OFFSET = -15;

    public MemorizerBoxScreen(MemorizerBoxMenu menu, Inventory inventory,
                              Component title) {
        super(menu, inventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
        this.titleLabelX = TITLE_X;
        this.titleLabelY = TITLE_Y;
        this.inventoryLabelY = 10000;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int
            mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight,
                this.imageWidth, this.imageHeight);

        renderSlotMemoryItems(graphics, x, y);
    }

    private void renderSlotMemoryItems(GuiGraphics graphics, int guiLeft, int guiTop) {
        for (int i = 0; i < 18; i++) {
            Slot slot = this.menu.getSlot(i);
            if (slot.hasItem()) {
                ItemStack stack = slot.getItem();

                if (stack.is(ModItems.ELEMENIC_MEMORIZER)) {
                    MemoryData memoryData = MemorizerItem.getOrCreateMemories(stack);
                    ItemStack memoryItem = memoryData.firstItem();

                    if (!memoryItem.isEmpty()) {
                        int slotX = guiLeft + slot.x;
                        int slotY = guiTop + slot.y;

                        graphics.pose().pushPose();

                        graphics.pose().translate(slotX, slotY, 0);
                        graphics.pose().translate(8, 8, 0);
                        graphics.pose().scale(ITEM_SCALE, ITEM_SCALE, 1.0f);
                        graphics.pose().translate(0, ITEM_Y_OFFSET / ITEM_SCALE, 0);
                        graphics.pose().translate(-8, -8, 0);
                        graphics.renderItem(memoryItem, 0, 0);

                        graphics.pose().popPose();
                    }
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float
            partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
