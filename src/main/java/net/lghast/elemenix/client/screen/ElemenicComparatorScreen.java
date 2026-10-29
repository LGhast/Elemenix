package net.lghast.elemenix.client.screen;

import net.lghast.elemenix.common.content.blockentity.ElemenicComparatorBlockEntity;
import net.lghast.elemenix.common.system.menu.ElemenicComparatorMenu;
import net.lghast.elemenix.network.comparator.ComparatorUpdatePayload;
import net.lghast.elemenix.utils.elemenix.Elemenix;
import net.lghast.elemenix.utils.misc.ModUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class ElemenicComparatorScreen extends AbstractContainerScreen<ElemenicComparatorMenu> {
    private static final int GUI_WIDTH = 151;
    private static final int GUI_HEIGHT = 128;
    private static final int[] ENTRY_Y = {42, 63, 84, 105};
    private static final int LEFT_BTN_X = 50;
    private static final int RIGHT_BTN_X = 129;
    private static final int CONTENT_CENTER_X = 95;
    private static final int BTN_SIZE = 13;
    private static final int ENTRY_NAME_X = 11;
    private static final int ENTRY_NAME_WIDTH = 39;
    private static final int CONTENT_X = 62;
    private static final int CONTENT_WIDTH = 67;

    private static final String[] RULE_SYMBOLS = {">", "<", "=", "≥", "≤"};
    private static final String[] RULE_KEYS = {
            "gui.elemenix.comparator_rule_greater",
            "gui.elemenix.comparator_rule_less",
            "gui.elemenix.comparator_rule_equal",
            "gui.elemenix.comparator_rule_at_least",
            "gui.elemenix.comparator_rule_at_most"
    };
    private static final String[] UNIT_KEYS = {
            "gui.elemenix.comparator_unit_point",
            "gui.elemenix.comparator_unit_ten_thousand",
            "gui.elemenix.comparator_unit_hundred_million",
            "gui.elemenix.comparator_unit_trillion"
    };
    private static final long[] UNIT_MULTIPLIERS = {1L, 10_000L, 100_000_000L, 1_000_000_000_000L};

    private final ResourceLocation guiTexture =
            ResourceLocation.fromNamespaceAndPath("elemenix", "textures/gui/elemenic_comparator.png");

    private int target;
    private int rule;
    private int threshold;
    private int unit;
    private EditBox thresholdField;

    public ElemenicComparatorScreen(ElemenicComparatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
        this.inventoryLabelY = 10000;
    }

    @Override
    protected void init() {
        super.init();
        int guiX = (this.width - this.imageWidth) / 2;
        int guiY = (this.height - this.imageHeight) / 2;

        this.target = menu.getTarget();
        this.rule = menu.getRule();
        this.threshold = menu.getThreshold();
        this.unit = menu.getUnit();

        addEntryButtons(guiX, guiY, 0, () -> target, v -> target = v, 7);
        addEntryButtons(guiX, guiY, 1, () -> rule, v -> rule = v, 4);
        addEntryButtons(guiX, guiY, 3, () -> unit, v -> unit = v, 3);

        int row = 2;
        this.addRenderableWidget(Button.builder(Component.literal("<"), b -> {
            threshold = Mth.clamp(threshold - 1000, 0, ElemenicComparatorBlockEntity.MAX_THRESHOLD);
            syncThresholdField();
            sendUpdate();
        }).bounds(guiX + LEFT_BTN_X - 1, guiY + ENTRY_Y[row] - 3, BTN_SIZE, BTN_SIZE).build());

        this.addRenderableWidget(Button.builder(Component.literal(">"), b -> {
            threshold = Mth.clamp(threshold + 1000, 0, ElemenicComparatorBlockEntity.MAX_THRESHOLD);
            syncThresholdField();
            sendUpdate();
        }).bounds(guiX + RIGHT_BTN_X - 1, guiY + ENTRY_Y[row] - 3, BTN_SIZE, BTN_SIZE).build());

        int fieldWidth = 60;
        int fieldX = guiX + CONTENT_X + (CONTENT_WIDTH - fieldWidth) / 2;
        this.thresholdField = new EditBox(this.font, fieldX, guiY + ENTRY_Y[row], fieldWidth, 9,
                Component.literal("threshold"));
        this.thresholdField.setMaxLength(7);
        this.thresholdField.setBordered(false);
        this.thresholdField.setValue(String.valueOf(threshold));
        this.thresholdField.setFilter(s -> s.isEmpty() || s.matches("[0-9]+"));
        this.thresholdField.setResponder(s -> {
            if (s.isEmpty()) {
                threshold = 0;
            } else {
                try {
                    threshold = Mth.clamp(Integer.parseInt(s), 0, ElemenicComparatorBlockEntity.MAX_THRESHOLD);
                } catch (NumberFormatException ignored) {
                    threshold = 0;
                }
            }
            sendUpdate();
        });
        this.addRenderableWidget(this.thresholdField);
    }

    private void addEntryButtons(int guiX, int guiY, int row, IntGetter getter, IntSetter setter, int max) {
        this.addRenderableWidget(Button.builder(Component.literal("<"), b -> {
            int cur = getter.get();
            int next = cur - 1 < 0 ? max : cur - 1;
            setter.set(next);
            sendUpdate();
        }).bounds(guiX + LEFT_BTN_X - 1, guiY + ENTRY_Y[row] - 3, BTN_SIZE, BTN_SIZE).build());

        this.addRenderableWidget(Button.builder(Component.literal(">"), b -> {
            int cur = getter.get();
            int next = cur + 1 > max ? 0 : cur + 1;
            setter.set(next);
            sendUpdate();
        }).bounds(guiX + RIGHT_BTN_X - 1, guiY + ENTRY_Y[row] - 3, BTN_SIZE, BTN_SIZE).build());
    }

    private void syncThresholdField() {
        if (thresholdField != null) {
            thresholdField.setValue(String.valueOf(threshold));
        }
    }

    private void sendUpdate() {
        if (this.minecraft != null && this.minecraft.getConnection() != null) {
            this.minecraft.getConnection().send(new ComparatorUpdatePayload(target, rule, threshold, unit));
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(guiTexture, x, y, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int titleX = (this.imageWidth - this.font.width(this.title)) / 2;
        graphics.drawString(this.font, this.title, titleX, 6, 0x404040, false);

        Component criterion = buildCriterion();
        int criterionX = (this.imageWidth - this.font.width(criterion)) / 2;
        graphics.drawString(this.font, criterion, criterionX, 21, 0xFFFFFF, false);

        String[] entryNames = {
                Component.translatable("gui.elemenix.comparator_target").getString(),
                Component.translatable("gui.elemenix.comparator_rule").getString(),
                Component.translatable("gui.elemenix.comparator_threshold").getString(),
                Component.translatable("gui.elemenix.comparator_unit").getString()
        };
        for (int i = 0; i < 4; i++) {
            int nameX = ENTRY_NAME_X + (ENTRY_NAME_WIDTH - this.font.width(entryNames[i])) / 2;
            graphics.drawString(this.font, entryNames[i], nameX, ENTRY_Y[i], 0x000000, false);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        drawCenteredEntry(graphics, x, y, 0, targetName(), targetColor());
        drawCenteredEntry(graphics, x, y, 1, ruleName(), 0xFFFFFF);
        drawCenteredEntry(graphics, x, y, 3, unitName(), 0xFFFFFF);

        this.renderTooltip(graphics, mouseX, mouseY);
    }

    private void drawCenteredEntry(GuiGraphics graphics, int x, int y, int row, String text, int color) {
        int cx = x + CONTENT_CENTER_X - this.font.width(text) / 2;
        graphics.drawString(this.font, text, cx, y + ENTRY_Y[row], color, false);
    }

    private String targetName() {
        if (target == ElemenicComparatorBlockEntity.TARGET_SUM) {
            return Component.translatable("gui.elemenix.comparator_target_sum").getString();
        }
        if (target == ElemenicComparatorBlockEntity.TARGET_ANY) {
            return Component.translatable("gui.elemenix.comparator_target_any").getString();
        }
        return Elemenix.values()[target].getName();
    }

    private int targetColor() {
        if (target == ElemenicComparatorBlockEntity.TARGET_SUM || target == ElemenicComparatorBlockEntity.TARGET_ANY) {
            return 0xFFFFFF;
        }
        return Elemenix.values()[target].getColor();
    }

    private String ruleName() {
        return Component.translatable(RULE_KEYS[rule]).getString();
    }

    private String unitName() {
        return Component.translatable(UNIT_KEYS[unit]).getString();
    }

    private Component buildCriterion() {
        String targetPart;
        if (target == ElemenicComparatorBlockEntity.TARGET_SUM) {
            targetPart = Component.translatable("gui.elemenix.comparator_criterion_sum").getString();
        } else if (target == ElemenicComparatorBlockEntity.TARGET_ANY) {
            targetPart = Component.translatable("gui.elemenix.comparator_criterion_any").getString();
        } else {
            targetPart = Elemenix.values()[target].getName();
        }

        long actualValue = (long) threshold * UNIT_MULTIPLIERS[unit];
        String valuePart = Screen.hasAltDown() ? String.valueOf(actualValue) : ModUtils.formatNumber(actualValue);

        return Component.literal(targetPart + " " + RULE_SYMBOLS[rule] + " " + valuePart);
    }

    @FunctionalInterface
    private interface IntGetter {
        int get();
    }

    @FunctionalInterface
    private interface IntSetter {
        void set(int value);
    }
}
