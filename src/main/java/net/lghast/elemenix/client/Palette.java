package net.lghast.elemenix.client;

/** Central palette of named colors used across the client UI. */
public final class Palette {
    private Palette() {}

    public static final int ORGANIX = 0xFF12C414;
    public static final int TERRIX = 0xFFC44A12;
    public static final int FLUMIX = 0xFF2A86E2;
    public static final int METALLIX = 0xFF7C8A9E;
    public static final int ENERGIX = 0xFFFD9942;
    public static final int ARCANIX = 0xFFC146EB;

    public static final int WHITE = 0xFFFFFFFF;
    public static final int BLACK = 0xFF000000;
    public static final int GRAY_LIGHT = 0xFFA6A6A6;
    public static final int GRAY_DARK = 0xFF555555;
    public static final int YELLOW = 0xFFFFFF00;
    public static final int GREEN_LIME = 0xFFB5E45A;
    public static final int GREEN_JADE = 0xFF26AF4A;
    public static final int AMBER = 0xFFFFAA00;
    public static final int MINT = 0xFFA1FBE8;
    public static final int MAGENTA = 0xFFF64CF6;

    public static final int SUCCESS = 0xFF55FF55;
    public static final int SUCCESS_BASE = 0x4000FF00;
    public static final int FAILURE = 0xFFFF5555;
    public static final int WARN = 0xFFFF3333;

    public static final int ANALYSIS = 0xFFD5FFF6;
    public static final int TRANSFER = 0xFF4AF3FD;

    public static final int HIGHLIGHT = 0x50FFFFFF;
    public static final int HIGHLIGHT_SOFT = 0x30FFFFFF;

    public static final int TEXT_COMMON = 0xFF404040;
    public static final int TEXT_HOVER = 0xFF808080;
    public static final int TEXT_BODY = 0xFFE0E0E0;
    public static final int TEXT_SEARCH = 0xFF707070;

    public static final int SCROLL_BASE_PALE = 0x48000000;
    public static final int SCROLL_BASE_DARK = 0x99000000;
    public static final int SCROLL_BAR_PALE = 0xFF8A8A8A;
    public static final int SCROLL_BAR_DARK = 0xB3303030;

    public static final int FRINGE_BRIGHT = 0x11FFFFFF;
    public static final int FRINGE_DARK = 0x22000000;
    public static final int FRINGE_BRIGHT_BLUE = 0xFF889494;
    public static final int FRINGE_DARK_BLUE = 0xFF708081;
    public static final int FRINGE_BRIGHT_BROWN = 0xFF9B7860;
    public static final int FRINGE_DARK_BROWN = 0xFF8F6546;

    public static final int CHECK_SELECTED = 0xFF57A64A;
    public static final int CHECK_LOCKED = 0xFF3C3C3C;
    public static final int CHECK_DISABLED_LOCKED = 0xFF5F5F5F;

    public static int darken(int argb, float factor) {
        int r = (int) ((argb >> 16 & 0xFF) * factor);
        int g = (int) ((argb >> 8 & 0xFF) * factor);
        int b = (int) ((argb & 0xFF) * factor);
        return (argb & 0xFF000000) | (r << 16) | (g << 8) | b;
    }
}
