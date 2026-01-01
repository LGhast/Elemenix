package net.lghast.elemenix.client.screen;

import net.lghast.elemenix.client.misc.ClientInfuserDataCache;
import net.lghast.elemenix.common.content.item.RemoteStorageItem;
import net.lghast.elemenix.common.content.item.StorageItem;
import net.lghast.elemenix.common.system.datacomponent.ElemenicStorage;
import net.lghast.elemenix.common.system.datacomponent.RemoteStorageBinding;
import net.lghast.elemenix.common.system.menu.AnalyzerMenu;
import net.lghast.elemenix.network.DeconstructionPayload;
import net.lghast.elemenix.network.RequestInfuserUpdatePayload;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.register.system.ModDataComponents;
import net.lghast.elemenix.utils.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class AnalyzerScreen extends AbstractContainerScreen<AnalyzerMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("elemenix", "textures/gui/elemenic_analyzer.png");

    private static final int GUI_WIDTH = 256;
    private static final int GUI_HEIGHT = 200;
    private static final int TITLE_X = 9;
    private static final int TITLE_Y = 6;
    private static final int DECONSTRUCT_BUTTON_X = 15;
    private static final int DECONSTRUCT_BUTTON_Y = 164;
    private static final int DECONSTRUCT_BUTTON_WIDTH = 60;
    private static final int DECONSTRUCT_BUTTON_HEIGHT = 20;
    private static final int LIST_X = 122;
    private static final int LIST_Y = 7;
    private static final int LIST_WIDTH = 127;
    private static final int LIST_HEIGHT = 105;
    private static final int PREV_BUTTON_X = 110;
    private static final int PREV_BUTTON_Y = 6;
    private static final int NEXT_BUTTON_X = 110;
    private static final int NEXT_BUTTON_Y = 101;
    private static final int BUTTON_SIZE = 12;
    private static final int VALUE_START_X = 22;
    private static final int VALUE_START_Y = 23;
    private static final int VALUE_SPACING = 14;
    private static final int PREVIEW_X = 63;

    private MemoryListWidget memoryListWidget;
    private Button prevPageButton;
    private Button nextPageButton;

    public AnalyzerScreen(AnalyzerMenu menu, Inventory inventory, Component title) {
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

        this.addRenderableWidget(Button.builder(Component.translatable("gui.elemenix.deconstruct"),
                button -> PacketDistributor.sendToServer(new DeconstructionPayload()))
                .bounds(leftPos + DECONSTRUCT_BUTTON_X, topPos + DECONSTRUCT_BUTTON_Y,
                DECONSTRUCT_BUTTON_WIDTH, DECONSTRUCT_BUTTON_HEIGHT).build());

        memoryListWidget = new MemoryListWidget(
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
        nextPageButton.active = memoryListWidget.getItemCount() > 7;
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
        LongContainerData containerData = menu.getLongContainerData();
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
        renderHoverPreview(graphics, mouseX, mouseY);
        this.renderTooltip(graphics, mouseX, mouseY);

        if (memoryListWidget != null) {
            memoryListWidget.forceUpdate();
        }
    }

    private void renderHoverPreview(GuiGraphics graphics, int mouseX, int mouseY) {
        ItemStack inputStack = menu.getInputItem();

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        Optional<ResourceLocation> hoveredOptional = memoryListWidget.getHoveredItemId(mouseX, mouseY);
        if (hoveredOptional.isPresent()) {
            ItemStack hoveredStack = new ItemStack(BuiltInRegistries.ITEM.get(hoveredOptional.get()));
            if(!hoveredStack.isEmpty()) {
                Constituents constituents = ElemenixInfo.getPremiumAppliedConstituents(hoveredStack);
                Elemenix[] types = Elemenix.values();
                for (int i = 0; i < types.length; i++) {
                    int amount = constituents.get(types[i]);
                    if (amount > 0) {
                        String previewText = ModUtils.formatNumber(amount, "-%s");
                        graphics.drawString(this.font, previewText, x + PREVIEW_X, y + VALUE_START_Y + i * VALUE_SPACING,
                                0xFF3333, false);
                    }
                }
                return;
            }
        }

        if (!inputStack.isEmpty()) {
            if (inputStack.is(ModItems.REMOTE_ELEMENIC_STORAGE)) {
                if (RemoteStorageItem.isBound(inputStack)) {
                    renderRemoteStoragePreview(graphics, inputStack, x, y);
                } else {
                    renderRegularItemPreview(graphics, inputStack, x, y);
                }
            } else if (inputStack.is(ModItems.ELEMENIC_STORAGE)) {
                ElemenicStorage storageData = StorageItem.getOrCreateData(inputStack);

                if (storageData.isEmpty()) {
                    renderRegularItemPreview(graphics, inputStack, x, y);
                } else {
                    renderStorageTransferPreview(graphics, storageData, x, y);
                }
            } else {
                renderRegularItemPreview(graphics, inputStack, x, y);
            }
        }
    }

    private void renderRegularItemPreview(GuiGraphics graphics, ItemStack inputStack, int x, int y) {
        Constituents constituents = ElemenixInfo.getDiscountAppliedConstituents(inputStack);
        if (constituents.isUnanalysable()) return;

        Elemenix[] types = Elemenix.values();
        for (int i = 0; i < types.length; i++) {
            int amount = constituents.get(types[i]) * inputStack.getCount();
            if (amount > 0) {
                String previewText = ModUtils.formatNumber(amount, "+%s");
                graphics.drawString(this.font, previewText, x + PREVIEW_X, y + VALUE_START_Y + i * VALUE_SPACING,
                        0xCC9900, false);
            }
        }
    }

    private void renderStorageTransferPreview(GuiGraphics graphics, ElemenicStorage storageData, int x, int y) {
        Elemenix[] types = Elemenix.values();
        LongContainerData containerData = menu.getLongContainerData();

        long[] elemenix = storageData.elemenix();

        for (int i = 0; i < types.length; i++) {
            long storageAmount = elemenix[i];
            if (storageAmount <= 0) continue;
            long analyzerAmount = containerData.getLong(i);
            long maxTransfer = Long.MAX_VALUE - analyzerAmount;

            long actualTransfer;
            if (maxTransfer <= 0) {
                actualTransfer = 0;
            } else {
                actualTransfer = Math.min(storageAmount, maxTransfer);
            }

            if (actualTransfer > 0) {
                String previewText = ModUtils.formatNumber(actualTransfer, "+%s");
                graphics.drawString(this.font, previewText, x + PREVIEW_X, y + VALUE_START_Y + i * VALUE_SPACING,
                        0x4AF3FD, false);
            }
        }
    }

    private void renderRemoteStoragePreview(GuiGraphics graphics, ItemStack remoteStack, int x, int y) {
        if(minecraft == null) return;
        if (!(minecraft.level instanceof ClientLevel clientLevel)) return;

        RemoteStorageBinding binding = remoteStack.get(ModDataComponents.REMOTE_STORAGE_BINDING.get());
        if (binding == null || !binding.isBound()) return;

        Optional<GlobalPos> globalPosOptional = binding.boundPos();
        if (globalPosOptional.isEmpty()) return;

        GlobalPos globalPos = globalPosOptional.get();
        if (clientLevel.dimension() != globalPos.dimension()) return;

        Optional<long[]> cachedData = ClientInfuserDataCache.getCachedData(globalPos);
        if (cachedData.isPresent()) {
            ElemenicStorage storageData = new ElemenicStorage(cachedData.get());
            if (!storageData.isEmpty()) {
                renderStorageTransferPreview(graphics, storageData, x, y);
                return;
            }
        }

        PacketDistributor.sendToServer(new RequestInfuserUpdatePayload(globalPos));
    }
}

