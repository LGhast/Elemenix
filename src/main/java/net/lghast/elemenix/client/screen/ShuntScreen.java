package net.lghast.elemenix.client.screen;

import net.lghast.elemenix.common.content.blockentity.ShuntBlockEntity;
import net.lghast.elemenix.common.system.menu.ShuntMenu;
import net.lghast.elemenix.network.shunt.ShuntFilterPayload;
import net.lghast.elemenix.utils.elemenix.Elemenix;
import net.lghast.elemenix.utils.misc.ModUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class ShuntScreen extends AbstractContainerScreen<ShuntMenu> {
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 170;
    private static final int TITLE_X = 6;
    private static final int TITLE_Y = 7;

    private static final int LEFT_BTN_X = 62;
    private static final int RIGHT_BTN_X = 141;
    private static final int BTN_Y = 21;
    private static final int BTN_SIZE = 13;
    private static final int ENTRY_NAME_X = 24;
    private static final int ENTRY_NAME_WIDTH = 37;
    private static final int ENTRY_NAME_Y = 24;
    private static final int CONTENT_CENTER_X = 108;
    private static final int CONTENT_Y = 24;

    private final ResourceLocation guiTexture = ResourceLocation.fromNamespaceAndPath("elemenix", "textures/gui/elemenic_shunt.png");

    private int filterIndex;

    public ShuntScreen(ShuntMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
        this.inventoryLabelY = 10000;
        this.titleLabelY = TITLE_Y;
        this.titleLabelX = TITLE_X;
    }

    @Override
    protected void init() {
        super.init();
        int guiX = (this.width - this.imageWidth) / 2;
        int guiY = (this.height - this.imageHeight) / 2;

        this.filterIndex = menu.getFilterIndex();

        this.addRenderableWidget(Button.builder(Component.literal("<"), b -> {
            filterIndex = filterIndex - 1 < 0 ? ShuntBlockEntity.FILTER_UNSPECIFIED : filterIndex - 1;
            sendUpdate();
        }).bounds(guiX + LEFT_BTN_X, guiY + BTN_Y, BTN_SIZE, BTN_SIZE).build());

        this.addRenderableWidget(Button.builder(Component.literal(">"), b -> {
            filterIndex = filterIndex + 1 > ShuntBlockEntity.FILTER_UNSPECIFIED ? 0 : filterIndex + 1;
            sendUpdate();
        }).bounds(guiX + RIGHT_BTN_X, guiY + BTN_Y, BTN_SIZE, BTN_SIZE).build());
    }

    private void sendUpdate() {
        if (this.minecraft != null && this.minecraft.getConnection() != null) {
            Objects.requireNonNull(this.minecraft.getConnection()).send(new ShuntFilterPayload(filterIndex));
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(guiTexture, x, y, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
        renderElemenixValues(graphics, x, y);
    }

    private void renderElemenixValues(GuiGraphics graphics, int x, int y) {
        int[] storage = menu.getStorage();
        if (storage == null || storage.length != 6) return;

        for (int i = 0; i <= 2; i++) {
            String text = ModUtils.formatNumber(storage[i]);
            graphics.drawString(this.font, text, x + 28, y + 44 + i * 14, Elemenix.values()[i].getColor(), false);
        }
        for (int i = 3; i <= 5; i++) {
            String text = ModUtils.formatNumber(storage[i]);
            graphics.drawString(this.font, text, x + 110, y + 44 + (i - 3) * 14, Elemenix.values()[i].getColor(), false);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, (this.imageWidth - this.font.width(this.title)) / 2, this.titleLabelY, 4210752, false);

        Component filterLabel = Component.translatable("gui.elemenix.target");
        int nameX = ENTRY_NAME_X + (ENTRY_NAME_WIDTH - this.font.width(filterLabel)) / 2;
        graphics.drawString(this.font, filterLabel, nameX, ENTRY_NAME_Y, 0x000000, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        drawCenteredFilter(graphics, x, y);

        this.renderTooltip(graphics, mouseX, mouseY);
    }

    private void drawCenteredFilter(GuiGraphics graphics, int x, int y) {
        String text = filterName();
        int color = filterColor();
        int cx = x + CONTENT_CENTER_X - this.font.width(text) / 2;
        graphics.drawString(this.font, text, cx, y + CONTENT_Y, color, false);
    }

    private String filterName() {
        if (filterIndex == ShuntBlockEntity.FILTER_UNSPECIFIED) {
            return Component.translatable("gui.elemenix.unspecified").getString();
        }
        return Elemenix.values()[filterIndex].getName();
    }

    private int filterColor() {
        if (filterIndex == ShuntBlockEntity.FILTER_UNSPECIFIED) {
            return 0xFFFFFF;
        }
        return Elemenix.values()[filterIndex].getColor();
    }
}
