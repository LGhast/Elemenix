package net.lghast.elemenix.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.lghast.elemenix.common.system.menu.AnalyzerMenu;
import net.lghast.elemenix.network.ReconstructionPayload;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.utils.ModUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static net.minecraft.client.gui.screens.Screen.hasShiftDown;

@OnlyIn(Dist.CLIENT)
public class MemoryListWidget extends AbstractWidget {
    private static final int ITEMS_PER_PAGE = 7;
    private static final int ITEM_HEIGHT = 15;
    private static final int ITEM_OFFSET = 4;
    private static final int ITEM_NAME_OFFSET = 8;

    private List<ResourceLocation> items = new ArrayList<>();
    private final List<ItemStack> itemStacks = new ArrayList<>();
    private final float itemScale;
    private int currentPage = 0;
    private Runnable onPageChange;
    private boolean needsUpdate = true;
    private List<ResourceLocation> lastMemories = new ArrayList<>();

    private static final List<Supplier<Item>> DEFAULT_ITEMS = List.of(
            ModItems.ORGANIX_ESSENCE,
            ModItems.TERRIX_ESSENCE,
            ModItems.FLUMIX_ESSENCE,
            ModItems.METALLIX_ESSENCE,
            ModItems.ENERGIX_ESSENCE,
            ModItems.ARCANIX_ESSENCE,
            ModItems.ELEMENIC_EQUILIBRIUM,
            ModItems.ORGANIX_ESSENPLEX,
            ModItems.TERRIX_ESSENPLEX,
            ModItems.FLUMIX_ESSENPLEX,
            ModItems.METALLIX_ESSENPLEX,
            ModItems.ENERGIX_ESSENPLEX,
            ModItems.ARCANIX_ESSENPLEX
    );

    public MemoryListWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
        this.itemScale = 0.7f;
    }

    public void forceUpdate() {
        this.needsUpdate = true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, Component.translatable("gui.elemenix.item_list"));
    }

    public void setOnPageChange(Runnable onPageChange) {
        this.onPageChange = onPageChange;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        if (needsUpdate) {
            updateItemList();
            needsUpdate = false;
        }

        if (!(minecraft.player.containerMenu instanceof AnalyzerMenu)) {
            return;
        }

        if (itemStacks.isEmpty()) {
            String text = Component.translatable("gui.elemenix.no_resolved_items").getString();
            int textWidth = minecraft.font.width(text);
            graphics.drawString(minecraft.font, text,
                    getX() + (width - textWidth) / 2,
                    getY() + height / 2 - 4,
                    0xAAAAAA, false);
            return;
        }

        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, itemStacks.size());
        int hoveredIndex = getHoveredItemIndex(mouseX, mouseY);

        for (int i = startIndex; i < endIndex; i++) {
            int displayIndex = i - startIndex;
            int itemY = getY() + displayIndex * ITEM_HEIGHT;

            if (i == hoveredIndex) {
                graphics.fill(getX(), itemY, getX() + width, itemY + ITEM_HEIGHT, 0x44FFFFFF);
            } else {
                graphics.fill(getX(), itemY, getX() + width, itemY + ITEM_HEIGHT,
                        displayIndex % 2 == 0 ? 0x22000000 : 0x11FFFFFF);
            }

            ItemStack stackToRender = itemStacks.get(i);
            renderItemAndName(graphics, minecraft, itemY, stackToRender);
        }
    }

    private void renderItemAndName(GuiGraphics graphics, Minecraft minecraft, int itemY, ItemStack stackToRender) {
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();

        int itemSize = (int) (16 * itemScale);
        int itemX = getX() + ITEM_OFFSET;
        int itemRenderY = itemY + (ITEM_HEIGHT - itemSize) / 2;

        poseStack.translate(itemX, itemRenderY, 0);
        poseStack.scale(itemScale, itemScale, 1.0f);

        ItemStack renderedItem = stackToRender.isEmpty() ? new ItemStack(Items.BARRIER) : stackToRender;
        graphics.renderItem(renderedItem, 0, 0);
        graphics.renderItemDecorations(minecraft.font, renderedItem, 0, 0);

        poseStack.popPose();

        int textX = getX() + itemSize + ITEM_NAME_OFFSET;
        int textY = itemY + (ITEM_HEIGHT - 8) / 2;
        if (stackToRender.isEmpty()) {
            graphics.drawString(minecraft.font, Component.translatable("gui.elemenix.invalid_item"),
                    textX, textY, 0xFF3333, false);
            return;
        }

        String displayName = stackToRender.getHoverName().getString();
        int textWidth = getWidth() - itemSize - 8;
        String truncatedName = ModUtils.truncateToWidth(minecraft.font, displayName, textWidth);
        graphics.drawString(minecraft.font, truncatedName, textX, textY, 0xFFFFFF, false);
    }

    private void updateItemList() {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if(player == null) return;

        if (!(player.containerMenu instanceof AnalyzerMenu menu)) {
            return;
        }

        ItemStack memorizerStack = menu.getMemorizerItem();
        List<ResourceLocation> currentMemories;

        if (memorizerStack.isEmpty()) {
            currentMemories = DEFAULT_ITEMS.stream()
                    .map(supplier -> BuiltInRegistries.ITEM.getKey(supplier.get()))
                    .collect(Collectors.toList());
        } else {
            if(menu.getMemories() != null) {
                currentMemories = menu.getMemories();
            }else{
                currentMemories = new ArrayList<>();
            }
        }

        boolean isJustAppended = false;
        if (lastMemories != null && currentMemories.size() == lastMemories.size() + 1) {
            isJustAppended = true;
            for (int i = 0; i < lastMemories.size(); i++) {
                if (!lastMemories.get(i).equals(currentMemories.get(i))) {
                    isJustAppended = false;
                    break;
                }
            }
        }

        boolean memorizerChanged = !currentMemories.equals(lastMemories) && !isJustAppended;

        if (memorizerChanged) {
            currentPage = 0;
            lastMemories = new ArrayList<>(currentMemories);
        }

        if (isJustAppended) {
            lastMemories = new ArrayList<>(currentMemories);
        }

        if (currentMemories.equals(this.items) && !memorizerChanged) {
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
                ResourceLocation itemId = items.get(itemIndex);
                boolean shiftClick = hasShiftDown();

                PacketDistributor.sendToServer(new ReconstructionPayload(itemId, shiftClick));
                this.needsUpdate = true;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private int getHoveredItemIndex(double mouseX, double mouseY) {
        if (!isMouseOver(mouseX, mouseY)) {
            return -1;
        }

        int relativeY = (int) (mouseY - getY());
        int displayIndex = relativeY / ITEM_HEIGHT;

        if (displayIndex >= 0 && displayIndex < ITEMS_PER_PAGE) {
            int itemIndex = currentPage * ITEMS_PER_PAGE + displayIndex;
            if (itemIndex >= 0 && itemIndex < items.size()) {
                return itemIndex;
            }
        }
        return -1;
    }

    public Optional<ResourceLocation> getHoveredItemId(double mouseX, double mouseY) {
        int index = getHoveredItemIndex(mouseX, mouseY);
        if (index >= 0 && index < items.size()) {
            return Optional.of(items.get(index));
        }
        return Optional.empty();
    }

    public void nextPage() {
        currentPage++;
        if (currentPage >= getTotalPages()) {
            currentPage = 0;
        }
        if (onPageChange != null) {
            onPageChange.run();
        }
    }

    public void previousPage() {
        currentPage--;
        if (currentPage < 0) {
            currentPage = Math.max(0, getTotalPages() - 1);
        }
        if (onPageChange != null) {
            onPageChange.run();
        }
    }

    public boolean hasPreviousPage() {
        return currentPage > 0;
    }

    private int getTotalPages() {
        return Math.max(1, (int) Math.ceil((double) items.size() / ITEMS_PER_PAGE));
    }

    public int getItemCount() {
        return items.size();
    }
}
