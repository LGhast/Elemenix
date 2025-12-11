package net.lghast.elemenix.client.screen;

import net.lghast.elemenix.common.content.block.EjectorBlock;
import net.lghast.elemenix.common.content.blockentity.EjectorBlockEntity;
import net.lghast.elemenix.common.system.menu.EjectorMenu;
import net.lghast.elemenix.utils.Elemenix;
import net.lghast.elemenix.utils.ModUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class EjectorScreen extends AbstractContainerScreen<EjectorMenu> {
    private static final int GUI_WIDTH = 200;
    private static final int GUI_HEIGHT = 176;
    private static final int TITLE_X = 6;
    private static final int TITLE_Y = 6;

    private final ResourceLocation guiTexture = ResourceLocation.fromNamespaceAndPath("elemenix", "textures/gui/elemenic_ejector.png");

    public EjectorScreen(EjectorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
        this.inventoryLabelY = 10000;
        this.titleLabelY = TITLE_Y;
        this.titleLabelX = TITLE_X;
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
        EjectorBlockEntity blockEntity = menu.getBlockEntity();
        if (blockEntity == null) return;

        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof EjectorBlock)) return;

        int[] storage = menu.getStorage();
        if(storage == null || storage.length != 6) return;

        for(int i = 0; i<=2; i++){
            String text = ModUtils.formatNumber(storage[i]);
            graphics.drawString(this.font, text, x + 28, y + 46 + i * 14, Elemenix.values()[i].getColor(), false);
        }
        for(int i = 3; i<=5; i++){
            String text = ModUtils.formatNumber(storage[i]);
            graphics.drawString(this.font, text, x + 110, y + 46 + (i-3) * 14, Elemenix.values()[i].getColor(), false);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
