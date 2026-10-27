package net.lghast.elemenix.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class OutletSlider extends AbstractWidget {
    private static final ResourceLocation SLIDER_TEXTURE = ResourceLocation.fromNamespaceAndPath("elemenix", "textures/gui/slider.png");
    private static final int HANDLE_WIDTH = 4;
    private static final int HANDLE_HEIGHT = 7;

    private final int trackStart;
    private final int trackEnd;
    private final IntConsumer onChange;
    private final IntSupplier percentSupplier;

    public OutletSlider(int trackStartX, int centerY, int trackEndX, IntSupplier percentSupplier, IntConsumer onChange) {
        super(trackStartX - HANDLE_WIDTH / 2, centerY - HANDLE_HEIGHT / 2, trackEndX - trackStartX + HANDLE_WIDTH, HANDLE_HEIGHT, Component.empty());
        this.trackStart = trackStartX;
        this.trackEnd = trackEndX;
        this.percentSupplier = percentSupplier;
        this.onChange = onChange;
    }

    private int getPercent() {
        return Math.max(0, Math.min(100, percentSupplier.getAsInt()));
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int percent = getPercent();
        int centerX = trackStart + (trackEnd - trackStart) * percent / 100;
        graphics.blit(SLIDER_TEXTURE, centerX - HANDLE_WIDTH / 2, this.getY(), 0, 0, HANDLE_WIDTH, HANDLE_HEIGHT, HANDLE_WIDTH, HANDLE_HEIGHT);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        updateFromMouse(mouseX);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        updateFromMouse(mouseX);
    }

    private void updateFromMouse(double mouseX) {
        int newPercent = (int) Math.round((mouseX - trackStart) * 100.0 / Math.max(1, trackEnd - trackStart));
        newPercent = Math.max(0, Math.min(100, newPercent));
        if (newPercent != getPercent()) {
            onChange.accept(newPercent);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, Component.literal("Output " + getPercent() + "%"));
    }
}
