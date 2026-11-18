package net.lghast.elemenix.client.screen.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.lghast.elemenix.client.misc.ScaledText;
import net.lghast.elemenix.common.system.datacomponent.PotionMemoryData;
import net.lghast.elemenix.common.system.menu.AnalyzerMenu;
import net.lghast.elemenix.network.analyzer.PotionReconstructionPayload;
import net.lghast.elemenix.network.analyzer.ReconstructionPayload;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.utils.elemenix.ElemenixInfo;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private static final int TIP_TEXT_PADDING = 4;
    private static final float MIN_NAME_SCALE = 0.6f;
    private static final float NON_CHINESE_NAME_SCALE = 0.8f;

    protected final List<ItemStack> displayStacks = new ArrayList<>();
    protected final List<ResourceLocation> itemIds = new ArrayList<>();
    protected final List<ResourceLocation> potionIds = new ArrayList<>();
    protected final float itemScale;
    protected int currentPage = 0;
    protected Runnable onPageChange;
    protected boolean needsUpdate = true;
    private List<String> lastSignatures = new ArrayList<>();
    private final Map<Integer, String> truncatedNames = new HashMap<>();

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

    protected String getTipText() {
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
            String text = getTipText();
            int maxWidth = width - TIP_TEXT_PADDING;
            List<ScaledText.Line> lines = ScaledText.wrapToFit(minecraft.font, text, maxWidth, 0.8f);
            int lineHeight = minecraft.font.lineHeight;
            int centerX = getX() + width / 2;
            int y = getY() + (height - lines.size() * lineHeight) / 2;
            for (int i = 0; i < lines.size(); i++) {
                ScaledText.Line line = lines.get(i);
                ScaledText.renderCentered(graphics, minecraft.font, line.text(), line.scale(), centerX, y + i * lineHeight, 0xAAAAAA, false);
            }
            return;
        }

        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, displayStacks.size());
        int hoveredIndex = getHoveredItemIndex(mouseX, mouseY);
        float nameScale = computePageNameScale(minecraft, startIndex, endIndex);
        truncatedNames.clear();

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
            renderItemAndName(graphics, minecraft, itemY, i, stackToRender, nameScale);
        }

        String truncatedName = truncatedNames.get(hoveredIndex);
        if (truncatedName != null) {
            graphics.renderTooltip(minecraft.font, Component.literal(truncatedName), mouseX, mouseY);
        }
    }

    protected int getItemNameColor(int index, ItemStack stack) {
        if (!ElemenixInfo.isCalculating() && ElemenixInfo.isUnreconstructable(stack)) {
            return 0xFF3333;
        }
        return 0xFFFFFF;
    }

    private void renderItemAndName(GuiGraphics graphics, Minecraft minecraft, int itemY, int index, ItemStack stackToRender, float nameScale) {
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
        float textY = itemY + (ITEM_HEIGHT - minecraft.font.lineHeight) / 2.0f + 0.5f;
        int color;
        ScaledText.Fit fit;

        if (stackToRender.isEmpty()) {
            String invalid = Component.translatable("gui.elemenix.invalid_item").getString();
            color = 0xFF3333;
            fit = ScaledText.Fit.fixed(invalid, nameScale);
        } else {
            color = getItemNameColor(index, stackToRender);
            String fullName = stackToRender.getHoverName().getString();
            fit = ScaledText.Fit.atScale(minecraft.font, fullName, getMaxNameWidth(), nameScale, false);
            if (fit.truncated()) {
                truncatedNames.put(index, fit.fullText());
            }
        }

        ScaledText.render(graphics, minecraft.font, fit, textX, textY, color, true);
    }

    private float computePageNameScale(Minecraft minecraft, int startIndex, int endIndex) {
        int maxWidth = getMaxNameWidth();
        int maxNameWidth = 0;
        for (int i = startIndex; i < endIndex; i++) {
            ItemStack stack = displayStacks.get(i);
            if (stack.isEmpty()) continue;
            maxNameWidth = Math.max(maxNameWidth, minecraft.font.width(stack.getHoverName().getString()));
        }

        float scale = getBaseNameScale();
        if (maxNameWidth > 0 && maxNameWidth * scale > maxWidth) {
            scale = (float) maxWidth / maxNameWidth;
            if (scale < MIN_NAME_SCALE) {
                scale = MIN_NAME_SCALE;
            }
        }
        return scale;
    }

    private int getMaxNameWidth() {
        int itemSize = (int) (16 * itemScale);
        return getWidth() - itemSize - ITEM_NAME_OFFSET - ITEM_NAME_RIGHT_PADDING;
    }

    private static float getBaseNameScale() {
        String language = Minecraft.getInstance().getLanguageManager().getSelected();
        if ("zh_cn".equals(language) || "zh_tw".equals(language) || "lzh".equals(language)) {
            return 1.0f;
        }
        return NON_CHINESE_NAME_SCALE;
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

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!isMouseOver(mouseX, mouseY)) return false;
        if (getTotalPages() <= 1) return false;
        if (scrollY > 0) {
            previousPage();
        } else if (scrollY < 0) {
            nextPage();
        }
        return true;
    }

    private int getHoveredItemIndex(double mouseX, double mouseY) {
        if (!isMouseOver(mouseX, mouseY)) {
            return -1;
        }

        int relativeY = (int) (mouseY - getY());
        int displayIndex = relativeY / ITEM_HEIGHT;

        if (displayIndex >= 0 && displayIndex < ITEMS_PER_PAGE) {
            int itemIndex = currentPage * ITEMS_PER_PAGE + displayIndex;
            if (itemIndex >= 0 && itemIndex < displayStacks.size()) {
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
        return getTotalPages() > 1;
    }

    public boolean hasNextPage() {
        return getTotalPages() > 1;
    }

    private int getTotalPages() {
        return Math.max(1, (int) Math.ceil((double) displayStacks.size() / ITEMS_PER_PAGE));
    }

    public void resetToFirstPage() {
        currentPage = 0;
        if (onPageChange != null) {
            onPageChange.run();
        }
    }
}
