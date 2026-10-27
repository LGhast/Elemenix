package net.lghast.elemenix.client.screen;

import net.lghast.elemenix.common.system.menu.TankMenu;
import net.lghast.elemenix.utils.misc.LongContainerData;
import net.lghast.elemenix.utils.misc.ModUtils;
import net.lghast.elemenix.utils.elemenix.Elemenix;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class TankScreen extends AbstractContainerScreen<TankMenu> {
    private static final int GUI_WIDTH = 110;
    private static final int GUI_HEIGHT = 110;

    private final ResourceLocation guiTexture = ResourceLocation.fromNamespaceAndPath("elemenix", "textures/gui/elemenic_tank.png");

    public TankScreen(TankMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
        this.inventoryLabelY = 10000;
        this.titleLabelY = 6;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(guiTexture, x, y, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
        renderElemenixValues(graphics, x, y);
    }

    private void renderElemenixValues(GuiGraphics graphics, int x, int y) {
        LongContainerData data = menu.getData();
        if (data == null) return;

        for (int i = 0; i < 6; i++) {
            graphics.drawString(this.font, ModUtils.formatNumber(data.getLong(i)), x + 25, y + 19 + i * 14, Elemenix.values()[i].getColor(), false);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
