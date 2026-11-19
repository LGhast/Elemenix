package net.lghast.elemenix.client.screen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import net.lghast.elemenix.client.cache.ClientRegistrarCache;
import net.lghast.elemenix.client.misc.ScaledText;
import net.lghast.elemenix.client.screen.widget.SmallCheckbox;
import net.lghast.elemenix.common.content.blockentity.MemoryRegistrarBlockEntity;
import net.lghast.elemenix.config.ClientConfig;
import net.lghast.elemenix.common.system.datacomponent.PotionMemoryData;
import net.lghast.elemenix.common.system.menu.RegistrarMenu;
import net.lghast.elemenix.compat.jech.JechCompat;
import net.lghast.elemenix.network.registrar.ToggleRegistrarDeveloperModePayload;
import net.lghast.elemenix.register.system.ModTags;
import net.lghast.elemenix.utils.elemenix.Constituents;
import net.lghast.elemenix.utils.elemenix.Elemenix;
import net.lghast.elemenix.utils.elemenix.ElemenixInfo;
import net.lghast.elemenix.utils.misc.ModUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class RegistrarScreen extends AbstractContainerScreen<RegistrarMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("elemenix", "textures/gui/memory_registrar.png");

    private static final ResourceLocation QUESTION_MARK_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("elemenix", "textures/gui/question_mark.png");

    private static final int GUI_WIDTH = 256;
    private static final int GUI_HEIGHT = 200;

    private static final int TITLE_LINE_X1 = 13;
    private static final int TITLE_LINE_X2 = 121;
    private static final int TITLE_LINE_Y = 27;
    private static final float TITLE_SCALE = 1.5f;
    private static final int TITLE_MAX_WIDTH = TITLE_LINE_X2 - TITLE_LINE_X1 - 8;
    private static final int TITLE_COLOR = 0x000000;

    private static final int SEARCH_X = 154;
    private static final int SEARCH_Y = 20;
    private static final int SEARCH_W = 90;
    private static final int SEARCH_H = 12;

    private static final int GRID_X = 13;
    private static final int GRID_Y = 38;
    private static final int GRID_PAD = 7;
    private static final int COLS = 11;
    private static final int ROWS = 6;
    private static final int SLOT_W = 20;
    private static final int SLOT_H = 22;

    private static final int PAGE_CENTER_X = 128;
    private static final int PAGE_Y = 187;
    private static final float PAGE_SCALE = 0.8f;

    private static final int BTN_W = 7;
    private static final int BTN_H = 9;
    private static final int PREV_BTN_X = 95;
    private static final int NEXT_BTN_X = 155;
    private static final int BTN_Y = 185;

    private static final int CHECK_Y = 6;
    private static final int CHECK_X1 = 198;
    private static final int CHECK_GAP = 12;

    private static final ResourceLocation SINGLE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("elemenix", "textures/gui/memory_registrar_single.png");
    private static final ResourceLocation SINGLE_SIMPLE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("elemenix", "textures/gui/memory_registrar_single_simple.png");
    private static final ResourceLocation ICONS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("elemenix", "textures/gui/registrar_icons.png");
    private static final ResourceLocation ITEM_BACKGROUND_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("elemenix", "textures/gui/item_background.png");

    private static final ResourceLocation WATER_POTION_ID = ResourceLocation.parse("minecraft:water");

    private static final int DETAIL_ITEM_X = 23;
    private static final int DETAIL_ITEM_Y = 40;
    private static final float DETAIL_ITEM_SCALE = 28.0f / 16.0f;
    private static final int DETAIL_LEFT_OFFSET = 7;

    private static final int BASIC_INFO_X = 60;
    private static final int BASIC_INFO_Y = 39;
    private static final int BASIC_INFO_MAX_WIDTH = 59;
    private static final float BASIC_INFO_MIN_SCALE = 0.75f;

    private static final int CONSTITUENTS_TITLE_CENTER_X = 70;
    private static final int CONSTITUENTS_TITLE_Y = 75;
    private static final int CONSTITUENTS_X = 33;
    private static final int CONSTITUENTS_START_Y = 86;
    private static final int CONSTITUENTS_GAP = 13;
    private static final int CONSTITUENTS_MAX_WIDTH = 84;
    private static final float CONSTITUENTS_MIN_SCALE = 0.5f;
    private static final int CONSTITUENTS_TITLE_MAX_WIDTH = 95;

    private static final int ICONS_X = 19;
    private static final int ICONS_Y = 165;
    private static final int ICONS_GAP = 13;
    private static final int ICON_SIZE = 12;

    private static final int BACK_BTN_X = 234;
    private static final int BACK_BTN_Y = 6;
    private static final int BACK_BTN_SIZE = 16;

    private static final int TAG_AREA_X = 129;
    private static final int TAG_AREA_Y = 31;
    private static final int TAG_AREA_W = 114;
    private static final int TAG_AREA_H = 56;
    private static final int TAG_SCROLLBAR_W = 6;
    private static final int TAG_ROW_H = 14;
    private static final int TAG_ICON_SIZE = 12;
    private static final float TAG_TEXT_SCALE = 0.8f;
    private static final int TAG_TEXT_MAX_WIDTH = 109;

    private static final int DESCRIPTION_X = 129;
    private static final int DESCRIPTION_Y = 88;
    private static final int DESCRIPTION_W = 114;
    private static final int DESCRIPTION_H = 93;
    private static final int DESCRIPTION_TEXT_PADDING = 4;
    private static final int DESCRIPTION_TOP_PADDING = 3;
    private static final int DESCRIPTION_BOTTOM_PADDING = 3;
    private static final float DESCRIPTION_TEXT_SCALE = 0.9f;
    private static final int DESCRIPTION_TEXT_MAX_WIDTH = DESCRIPTION_W - TAG_SCROLLBAR_W - DESCRIPTION_TEXT_PADDING * 2;

    private EditBox searchBox;
    private SmallCheckbox showUncollectedCheckbox;
    private SmallCheckbox putUncollectedLastCheckbox;
    private SmallCheckbox sortByModCheckbox;
    private SmallCheckbox showPotionlikeCheckbox;
    private SmallCheckbox normalUnanalysableCheckbox;
    private SmallCheckbox strictlyUnanalysableCheckbox;
    private SmallCheckbox commandIgnoredCheckbox;
    private SmallCheckbox spawnEggsCheckbox;
    private int developerFilter = -1;
    private boolean developerMode = false;
    private Button prevButton;
    private Button nextButton;
    private Button backButton;
    private ItemStack detailStack = ItemStack.EMPTY;
    private int tagScroll = 0;
    private float descriptionScroll = 0.0f;
    private List<IconEntry> detailIcons = List.of();

    private final List<Entry> entries = new ArrayList<>();
    private int currentPage = 0;
    private Object pinyinSearcher = null;
    private List<ItemStack> pinyinIndexedStacks = null;

    private record Entry(ItemStack stack, boolean collected, String sortNamespace, String sortPath, String sortTiebreak) {}

    private record IconEntry(int u, int v, List<Component> tooltip) {}

    private static class PageButton extends Button {
        private static final ResourceLocation BUTTON_TEXTURE =
                ResourceLocation.fromNamespaceAndPath("elemenix", "textures/gui/page_button.png");

        private final boolean left;

        private PageButton(int x, int y, boolean left, OnPress onPress) {
            super(x, y, BTN_W, BTN_H, Component.empty(), onPress, Button.DEFAULT_NARRATION);
            this.left = left;
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int u = left ? 0 : BTN_W;
            graphics.blit(BUTTON_TEXTURE, getX(), getY(), u, 0, BTN_W, BTN_H, BTN_W * 2, BTN_H);
            if (isHovered()) {
                graphics.fill(getX(), getY(), getX() + BTN_W, getY() + BTN_H, 0x22FFFFFF);
            }
        }
    }

    private static class CrossButton extends Button {
        private static final ResourceLocation CROSS_TEXTURE =
                ResourceLocation.fromNamespaceAndPath("elemenix", "textures/gui/cross_button.png");

        private CrossButton(int x, int y, OnPress onPress) {
            super(x, y, BACK_BTN_SIZE, BACK_BTN_SIZE, Component.empty(), onPress, Button.DEFAULT_NARRATION);
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.blit(CROSS_TEXTURE, getX(), getY(), 0, 0, BACK_BTN_SIZE, BACK_BTN_SIZE, BACK_BTN_SIZE, BACK_BTN_SIZE);
            if (isHovered()) {
                graphics.fill(getX(), getY(), getX() + BACK_BTN_SIZE, getY() + BACK_BTN_SIZE, 0x22FFFFFF);
            }
        }
    }

    public RegistrarScreen(RegistrarMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
        this.titleLabelY = 10000;
        this.inventoryLabelY = 10000;
    }

    @Override
    protected void init() {
        super.init();

        this.searchBox = new EditBox(this.font, leftPos + SEARCH_X, topPos + SEARCH_Y,
                SEARCH_W, SEARCH_H, Component.translatable("gui.elemenix.registrar.search"));
        this.searchBox.setMaxLength(100);
        this.searchBox.setResponder(s -> rebuildEntries());
        this.addRenderableWidget(this.searchBox);

        this.showUncollectedCheckbox = new SmallCheckbox(
                leftPos + CHECK_X1, topPos + CHECK_Y,
                Component.empty(), this.font, ClientRegistrarCache.isShowUnregistered(),
                (checkbox, selected) -> {
                    ClientRegistrarCache.setShowUnregistered(selected);
                    rebuildEntries();
                });
        this.putUncollectedLastCheckbox = new SmallCheckbox(
                leftPos + CHECK_X1 + CHECK_GAP, topPos + CHECK_Y,
                Component.empty(), this.font, ClientRegistrarCache.isPutUnregisteredLast(),
                (checkbox, selected) -> {
                    ClientRegistrarCache.setPutUnregisteredLast(selected);
                    rebuildEntries();
                });
        this.sortByModCheckbox = new SmallCheckbox(
                leftPos + CHECK_X1 + CHECK_GAP * 2, topPos + CHECK_Y,
                Component.empty(), this.font, ClientRegistrarCache.isSortedByMod(),
                (checkbox, selected) -> {
                    ClientRegistrarCache.setSortedByMod(selected);
                    rebuildEntries();
                });
        this.showPotionlikeCheckbox = new SmallCheckbox(
                leftPos + CHECK_X1 + CHECK_GAP * 3, topPos + CHECK_Y,
                Component.empty(), this.font, ClientRegistrarCache.isPotionlikeShown(),
                (checkbox, selected) -> {
                    ClientRegistrarCache.setPotionlikeShown(selected);
                    rebuildEntries();
                });

        this.normalUnanalysableCheckbox = new SmallCheckbox(
                leftPos + CHECK_X1, topPos + CHECK_Y,
                Component.empty(), this.font, false,
                (checkbox, selected) -> onDeveloperCheckboxClicked(0, selected));
        this.strictlyUnanalysableCheckbox = new SmallCheckbox(
                leftPos + CHECK_X1 + CHECK_GAP, topPos + CHECK_Y,
                Component.empty(), this.font, false,
                (checkbox, selected) -> onDeveloperCheckboxClicked(1, selected));
        this.commandIgnoredCheckbox = new SmallCheckbox(
                leftPos + CHECK_X1 + CHECK_GAP * 2, topPos + CHECK_Y,
                Component.empty(), this.font, false,
                (checkbox, selected) -> onDeveloperCheckboxClicked(2, selected));
        this.spawnEggsCheckbox = new SmallCheckbox(
                leftPos + CHECK_X1 + CHECK_GAP * 3, topPos + CHECK_Y,
                Component.empty(), this.font, false,
                (checkbox, selected) -> onDeveloperCheckboxClicked(3, selected));

        this.addRenderableWidget(this.showUncollectedCheckbox);
        this.addRenderableWidget(this.putUncollectedLastCheckbox);
        this.addRenderableWidget(this.sortByModCheckbox);
        this.addRenderableWidget(this.showPotionlikeCheckbox);
        this.addRenderableWidget(this.normalUnanalysableCheckbox);
        this.addRenderableWidget(this.strictlyUnanalysableCheckbox);
        this.addRenderableWidget(this.commandIgnoredCheckbox);
        this.addRenderableWidget(this.spawnEggsCheckbox);

        this.prevButton = new PageButton(leftPos + PREV_BTN_X, topPos + BTN_Y, true, b -> changePage(-1));
        this.nextButton = new PageButton(leftPos + NEXT_BTN_X, topPos + BTN_Y, false, b -> changePage(1));
        this.addRenderableWidget(this.prevButton);
        this.addRenderableWidget(this.nextButton);

        this.backButton = new CrossButton(leftPos + BACK_BTN_X, topPos + BACK_BTN_Y, b -> {
            detailStack = ItemStack.EMPTY;
            rebuildEntries();
        });
        this.backButton.visible = false;
        this.addRenderableWidget(this.backButton);

        rebuildEntries();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        boolean cacheMode = ClientRegistrarCache.isDeveloperMode();
        if (developerMode != cacheMode) {
            developerMode = cacheMode;
            developerFilter = -1;
            syncDeveloperCheckboxes();
            currentPage = 0;
            detailStack = ItemStack.EMPTY;
            detailIcons = List.of();
            tagScroll = 0;
            descriptionScroll = 0.0f;
            if (searchBox != null) searchBox.setValue("");
        }
        rebuildEntries();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && !detailStack.isEmpty()) {
            detailStack = ItemStack.EMPTY;
            rebuildEntries();
            return true;
        }
        if (this.searchBox != null && this.searchBox.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                if ("*lg".equals(this.searchBox.getValue().trim())) {
                    toggleDeveloperMode();
                    return true;
                }
            }
            this.searchBox.keyPressed(keyCode, scanCode, modifiers);
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_TAB) {
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void toggleDeveloperMode() {
        this.searchBox.setValue("");
        PacketDistributor.sendToServer(new ToggleRegistrarDeveloperModePayload());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (detailStack.isEmpty()) {
            int index = getHoveredIndex((int) mouseX, (int) mouseY);
            if (index >= 0 && index < entries.size()) {
                Entry entry = entries.get(index);
                if (entry.collected()) {
                    detailStack = entry.stack().copy();
                    detailIcons = getDetailIcons(detailStack);
                    tagScroll = 0;
                    descriptionScroll = 0.0f;
                    return true;
                }
            }
        } else if (button == 0) {
            int mx = (int) mouseX;
            int my = (int) mouseY;
            int yOffset = detailIcons.isEmpty() ? DETAIL_LEFT_OFFSET : 0;
            if (isSourceHovered(mx, my, yOffset)) {
                String namespace = BuiltInRegistries.ITEM.getKey(detailStack.getItem()).getNamespace();
                jumpToFilter("@" + namespace);
                return true;
            }
            TagInfo tag = getHoveredTag(mx, my);
            if (tag != null) {
                if (tag.entity()) {
                    if (developerMode && developerFilter == 3) {
                        jumpToFilter("#" + tag.tag());
                        return true;
                    }
                } else {
                    jumpToFilter("#" + tag.tag());
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void jumpToFilter(String query) {
        detailStack = ItemStack.EMPTY;
        detailIcons = List.of();
        tagScroll = 0;
        descriptionScroll = 0.0f;
        currentPage = 0;
        searchBox.setValue(query);
        rebuildEntries();
    }

    private boolean isSourceHovered(int mouseX, int mouseY, int yOffset) {
        int textX = leftPos + BASIC_INFO_X;
        int textY = topPos + BASIC_INFO_Y + 11 + yOffset;
        return mouseX >= textX && mouseX < textX + BASIC_INFO_MAX_WIDTH
                && mouseY >= textY && mouseY < textY + this.font.lineHeight;
    }

    private TagInfo getHoveredTag(int mouseX, int mouseY) {
        List<TagInfo> tags = getSortedTags();
        int visible = TAG_AREA_H / TAG_ROW_H;
        int maxScroll = Math.max(0, tags.size() - visible);
        if (tagScroll > maxScroll) tagScroll = maxScroll;
        int listX = leftPos + TAG_AREA_X;
        int listY = topPos + TAG_AREA_Y;
        int listW = TAG_AREA_W - TAG_SCROLLBAR_W;
        for (int i = 0; i < visible; i++) {
            int tagIndex = tagScroll + i;
            if (tagIndex >= tags.size()) break;
            int rowY = listY + i * TAG_ROW_H;
            if (mouseX >= listX && mouseX < listX + listW + 1 && mouseY >= rowY && mouseY < rowY + TAG_ROW_H) {
                return tags.get(tagIndex);
            }
        }
        return null;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int mx = (int) mouseX;
        int my = (int) mouseY;

        if (detailStack.isEmpty()) {
            if (isOverGrid(mx, my) && getTotalPages() > 1) {
                if (scrollY > 0) {
                    changePage(-1);
                } else if (scrollY < 0) {
                    changePage(1);
                }
                return true;
            }
        } else {
            int listX = leftPos + TAG_AREA_X;
            int listY = topPos + TAG_AREA_Y;
            int listW = TAG_AREA_W - TAG_SCROLLBAR_W;
            if (mouseX >= listX && mouseX < listX + listW && mouseY >= listY && mouseY < listY + TAG_AREA_H) {
                int total = getSortedTags().size();
                int visible = TAG_AREA_H / TAG_ROW_H;
                int maxScroll = Math.max(0, total - visible);
                if (maxScroll > 0) {
                    tagScroll = Math.max(0, Math.min(tagScroll - (int) scrollY, maxScroll));
                    return true;
                }
            }

            int descX = leftPos + DESCRIPTION_X;
            int descY = topPos + DESCRIPTION_Y;
            int descW = DESCRIPTION_W - TAG_SCROLLBAR_W;
            if (mouseX >= descX && mouseX < descX + descW && mouseY >= descY && mouseY < descY + DESCRIPTION_H) {
                int hidden = Math.max(0, getDescriptionContentHeight() - DESCRIPTION_H);
                if (hidden > 0) {
                    float delta = (float) (scrollY * 18.0 / hidden);
                    descriptionScroll = Math.max(0.0f, Math.min(descriptionScroll - delta, 1.0f));
                    return true;
                }
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void rebuildEntries() {
        entries.clear();

        if (developerMode) {
            rebuildDeveloperEntries();
            sortEntries();
            applySearchFilter();
            clampPage();
            return;
        }

        Set<ResourceLocation> collectedItems = ClientRegistrarCache.getItems();
        Set<PotionMemoryData.Entry> collectedPotions = ClientRegistrarCache.getPotions();
        boolean showUncollected = showUncollectedCheckbox != null && showUncollectedCheckbox.selected();
        boolean showPotions = showPotionlikeCheckbox != null && showPotionlikeCheckbox.selected();

        for (ResourceLocation id : collectedItems) {
            Item item = BuiltInRegistries.ITEM.get(id);
            if (shouldHide(item)) continue;
            entries.add(itemEntry(new ItemStack(item), true));
        }

        if (showPotions) {
            for (PotionMemoryData.Entry potion : collectedPotions) {
                ItemStack stack = createPotionStack(potion);
                if (stack.isEmpty()) continue;
                entries.add(potionEntry(stack, potion));
            }
        }

        if (showUncollected) {
            for (Item item : BuiltInRegistries.ITEM) {
                if (shouldHide(item)) continue;
                if (isPotionLike(item)) continue;
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
                if (collectedItems.contains(id)) continue;
                entries.add(itemEntry(new ItemStack(item), false));
            }
        }

        if (showUncollected && showPotions) {
            Set<ResourceLocation> collectedPotionItems = new HashSet<>();
            for (PotionMemoryData.Entry potion : collectedPotions) {
                collectedPotionItems.add(potion.itemId());
            }
            for (Item item : BuiltInRegistries.ITEM) {
                if (item == Items.AIR) continue;
                if (!isPotionLike(item)) continue;
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
                if (collectedPotionItems.contains(id)) continue;
                entries.add(itemEntry(new ItemStack(item), false));
            }
        }

        sortEntries();
        applySearchFilter();
        clampPage();
    }

    private void rebuildDeveloperEntries() {
        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR) continue;
            if (developerFilter >= 0 && !matchesDeveloperFilter(item)) continue;
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (isPotionLike(item)) {
                PotionMemoryData.Entry entry = new PotionMemoryData.Entry(id, WATER_POTION_ID);
                ItemStack stack = createPotionStack(entry);
                if (stack.isEmpty()) continue;
                entries.add(potionEntry(stack, entry));
            } else {
                entries.add(itemEntry(new ItemStack(item), true));
            }
        }
    }

    private boolean matchesDeveloperFilter(Item item) {
        ItemStack stack = new ItemStack(item);
        return switch (developerFilter) {
            case 0 -> ElemenixInfo.isUnanalysable(item)
                    && !stack.is(ModTags.STRICTLY_UNANALYSABLE)
                    && !stack.is(ModTags.COMMAND_IGNORED)
                    && !stack.is(ModTags.SPAWN_EGGS);
            case 1 -> stack.is(ModTags.STRICTLY_UNANALYSABLE);
            case 2 -> stack.is(ModTags.COMMAND_IGNORED);
            case 3 -> stack.is(ModTags.SPAWN_EGGS);
            default -> true;
        };
    }

    private void onDeveloperCheckboxClicked(int filter, boolean selected) {
        developerFilter = selected ? filter : -1;
        syncDeveloperCheckboxes();
        rebuildEntries();
    }

    private void syncDeveloperCheckboxes() {
        if (normalUnanalysableCheckbox == null) return;
        normalUnanalysableCheckbox.setSelected(developerFilter == 0);
        strictlyUnanalysableCheckbox.setSelected(developerFilter == 1);
        commandIgnoredCheckbox.setSelected(developerFilter == 2);
        spawnEggsCheckbox.setSelected(developerFilter == 3);
    }

    private void sortEntries() {
        if (sortByModCheckbox != null && sortByModCheckbox.selected()) {
            entries.sort(Comparator.comparing((Entry e) -> modSortKey(e.sortNamespace()))
                    .thenComparing(Entry::sortNamespace)
                    .thenComparing(Entry::sortPath)
                    .thenComparing(Entry::sortTiebreak));
        } else {
            entries.sort(Comparator.comparing(Entry::sortPath)
                    .thenComparing(Entry::sortTiebreak)
                    .thenComparing(Entry::sortNamespace));
        }
        if (putUncollectedLastCheckbox != null && putUncollectedLastCheckbox.selected()) {
            entries.sort(Comparator.comparing(Entry::collected).reversed());
        }
    }

    private void applySearchFilter() {
        if (searchBox == null) return;
        String query = searchBox.getValue().trim();
        if (query.isEmpty()) return;

        if (query.startsWith("#")) {
            String tagQuery = query.substring(1).trim();
            if (developerMode && developerFilter == 3) {
                applyEntityTagFilter(tagQuery);
            } else {
                applyTagFilter(tagQuery);
            }
            return;
        }
        if (query.startsWith("@")) {
            applyNamespaceFilter(query.substring(1).trim());
            return;
        }

        entries.removeIf(e -> !e.collected());
        if (entries.isEmpty()) return;

        String lowerQuery = query.toLowerCase(Locale.ROOT);

        if (JechCompat.isAvailable()) {
            List<ItemStack> stacks = new ArrayList<>();
            for (Entry entry : entries) {
                stacks.add(entry.stack());
            }
            ensurePinyinIndex(stacks);
            Set<Object> matched = JechCompat.searchValues(pinyinSearcher, query);
            if (matched != null && !matched.isEmpty()) {
                List<Entry> filtered = new ArrayList<>();
                for (int i = 0; i < entries.size(); i++) {
                    if (matched.contains(i)) {
                        filtered.add(entries.get(i));
                    }
                }
                entries.clear();
                entries.addAll(filtered);
                return;
            }
        }

        entries.removeIf(e -> !e.stack().getHoverName().getString().toLowerCase(Locale.ROOT).contains(lowerQuery));
    }

    private void applyTagFilter(String tagQuery) {
        if (tagQuery.isEmpty()) {
            entries.clear();
            return;
        }
        List<TagKey<Item>> tags = new ArrayList<>();
        if (tagQuery.contains(":")) {
            ResourceLocation id = ResourceLocation.tryParse(tagQuery);
            if (id != null) {
                tags.add(ItemTags.create(id));
            }
        } else {
            BuiltInRegistries.ITEM.getTags()
                    .map(Pair::getFirst)
                    .filter(tag -> tag.location().getPath().equals(tagQuery))
                    .forEach(tags::add);
        }
        if (tags.isEmpty()) {
            entries.clear();
            return;
        }
        entries.removeIf(entry -> {
            for (TagKey<Item> tag : tags) {
                if (entry.stack().is(tag)) return false;
            }
            return true;
        });
    }

    private void applyEntityTagFilter(String tagQuery) {
        if (tagQuery.isEmpty()) {
            entries.clear();
            return;
        }
        List<TagKey<EntityType<?>>> tags = new ArrayList<>();
        if (tagQuery.contains(":")) {
            ResourceLocation id = ResourceLocation.tryParse(tagQuery);
            if (id != null) {
                tags.add(TagKey.create(Registries.ENTITY_TYPE, id));
            }
        } else {
            BuiltInRegistries.ENTITY_TYPE.getTags()
                    .map(Pair::getFirst)
                    .filter(tag -> tag.location().getPath().equals(tagQuery))
                    .forEach(tags::add);
        }
        if (tags.isEmpty()) {
            entries.clear();
            return;
        }
        entries.removeIf(entry -> {
            if (entry.stack().getItem() instanceof SpawnEggItem spawnEggItem) {
                EntityType<?> entityType = spawnEggItem.getType(entry.stack());
                for (TagKey<EntityType<?>> tag : tags) {
                    if (entityType.is(tag)) return false;
                }
            }
            return true;
        });
    }

    private void applyNamespaceFilter(String namespaceQuery) {
        if (namespaceQuery.isEmpty()) {
            entries.clear();
            return;
        }
        String lower = namespaceQuery.toLowerCase(Locale.ROOT);
        entries.removeIf(entry -> {
            String namespace = BuiltInRegistries.ITEM.getKey(entry.stack().getItem()).getNamespace();
            return !namespace.toLowerCase(Locale.ROOT).contains(lower);
        });
    }

    private void ensurePinyinIndex(List<ItemStack> fullStacks) {
        if (pinyinSearcher != null && pinyinIndexedStacks != null && pinyinIndexedStacks.equals(fullStacks)) {
            return;
        }
        pinyinSearcher = JechCompat.createSearcher();
        pinyinIndexedStacks = new ArrayList<>(fullStacks);
        if (pinyinSearcher == null) return;
        for (int i = 0; i < fullStacks.size(); i++) {
            ItemStack stack = fullStacks.get(i);
            if (!stack.isEmpty()) {
                JechCompat.putEntry(pinyinSearcher, stack.getHoverName().getString(), i);
            }
        }
    }

    private static String modSortKey(String namespace) {
        return "minecraft".equals(namespace) ? "" : namespace;
    }

    private static Entry itemEntry(ItemStack stack, boolean collected) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return new Entry(stack, collected, id.getNamespace(), id.getPath(), "");
    }

    private static Entry potionEntry(ItemStack stack, PotionMemoryData.Entry potion) {
        String tiebreak = potion.potionId().equals(WATER_POTION_ID) ? "" : potion.potionId().getPath();
        return new Entry(stack, true, potion.potionId().getNamespace(), potion.itemId().getPath(), tiebreak);
    }

    private static boolean isPotionLike(Item item) {
        return new ItemStack(item).is(ModTags.POTIONLIKE);
    }

    private static boolean shouldHide(Item item) {
        if (item == Items.AIR) return true;
        if (MemoryRegistrarBlockEntity.isBuiltInMemoryItem(item)) return false;
        ItemStack stack = new ItemStack(item);
        if (stack.is(ModTags.ANALYZER_UNRECORDABLE)) return true;
        if (stack.is(ModTags.REGISTRAR_HIDDEN)) return true;
        return ElemenixInfo.isUnanalysable(item);
    }

    private static ItemStack createPotionStack(PotionMemoryData.Entry entry) {
        try {
            Item item = BuiltInRegistries.ITEM.get(entry.itemId());
            Optional<Holder.Reference<Potion>> potion = BuiltInRegistries.POTION
                    .getHolder(ResourceKey.create(Registries.POTION, entry.potionId()));
            if (potion.isPresent()) {
                return PotionContents.createItemStack(item, potion.get());
            }
        } catch (Exception ignored) {
        }
        return ItemStack.EMPTY;
    }

    private int getTotalPages() {
        return Math.max(1, (int) Math.ceil((double) entries.size() / (COLS * ROWS)));
    }

    private int getCollectedCount() {
        Set<ResourceLocation> collectedItems = ClientRegistrarCache.getItems();
        Set<PotionMemoryData.Entry> collectedPotions = ClientRegistrarCache.getPotions();
        Set<ResourceLocation> baseIds = new HashSet<>();
        for (ResourceLocation id : collectedItems) {
            Item item = BuiltInRegistries.ITEM.get(id);
            if (shouldHide(item)) continue;
            baseIds.add(id);
        }
        for (PotionMemoryData.Entry potion : collectedPotions) {
            Item item = BuiltInRegistries.ITEM.get(potion.itemId());
            if (shouldHide(item)) continue;
            baseIds.add(potion.itemId());
        }
        return baseIds.size();
    }

    private int getTotalCount() {
        int total = 0;
        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR) continue;
            if (shouldHide(item)) continue;
            total++;
        }
        return total;
    }

    private void clampPage() {
        int totalPages = getTotalPages();
        if (currentPage >= totalPages) {
            currentPage = totalPages - 1;
        }
        if (currentPage < 0) {
            currentPage = 0;
        }
        updateButtonStates();
    }

    private void changePage(int delta) {
        int totalPages = getTotalPages();
        currentPage = (currentPage + delta + totalPages) % totalPages;
        updateButtonStates();
    }

    private void updateButtonStates() {
        if (prevButton != null && nextButton != null) {
            boolean multiplePages = getTotalPages() > 1;
            prevButton.active = multiplePages;
            nextButton.active = multiplePages;
        }
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        if (detailStack.isEmpty()) {
            graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
            renderGrid(graphics, mouseX, mouseY);
        } else {
            ResourceLocation texture = detailIcons.isEmpty() ? SINGLE_SIMPLE_TEXTURE : SINGLE_TEXTURE;
            graphics.blit(texture, x, y, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        updateWidgetsVisibility();
        super.render(graphics, mouseX, mouseY, partialTick);
        if (detailStack.isEmpty()) {
            renderTitle(graphics);
            renderPageNumber(graphics, mouseX, mouseY);
            renderSearchHint(graphics);
            renderCheckboxTooltips(graphics, mouseX, mouseY);
            this.renderTooltip(graphics, mouseX, mouseY);
            renderGridTooltip(graphics, mouseX, mouseY);
        } else {
            renderDetail(graphics, mouseX, mouseY);
        }
    }

    private void renderTitle(GuiGraphics graphics) {
        String title = this.title.getString();
        float scale = Math.min(TITLE_SCALE, (float) TITLE_MAX_WIDTH / this.font.width(title));
        int centerX = leftPos + (TITLE_LINE_X1 + TITLE_LINE_X2) / 2;
        int top = topPos + TITLE_LINE_Y - Math.round(this.font.lineHeight * scale);
        ScaledText.renderCentered(graphics, this.font, title, scale, centerX, top, TITLE_COLOR, false);
    }

    private void renderPageNumber(GuiGraphics graphics, int mouseX, int mouseY) {
        String text = (currentPage + 1) + " / " + getTotalPages();
        int textWidth = this.font.width(text);
        int x = leftPos + PAGE_CENTER_X - Math.round(textWidth * PAGE_SCALE / 2.0f);
        int y = topPos + PAGE_Y;
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0F);
        graphics.pose().scale(PAGE_SCALE, PAGE_SCALE, 1.0F);
        graphics.drawString(this.font, text, 0, 0, 0xFFFFFF, false);
        graphics.pose().popPose();

        if (developerMode) return;

        int w = Math.round(textWidth * PAGE_SCALE);
        int h = Math.round(this.font.lineHeight * PAGE_SCALE);
        if (mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h) {
            int collected;
            int total;
            String query = searchBox.getValue().trim();
            if (query.startsWith("#") || query.startsWith("@")) {
                collected = 0;
                for (Entry entry : entries) {
                    if (entry.collected()) collected++;
                }
                total = entries.size();
            } else {
                collected = getCollectedCount();
                total = getTotalCount();
            }
            graphics.renderTooltip(this.font,
                    Component.translatable("gui.elemenix.registrar.progress", collected, total), mouseX, mouseY);
        }
    }

    private void renderSearchHint(GuiGraphics graphics) {
        if (searchBox.getValue().isEmpty() && !searchBox.isFocused()) {
            graphics.drawString(this.font,
                    Component.translatable("gui.elemenix.registrar.search"),
                    leftPos + SEARCH_X + 4, topPos + SEARCH_Y + 2, 0xFF707070, false);
        }
    }

    private void renderGrid(GuiGraphics graphics, int mouseX, int mouseY) {
        int startIndex = currentPage * COLS * ROWS;
        int endIndex = Math.min(startIndex + COLS * ROWS, entries.size());
        int hovered = getHoveredIndex(mouseX, mouseY);

        for (int i = startIndex; i < endIndex; i++) {
            int offset = i - startIndex;
            int row = offset / COLS;
            int col = offset % COLS;
            Entry entry = entries.get(i);
            int itemX = leftPos + GRID_X + GRID_PAD + col * SLOT_W;
            int itemY = topPos + GRID_Y + GRID_PAD + row * SLOT_H;

            graphics.blit(ITEM_BACKGROUND_TEXTURE, itemX - 1, itemY - 1, 0, 0, 18, 18, 18, 18);
            if (i == hovered) {
                graphics.fill(itemX - 1, itemY - 1, itemX + 17, itemY + 17, 0x50FFFFFF);
            }
            if (entry.collected() || ClientConfig.SHOW_UNCOLLECTED_ICON.get()) {
                graphics.renderItem(entry.stack(), itemX, itemY);
            } else {
                graphics.blit(QUESTION_MARK_TEXTURE, itemX, itemY, 0, 0.0f, 0.0f, 16, 16, 16, 16);
            }
        }
    }

    private void renderGridTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        int index = getHoveredIndex(mouseX, mouseY);
        if (index < 0 || index >= entries.size()) return;
        Entry entry = entries.get(index);
        Component text;
        if (entry.collected()) {
            text = entry.stack().getHoverName();
        } else if (ClientConfig.SHOW_UNCOLLECTED_NAME.get()) {
            text = Component.translatable("gui.elemenix.registrar.uncollected_named", entry.stack().getHoverName());
        } else {
            text = Component.translatable("gui.elemenix.uncollected");
        }
        graphics.renderTooltip(this.font, text, mouseX, mouseY);
    }

    private void renderCheckboxTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        if (developerMode) {
            if (normalUnanalysableCheckbox.isHovered()) {
                graphics.renderTooltip(this.font, Component.translatable("gui.elemenix.registrar.dev_normal_unanalysable"), mouseX, mouseY);
            } else if (strictlyUnanalysableCheckbox.isHovered()) {
                graphics.renderTooltip(this.font, Component.translatable("gui.elemenix.registrar.dev_strictly_unanalysable"), mouseX, mouseY);
            } else if (commandIgnoredCheckbox.isHovered()) {
                graphics.renderTooltip(this.font, Component.translatable("gui.elemenix.registrar.dev_command_ignored"), mouseX, mouseY);
            } else if (spawnEggsCheckbox.isHovered()) {
                graphics.renderTooltip(this.font, Component.translatable("gui.elemenix.registrar.dev_spawn_eggs"), mouseX, mouseY);
            }
            return;
        }
        if (showUncollectedCheckbox.isHovered()) {
            graphics.renderTooltip(this.font, Component.translatable("gui.elemenix.registrar.show_uncollected"), mouseX, mouseY);
        } else if (putUncollectedLastCheckbox.isHovered()) {
            graphics.renderTooltip(this.font, Component.translatable("gui.elemenix.registrar.put_uncollected_last"), mouseX, mouseY);
        } else if (sortByModCheckbox.isHovered()) {
            graphics.renderTooltip(this.font, Component.translatable("gui.elemenix.registrar.sort_by_mod"), mouseX, mouseY);
        } else if (showPotionlikeCheckbox.isHovered()) {
            graphics.renderTooltip(this.font, Component.translatable("gui.elemenix.registrar.show_potions"), mouseX, mouseY);
        }
    }

    private void renderDetail(GuiGraphics graphics, int mouseX, int mouseY) {
        ItemStack stack = detailStack;
        int contentYOffset = detailIcons.isEmpty() ? DETAIL_LEFT_OFFSET : 0;
        renderTitle(graphics);
        renderLargeItem(graphics, stack, leftPos + DETAIL_ITEM_X, topPos + DETAIL_ITEM_Y + contentYOffset);
        renderBasicInfo(graphics, stack, mouseX, mouseY, contentYOffset);
        renderConstituents(graphics, stack, contentYOffset);
        renderDescription(graphics);
        renderTagArea(graphics, mouseX, mouseY);
        renderIcons(graphics, detailIcons, mouseX, mouseY);
        int itemSize = Math.round(16 * DETAIL_ITEM_SCALE);
        if (mouseX >= leftPos + DETAIL_ITEM_X && mouseX < leftPos + DETAIL_ITEM_X + itemSize
                && mouseY >= topPos + DETAIL_ITEM_Y + contentYOffset && mouseY < topPos + DETAIL_ITEM_Y + contentYOffset + itemSize) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            graphics.renderTooltip(this.font,
                    List.of(Component.translatable("gui.elemenix.registrar.item_id"), Component.literal(id.toString())),
                    Optional.empty(), mouseX, mouseY);
        }
        if (backButton.isHovered()) {
            graphics.renderTooltip(this.font, Component.translatable("gui.elemenix.registrar.back"), mouseX, mouseY);
        }
    }

    private void updateWidgetsVisibility() {
        boolean list = detailStack.isEmpty();
        searchBox.visible = list;
        showUncollectedCheckbox.visible = list && !developerMode;
        putUncollectedLastCheckbox.visible = list && !developerMode;
        sortByModCheckbox.visible = list && !developerMode;
        showPotionlikeCheckbox.visible = list && !developerMode;
        normalUnanalysableCheckbox.visible = list && developerMode;
        strictlyUnanalysableCheckbox.visible = list && developerMode;
        commandIgnoredCheckbox.visible = list && developerMode;
        spawnEggsCheckbox.visible = list && developerMode;
        prevButton.visible = list;
        nextButton.visible = list;
        backButton.visible = !list;
    }

    private void renderLargeItem(GuiGraphics graphics, ItemStack stack, int x, int y) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0F);
        graphics.pose().scale(DETAIL_ITEM_SCALE, DETAIL_ITEM_SCALE, 1.0F);
        graphics.renderItem(stack, 0, 0);
        graphics.pose().popPose();
    }

    private void renderBasicInfo(GuiGraphics graphics, ItemStack stack, int mouseX, int mouseY, int yOffset) {
        String name = stack.getHoverName().getString();
        String namespace = BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace();
        String source = Component.translatable("gui.elemenix.registrar.source", getModDisplayName(namespace)).getString();
        String stackSize = Component.translatable("gui.elemenix.registrar.stack_size", stack.getMaxStackSize()).getString();

        int infoX = leftPos + BASIC_INFO_X;

        float nameScale = computeTextScale(name, true);
        ScaledText.Fit nameFit = ScaledText.Fit.atScale(this.font, name, BASIC_INFO_MAX_WIDTH, nameScale, true);
        ScaledText.render(graphics, this.font, nameFit, infoX, topPos + BASIC_INFO_Y - 1 + yOffset, 0x404040, false);
        ScaledText.renderTooltip(graphics, this.font, nameFit, infoX, topPos + BASIC_INFO_Y - 1 + yOffset, mouseX, mouseY);

        float infoScale = Math.min(computeTextScale(source, false), computeTextScale(stackSize, false));
        infoScale = Math.min(infoScale, nameScale);
        ScaledText.Fit sourceFit = ScaledText.Fit.atScale(this.font, source, BASIC_INFO_MAX_WIDTH, infoScale, false);
        ScaledText.Fit stackFit = ScaledText.Fit.atScale(this.font, stackSize, BASIC_INFO_MAX_WIDTH, infoScale, false);
        int sourceColor = isSourceHovered(mouseX, mouseY, yOffset) ? 0x808080 : 0x404040;
        ScaledText.render(graphics, this.font, sourceFit, infoX, topPos + BASIC_INFO_Y + 11 + yOffset, sourceColor, false);
        ScaledText.render(graphics, this.font, stackFit, infoX, topPos + BASIC_INFO_Y + 21 + yOffset, 0x404040, false);
        ScaledText.renderTooltip(graphics, this.font, sourceFit, infoX, topPos + BASIC_INFO_Y + 11 + yOffset, mouseX, mouseY);
        ScaledText.renderTooltip(graphics, this.font, stackFit, infoX, topPos + BASIC_INFO_Y + 21 + yOffset, mouseX, mouseY);
    }

    private void renderConstituents(GuiGraphics graphics, ItemStack stack, int yOffset) {
        Constituents constituents = ElemenixInfo.getConstituents(stack);
        if (constituents == null) return;

        renderConstituentsTitle(graphics, yOffset);

        Elemenix[] order = {Elemenix.ORGANIX, Elemenix.TERRIX, Elemenix.FLUMIX, Elemenix.METALLIX, Elemenix.ENERGIX, Elemenix.ARCANIX};
        if (constituents.isUnanalysable()) {
            String questionMarks = Component.translatable("gui.elemenix.uncollected").getString();
            for (int i = 0; i < order.length; i++) {
                ScaledText.Fit fit = ScaledText.Fit.of(this.font, questionMarks, CONSTITUENTS_MAX_WIDTH, CONSTITUENTS_MIN_SCALE);
                ScaledText.render(graphics, this.font, fit, leftPos + CONSTITUENTS_X, topPos + CONSTITUENTS_START_Y + i * CONSTITUENTS_GAP + yOffset, 0xFF3333, false);
            }
            return;
        }

        for (int i = 0; i < order.length; i++) {
            Elemenix e = order[i];
            long value = constituents.get(e);
            String text = Long.toString(value);
            ScaledText.Fit fit = ScaledText.Fit.of(this.font, text, CONSTITUENTS_MAX_WIDTH, CONSTITUENTS_MIN_SCALE);
            ScaledText.render(graphics, this.font, fit, leftPos + CONSTITUENTS_X, topPos + CONSTITUENTS_START_Y + i * CONSTITUENTS_GAP + yOffset, e.getColor(), false);
        }
    }

    private void renderConstituentsTitle(GuiGraphics graphics, int yOffset) {
        String title = Component.translatable("gui.elemenix.registrar.constituents").getString();
        float scale = Math.min(0.9f, (float) CONSTITUENTS_TITLE_MAX_WIDTH / this.font.width(title));
        ScaledText.renderCentered(graphics, this.font, title, scale, leftPos + CONSTITUENTS_TITLE_CENTER_X, topPos + CONSTITUENTS_TITLE_Y + yOffset, 0x404040, false);
    }

    private void renderIcons(GuiGraphics graphics, List<IconEntry> icons, int mouseX, int mouseY) {
        int x = leftPos + ICONS_X;
        int y = topPos + ICONS_Y;
        IconEntry hovered = null;

        List<IconEntry> display = icons;
        if (icons.size() > 8) {
            display = new ArrayList<>(icons.subList(0, 7));
            display.add(createEllipsisIcon(icons.subList(7, icons.size())));
        }

        for (IconEntry icon : display) {
            graphics.blit(ICONS_TEXTURE, x, y, icon.u(), icon.v(), ICON_SIZE, ICON_SIZE, 48, 36);
            if (mouseX >= x && mouseX < x + ICON_SIZE && mouseY >= y && mouseY < y + ICON_SIZE) {
                graphics.fill(x, y, x + ICON_SIZE, y + ICON_SIZE, 0x50FFFFFF);
                hovered = icon;
            }
            x += ICONS_GAP;
        }
        if (hovered != null) {
            graphics.renderTooltip(this.font, hovered.tooltip(), Optional.empty(), mouseX, mouseY);
        }
    }

    private IconEntry createEllipsisIcon(List<IconEntry> omitted) {
        List<Component> tooltip = new ArrayList<>();
        for (int i = 0; i < omitted.size(); i++) {
            if (i > 0) tooltip.add(Component.empty());
            tooltip.addAll(omitted.get(i).tooltip());
        }
        return new IconEntry(36, 24, tooltip);
    }

    private List<IconEntry> getDetailIcons(ItemStack stack) {
        List<IconEntry> icons = new ArrayList<>();

        Rarity rarity = stack.get(DataComponents.RARITY);
        if (rarity != null && rarity != Rarity.COMMON) {
            Component rarityName = Component.translatable("rarity." + rarity.name().toLowerCase(Locale.ROOT));
            icons.add(new IconEntry(0, 0, List.of(Component.translatable("gui.elemenix.registrar.rarity", rarityName))));
        }

        if (stack.getMaxDamage() > 0) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.translatable("gui.elemenix.registrar.durability", stack.getMaxDamage()));
            String repairName = getRepairItemName(stack);
            if (repairName != null) {
                tooltip.add(Component.translatable("gui.elemenix.registrar.repair_item", repairName));
            }
            icons.add(new IconEntry(12, 0, tooltip));
        }

        if (stack.isEnchantable()) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.translatable("gui.elemenix.registrar.enchantability", stack.getEnchantmentValue()));
            List<String> enchantNames = getEnchantmentNames(stack);
            if (enchantNames.size() == 1) {
                tooltip.add(Component.translatable("gui.elemenix.registrar.enchantments")
                        .append(Component.literal(enchantNames.getFirst()).withStyle(ChatFormatting.LIGHT_PURPLE)));
            } else if (enchantNames.size() > 1) {
                tooltip.add(Component.translatable("gui.elemenix.registrar.enchantments"));
                for (String name : enchantNames) {
                    tooltip.add(Component.literal(name).withStyle(ChatFormatting.LIGHT_PURPLE));
                }
            }
            icons.add(new IconEntry(24, 0, tooltip));
        }

        FoodProperties food = stack.get(DataComponents.FOOD);
        if (food != null) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.translatable("gui.elemenix.registrar.food_hunger", food.nutrition()));
            tooltip.add(Component.translatable("gui.elemenix.registrar.food_saturation",
                    String.format(Locale.ROOT, "%.1f", food.saturation())));
            icons.add(new IconEntry(36, 0, tooltip));
        }

        List<Component> effectLines = new ArrayList<>();
        PotionContents potionContents = stack.get(DataComponents.POTION_CONTENTS);
        if (potionContents != null) {
            for (MobEffectInstance effect : potionContents.getAllEffects()) {
                effectLines.add(Component.literal(formatEffect(effect)));
            }
        }
        if (food != null) {
            for (FoodProperties.PossibleEffect possible : food.effects()) {
                effectLines.add(Component.literal(formatEffect(possible.effect())));
            }
        }
        if (!effectLines.isEmpty()) {
            icons.add(new IconEntry(0, 12, effectLines));
        }

        double attackDamage = 0;
        double attackSpeed = 0;
        boolean hasWeapon = false;
        for (ItemAttributeModifiers.Entry entry : stack.getAttributeModifiers().modifiers()) {
            if (entry.attribute().value() == Attributes.ATTACK_DAMAGE.value()) {
                attackDamage = entry.modifier().amount();
                hasWeapon = true;
            } else if (entry.attribute().value() == Attributes.ATTACK_SPEED.value()) {
                attackSpeed = entry.modifier().amount() + 4.0;
                hasWeapon = true;
            }
        }
        if (hasWeapon) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.translatable("gui.elemenix.registrar.attack_damage", formatAttributeValue(attackDamage)));
            tooltip.add(Component.translatable("gui.elemenix.registrar.attack_speed", formatAttributeValue(attackSpeed)));
            icons.add(new IconEntry(12, 12, tooltip));
        }

        double armor = 0;
        double armorToughness = 0;
        double knockbackResistance = 0;
        for (ItemAttributeModifiers.Entry entry : stack.getAttributeModifiers().modifiers()) {
            if (entry.attribute().value() == Attributes.ARMOR.value()) {
                armor = entry.modifier().amount();
            } else if (entry.attribute().value() == Attributes.ARMOR_TOUGHNESS.value()) {
                armorToughness = entry.modifier().amount();
            } else if (entry.attribute().value() == Attributes.KNOCKBACK_RESISTANCE.value()) {
                knockbackResistance = entry.modifier().amount();
            }
        }
        List<Component> armorTooltip = new ArrayList<>();
        if (armor != 0) {
            armorTooltip.add(Component.translatable("gui.elemenix.registrar.armor_value", formatAttributeValue(armor)));
        }
        if (armorToughness != 0) {
            armorTooltip.add(Component.translatable("gui.elemenix.registrar.armor_toughness", formatAttributeValue(armorToughness)));
        }
        if (knockbackResistance != 0) {
            armorTooltip.add(Component.translatable("gui.elemenix.registrar.knockback_resistance",
                    formatAttributeValue(knockbackResistance * 100) + "%"));
        }
        if (!armorTooltip.isEmpty()) {
            icons.add(new IconEntry(24, 12, armorTooltip));
        }

        int burnTime = stack.getBurnTime(RecipeType.SMELTING);
        if (burnTime > 0) {
            icons.add(new IconEntry(36, 12, List.of(Component.translatable("gui.elemenix.registrar.fuel", burnTime))));
        }

        Constituents constituents = ElemenixInfo.getConstituents(stack);
        if (constituents != null && !constituents.isUnanalysable()) {
            for (Elemenix e : Elemenix.values()) {
                if (constituents.isPure(e)) {
                    icons.add(new IconEntry(0, 24, List.of(
                            Component.translatable("gui.elemenix.registrar.pure", e.getName()).withColor(e.getColor())
                    )));
                    break;
                }
            }
        }

        if (stack.has(DataComponents.FIRE_RESISTANT)) {
            icons.add(new IconEntry(12, 24, List.of(Component.translatable("gui.elemenix.registrar.fireproof"))));
        }

        List<Component> trimMobTooltip = new ArrayList<>();
        if (stack.is(ItemTags.TRIM_MATERIALS)) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level != null) {
                Optional<Holder.Reference<TrimMaterial>> trimMaterial =
                        TrimMaterials.getFromIngredient(minecraft.level.registryAccess(), stack);
                if (trimMaterial.isPresent()) {
                    TextColor textColor = trimMaterial.get().value().description().getStyle().getColor();
                    int trimColor = textColor != null ? textColor.getValue() : 0xFFFFFF;
                    Component colorBlock = Component.literal("■").withColor(trimColor);
                    trimMobTooltip.add(Component.translatable("gui.elemenix.registrar.trim_color", colorBlock));
                }
            }
        }
        if (stack.getItem() instanceof SpawnEggItem spawnEggItem) {
            EntityType<?> entityType = spawnEggItem.getType(stack);
            if (!trimMobTooltip.isEmpty()) {
                trimMobTooltip.add(Component.empty());
            }
            trimMobTooltip.add(Component.translatable("gui.elemenix.registrar.mob", entityType.getDescription()));
        }
        if (!trimMobTooltip.isEmpty()) {
            icons.add(new IconEntry(24, 24, trimMobTooltip));
        }

        return icons;
    }

    private static String formatAttributeValue(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.format(Locale.ROOT, "%+.0f", value);
        }
        return String.format(Locale.ROOT, "%+.1f", value);
    }

    private static String formatEffect(MobEffectInstance effect) {
        StringBuilder sb = new StringBuilder(Component.translatable(effect.getDescriptionId()).getString());
        if (effect.getAmplifier() > 0) {
            sb.append(' ').append(Component.translatable("potion.potency." + effect.getAmplifier()).getString());
        }
        if (effect.isInfiniteDuration()) {
            sb.append(" ∞");
        } else if (effect.getDuration() > 20) {
            sb.append(' ').append(formatEffectDuration(effect.getDuration()));
        }
        return sb.toString();
    }

    private static String formatEffectDuration(int ticks) {
        int seconds = ticks / 20;
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return minutes + ":" + String.format(Locale.ROOT, "%02d", secs);
    }

    private static List<String> getEnchantmentNames(ItemStack stack) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return List.of();
        Registry<Enchantment> registry = minecraft.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        List<String> names = new ArrayList<>();
        registry.holders()
                .filter(stack::supportsEnchantment)
                .sorted(Comparator.comparing((Holder<Enchantment> h) -> namespaceKey(enchantNamespace(h)))
                        .thenComparingInt(RegistrarScreen::enchantCategory)
                        .thenComparing(RegistrarScreen::enchantPath))
                .forEach(holder -> names.add(holder.value().description().getString()));
        return names;
    }

    private static String namespaceKey(String namespace) {
        if ("minecraft".equals(namespace)) return "";
        if ("c".equals(namespace)) return "!";
        return namespace;
    }

    private static int enchantCategory(Holder<Enchantment> holder) {
        if (holder.is(EnchantmentTags.CURSE)) return 2;
        if (holder.is(EnchantmentTags.TREASURE)) return 1;
        return 0;
    }

    private static String enchantNamespace(Holder<Enchantment> holder) {
        return holder.unwrapKey().map(key -> key.location().getNamespace()).orElse("");
    }

    private static String enchantPath(Holder<Enchantment> holder) {
        return holder.unwrapKey().map(key -> key.location().getPath()).orElse("");
    }

    @Nullable
    private static String getRepairItemName(ItemStack stack) {
        Ingredient ingredient = findRepairIngredient(stack.getItem());
        if (ingredient == null) return null;
        ItemStack[] items = ingredient.getItems();
        if (items.length == 0) return null;
        return items[0].getHoverName().getString();
    }

    @Nullable
    private static Ingredient findRepairIngredient(Item item) {
        if (item instanceof TieredItem tiered) {
            return tiered.getTier().getRepairIngredient();
        }
        if (item instanceof ArmorItem armor) {
            return armor.getMaterial().value().repairIngredient().get();
        }
        if (item == Items.SHIELD) return Ingredient.of(ItemTags.PLANKS);
        if (item == Items.ELYTRA) return Ingredient.of(Items.PHANTOM_MEMBRANE);
        if (item == Items.WOLF_ARMOR) return Ingredient.of(Items.ARMADILLO_SCUTE);
        return null;
    }

    private static String getModDisplayName(String modId) {
        String key = "modname." + modId;
        if (I18n.exists(key)) {
            return I18n.get(key);
        }
        String name = ModList.get().getModContainerById(modId)
                .map(container -> container.getModInfo().getDisplayName())
                .orElse(null);
        if (name == null || name.isEmpty()) {
            return idToWords(modId);
        }
        return name;
    }

    private static String idToWords(String id) {
        StringBuilder sb = new StringBuilder();
        boolean capitalize = true;
        for (char c : id.toCharArray()) {
            if (c == '_' || c == '-') {
                sb.append(' ');
                capitalize = true;
            } else if (capitalize) {
                sb.append(Character.toUpperCase(c));
                capitalize = false;
            } else if (Character.isUpperCase(c)) {
                sb.append(' ').append(c);
            } else {
                sb.append(c);
            }
        }
        return sb.toString().trim();
    }

    private float computeTextScale(String text, boolean bold) {
        int width = bold ? this.font.width(Component.literal(text).withStyle(ChatFormatting.BOLD)) : this.font.width(text);
        if (width <= BASIC_INFO_MAX_WIDTH) return 1.0f;
        return Math.max(BASIC_INFO_MIN_SCALE, (float) BASIC_INFO_MAX_WIDTH / width);
    }

    private void renderTagArea(GuiGraphics graphics, int mouseX, int mouseY) {
        List<TagInfo> tags = getSortedTags();
        int visible = TAG_AREA_H / TAG_ROW_H;
        int maxScroll = Math.max(0, tags.size() - visible);
        if (tagScroll > maxScroll) tagScroll = maxScroll;

        int listX = leftPos + TAG_AREA_X;
        int listY = topPos + TAG_AREA_Y;
        int listW = TAG_AREA_W - TAG_SCROLLBAR_W;

        if (tags.isEmpty()) {
            String noTags = Component.translatable("gui.elemenix.registrar.no_tags").getString();
            int textWidth = Math.round(this.font.width(noTags) * TAG_TEXT_SCALE);
            int cx = listX + TAG_AREA_W / 2;
            int cy = listY + TAG_AREA_H / 2;
            graphics.pose().pushPose();
            graphics.pose().translate(cx - textWidth / 2.0f, cy - Math.round(this.font.lineHeight * TAG_TEXT_SCALE / 2.0f), 0.0F);
            graphics.pose().scale(TAG_TEXT_SCALE, TAG_TEXT_SCALE, 1.0F);
            graphics.drawString(this.font, noTags, 0, 0, 0xFFFFFFFF, false);
            graphics.pose().popPose();
            return;
        }

        TagInfo hoveredTag = null;
        for (int i = 0; i < visible; i++) {
            int tagIndex = tagScroll + i;
            if (tagIndex >= tags.size()) break;
            TagInfo info = tags.get(tagIndex);
            int rowY = listY + i * TAG_ROW_H;
            boolean hovered = mouseX >= listX && mouseX < listX + listW + 1 && mouseY >= rowY && mouseY < rowY + TAG_ROW_H;

            int bgColor = info.entity()
                    ? ((tagIndex % 2 == 0) ? 0x14142240 : 0x38304068)
                    : ((tagIndex % 2 == 0) ? 0x10000000 : 0x34000000);
            graphics.fill(listX, rowY, listX + listW + 1, rowY + TAG_ROW_H, bgColor);
            if (hovered) {
                graphics.fill(listX, rowY, listX + listW + 1, rowY + TAG_ROW_H, 0x30FFFFFF);
                hoveredTag = info;
            }

            ItemStack icon = getTagRepresentativeIcon(info);
            if (!icon.isEmpty()) {
                renderSmallItem(graphics, icon, listX + 2, rowY + 1);
            } else {
                renderSmallQuestionMark(graphics, listX + 2, rowY + 1);
            }
            String text = ModUtils.truncateToWidth(this.font, getTagName(info), TAG_TEXT_MAX_WIDTH);
            ScaledText.render(graphics, this.font, ScaledText.Fit.fixed(text, TAG_TEXT_SCALE), listX + TAG_ICON_SIZE + 5, rowY + 4, 0xFFE0E0E0, false);
        }

        renderTagScrollbar(graphics, listX + listW + 1, listY, tags.size(), visible, tagScroll);

        if (hoveredTag != null) {
            String tagName = getTagName(hoveredTag);
            String shownText = ModUtils.truncateToWidth(this.font, tagName, TAG_TEXT_MAX_WIDTH);
            if (!tagName.equals(hoveredTag.tag().toString()) && !tagName.equals(shownText)) {
                graphics.renderTooltip(this.font, List.of(
                        Component.literal(tagName),
                        Component.literal(hoveredTag.tag().toString())
                ), Optional.empty(), mouseX, mouseY);
            } else {
                graphics.renderTooltip(this.font, Component.literal(hoveredTag.tag().toString()), mouseX, mouseY);
            }
        }
    }

    private void renderSmallItem(GuiGraphics graphics, ItemStack stack, int x, int y) {
        float scale = TAG_ICON_SIZE / 16.0f;
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.renderItem(stack, 0, 0);
        graphics.pose().popPose();
    }

    private void renderSmallQuestionMark(GuiGraphics graphics, int x, int y) {
        float scale = TAG_ICON_SIZE / 16.0f;
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.blit(QUESTION_MARK_TEXTURE, 0, 0, 0, 0, 16, 16, 16, 16);
        graphics.pose().popPose();
    }

    private void renderTagScrollbar(GuiGraphics graphics, int x, int y, int total, int visible, int scroll) {
        graphics.fill(x, y, x + TAG_SCROLLBAR_W, y + TAG_AREA_H, 0x48000000);
        if (total <= visible) return;
        int maxScroll = total - visible;
        int thumbH = Math.max(8, TAG_AREA_H * visible / total);
        int thumbY = y + (TAG_AREA_H - thumbH) * scroll / maxScroll;
        graphics.fill(x + 1, thumbY, x + TAG_SCROLLBAR_W - 1, thumbY + thumbH, 0xFF303030);
    }

    private void renderDescription(GuiGraphics graphics) {
        int areaX = leftPos + DESCRIPTION_X;
        int areaY = topPos + DESCRIPTION_Y;
        int listW = DESCRIPTION_W - TAG_SCROLLBAR_W;

        String description = getDescription(detailStack);
        if (description == null || description.isEmpty()) {
            String text = Component.translatable("gui.elemenix.registrar.no_description").getString();
            List<String> lines = ScaledText.wrap(this.font, text, DESCRIPTION_TEXT_MAX_WIDTH);
            int lineHeight = this.font.lineHeight;
            int totalHeight = lines.size() * lineHeight;
            int startY = areaY + (DESCRIPTION_H - totalHeight) / 2;
            for (int i = 0; i < lines.size(); i++) {
                int lineWidth = this.font.width(lines.get(i));
                int cx = areaX + listW / 2;
                graphics.drawString(this.font, lines.get(i), cx - lineWidth / 2, startY + i * lineHeight, 0xFFFFFFFF, false);
            }
            renderDescriptionScrollbar(graphics, areaX + listW + 1, areaY, 0, 0.0f);
            return;
        }

        int wrapWidth = (int) (DESCRIPTION_TEXT_MAX_WIDTH / DESCRIPTION_TEXT_SCALE);
        List<String> lines = wrapDescription(description, wrapWidth);
        int lineHeight = Math.round((this.font.lineHeight + 2) * DESCRIPTION_TEXT_SCALE);
        int contentHeight = lines.size() * lineHeight;
        int scrollableHeight = contentHeight + DESCRIPTION_BOTTOM_PADDING;
        int hidden = Math.max(0, scrollableHeight - DESCRIPTION_H);
        if (descriptionScroll > 1.0f) descriptionScroll = 1.0f;
        float scrollPixels = hidden * descriptionScroll;

        graphics.enableScissor(areaX, areaY, areaX + listW, areaY + DESCRIPTION_H);
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, -scrollPixels, 0.0F);
        try {
            for (int i = 0; i < lines.size(); i++) {
                graphics.pose().pushPose();
                graphics.pose().translate(areaX + DESCRIPTION_TEXT_PADDING, areaY + DESCRIPTION_TOP_PADDING + i * lineHeight, 0.0F);
                graphics.pose().scale(DESCRIPTION_TEXT_SCALE, DESCRIPTION_TEXT_SCALE, 1.0F);
                graphics.drawString(this.font, lines.get(i), 0, 0, 0xFFE0E0E0, false);
                graphics.pose().popPose();
            }
        } finally {
            graphics.pose().popPose();
            graphics.disableScissor();
        }

        renderDescriptionScrollbar(graphics, areaX + listW + 1, areaY, scrollableHeight, descriptionScroll);
    }

    private int getDescriptionContentHeight() {
        String description = getDescription(detailStack);
        if (description == null || description.isEmpty()) return 0;
        int wrapWidth = (int) (DESCRIPTION_TEXT_MAX_WIDTH / DESCRIPTION_TEXT_SCALE);
        int lineHeight = Math.round((this.font.lineHeight + 2) * DESCRIPTION_TEXT_SCALE);
        return wrapDescription(description, wrapWidth).size() * lineHeight + DESCRIPTION_BOTTOM_PADDING;
    }

    private void renderDescriptionScrollbar(GuiGraphics graphics, int x, int y, int contentHeight, float scroll) {
        graphics.fill(x, y, x + TAG_SCROLLBAR_W, y + DESCRIPTION_H, 0x48000000);
        if (contentHeight <= DESCRIPTION_H) return;
        int thumbH = Math.max(8, DESCRIPTION_H * DESCRIPTION_H / contentHeight);
        int thumbY = y + Math.round((DESCRIPTION_H - thumbH) * scroll);
        graphics.fill(x + 1, thumbY, x + TAG_SCROLLBAR_W - 1, thumbY + thumbH, 0xFF303030);
    }

    private static String getDescription(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String ns = id.getNamespace();
        String path = id.getPath();
        String[] keys = {
                "description." + ns + "." + path,
                "lore." + ns + "." + path,
                "jei.info." + ns + "." + path,
                "jei." + ns + ".info." + path,
                "jei.info." + path,
                "item." + ns + "." + path + ".desc",
                "item." + ns + "." + path + ".description",
        };
        for (String key : keys) {
            if (I18n.exists(key)) {
                return I18n.get(key);
            }
        }
        return null;
    }

    private List<String> wrapDescription(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] paragraphs = text.split("\n", -1);
        for (String paragraph : paragraphs) {
            if (paragraph.isEmpty()) {
                lines.add("");
                continue;
            }
            int start = 0;
            while (start < paragraph.length()) {
                int end = start;
                int width = 0;
                while (end < paragraph.length()) {
                    int charWidth = this.font.width(String.valueOf(paragraph.charAt(end)));
                    if (width + charWidth > maxWidth) break;
                    width += charWidth;
                    end++;
                }
                if (end == start) end = start + 1;

                boolean englishBreak = end > start && end < paragraph.length()
                        && isEnglishLetter(paragraph.charAt(end - 1))
                        && isEnglishLetter(paragraph.charAt(end));

                if (englishBreak) {
                    int wordEnd = end;
                    while (wordEnd < paragraph.length() && isEnglishLetter(paragraph.charAt(wordEnd))) {
                        wordEnd++;
                    }
                    if (wordEnd - end == 1) {
                        int wordStart = end;
                        while (wordStart > start && isEnglishLetter(paragraph.charAt(wordStart - 1))) {
                            wordStart--;
                        }
                        if (wordStart > start) {
                            end = wordStart;
                            englishBreak = false;
                        }
                    }
                }

                if (!englishBreak) {
                    while (end < paragraph.length() && isForbiddenLineStart(paragraph.charAt(end))) {
                        end++;
                    }
                }

                String line = paragraph.substring(start, end).stripTrailing();
                if (englishBreak) {
                    line = line + "-";
                }
                lines.add(line);
                start = end;
                while (start < paragraph.length() && paragraph.charAt(start) == ' ') {
                    start++;
                }
            }
        }
        return lines;
    }

    private static boolean isEnglishLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private static boolean isForbiddenLineStart(char c) {
        return switch (c) {
            case '，', '。', '、', '；', '：', '？', '！', '）', '】', '》', '〉', '〕', '」', '』',
                 ',', '.', ';', ':', '?', '!', ')', ']', '}', '…' -> true;
            default -> false;
        };
    }

    private List<TagInfo> getSortedTags() {
        List<TagInfo> tags = new ArrayList<>();
        detailStack.getTags()
                .map(TagKey::location)
                .map(tag -> new TagInfo(tag, false))
                .forEach(tags::add);
        if (detailStack.getItem() instanceof SpawnEggItem spawnEggItem) {
            EntityType<?> entityType = spawnEggItem.getType(detailStack);
            entityType.getTags()
                    .map(TagKey::location)
                    .map(tag -> new TagInfo(tag, true))
                    .forEach(tags::add);
        }
        tags.sort(Comparator.comparing((TagInfo t) -> t.entity() ? 0 : 1)
                .thenComparing(t -> namespaceKey(t.tag().getNamespace()))
                .thenComparing(t -> t.tag().getPath()));
        return tags;
    }

    private record TagInfo(ResourceLocation tag, boolean entity) {}

    private static String getTagName(TagInfo info) {
        String prefix = info.entity() ? "tag.entity_type." : "tag.item.";
        String namespace = info.tag().getNamespace();
        String path = info.tag().getPath();
        String key = prefix + namespace + "." + path;
        if (I18n.exists(key)) {
            return I18n.get(key);
        }
        if (path.contains("/")) {
            key = prefix + namespace + "." + path.replace('/', '.');
            if (I18n.exists(key)) {
                return I18n.get(key);
            }
        }
        return info.tag().toString();
    }

    private static ItemStack getTagRepresentativeIcon(TagInfo info) {
        if (info.entity()) {
            ItemStack icon = getEntityTagRepresentatives().get(info.tag());
            return icon != null ? icon : ItemStack.EMPTY;
        }
        Item item = getItemTagRepresentatives().get(info.tag());
        return item != null ? new ItemStack(item) : ItemStack.EMPTY;
    }

    private static Map<ResourceLocation, Item> itemTagRepresentatives;

    private static Map<ResourceLocation, Item> getItemTagRepresentatives() {
        if (itemTagRepresentatives == null) {
            itemTagRepresentatives = loadItemTagRepresentatives();
        }
        return itemTagRepresentatives;
    }

    private static Map<ResourceLocation, ItemStack> entityTagRepresentatives;

    private static Map<ResourceLocation, ItemStack> getEntityTagRepresentatives() {
        if (entityTagRepresentatives == null) {
            entityTagRepresentatives = loadEntityTagRepresentatives();
        }
        return entityTagRepresentatives;
    }

    private static Map<ResourceLocation, Item> loadItemTagRepresentatives() {
        return loadItemRepresentatives();
    }

    private static Map<ResourceLocation, Item> loadItemRepresentatives() {
        Map<ResourceLocation, Item> map = new HashMap<>();
        for (Resource resource : getRepresentativeResources("item.json")) {
            try (Reader reader = resource.openAsReader()) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                if (json.has("replace") && json.get("replace").getAsBoolean()) {
                    map.clear();
                }
                for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    if (entry.getKey().equals("replace")) continue;
                    ResourceLocation tagId = ResourceLocation.tryParse(entry.getKey());
                    if (tagId == null) continue;
                    for (String candidate : parseCandidates(entry.getValue())) {
                        ResourceLocation itemId = ResourceLocation.tryParse(candidate);
                        if (itemId == null) continue;
                        Item item = BuiltInRegistries.ITEM.get(itemId);
                        if (item != Items.AIR) {
                            map.put(tagId, item);
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return map;
    }

    private static Map<ResourceLocation, ItemStack> loadEntityTagRepresentatives() {
        Map<ResourceLocation, ItemStack> map = new HashMap<>();
        for (Resource resource : getRepresentativeResources("entity.json")) {
            try (Reader reader = resource.openAsReader()) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                if (json.has("replace") && json.get("replace").getAsBoolean()) {
                    map.clear();
                }
                for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    if (entry.getKey().equals("replace")) continue;
                    ResourceLocation tagId = ResourceLocation.tryParse(entry.getKey());
                    if (tagId == null) continue;
                    for (String candidate : parseCandidates(entry.getValue())) {
                        ItemStack icon = resolveEntityRepresentative(candidate);
                        if (icon != null) {
                            map.put(tagId, icon);
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return map;
    }

    private static List<Resource> getRepresentativeResources(String fileName) {
        List<Resource> resources = new ArrayList<>();
        Minecraft minecraft = Minecraft.getInstance();
        List<String> namespaces = new ArrayList<>(minecraft.getResourceManager().getNamespaces());
        namespaces.sort(Comparator.naturalOrder());
        for (String namespace : namespaces) {
            ResourceLocation location = ResourceLocation.fromNamespaceAndPath(namespace, "representatives/" + fileName);
            resources.addAll(minecraft.getResourceManager().getResourceStack(location));
        }
        return resources;
    }

    @Nullable
    private static ItemStack resolveEntityRepresentative(String candidate) {
        ResourceLocation id = ResourceLocation.tryParse(candidate);
        if (id == null) return null;
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(id);
        SpawnEggItem egg = SpawnEggItem.byId(entityType);
        if (egg != null) return new ItemStack(egg);
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item != Items.AIR) return new ItemStack(item);
        return null;
    }

    private static List<String> parseCandidates(JsonElement element) {
        List<String> candidates = new ArrayList<>();
        if (element.isJsonArray()) {
            for (JsonElement e : element.getAsJsonArray()) {
                if (e.isJsonPrimitive()) {
                    candidates.add(e.getAsString());
                }
            }
        } else if (element.isJsonPrimitive()) {
            for (String part : element.getAsString().split(";")) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    candidates.add(trimmed);
                }
            }
        }
        return candidates;
    }

    private boolean isOverGrid(int mouseX, int mouseY) {
        int relX = mouseX - (leftPos + GRID_X + GRID_PAD);
        int relY = mouseY - (topPos + GRID_Y + GRID_PAD);
        if (relX < 0 || relY < 0) return false;
        return relX / SLOT_W < COLS && relY / SLOT_H < ROWS;
    }

    private int getHoveredIndex(int mouseX, int mouseY) {
        int relX = mouseX - (leftPos + GRID_X + GRID_PAD);
        int relY = mouseY - (topPos + GRID_Y + GRID_PAD);
        if (relX < 0 || relY < 0) return -1;
        int col = relX / SLOT_W;
        int row = relY / SLOT_H;
        if (col >= COLS || row >= ROWS) return -1;
        int dx = relX - col * SLOT_W;
        int dy = relY - row * SLOT_H;
        if (dx < -1 || dx >= 17 || dy < -1 || dy >= 17) return -1;
        int index = currentPage * COLS * ROWS + row * COLS + col;
        return (index >= 0 && index < entries.size()) ? index : -1;
    }
}
