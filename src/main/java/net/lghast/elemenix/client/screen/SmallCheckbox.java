package net.lghast.elemenix.client.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.BiConsumer;

@OnlyIn(Dist.CLIENT)
public class SmallCheckbox extends AbstractWidget {

    private static final int BOX_SIZE = 10;
    private static final int TEXT_GAP = 4;
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final int SELECTED_COLOR = 0xFF57A64A;

    private boolean selected;
    private final Font font;
    private final BiConsumer<SmallCheckbox, Boolean> onValueChange;

    public SmallCheckbox(int x, int y, Component label, Font font, boolean selected,
                         BiConsumer<SmallCheckbox, Boolean> onValueChange) {
        super(x, y, BOX_SIZE, BOX_SIZE, label);
        this.font = font;
        this.selected = selected;
        this.onValueChange = onValueChange;
    }

    public boolean selected() {
        return selected;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.selected = !this.selected;
        if (onValueChange != null) {
            onValueChange.accept(this, this.selected);
        }
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        boolean enabled = isActive();

        graphics.fill(getX(), getY(), getX() + BOX_SIZE, getY() + BOX_SIZE, 0xFF000000);
        if (enabled) {
            graphics.fill(getX() + 1, getY() + 1, getX() + BOX_SIZE - 1, getY() + BOX_SIZE - 1, 0xFFFFFFFF);
            if (selected) {
                graphics.fill(getX() + 2, getY() + 2, getX() + BOX_SIZE - 2, getY() + BOX_SIZE - 2, SELECTED_COLOR);
            }
        } else {
            graphics.fill(getX() + 1, getY() + 1, getX() + BOX_SIZE - 1, getY() + BOX_SIZE - 1, 0xFF3C3C3C);
            if (selected) {
                graphics.fill(getX() + 2, getY() + 2, getX() + BOX_SIZE - 2, getY() + BOX_SIZE - 2, 0xFF5F5F5F);
            }
        }
        graphics.drawString(font, getMessage(),
                getX() + BOX_SIZE + TEXT_GAP,
                getY() + (BOX_SIZE - font.lineHeight) / 2 + 1,
                enabled ? TEXT_COLOR : 0xFF6F6F6F, false);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= getX() && mouseX < getX() + BOX_SIZE + TEXT_GAP + font.width(getMessage())
                && mouseY >= getY() && mouseY < getY() + BOX_SIZE;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, getMessage());
    }
}
