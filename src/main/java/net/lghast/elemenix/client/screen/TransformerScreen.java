package net.lghast.elemenix.client.screen;

import net.lghast.elemenix.common.content.block.TransformerBlock;
import net.lghast.elemenix.common.content.blockentity.TransformerBlockEntity;
import net.lghast.elemenix.common.system.menu.TransformerMenu;
import net.lghast.elemenix.utils.ModUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TransformerScreen extends AbstractContainerScreen<TransformerMenu> {
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 170;
    private static final int TITLE_X = 6;
    private static final int TITLE_Y = 6;

    private final ResourceLocation guiTexture;

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
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        graphics.blit(guiTexture, x, y, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
        renderElemenixValues(graphics, x, y);
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
        graphics.drawString(this.font, textA, x + 77, y + 26, block.getInputTypeA().getColor(), false);

        String textB = ModUtils.formatNumber(inputB);
        graphics.drawString(this.font, textB, x + 77, y + 44, block.getInputTypeB().getColor(), false);

        String textC = ModUtils.formatNumber(outputC);
        graphics.drawString(this.font, textC, x + 77, y + 68, block.getOutputType().getColor(), false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
