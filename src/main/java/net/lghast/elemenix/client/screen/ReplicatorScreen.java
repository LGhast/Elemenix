package net.lghast.elemenix.client.screen;

import net.lghast.elemenix.client.misc.Palette;
import net.lghast.elemenix.client.render.ScaledText;
import net.lghast.elemenix.client.screen.widget.ReplicatorMemoryListWidget;
import net.lghast.elemenix.common.system.menu.HintSlot;
import net.lghast.elemenix.common.system.menu.ReplicatorMenu;
import net.lghast.elemenix.config.CommonConfig;
import net.lghast.elemenix.core.Elemenix;
import net.lghast.elemenix.common.system.menu.LongContainerData;
import net.lghast.elemenix.common.util.ModUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class ReplicatorScreen extends AbstractContainerScreen<ReplicatorMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("elemenix", "textures/gui/replicator.png");

    private static final int GUI_WIDTH = 237;
    private static final int GUI_HEIGHT = 200;
    private static final int TITLE_CENTER_X = 48;
    private static final int TITLE_Y = 8;
    private static final int LIST_X = 103;
    private static final int LIST_Y = 7;
    private static final int LIST_WIDTH = 127;
    private static final int LIST_HEIGHT = 105;
    private static final int PREV_BUTTON_X = 91;
    private static final int PREV_BUTTON_Y = 6;
    private static final int NEXT_BUTTON_X = 91;
    private static final int NEXT_BUTTON_Y = 101;
    private static final int BUTTON_SIZE = 12;
    private static final int TITLE_MAX_WIDTH = 75;
    private static final float TITLE_MIN_SCALE = 0.8f;
    private static final int ENERGIX_VALUE_X = 23;
    private static final int ENERGIX_VALUE_Y = 56;

    private ReplicatorMemoryListWidget memoryListWidget;
    private Button prevPageButton;
    private Button nextPageButton;

    public ReplicatorScreen(ReplicatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
        this.titleLabelX = TITLE_CENTER_X;
        this.titleLabelY = TITLE_Y;
        this.inventoryLabelY = 10000;
    }

    @Override
    protected void init() {
        super.init();

        memoryListWidget = new ReplicatorMemoryListWidget(
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

        memoryListWidget.setOnPageChange(this::updateButtonStates);
        updateButtonStates();
    }

    private void updateButtonStates() {
        prevPageButton.active = memoryListWidget.hasPreviousPage();
        nextPageButton.active = memoryListWidget.hasNextPage();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        renderTitle(graphics, this.title.getString());
    }

    private void renderTitle(GuiGraphics graphics, String titleText) {
        List<ScaledText.Line> lines = ScaledText.wrapToFit(this.font, titleText, TITLE_MAX_WIDTH, TITLE_MIN_SCALE);
        int lineHeight = this.font.lineHeight;
        for (int i = 0; i < lines.size(); i++) {
            ScaledText.Line line = lines.get(i);
            ScaledText.renderCentered(graphics, this.font, line.text(), line.scale(), TITLE_CENTER_X, TITLE_Y + i * lineHeight, Palette.TEXT_COMMON, true);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
        renderEnergixValue(graphics, x, y);
    }

    private void renderEnergixValue(GuiGraphics graphics, int x, int y) {
        LongContainerData data = menu.getData();
        long value = data.getLong(Elemenix.ENERGIX.getIndex());
        String text = ModUtils.formatNumber(value);
        graphics.drawString(this.font, text, x + ENERGIX_VALUE_X, y + ENERGIX_VALUE_Y, Elemenix.ENERGIX.getColor(), false);
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
        renderEmptySlotHints(graphics, mouseX, mouseY);
        this.renderTooltip(graphics, mouseX, mouseY);
        renderEnergixTooltip(graphics, mouseX, mouseY);
        if (memoryListWidget != null) {
            memoryListWidget.forceUpdate();
        }
    }

    private void renderEnergixTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        LongContainerData data = menu.getData();
        String text = ModUtils.formatNumber(data.getLong(Elemenix.ENERGIX.getIndex()));
        int x = leftPos + ENERGIX_VALUE_X;
        int y = topPos + ENERGIX_VALUE_Y;
        int textWidth = this.font.width(text);
        int textHeight = this.font.lineHeight;
        if (mouseX < x || mouseX >= x + textWidth || mouseY < y || mouseY >= y + textHeight) {
            return;
        }
        long cost;
        MutableComponent tooltip;
        if (menu.getMasterStack().isEmpty()) {
            cost = CommonConfig.REPLICATOR_REPLICATION_ENERGIX_COST.get();
            tooltip = Component.translatable("gui.elemenix.replicator.energix_cost_per_memory", cost);
        } else {
            cost = (long) menu.getBlockEntity().getMasterMemoryCount() * CommonConfig.REPLICATOR_REPLICATION_ENERGIX_COST.get();
            tooltip = Component.translatable("gui.elemenix.replicator.energix_cost_per_memorizer", cost);
        }
        tooltip = tooltip.withStyle(style -> style.withColor(Elemenix.ENERGIX.getColor()));
        graphics.renderTooltip(this.font, tooltip, mouseX, mouseY);
    }

    private void renderEmptySlotHints(GuiGraphics graphics, int mouseX, int mouseY) {
        Slot slot = this.getSlotUnderMouse();
        if (slot instanceof HintSlot hintSlot && !slot.hasItem()) {
            graphics.renderTooltip(this.font, hintSlot.getHint(), mouseX, mouseY);
        }
    }
}
