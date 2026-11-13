package net.lghast.elemenix.client.screen;

import net.lghast.elemenix.common.content.blockentity.AbstractPrinterBlockEntity;
import net.lghast.elemenix.common.system.menu.PrinterMenu;
import net.lghast.elemenix.network.printer.PrinterModePayload;
import net.lghast.elemenix.utils.elemenix.Elemenix;
import net.lghast.elemenix.utils.misc.LongContainerData;
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

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class PrinterScreen extends AbstractContainerScreen<PrinterMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("elemenix", "textures/gui/printer.png");

    private static final int GUI_WIDTH = 256;
    private static final int GUI_HEIGHT = 200;
    private static final int TITLE_X = 9;
    private static final int TITLE_Y = 7;
    private static final int LIST_X = 122;
    private static final int LIST_Y = 7;
    private static final int LIST_WIDTH = 127;
    private static final int LIST_HEIGHT = 105;
    private static final int PREV_BUTTON_X = 110;
    private static final int TITLE_MAX_WIDTH = PREV_BUTTON_X - TITLE_X - 4;
    private static final int PREV_BUTTON_Y = 6;
    private static final int NEXT_BUTTON_X = 110;
    private static final int NEXT_BUTTON_Y = 101;
    private static final int BUTTON_SIZE = 12;
    private static final int VALUE_START_X = 22;
    private static final int VALUE_START_Y = 23;
    private static final int VALUE_SPACING = 14;
    private static final int MODE_LEFT_BUTTON_X = 9;
    private static final int MODE_RIGHT_BUTTON_X = 68;
    private static final int MODE_BUTTON_Y = 179;
    private static final int MODE_BUTTON_SIZE = 13;
    private static final int MODE_TEXT_CENTER_X = 45;
    private static final int MODE_TEXT_Y = 182;
    private static final int MODE_TEXT_MAX_WIDTH = MODE_RIGHT_BUTTON_X - MODE_LEFT_BUTTON_X - MODE_BUTTON_SIZE - 4;

    private PrinterMemoryListWidget memoryListWidget;
    private Button prevPageButton;
    private Button nextPageButton;
    private int mode;

    public PrinterScreen(PrinterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
        this.titleLabelY = TITLE_Y;
        this.titleLabelX = TITLE_X;
        this.inventoryLabelY = 10000;
    }

    @Override
    protected void init() {
        super.init();

        this.mode = menu.getMode();

        memoryListWidget = new PrinterMemoryListWidget(
                leftPos + LIST_X,
                topPos + LIST_Y,
                LIST_WIDTH,
                LIST_HEIGHT
        );
        this.addRenderableWidget(memoryListWidget);

        prevPageButton = Button.builder(Component.literal("←"), button -> {
            memoryListWidget.previousPage();
            updateButtonStates();
        }).bounds(leftPos + PREV_BUTTON_X, topPos + PREV_BUTTON_Y, BUTTON_SIZE, BUTTON_SIZE).build();
        this.addRenderableWidget(prevPageButton);

        nextPageButton = Button.builder(Component.literal("→"), button -> {
            memoryListWidget.nextPage();
            updateButtonStates();
        }).bounds(leftPos + NEXT_BUTTON_X, topPos + NEXT_BUTTON_Y, BUTTON_SIZE, BUTTON_SIZE).build();
        this.addRenderableWidget(nextPageButton);

        this.addRenderableWidget(Button.builder(Component.literal("<"), button -> cycleMode(-1))
                .bounds(leftPos + MODE_LEFT_BUTTON_X, topPos + MODE_BUTTON_Y, MODE_BUTTON_SIZE, MODE_BUTTON_SIZE).build());

        this.addRenderableWidget(Button.builder(Component.literal(">"), button -> cycleMode(1))
                .bounds(leftPos + MODE_RIGHT_BUTTON_X, topPos + MODE_BUTTON_Y, MODE_BUTTON_SIZE, MODE_BUTTON_SIZE).build());

        memoryListWidget.setOnPageChange(this::updateButtonStates);
        updateButtonStates();
    }

    private void cycleMode(int delta) {
        mode = Math.floorMod(mode + delta, AbstractPrinterBlockEntity.MODE_COUNT);
        sendModeUpdate();
    }

    private void sendModeUpdate() {
        if (this.minecraft != null && this.minecraft.getConnection() != null) {
            this.minecraft.getConnection().send(new PrinterModePayload(mode));
        }
    }

    private String getModeName() {
        return switch (mode) {
            case AbstractPrinterBlockEntity.MODE_FIRST ->
                    Component.translatable("mode.elemenix.printer.first").getString();
            case AbstractPrinterBlockEntity.MODE_SEQUENTIAL ->
                    Component.translatable("mode.elemenix.printer.sequential").getString();
            case AbstractPrinterBlockEntity.MODE_RANDOM ->
                    Component.translatable("mode.elemenix.printer.random").getString();
            default -> "";
        };
    }

    private void renderModeText(GuiGraphics graphics) {
        String modeText = getModeName();
        int textWidth = this.font.width(modeText);

        if (textWidth <= MODE_TEXT_MAX_WIDTH) {
            int textX = leftPos + MODE_TEXT_CENTER_X - textWidth / 2;
            graphics.drawString(this.font, modeText, textX, topPos + MODE_TEXT_Y, 0xFFFFFF, false);
            return;
        }

        float scale = (float) MODE_TEXT_MAX_WIDTH / textWidth;
        int scaledWidth = (int) (textWidth * scale);
        int textX = leftPos + MODE_TEXT_CENTER_X - scaledWidth / 2;
        float verticalShift = (this.font.lineHeight * (1.0f - scale)) / 2.0f;

        graphics.pose().pushPose();
        graphics.pose().translate(textX, topPos + MODE_TEXT_Y + verticalShift, 0.0f);
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.drawString(this.font, modeText, 0, 0, 0xFFFFFF, false);
        graphics.pose().popPose();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        String titleText = this.title.getString();
        int textWidth = this.font.width(titleText);

        if (textWidth <= TITLE_MAX_WIDTH) {
            graphics.drawString(this.font, titleText, TITLE_X, TITLE_Y, 0x404040, false);
            return;
        }

        float scale = (float) TITLE_MAX_WIDTH / textWidth;
        float verticalShift = (this.font.lineHeight * (1.0f - scale)) / 2.0f;

        graphics.pose().pushPose();
        graphics.pose().translate(TITLE_X, TITLE_Y + verticalShift, 0.0f);
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.drawString(this.font, titleText, 0, 0, 0x404040, false);
        graphics.pose().popPose();
    }

    private void updateButtonStates() {
        prevPageButton.active = memoryListWidget.hasPreviousPage();
        nextPageButton.active = memoryListWidget.getItemCount() > 7;
    }

    private void renderDisabledTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (memoryListWidget == null) return;
        String hoveredKey = memoryListWidget.getHoveredItemKey(mouseX, mouseY);
        if (hoveredKey != null && menu.isDisabled(hoveredKey)) {
            graphics.renderTooltip(this.font, Component.translatable("gui.elemenix.disabled"), mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
        renderElemenixValues(graphics, x, y);
    }

    private void renderElemenixValues(GuiGraphics graphics, int x, int y) {
        Elemenix[] types = Elemenix.values();
        LongContainerData containerData = menu.getData();
        for (int i = 0; i < types.length; i++) {
            long value = containerData.getLong(i);
            String text = ModUtils.formatNumber(value);
            graphics.drawString(this.font, text, x + VALUE_START_X, y + VALUE_START_Y + i * VALUE_SPACING,
                    types[i].getColor(), false);
        }
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (memoryListWidget != null) {
            memoryListWidget.forceUpdate();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);

        renderModeText(graphics);

        renderDisabledTooltip(graphics, mouseX, mouseY);
        this.renderTooltip(graphics, mouseX, mouseY);

        if (memoryListWidget != null) {
            memoryListWidget.forceUpdate();
        }
    }
}
