package net.lghast.elemenix.client.screen;

import net.lghast.elemenix.client.misc.Palette;
import net.lghast.elemenix.client.render.ItemIconRenderer;
import net.lghast.elemenix.client.screen.widget.MemoryBurnerListWidget;
import net.lghast.elemenix.common.content.item.MemorizerItem;
import net.lghast.elemenix.common.content.item.WitchcraftMemorizerItem;
import net.lghast.elemenix.common.system.menu.MemoryBurnerMenu;
import net.lghast.elemenix.network.burner.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class MemoryBurnerScreen extends AbstractContainerScreen<MemoryBurnerMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("elemenix", "textures/gui/memory_burner.png");

    private static final int GUI_WIDTH = 256;
    private static final int GUI_HEIGHT = 200;
    private static final int TITLE_X = 10;
    private static final int TITLE_Y = 8;
    private static final int LIST_X = 120;
    private static final int LIST_Y = 7;
    private static final int LIST_WIDTH = 127;
    private static final int LIST_HEIGHT = 105;
    private static final int PREV_BUTTON_X = 108;
    private static final int PREV_BUTTON_Y = 6;
    private static final int NEXT_BUTTON_X = 108;
    private static final int NEXT_BUTTON_Y = 101;
    private static final int BUTTON_SIZE = 12;
    private static final int MODE_AREA_X = 10;
    private static final int MODE_AREA_Y = 117;
    private static final int MODE_AREA_WIDTH = 71;
    private static final int MODE_AREA_HEIGHT = 75;
    private static final int ACTION_BUTTON_Y = 80;
    private static final int ACTION_BUTTON_WIDTH = 72;
    private static final int ACTION_BUTTON_HEIGHT = 21;
    private static final int SELECTED_ITEM_ICON_Y = 83;
    private static final int SELECTED_ITEM_TEXT_Y = 87;

    private MemoryBurnerListWidget memoryListWidget;
    private Button prevPageButton;
    private Button nextPageButton;
    private Button actionButton;
    private Component currentModeText;
    private ItemStack selectedItem = ItemStack.EMPTY;
    private Component selectedItemText = Component.empty();
    private boolean hasSelectedItem = false;
    private int selectedItemIndex = -1;
    private MemoryBurnerMode currentSelectedMode = null;

    public MemoryBurnerScreen(MemoryBurnerMenu menu, Inventory inventory, Component title) {
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

        memoryListWidget = new MemoryBurnerListWidget(
                leftPos + LIST_X,
                topPos + LIST_Y,
                LIST_WIDTH,
                LIST_HEIGHT,
                this.menu
        );
        memoryListWidget.setOnPageChange(this::updateButtonStates);
        memoryListWidget.setOnItemClick(this::handleItemClick);
        this.addRenderableWidget(memoryListWidget);

        prevPageButton = Button.builder(Component.literal("←"), button -> {
            memoryListWidget.previousPage();
            updateButtonStates();
        }).bounds(leftPos + PREV_BUTTON_X, topPos + PREV_BUTTON_Y, BUTTON_SIZE,
                BUTTON_SIZE).build();
        this.addRenderableWidget(prevPageButton);

        nextPageButton = Button.builder(Component.literal("→"), button -> {
            memoryListWidget.nextPage();
            updateButtonStates();
        }).bounds(leftPos + NEXT_BUTTON_X, topPos + NEXT_BUTTON_Y, BUTTON_SIZE,
                BUTTON_SIZE).build();
        this.addRenderableWidget(nextPageButton);

        int modeAreaCenterX = leftPos + MODE_AREA_X + MODE_AREA_WIDTH / 2;

        Button modeToggleButton = Button.builder(
                Component.translatable("mode.elemenix.burner.toggle"),
                button -> {
                    menu.cycleMode();
                    updateModeText();
                    stopSelection();
                }
        ).bounds(
                leftPos + MODE_AREA_X + (MODE_AREA_WIDTH - 60) / 2,
                topPos + MODE_AREA_Y + 46,
                60, 20
        ).build();
        this.addRenderableWidget(modeToggleButton);

        actionButton = Button.builder(Component.empty(), button -> {
            MemoryBurnerMode mode = menu.getMode();
            if (mode == MemoryBurnerMode.COPY || mode == MemoryBurnerMode.CONCATENATE) {
                PacketDistributor.sendToServer(new BurnerActionPayload(mode));
                if (memoryListWidget != null && mode == MemoryBurnerMode.COPY) {
                    memoryListWidget.resetToFirstPage();
                }
            }
        }).bounds(
                modeAreaCenterX - ACTION_BUTTON_WIDTH / 2,
                topPos + ACTION_BUTTON_Y,
                ACTION_BUTTON_WIDTH,
                ACTION_BUTTON_HEIGHT
        ).build();
        actionButton.visible = false;
        this.addRenderableWidget(actionButton);

        updateButtonStates();
        updateModeText();
    }

    private void updateButtonStates() {
        prevPageButton.active = memoryListWidget.hasPreviousPage();
        nextPageButton.active = memoryListWidget.hasNextPage();

        MemoryBurnerMode mode = menu.getMode();
        actionButton.visible = mode == MemoryBurnerMode.COPY || mode == MemoryBurnerMode.CONCATENATE;

        if (actionButton.visible) {
            String key = mode == MemoryBurnerMode.COPY ? "mode.elemenix.burner.copy" : "mode.elemenix.burner.concatenate";
            actionButton.setMessage(Component.translatable(key));

            if (mode == MemoryBurnerMode.CONCATENATE) {
                ItemStack primary = menu.getPrimaryMemorizer();
                actionButton.active = menu.isPrimaryPotionMemorizer()
                        ? WitchcraftMemorizerItem.isNotFull(primary)
                        : MemorizerItem.isNotFull(primary);
            }else{
                actionButton.active = true;
            }
        }
    }

    private void updateModeText() {
        MemoryBurnerMode mode = menu.getMode();
        String modeKey = "";
        switch (mode) {
            case MOVE_UP -> modeKey = "mode.elemenix.burner.move_up";
            case MOVE_DOWN -> modeKey = "mode.elemenix.burner.move_down";
            case INSERT_BEFORE -> modeKey = "mode.elemenix.burner.insert_before";
            case SWAP -> modeKey = "mode.elemenix.burner.swap";
            case DELETE -> modeKey = "mode.elemenix.burner.delete";
            case CONCATENATE -> modeKey = "mode.elemenix.burner.concatenate";
            case COPY -> modeKey = "mode.elemenix.burner.copy";
        }
        currentModeText = Component.translatable(modeKey);
    }

    private void handleItemClick(int itemIndex, boolean shiftClick) {
        MemoryBurnerMode mode = menu.getMode();
        switch (mode) {
            case MOVE_UP -> PacketDistributor.sendToServer(new BurnerMovePayload(itemIndex, -1));
            case MOVE_DOWN -> PacketDistributor.sendToServer(new BurnerMovePayload(itemIndex, 1));
            case INSERT_BEFORE -> handleInsertBeforeSelection(itemIndex);
            case SWAP -> handleSwapSelection(itemIndex);
            case DELETE -> handleDeleteSelection(itemIndex);
            default -> {}
        }
    }

    private void handleSwapSelection(int itemIndex) {
        if (currentSelectedMode != MemoryBurnerMode.SWAP || selectedItemIndex == -1) {
            stopSelection();
            currentSelectedMode = MemoryBurnerMode.SWAP;
        }

        if (selectedItemIndex == -1) {
            selectedItemIndex = itemIndex;
            selectedItem = menu.getMemoryItemStack(itemIndex);
            if (!selectedItem.isEmpty()) {
                selectedItemText = Component.translatable("mode.elemenix.burner.selected");
                hasSelectedItem = true;
                currentSelectedMode = MemoryBurnerMode.SWAP;
            }
        } else {
            if (selectedItemIndex == itemIndex) {
                stopSelection();
            } else {
                PacketDistributor.sendToServer(new BurnerSwapPayload(selectedItemIndex, itemIndex));
                stopSelection();
            }
        }
    }

    private void handleDeleteSelection(int itemIndex) {
        if (currentSelectedMode != MemoryBurnerMode.DELETE || selectedItemIndex == -1) {
            stopSelection();
            currentSelectedMode = MemoryBurnerMode.DELETE;
        }

        if (selectedItemIndex == -1) {
            selectedItemIndex = itemIndex;
            selectedItem = menu.getMemoryItemStack(itemIndex);
            if (!selectedItem.isEmpty()) {
                selectedItemText = Component.translatable("mode.elemenix.burner.selected");
                hasSelectedItem = true;
                currentSelectedMode = MemoryBurnerMode.DELETE;
            }
        } else {
            if (selectedItemIndex == itemIndex) {
                PacketDistributor.sendToServer(new BurnerDeletePayload(itemIndex));
                stopSelection();
            } else {
                selectedItemIndex = itemIndex;
                selectedItem = menu.getMemoryItemStack(itemIndex);
                if (!selectedItem.isEmpty()) {
                    selectedItemText = Component.translatable("mode.elemenix.burner.selected");
                    hasSelectedItem = true;
                    currentSelectedMode = MemoryBurnerMode.DELETE;
                }
            }
        }
    }

    private void handleInsertBeforeSelection(int itemIndex) {
        if (currentSelectedMode != MemoryBurnerMode.INSERT_BEFORE || selectedItemIndex == -1) {
            stopSelection();
            currentSelectedMode = MemoryBurnerMode.INSERT_BEFORE;
        }

        if (selectedItemIndex == -1) {
            selectedItemIndex = itemIndex;
            selectedItem = menu.getMemoryItemStack(itemIndex);
            if (!selectedItem.isEmpty()) {
                selectedItemText = Component.translatable("mode.elemenix.burner.selected");
                hasSelectedItem = true;
                currentSelectedMode = MemoryBurnerMode.INSERT_BEFORE;
            }
        } else {
            if (selectedItemIndex == itemIndex) {
                stopSelection();
            } else {
                PacketDistributor.sendToServer(new BurnerInsertBeforePayload(selectedItemIndex, itemIndex));
                stopSelection();
            }
        }
    }

    public void clearSelectedItem() {
        selectedItem = ItemStack.EMPTY;
        selectedItemText = Component.empty();
        hasSelectedItem = false;
    }

    public void stopSelection() {
        selectedItemIndex = -1;
        clearSelectedItem();
        currentSelectedMode = null;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

        graphics.fill(x + MODE_AREA_X, y + MODE_AREA_Y,
                x + MODE_AREA_X + MODE_AREA_WIDTH,
                y + MODE_AREA_Y + MODE_AREA_HEIGHT, Palette.FRINGE_DARK);

        String currentModeLabel = Component.translatable("mode.elemenix.burner.current").getString();
        graphics.drawString(this.font, currentModeLabel,
                x + MODE_AREA_X + (MODE_AREA_WIDTH - font.width(currentModeLabel)) / 2,
                y + MODE_AREA_Y + 7, Palette.WHITE, false);
        graphics.drawString(this.font, currentModeText,
                x + MODE_AREA_X + (MODE_AREA_WIDTH - font.width(currentModeText)) / 2,
                y + MODE_AREA_Y + 21, Palette.YELLOW, false);

        if (hasSelectedItem) {
            if (!selectedItem.isEmpty()) {
                int itemWidth = 16;
                int textWidth = font.width(selectedItemText);
                int totalWidth = itemWidth + 4 + textWidth;

                int startX = x + MODE_AREA_X + (MODE_AREA_WIDTH - totalWidth) / 2;

                ItemIconRenderer.render(graphics, selectedItem, startX, y + SELECTED_ITEM_ICON_Y);
                graphics.renderItemDecorations(this.font, selectedItem,
                        startX, y + SELECTED_ITEM_ICON_Y);

                graphics.drawString(this.font, selectedItemText,
                        startX + itemWidth + 4, y + SELECTED_ITEM_TEXT_Y, Palette.YELLOW, false);
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        if (memoryListWidget != null) {
            memoryListWidget.forceUpdate();
        }

        updateButtonStates();
    }

    @Override
    public void containerTick() {
        super.containerTick();

        MemoryBurnerMode currentMode = menu.getMode();
        if (currentSelectedMode != null && currentSelectedMode != currentMode) {
            stopSelection();
        }

        if (menu.isPrimaryMemorizerChanged()) {
            stopSelection();
            menu.resetPrimaryChangedFlag();
        }

        if (memoryListWidget != null) {
            memoryListWidget.forceUpdate();
        }
    }
}
