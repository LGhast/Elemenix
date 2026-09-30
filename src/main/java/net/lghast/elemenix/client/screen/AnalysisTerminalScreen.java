package net.lghast.elemenix.client.screen;

import net.lghast.elemenix.common.content.item.MemorizerItem;
import net.lghast.elemenix.common.system.menu.AnalysisTerminalMenu;
import net.lghast.elemenix.network.terminal.TerminalCarriedDeconstructPayload;
import net.lghast.elemenix.network.terminal.TerminalMemorizerSelectPayload;
import net.lghast.elemenix.network.terminal.TerminalFlagsPayload;
import net.lghast.elemenix.network.terminal.TerminalReconstructPayload;
import net.lghast.elemenix.utils.Constituents;
import net.lghast.elemenix.utils.LongContainerData;
import net.lghast.elemenix.utils.ModUtils;
import net.lghast.elemenix.utils.elemenix.Elemenix;
import net.lghast.elemenix.utils.elemenix.ElemenixInfo;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

@OnlyIn(Dist.CLIENT)
public class AnalysisTerminalScreen extends AbstractContainerScreen<AnalysisTerminalMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("elemenix", "textures/gui/analysis_terminal.png");

    private static final ResourceLocation MEMORIZER_ICON_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("elemenix", "textures/gui/memorizer_icon.png");

    private static final int GUI_WIDTH = 256;
    private static final int GUI_HEIGHT = 227;
    private static final int TITLE_X = 9;
    private static final int TITLE_Y = 7;

    private static final int MEM_NAME_MAX_WIDTH = 60;
    private static final int MEM_AREA_CX = 43;
    private static final int MEM_NAME_Y = 26;
    private static final int MEM_ICON_X = 35;
    private static final int MEM_ICON_Y = 39;
    private static final int MEM_BTN_Y = 62;
    private static final int MEM_BTN_SIZE = 14;
    private static final int PREV_X = 28;
    private static final int NEXT_X = 44;
    private static final int ALL_BTN_X = 23;
    private static final int ALL_BTN_Y = 85;
    private static final int ALL_BTN_W = 40;
    private static final int ALL_BTN_H = 16;

    private static final int SEARCH_X = 141;
    private static final int SEARCH_Y = 4;
    private static final int SEARCH_W = 107;
    private static final int SEARCH_H = 14;
    private static final int GRID_X = 87;
    private static final int GRID_Y = 19;
    private static final int COLS = 8;
    private static final int ROWS = 5;
    private static final int SLOT_SIZE = 18;
    private static final int SCROLL_X = 236;
    private static final int SCROLL_Y = 20;
    private static final int SCROLL_W = 12;
    private static final int SCROLL_H = 88;
    private static final int MIN_THUMB_HEIGHT = 12;

    private static final float COUNT_SCALE = 0.5f;
    private static final int COUNT_MARGIN_RIGHT = 2;
    private static final int COUNT_MARGIN_BOTTOM = 1;

    private static final int ELEMENIX_ROW1_Y = 118;
    private static final int ELEMENIX_ROW2_Y = 128;
    private static final int ELEMENIX_X0 = 22;
    private static final int ELEMENIX_SPAN = 80;
    private static final int PREVIEW_OFFSET = 4;

    private static final int CHECK_X = 12;
    private static final int CHECK_Y0 = 154;
    private static final int CHECK_GAP = 22;

    private static final Elemenix[][] ESSENCE_ROWS = {
            {Elemenix.ORGANIX, Elemenix.FLUMIX, Elemenix.ENERGIX},
            {Elemenix.TERRIX, Elemenix.METALLIX, Elemenix.ARCANIX}
    };

    private EditBox searchBox;
    private SmallCheckbox deconstructCheckbox;
    private SmallCheckbox memoryCheckbox;
    private SmallCheckbox transferCheckbox;
    private Button prevButton;
    private Button nextButton;
    private Button allButton;

    private int selectedDisk = -1;
    private final List<ItemStack> diskStacks = new ArrayList<>();
    private final List<ResourceLocation> visibleItems = new ArrayList<>();
    private int scrollOffset = 0;
    private boolean scrollbarDragging = false;

    public AnalysisTerminalScreen(AnalysisTerminalMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
        this.titleLabelX = TITLE_X;
        this.titleLabelY = TITLE_Y;
        this.inventoryLabelY = 10000;
    }

    @Override
    protected void init() {
        super.init();

        this.searchBox = new EditBox(this.font, leftPos + SEARCH_X, topPos + SEARCH_Y,
                SEARCH_W, SEARCH_H, Component.translatable("gui.elemenix.analysis_terminal.search"));
        this.searchBox.setMaxLength(100);
        this.searchBox.setResponder(s -> {
            updateVisibleItems();
            clampScroll();
        });
        this.addRenderableWidget(this.searchBox);

        this.deconstructCheckbox = new SmallCheckbox(
                leftPos + CHECK_X, topPos + CHECK_Y0,
                Component.translatable("gui.elemenix.analysis_terminal.enable_deconstruct"),
                this.font, true,
                (checkbox, selected) -> {
                    updateCheckboxLockStates();
                    sendFlags();
                });
        this.memoryCheckbox = new SmallCheckbox(
                leftPos + CHECK_X, topPos + CHECK_Y0 + CHECK_GAP,
                Component.translatable("gui.elemenix.analysis_terminal.allow_memory"),
                this.font, true,
                (checkbox, selected) -> sendFlags());
        this.transferCheckbox = new SmallCheckbox(
                leftPos + CHECK_X, topPos + CHECK_Y0 + CHECK_GAP * 2,
                Component.translatable("gui.elemenix.analysis_terminal.allow_transfer"),
                this.font, false,
                (checkbox, selected) -> sendFlags());
        this.addRenderableWidget(this.deconstructCheckbox);
        this.addRenderableWidget(this.memoryCheckbox);
        this.addRenderableWidget(this.transferCheckbox);
        updateCheckboxLockStates();
        sendFlags();

        this.prevButton = Button.builder(Component.literal("←"), b -> pressPrev())
                .bounds(leftPos + PREV_X, topPos + MEM_BTN_Y, MEM_BTN_SIZE, MEM_BTN_SIZE).build();
        this.nextButton = Button.builder(Component.literal("→"), b -> pressNext())
                .bounds(leftPos + NEXT_X, topPos + MEM_BTN_Y, MEM_BTN_SIZE, MEM_BTN_SIZE).build();
        this.allButton = Button.builder(Component.translatable("gui.elemenix.analysis_terminal.all"),
                        b -> selectDisk(-1))
                .bounds(leftPos + ALL_BTN_X, topPos + ALL_BTN_Y, ALL_BTN_W, ALL_BTN_H).build();
        this.addRenderableWidget(this.prevButton);
        this.addRenderableWidget(this.nextButton);
        this.addRenderableWidget(this.allButton);

        updateLists();
    }

    private void updateCheckboxLockStates() {
        boolean deconstructEnabled = deconstructCheckbox.selected();
        memoryCheckbox.active = deconstructEnabled;
        transferCheckbox.active = deconstructEnabled;
    }

    private void updateLists() {
        diskStacks.clear();
        for (int i = 0; i < 18; i++) {
            ItemStack stack = menu.getBoxItem(i);
            if (stack.getItem() instanceof MemorizerItem) {
                diskStacks.add(stack);
            }
        }
        if (selectedDisk >= diskStacks.size()) {
            selectedDisk = -1;
        }
        updateVisibleItems();
        updateButtonStates();
    }

    private void updateVisibleItems() {
        List<ResourceLocation> fullList = new ArrayList<>();
        if (selectedDisk >= 0 && selectedDisk < diskStacks.size()) {
            fullList.addAll(MemorizerItem.getOrCreateMemories(diskStacks.get(selectedDisk)).resolvedItems());
        } else {
            LinkedHashSet<ResourceLocation> merged = new LinkedHashSet<>();
            for (ItemStack disk : diskStacks) {
                merged.addAll(MemorizerItem.getOrCreateMemories(disk).resolvedItems());
            }
            fullList.addAll(merged);
        }

        String query = searchBox.getValue().trim().toLowerCase(Locale.ROOT);
        visibleItems.clear();
        if (query.isEmpty()) {
            visibleItems.addAll(fullList);
        } else {
            for (ResourceLocation itemId : fullList) {
                ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(itemId));
                if (!stack.isEmpty()
                        && stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains(query)) {
                    visibleItems.add(itemId);
                }
            }
        }
        clampScroll();
    }

    private void updateButtonStates() {
        if (prevButton == null) {
            return;
        }
        boolean hasDisks = !diskStacks.isEmpty();
        prevButton.active = hasDisks;
        nextButton.active = hasDisks;
        allButton.active = selectedDisk != -1;
    }

    private void selectDisk(int index) {
        this.selectedDisk = index;
        PacketDistributor.sendToServer(new TerminalMemorizerSelectPayload(index));
        updateLists();
    }

    private void pressPrev() {
        int count = diskStacks.size();
        if (count == 0) {
            return;
        }
        selectDisk(selectedDisk < 0 ? count - 1 : (selectedDisk - 1 + count) % count);
    }

    private void pressNext() {
        int count = diskStacks.size();
        if (count == 0) {
            return;
        }
        selectDisk(selectedDisk < 0 ? 0 : (selectedDisk + 1) % count);
    }

    private void sendFlags() {
        PacketDistributor.sendToServer(new TerminalFlagsPayload(
                deconstructCheckbox.selected(),
                memoryCheckbox.selected(),
                transferCheckbox.selected()));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
        renderGrid(graphics, mouseX, mouseY);
        renderScrollbar(graphics);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        updateLists();
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderMemoryInfo(graphics);
        renderElemenixValues(graphics);
        renderPreviews(graphics, mouseX, mouseY);
        renderSearchHint(graphics);
        this.renderTooltip(graphics, mouseX, mouseY);
        renderGridTooltip(graphics, mouseX, mouseY);
    }

    private void renderMemoryInfo(GuiGraphics graphics) {
        String name;
        int nameColor = 0xFFFFFF;
        if (selectedDisk >= 0 && selectedDisk < diskStacks.size()) {
            ItemStack disk = diskStacks.get(selectedDisk);
            name = disk.getHoverName().getString();
            if (MemorizerItem.isReadonly(disk)) {
                nameColor = 0xFFAA00;
            }
        } else {
            name = Component.translatable("gui.elemenix.analysis_terminal.all_memorizers").getString();
        }

        int maxWidth = MEM_NAME_MAX_WIDTH;
        int nameWidth = this.font.width(name);
        int centerX = leftPos + MEM_AREA_CX;
        int baseY = topPos + MEM_NAME_Y;

        if (nameWidth <= maxWidth) {
            graphics.drawString(this.font, name, centerX - nameWidth / 2, baseY, nameColor, false);
        } else {
            float scale = (float) maxWidth / nameWidth;
            graphics.pose().pushPose();
            graphics.pose().translate(centerX, baseY, 0.0F);
            graphics.pose().scale(scale, scale, 1.0F);
            graphics.pose().translate(-nameWidth / 2.0F, 0.0F, 0.0F);
            graphics.drawString(this.font, name, 0, 0, nameColor, false);
            graphics.pose().popPose();
        }

        if (selectedDisk >= 0 && selectedDisk < diskStacks.size()) {
            graphics.renderItem(diskStacks.get(selectedDisk), leftPos + MEM_ICON_X, topPos + MEM_ICON_Y);
        } else {
            graphics.blit(MEMORIZER_ICON_TEXTURE,
                    leftPos + MEM_ICON_X, topPos + MEM_ICON_Y,
                    0, 0.0f, 0.0f, 16, 16, 16, 16);
        }
    }

    private void renderElemenixValues(GuiGraphics graphics) {
        LongContainerData containerData = menu.getLongContainerData();
        for (int row = 0; row < 2; row++) {
            int y = topPos + (row == 0 ? ELEMENIX_ROW1_Y : ELEMENIX_ROW2_Y);
            for (int col = 0; col < 3; col++) {
                Elemenix type = ESSENCE_ROWS[row][col];
                String text = ModUtils.formatNumber(containerData.getLong(type.getIndex()));
                graphics.drawString(this.font, text,
                        leftPos + ELEMENIX_X0 + col * ELEMENIX_SPAN, y, type.getColor(), false);
            }
        }
    }

    private void renderPreviews(GuiGraphics graphics, int mouseX, int mouseY) {
        int hovered = getHoveredCell(mouseX, mouseY);
        if (hovered < 0 || hovered >= visibleItems.size()) {
            return;
        }
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(visibleItems.get(hovered)));
        if (stack.isEmpty()) {
            return;
        }
        Constituents required = ElemenixInfo.getPremiumAppliedConstituents(stack);
        if (required.isUnanalysable()) {
            return;
        }
        LongContainerData containerData = menu.getLongContainerData();
        for (int row = 0; row < 2; row++) {
            int y = topPos + (row == 0 ? ELEMENIX_ROW1_Y : ELEMENIX_ROW2_Y);
            for (int col = 0; col < 3; col++) {
                Elemenix type = ESSENCE_ROWS[row][col];
                int amount = required.get(type);
                if (amount <= 0) {
                    continue;
                }
                String valueText = ModUtils.formatNumber(containerData.getLong(type.getIndex()));
                int x = leftPos + ELEMENIX_X0 + col * ELEMENIX_SPAN
                        + this.font.width(valueText) + PREVIEW_OFFSET;
                graphics.drawString(this.font, "-" + ModUtils.formatNumber(amount), x, y, 0xFF3333, false);
            }
        }
    }

    private void renderGrid(GuiGraphics graphics, int mouseX, int mouseY) {
        int hoveredIndex = getHoveredCell(mouseX, mouseY);
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int x = leftPos + GRID_X + col * SLOT_SIZE;
                int y = topPos + GRID_Y + row * SLOT_SIZE;

                int index = (scrollOffset + row) * COLS + col;
                if (index >= visibleItems.size()) {
                    continue;
                }
                ResourceLocation itemId = visibleItems.get(index);
                ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(itemId));
                if (stack.isEmpty()) {
                    continue;
                }

                if (index == hoveredIndex) {
                    graphics.fill(x + 1, y + 1, x + 1 + 16, y + 1 + 16, 0x50FFFFFF);
                }

                graphics.renderItem(stack, x + 1, y + 1);

                String countText = getMaxReconstructableText(stack);
                int color = countText.equals("0") ? 0xFFAAAAAA : 0xFFFFFF;
                int textWidth = (int) (this.font.width(countText) * COUNT_SCALE);
                int textHeight = (int) (this.font.lineHeight * COUNT_SCALE);
                int textX = x + SLOT_SIZE - COUNT_MARGIN_RIGHT - textWidth;
                int textY = y + SLOT_SIZE - COUNT_MARGIN_BOTTOM - textHeight;

                graphics.pose().pushPose();
                graphics.pose().translate(0.0F, 0.0F, 200.0F);
                graphics.pose().translate(textX, textY, 0.0F);
                graphics.pose().scale(COUNT_SCALE, COUNT_SCALE, 1.0F);
                graphics.drawString(this.font, countText, 0, 0, color, true);
                graphics.pose().popPose();
            }
        }
    }

    private String getMaxReconstructableText(ItemStack stack) {
        if (ElemenixInfo.isUnreconstructable(stack)) {
            return "0";
        }
        Constituents required = ElemenixInfo.getPremiumAppliedConstituents(stack);
        if (required.isUnanalysable()) {
            return "0";
        }
        LongContainerData containerData = menu.getLongContainerData();
        long max = Long.MAX_VALUE;
        for (Elemenix type : Elemenix.values()) {
            int req = required.get(type);
            if (req > 0) {
                long available = containerData.getLong(type.getIndex()) & 0xFFFFFFFFL;
                max = Math.min(max, available / req);
            }
        }
        if (max > 999) {
            return "999+";
        }
        return String.valueOf(max);
    }

    private void renderScrollbar(GuiGraphics graphics) {
        int trackX = leftPos + SCROLL_X;
        int trackY = topPos + SCROLL_Y;
        graphics.fill(trackX, trackY, trackX + SCROLL_W, trackY + SCROLL_H, 0x99000000);

        int maxOffset = getMaxOffset();
        int thumbHeight = Math.max(MIN_THUMB_HEIGHT, SCROLL_H * ROWS / getTotalRows());
        int thumbY = maxOffset <= 0
                ? trackY
                : trackY + (SCROLL_H - thumbHeight) * scrollOffset / maxOffset;
        graphics.fill(trackX, thumbY, trackX + SCROLL_W, thumbY + thumbHeight, 0xFF8A8A8A);
    }

    private void renderSearchHint(GuiGraphics graphics) {
        if (searchBox.getValue().isEmpty() && !searchBox.isFocused()) {
            graphics.drawString(this.font,
                    Component.translatable("gui.elemenix.analysis_terminal.search"),
                    leftPos + SEARCH_X + 4, topPos + SEARCH_Y + 3, 0xFF707070, false);
        }
    }

    private void renderGridTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        int hovered = getHoveredCell(mouseX, mouseY);
        if (hovered < 0 || hovered >= visibleItems.size()) {
            return;
        }
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(visibleItems.get(hovered)));
        if (stack.isEmpty()) {
            return;
        }
        graphics.renderTooltip(
                this.font,
                this.getTooltipFromContainerItem(stack),
                stack.getTooltipImage(),
                stack,
                mouseX,
                mouseY);
    }

    private int getTotalRows() {
        return Math.max(ROWS, (visibleItems.size() + COLS - 1) / COLS);
    }

    private int getMaxOffset() {
        return Math.max(0, getTotalRows() - ROWS);
    }

    private void clampScroll() {
        int maxOffset = getMaxOffset();
        if (scrollOffset > maxOffset) {
            scrollOffset = maxOffset;
        }
        if (scrollOffset < 0) {
            scrollOffset = 0;
        }
    }

    private boolean isOverGrid(int mouseX, int mouseY) {
        return mouseX >= leftPos + GRID_X && mouseX < leftPos + GRID_X + COLS * SLOT_SIZE
                && mouseY >= topPos + GRID_Y && mouseY < topPos + GRID_Y + ROWS * SLOT_SIZE;
    }

    private boolean isOverScrollbar(int mouseX, int mouseY) {
        return mouseX >= leftPos + SCROLL_X && mouseX < leftPos + SCROLL_X + SCROLL_W
                && mouseY >= topPos + SCROLL_Y && mouseY < topPos + SCROLL_Y + SCROLL_H;
    }

    private int getHoveredCell(int mouseX, int mouseY) {
        if (!isOverGrid(mouseX, mouseY)) {
            return -1;
        }
        int col = (mouseX - (leftPos + GRID_X)) / SLOT_SIZE;
        int row = (mouseY - (topPos + GRID_Y)) / SLOT_SIZE;
        int index = (scrollOffset + row) * COLS + col;
        return (index >= 0 && index < visibleItems.size()) ? index : -1;
    }

    private void updateScrollFromMouse(int mouseY) {
        int maxOffset = getMaxOffset();
        if (maxOffset <= 0) {
            scrollOffset = 0;
            return;
        }
        int trackY = topPos + SCROLL_Y;
        int thumbHeight = Math.max(MIN_THUMB_HEIGHT, SCROLL_H * ROWS / getTotalRows());
        double usable = SCROLL_H - thumbHeight;
        double ratio = (mouseY - trackY - thumbHeight / 2.0) / usable;
        scrollOffset = (int) Math.round(Math.max(0.0, Math.min(1.0, ratio)) * maxOffset);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isOverGrid((int) mouseX, (int) mouseY) || isOverScrollbar((int) mouseX, (int) mouseY)) {
            int maxOffset = getMaxOffset();
            if (maxOffset > 0) {
                scrollOffset = Math.max(0, Math.min(maxOffset, scrollOffset - (int) scrollY));
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 || button == 1) {
            int cell = getHoveredCell((int) mouseX, (int) mouseY);
            if (cell >= 0) {
                if (button == 0) {
                    PacketDistributor.sendToServer(
                            new TerminalReconstructPayload(visibleItems.get(cell), Screen.hasShiftDown()));
                    return true;
                }
                if (!menu.getCarried().isEmpty() && deconstructCheckbox.selected()) {
                    PacketDistributor.sendToServer(new TerminalCarriedDeconstructPayload());
                    return true;
                }
            }
        }
        if (button == 0 && isOverScrollbar((int) mouseX, (int) mouseY)) {
            scrollbarDragging = true;
            updateScrollFromMouse((int) mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (scrollbarDragging) {
            updateScrollFromMouse((int) mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && scrollbarDragging) {
            scrollbarDragging = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
}
