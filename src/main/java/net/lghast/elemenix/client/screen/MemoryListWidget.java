package net.lghast.elemenix.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.lghast.elemenix.common.system.datacomponent.PotionMemoryData;
import net.lghast.elemenix.common.system.menu.AnalyzerMenu;
import net.lghast.elemenix.network.analyzer.PotionReconstructionPayload;
import net.lghast.elemenix.network.analyzer.ReconstructionPayload;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.utils.elemenix.ElemenixInfo;
import net.lghast.elemenix.utils.ModUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static net.minecraft.client.gui.screens.Screen.hasShiftDown;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class MemoryListWidget extends AbstractWidget {
    protected static final int ITEMS_PER_PAGE = 7;
    protected static final int ITEM_HEIGHT = 15;
    protected static final int ITEM_OFFSET = 4;
    protected static final int ITEM_NAME_OFFSET = 8;
    protected static final int ITEM_NAME_RIGHT_PADDING = 6;

    protected final List<ItemStack> displayStacks = new ArrayList<>();
    protected final List<ResourceLocation> itemIds = new ArrayList<>();
    protected final List<ResourceLocation> potionIds = new ArrayList<>();
    protected final float itemScale;
    protected int currentPage = 0;
    protected Runnable onPageChange;
    protected boolean needsUpdate = true;
    private List<String> lastSignatures = new ArrayList<>();

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
            ModItems.ARCANIX_ESSENPLEX,
            ModItems.ELEMENIC_EQUILIPLEX
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

    protected String getEmptyText() {
        return Component.translatable("gui.elemenix.no_resolved_items").getString();
    }

    protected boolean isMenuWrong(){
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return true;
        return !(minecraft.player.containerMenu instanceof AnalyzerMenu);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        if (needsUpdate) {
            updateItemList();
            needsUpdate = false;
        }

        int startIndex = currentPage * ITEMS_PER_PAGE;
        if (startIndex >= displayStacks.size() && currentPage > 0) {
            resetToFirstPage();
            return;
        }

        if (isMenuWrong()) {
            return;
        }

        if (displayStacks.isEmpty()) {
            String text = getEmptyText();
            int textWidth = minecraft.font.width(text);
            graphics.drawString(minecraft.font, text,
                    getX() + (width - textWidth) / 2,
                    getY() + height / 2 - 4,
                    0xAAAAAA, false);
            return;
        }

        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, displayStacks.size());
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

            ItemStack stackToRender = displayStacks.get(i);
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
            graphics.drawString(minecraft.font, Component.translatable("gui.elemenix.invalid_item"), textX, textY, 0xFF3333, false);
            return;
        }

        String displayName = stackToRender.getHoverName().getString();
        int maxWidth = getWidth() - itemSize - ITEM_NAME_OFFSET - ITEM_NAME_RIGHT_PADDING;
        int nameWidth = minecraft.font.width(displayName);

        int color = 0xFFFFFF;
        if (!ElemenixInfo.isCalculating() && ElemenixInfo.isUnreconstructable(stackToRender)) {
            color = 0xFF3333;
        }

        if (nameWidth <= maxWidth) {
            graphics.drawString(minecraft.font, displayName, textX, textY, color, false);
            return;
        }

        float scale = Math.max(0.6f, (float) maxWidth / nameWidth);
        String text = displayName;
        if (nameWidth * scale > maxWidth) {
            text = ModUtils.truncateToWidth(minecraft.font, displayName, (int) (maxWidth / scale));
        }

        poseStack.pushPose();
        poseStack.translate(textX, textY + 5.0F * (1.0F - scale), 0.0F);
        poseStack.scale(scale, scale, 1.0F);
        graphics.drawString(minecraft.font, text, 0, 0, color, false);
        poseStack.popPose();
    }

    protected void updateItemList() {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if(player == null) return;

        if (!(player.containerMenu instanceof AnalyzerMenu menu)) {
            return;
        }

        ItemStack memorizerStack = menu.getMemorizerItem();

        List<ResourceLocation> newItemIds = new ArrayList<>();
        List<ResourceLocation> newPotionIds = new ArrayList<>();
        List<ItemStack> newStacks = new ArrayList<>();

        if (memorizerStack.isEmpty()) {
            for (ResourceLocation id : DEFAULT_ITEMS.stream()
                    .map(supplier -> BuiltInRegistries.ITEM.getKey(supplier.get()))
                    .toList()) {
                Item item = BuiltInRegistries.ITEM.get(id);
                ItemStack stack = new ItemStack(item);
                newItemIds.add(id);
                newPotionIds.add(null);
                newStacks.add(stack);
            }
        } else if (memorizerStack.is(ModItems.WITCHCRAFT_MEMORIZER)) {
            List<PotionMemoryData.Entry> entries = menu.getPotionMemories();
            if (entries != null) {
                for (PotionMemoryData.Entry entry : entries) {
                    newItemIds.add(entry.itemId());
                    newPotionIds.add(entry.potionId());
                    newStacks.add(createPotionStack(entry.itemId(), entry.potionId()));
                }
            }
        } else {
            List<ResourceLocation> memories = menu.getMemories();
            if (memories != null) {
                for (ResourceLocation id : memories) {
                    Item item = BuiltInRegistries.ITEM.get(id);
                    ItemStack stack = new ItemStack(item);
                    newItemIds.add(id);
                    newPotionIds.add(null);
                    newStacks.add(stack);
                }
            }
        }

        List<String> currentSignatures = buildSignatures(newItemIds, newPotionIds);

        boolean isJustAppended = false;
        if (currentSignatures.size() == lastSignatures.size() + 1) {
            isJustAppended = true;
            for (int i = 0; i < lastSignatures.size(); i++) {
                if (!lastSignatures.get(i).equals(currentSignatures.get(i))) {
                    isJustAppended = false;
                    break;
                }
            }
        }

        boolean changed = !currentSignatures.equals(lastSignatures) && !isJustAppended;

        if (changed) {
            currentPage = 0;
        }
        lastSignatures = new ArrayList<>(currentSignatures);

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

    private static List<String> buildSignatures(List<ResourceLocation> items, List<ResourceLocation> potions) {
        List<String> signatures = new ArrayList<>(items.size());
        for (int i = 0; i < items.size(); i++) {
            ResourceLocation potion = potions.get(i);
            signatures.add(potion == null ? "i:" + items.get(i) : "p:" + items.get(i) + "|" + potion);
        }
        return signatures;
    }

    protected static ItemStack createPotionStack(ResourceLocation itemId, ResourceLocation potionId) {
        Item item = BuiltInRegistries.ITEM.get(itemId);

        var potionHolder = BuiltInRegistries.POTION.getHolder(ResourceKey.create(Registries.POTION, potionId));
        return potionHolder.map(potionReference -> net.minecraft.world.item.alchemy.PotionContents.createItemStack(item, potionReference)).orElseGet(() -> new ItemStack(item));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY)) return false;

        int relativeY = (int) (mouseY - getY());
        int displayIndex = relativeY / ITEM_HEIGHT;

        if (displayIndex >= 0 && displayIndex < ITEMS_PER_PAGE) {
            int itemIndex = currentPage * ITEMS_PER_PAGE + displayIndex;

            if (itemIndex >= 0 && itemIndex < itemIds.size()) {
                boolean shiftClick = hasShiftDown();
                ResourceLocation potionId = potionIds.get(itemIndex);

                if (potionId != null) {
                    PacketDistributor.sendToServer(new PotionReconstructionPayload(itemIds.get(itemIndex), potionId, shiftClick));
                } else {
                    PacketDistributor.sendToServer(new ReconstructionPayload(itemIds.get(itemIndex), shiftClick));
                }
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
            if (itemIndex >= 0 && itemIndex < itemIds.size()) {
                return itemIndex;
            }
        }
        return -1;
    }

    public Optional<ItemStack> getHoveredItemStack(double mouseX, double mouseY) {
        int index = getHoveredItemIndex(mouseX, mouseY);
        if (index >= 0 && index < displayStacks.size()) {
            return Optional.of(displayStacks.get(index));
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
        return Math.max(1, (int) Math.ceil((double) itemIds.size() / ITEMS_PER_PAGE));
    }

    public int getItemCount() {
        return itemIds.size();
    }

    public void resetToFirstPage() {
        currentPage = 0;
        if (onPageChange != null) {
            onPageChange.run();
        }
    }
}
