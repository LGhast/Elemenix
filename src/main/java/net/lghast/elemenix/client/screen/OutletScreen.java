package net.lghast.elemenix.client.screen;

import net.lghast.elemenix.common.system.menu.OutletMenu;
import net.lghast.elemenix.config.CommonConfig;
import net.lghast.elemenix.network.outlet.OutletPercentPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class OutletScreen extends AbstractContainerScreen<OutletMenu> {
    private static final int GUI_WIDTH = 110;
    private static final int GUI_HEIGHT = 50;
    private static final int SLIDER_TRACK_START = 14;
    private static final int SLIDER_TRACK_END = 97;
    private static final int SLIDER_CENTER_Y = 26;

    private final ResourceLocation guiTexture = ResourceLocation.fromNamespaceAndPath("elemenix", "textures/gui/elemenic_outlet.png");

    public OutletScreen(OutletMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
        this.inventoryLabelY = 10000;
        this.titleLabelY = 6;
    }

    @Override
    protected void init() {
        super.init();

        int guiX = (this.width - this.imageWidth) / 2;
        int guiY = (this.height - this.imageHeight) / 2;

        this.addRenderableWidget(new OutletSlider(
                guiX + SLIDER_TRACK_START,
                guiY + SLIDER_CENTER_Y,
                guiX + SLIDER_TRACK_END,
                menu::getOutputPercent,
                percent -> {
                    menu.setOutputPercent(percent);
                    if (this.minecraft != null) {
                        Objects.requireNonNull(this.minecraft.getConnection()).send(new OutletPercentPayload(percent));
                    }
                }
        ));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(guiTexture, x, y, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.pose().pushPose();
        float titleScale = 0.8f;
        graphics.pose().scale(titleScale, titleScale, 1.0f);
        int titleX = Math.round(this.imageWidth / 2f / titleScale - this.font.width(this.title) / 2f);
        int titleY = Math.round(this.titleLabelY / titleScale);
        graphics.drawString(this.font, this.title, titleX, titleY, 4210752, false);
        graphics.pose().popPose();
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.getFocused() instanceof OutletSlider slider) {
            slider.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        int percent = menu.getOutputPercent();
        long maxOutput = (long) CommonConfig.OUTLET_MAX_OUTPUT_PER_TYPE.get() * percent / 100;

        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0);
        float valueScale = 0.6f;
        graphics.pose().scale(valueScale, valueScale, 1.0f);
        int centerX = Math.round(this.imageWidth / 2f / valueScale);
        String percentText = percent + "%";
        String maxText = String.valueOf(maxOutput);
        int maxTextScaledX = Math.round(centerX - this.font.width(maxText) / 2f);
        int maxTextScaledY = Math.round(31f / valueScale);
        graphics.drawString(this.font, percentText, Math.round(centerX - this.font.width(percentText) / 2f), Math.round(18f / valueScale), 0xFFFFFF, false);
        graphics.drawString(this.font, maxText, maxTextScaledX, maxTextScaledY, 0xFFFFFF, false);
        graphics.pose().popPose();

        int valueX = x + Math.round(maxTextScaledX * valueScale);
        int valueY = y + Math.round(maxTextScaledY * valueScale);
        int valueW = Math.round(this.font.width(maxText) * valueScale);
        int valueH = Math.round(this.font.lineHeight * valueScale);
        if (mouseX >= valueX && mouseX < valueX + valueW && mouseY >= valueY && mouseY < valueY + valueH) {
            graphics.renderTooltip(this.font, Component.translatable("gui.elemenix.outlet_max_output", maxOutput), mouseX, mouseY);
        }

        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
