package net.lghast.elemenix.client.misc;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-side helpers for fitting text into a fixed pixel width.
 */
@OnlyIn(Dist.CLIENT)
@SuppressWarnings("SameParameterValue")
public final class ScaledText {
    private static final String ELLIPSIS = "...";

    private ScaledText() {}

    /**
     * Result of fitting a string into maxWidth.
     *
     * @param text      text to draw
     * @param fullText  original text, used for the tooltip when truncated
     * @param scale     scale to draw at
     * @param truncated whether text was truncated and fullText differs
     * @param bold      whether text is measured and drawn bold
     */
    public record Fit(String text, String fullText, float scale, boolean truncated, boolean bold) {
        public static Fit of(Font font, String text, int maxWidth, float minScale) {
            return of(font, text, maxWidth, minScale, false);
        }

        /**
         * Scales down to fit, but never below minScale; when even minScale overflows the text is truncated.
         * minScale 0 disables truncation (scale to fit only), minScale 1 disables scaling (truncate only).
         */
        public static Fit of(Font font, String text, int maxWidth, float minScale, boolean bold) {
            int width = measure(font, text, bold);
            if (width <= maxWidth) {
                return new Fit(text, text, 1.0f, false, bold);
            }
            float scale = (float) maxWidth / width;
            if (scale >= minScale) {
                return new Fit(text, text, scale, false, bold);
            }
            String truncated = truncate(font, text, (int) (maxWidth / minScale), bold);
            return new Fit(truncated, text, minScale, true, bold);
        }

        /**
         * Truncates text to fit at a fixed scale, without recomputing the scale.
         */
        public static Fit atScale(Font font, String text, int maxWidth, float scale, boolean bold) {
            int width = measure(font, text, bold);
            if (width * scale <= maxWidth) {
                return new Fit(text, text, scale, false, bold);
            }
            String truncated = truncate(font, text, (int) (maxWidth / scale), bold);
            return new Fit(truncated, text, scale, true, bold);
        }

        /** A fixed fit with no truncation: text is drawn at scale unchanged. */
        public static Fit fixed(String text, float scale) {
            return new Fit(text, text, scale, false, false);
        }
    }

    /**
     * One wrapped line with the scale it is drawn at.
     */
    public record Line(String text, float scale) {}

    /** Draws the fitted text left-aligned at (x, y). verticallyCentered centers the text in its line height. */
    public static void render(GuiGraphics graphics, Font font, Fit fit, float x, float y, int color, boolean verticallyCentered) {
        float verticalShift = verticallyCentered ? (font.lineHeight * (1.0f - fit.scale())) / 2.0f : 0.0f;
        graphics.pose().pushPose();
        graphics.pose().translate(x, y + verticalShift, 0.0f);
        graphics.pose().scale(fit.scale(), fit.scale(), 1.0f);
        drawString(graphics, font, fit.text(), 0, 0, color, fit.bold());
        graphics.pose().popPose();
    }

    /** Draws the fitted text centered on centerX at y. */
    public static void renderCentered(GuiGraphics graphics, Font font, Fit fit, int centerX, int y, int color, boolean verticallyCentered) {
        renderCentered(graphics, font, fit.text(), fit.scale(), centerX, y, color, fit.bold(), verticallyCentered);
    }

    /** Draws a raw string centered on centerX at y with the given scale. */
    public static void renderCentered(GuiGraphics graphics, Font font, String text, float scale, int centerX, int y, int color, boolean verticallyCentered) {
        renderCentered(graphics, font, text, scale, centerX, y, color, false, verticallyCentered);
    }

    private static void renderCentered(GuiGraphics graphics, Font font, String text, float scale, int centerX, int y, int color, boolean bold, boolean verticallyCentered) {
        int scaledWidth = (int) (measure(font, text, bold) * scale);
        float verticalShift = verticallyCentered ? (font.lineHeight * (1.0f - scale)) / 2.0f : 0.0f;
        graphics.pose().pushPose();
        graphics.pose().translate(centerX - scaledWidth / 2.0f, y + verticalShift, 0.0f);
        graphics.pose().scale(scale, scale, 1.0f);
        drawString(graphics, font, text, 0, 0, color, bold);
        graphics.pose().popPose();
    }

    /** Renders a tooltip with the full text when the fit was truncated and the mouse is over it. */
    public static void renderTooltip(GuiGraphics graphics, Font font, Fit fit, int x, int y, int mouseX, int mouseY) {
        if (!fit.truncated()) return;
        int width = (int) (measure(font, fit.text(), fit.bold()) * fit.scale());
        int height = (int) (font.lineHeight * fit.scale());
        if (mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height) {
            graphics.renderTooltip(font, Component.literal(fit.fullText()), mouseX, mouseY);
        }
    }

    /**
     * Shrinks text to fit maxWidth, but never below minScale; when even minScale overflows, wraps into
     * lines at word boundaries, each scaled to fit. Returns at least one line.
     */
    public static List<Line> wrapToFit(Font font, String text, int maxWidth, float minScale) {
        Fit fit = Fit.of(font, text, maxWidth, minScale);
        if (!fit.truncated()) {
            return List.of(new Line(fit.text(), fit.scale()));
        }
        List<String> lines = wrap(font, text, (int) (maxWidth / minScale));
        List<Line> result = new ArrayList<>(lines.size());
        for (String line : lines) {
            result.add(new Line(line, Math.min(1.0f, (float) maxWidth / font.width(line))));
        }
        return result;
    }

    /**
     * Wraps text into lines no wider than maxWidth, breaking at spaces.
     * A word wider than maxWidth stays on its own line; text without spaces returns a single line.
     */
    public static List<String> wrap(Font font, String text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return List.of(text);
        }
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.trim().split("\\s+")) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (!line.isEmpty() && font.width(candidate) > maxWidth) {
                lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(candidate);
            }
        }
        if (!line.isEmpty()) {
            lines.add(line.toString());
        }
        return lines;
    }

    private static int measure(Font font, String text, boolean bold) {
        return bold ? font.width(Component.literal(text).withStyle(ChatFormatting.BOLD)) : font.width(text);
    }

    private static String truncate(Font font, String text, int maxWidth, boolean bold) {
        if (measure(font, text, bold) <= maxWidth) {
            return text;
        }
        int targetWidth = maxWidth - measure(font, ELLIPSIS, bold);
        if (targetWidth <= 0) {
            return ELLIPSIS;
        }
        int low = 0, high = text.length(), best = 0;
        while (low <= high) {
            int mid = (low + high) / 2;
            if (measure(font, text.substring(0, mid), bold) <= targetWidth) {
                best = mid;
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        if (best == 0) best = 1;
        return text.substring(0, best).trim() + ELLIPSIS;
    }

    private static void drawString(GuiGraphics graphics, Font font, String text, int x, int y, int color, boolean bold) {
        if (bold) {
            graphics.drawString(font, Component.literal(text).withStyle(ChatFormatting.BOLD), x, y, color, false);
        } else {
            graphics.drawString(font, text, x, y, color, false);
        }
    }
}
