package net.lghast.elemenix.client.screen;

import net.lghast.elemenix.common.content.block.GerminalAcceleratorBlock;
import net.lghast.elemenix.common.content.block.TransformerBlock;
import net.lghast.elemenix.common.content.blockentity.TransformerBlockEntity;
import net.lghast.elemenix.common.system.menu.TransformerMenu;
import net.lghast.elemenix.common.system.recipe.MineralizingRecipe;
import net.lghast.elemenix.network.transformer.TransformerSwitchPagePayload;
import net.lghast.elemenix.utils.ModUtils;
import net.lghast.elemenix.utils.elemenix.Elemenix;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class TransformerScreen extends AbstractContainerScreen<TransformerMenu> {
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 170;
    private static final int TITLE_X = 6;
    private static final int TITLE_Y = 6;

    private static final int FLUMIX_VALUE_X = 62;
    private static final int FLUMIX_VALUE_Y = 21;

    private static final int MINERAL_LABEL_X = 26;
    private static final int MEDIUM_LABEL_X = 134;
    private static final int SLOT_LABEL_Y = 36;

    private static final int TEMPLATE_LABEL_X = 54;
    private static final int TEMPLATE_LABEL_Y = 19;
    private static final float TEMPLATE_LABEL_SCALE = 0.8F;
    private static final int GERM_FLUMIX_X = 90;
    private static final int GERM_FLUMIX_Y = 29;
    private static final int GERM_TERRIX_X = 90;
    private static final int GERM_TERRIX_Y = 39;
    private static final int PROGRESS_X1 = 49;
    private static final int PROGRESS_X2 = 155;
    private static final int PROGRESS_Y = 56;

    private static final ResourceLocation MINERALIZING_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("elemenix", "textures/gui/geological_simulator_mineralizing.png");

    private final ResourceLocation guiTexture;

    private ModeSwitchButton modeButton;

    public TransformerScreen(TransformerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
        this.inventoryLabelY = 10000;
        this.titleLabelY = TITLE_Y;
        this.titleLabelX = TITLE_X;
        this.guiTexture = menu.getGuiTexture();
    }

    @Override
    protected void init() {
        super.init();

        if (menu.getPageCount() >= 2) {
            modeButton = new ModeSwitchButton(leftPos + GUI_WIDTH - 20, topPos + 4, button -> {
                int next = (menu.getMode() + 1) % menu.getPageCount();
                PacketDistributor.sendToServer(new TransformerSwitchPagePayload(next));
            });
            this.addRenderableWidget(modeButton);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        if (menu.getMode() == TransformerBlockEntity.MODE_MINERALIZING) {
            TransformerBlock block = menu.getBlockEntity().getTransformerBlock();
            ResourceLocation secondTexture = block != null ? block.getSecondaryGuiTexture() : null;
            if (secondTexture == null) secondTexture = MINERALIZING_TEXTURE;

            graphics.blit(secondTexture, x, y, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

            if (block instanceof GerminalAcceleratorBlock) {
                renderGerminatingPage(graphics, x, y);
            } else {
                renderFlumix(graphics, x, y);
                renderMineralizingLabels(graphics, x, y);
            }
        } else {
            graphics.blit(guiTexture, x, y, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
            renderElemenixValues(graphics, x, y);
        }
    }

    private void renderGerminatingPage(GuiGraphics graphics, int x, int y) {
        drawScaledCenteredLabel(graphics,
                Component.translatable("gui.elemenix.germinating.template"),
                x + TEMPLATE_LABEL_X, y + TEMPLATE_LABEL_Y);

        int[] cost = menu.getGerminatingCost();
        renderGerminatingElemenix(graphics, x + GERM_FLUMIX_X, y + GERM_FLUMIX_Y, menu.getInputB(), cost, 1, Elemenix.FLUMIX);
        renderGerminatingElemenix(graphics, x + GERM_TERRIX_X, y + GERM_TERRIX_Y, menu.getInputA(), cost, 0, Elemenix.TERRIX);

        int organixColor = Elemenix.ORGANIX.getColor();
        graphics.fill(x + PROGRESS_X1, y + PROGRESS_Y, x + PROGRESS_X2, y + PROGRESS_Y + 3,
                darkenColor(organixColor, 0.35F));
        int progress = menu.getGerminatingProgress();
        if (progress > 0) {
            int interval = getGerminationInterval();
            int width = (int) Math.min(PROGRESS_X2 - PROGRESS_X1,
                   1 + (long) (PROGRESS_X2 - PROGRESS_X1) * progress / Math.max(1, interval));
            graphics.fill(x + PROGRESS_X1, y + PROGRESS_Y, x + PROGRESS_X1 + width, y + PROGRESS_Y + 3,
                    organixColor);
        }
    }

    private int getGerminationInterval() {
        TransformerBlock block = menu.getBlockEntity().getTransformerBlock();
        return block instanceof GerminalAcceleratorBlock germinal ? germinal.getGerminationInterval() : 100;
    }

    private void renderGerminatingElemenix(GuiGraphics graphics, int textX, int textY,
                                           long reserve, @Nullable int[] cost, int costIndex, Elemenix elemenix) {
        String valueText = ModUtils.formatNumber(reserve);
        graphics.drawString(this.font, valueText, textX, textY, elemenix.getColor(), false);
        if (cost == null || costIndex < 0 || costIndex >= cost.length) {
            return;
        }
        int deduction = cost[costIndex];
        if (deduction > 0) {
            String preview = ModUtils.formatNumber(deduction, "-%s");
            graphics.drawString(this.font, preview, textX + this.font.width(valueText) + 4, textY, 0xFF3333, false);
        }
    }

    private void renderFlumix(GuiGraphics graphics, int x, int y) {
        String valueText = ModUtils.formatNumber(menu.getInputB());
        graphics.drawString(this.font, valueText, x + FLUMIX_VALUE_X, y + FLUMIX_VALUE_Y,
                Elemenix.FLUMIX.getColor(), false);
        MineralizingRecipe recipe = menu.getCurrentRecipe();
        if (recipe == null) return;
        ItemStack mineral = menu.getBlockEntity().getItem(TransformerMenu.MINERAL_SLOT);
        ItemStack medium = menu.getBlockEntity().getItem(TransformerMenu.MEDIUM_SLOT);
        int cost = recipe.getFlumixCost(mineral);
        if (cost <= 0) return;
        int possibleCrafts = Math.min(mineral.getCount(), medium.getCount());
        long totalCost = (long) cost * possibleCrafts;
        if (totalCost > 0) {
            String previewText = ModUtils.formatNumber(totalCost, "-%s");
            graphics.drawString(this.font, previewText,
                    x + FLUMIX_VALUE_X + this.font.width(valueText) + 4,
                    y + FLUMIX_VALUE_Y, 0xFF3333, false);
        }
    }

    private void renderMineralizingLabels(GuiGraphics graphics, int x, int y) {
        drawCenteredLabel(graphics,
                Component.translatable("gui.elemenix.mineralizing.mineral"),
                x + MINERAL_LABEL_X + 8, y + SLOT_LABEL_Y);
        drawCenteredLabel(graphics,
                Component.translatable("gui.elemenix.mineralizing.medium"),
                x + MEDIUM_LABEL_X + 8, y + SLOT_LABEL_Y);
    }

    private void drawCenteredLabel(GuiGraphics graphics, Component label, int centerX, int textY) {
        String text = label.getString();
        int textWidth = this.font.width(text);
        graphics.drawString(this.font, text, centerX - textWidth / 2, textY, 0x404040, false);
    }

    private void drawScaledCenteredLabel(GuiGraphics graphics, Component label, int centerX, int textY) {
        String text = label.getString();
        int textWidth = this.font.width(text);
        float scaledWidth = textWidth * TransformerScreen.TEMPLATE_LABEL_SCALE;
        graphics.pose().pushPose();
        graphics.pose().translate(centerX - scaledWidth / 2F, textY, 0F);
        graphics.pose().scale(TransformerScreen.TEMPLATE_LABEL_SCALE, TransformerScreen.TEMPLATE_LABEL_SCALE, 1F);
        graphics.drawString(this.font, text, 0, 0, 0x404040, false);
        graphics.pose().popPose();
    }

    private static int darkenColor(int color, float factor) {
        int r = (int) ((color >> 16 & 0xFF) * factor);
        int g = (int) ((color >> 8 & 0xFF) * factor);
        int b = (int) ((color & 0xFF) * factor);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private void renderElemenixValues(GuiGraphics graphics, int x, int y) {
        TransformerBlockEntity blockEntity = menu.getBlockEntity();
        if (blockEntity == null) return;

        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof TransformerBlock block)) return;

        long inputA = menu.getInputA();
        long inputB = menu.getInputB();
        long outputC = menu.getOutputC();

        String textA = ModUtils.formatNumber(inputA);
        graphics.drawString(this.font, textA, x + 77, y + 26, block.getInputElemenixA().getColor(), false);

        String textB = ModUtils.formatNumber(inputB);
        graphics.drawString(this.font, textB, x + 77, y + 44, block.getInputElemenixB().getColor(), false);

        String textC = ModUtils.formatNumber(outputC);
        graphics.drawString(this.font, textC, x + 77, y + 68, block.getOutputElemenix().getColor(), false);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int titleWidth = this.font.width(this.title);
        int centeredX = (this.imageWidth - titleWidth) / 2;
        graphics.drawString(this.font, this.title, centeredX, TITLE_Y, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);

        if (modeButton != null && modeButton.isHovered()) {
            Component tip;
            if (menu.isGerminalPage()) {
                tip = menu.getMode() == TransformerBlockEntity.MODE_MINERALIZING
                        ? Component.translatable("gui.elemenix.mode_switch.to_transforming")
                        : Component.translatable("gui.elemenix.mode_switch.to_germinating");
            } else {
                tip = menu.getMode() == TransformerBlockEntity.MODE_MINERALIZING
                        ? Component.translatable("gui.elemenix.mode_switch.to_transforming")
                        : Component.translatable("gui.elemenix.mode_switch.to_mineralizing");
            }
            graphics.renderTooltip(this.font, tip, mouseX, mouseY);
        }

        this.renderTooltip(graphics, mouseX, mouseY);
    }

    private static class ModeSwitchButton extends Button {
        private static final ResourceLocation BUTTON_TEXTURE =
                ResourceLocation.fromNamespaceAndPath("elemenix", "textures/gui/menu_transferring_button.png");

        private ModeSwitchButton(int x, int y, OnPress onPress) {
            super(x, y, 16, 16, Component.empty(), onPress, Button.DEFAULT_NARRATION);
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.blit(BUTTON_TEXTURE, getX(), getY(), 0, 0.0f, 0.0f, 16, 16, 16, 16);
            if (isHovered()) {
                graphics.fill(getX(), getY(), getX() + 16, getY() + 16, 0x22FFFFFF);
            }
        }
    }
}
