package net.lghast.elemenix.core;

import net.lghast.elemenix.client.Palette;
import net.minecraft.network.chat.Component;

public enum Elemenix {
    ORGANIX("elemenix.elemenix.organix", Palette.ORGANIX, 0),
    TERRIX("elemenix.elemenix.terrix", Palette.TERRIX, 1),
    FLUMIX("elemenix.elemenix.flumix", Palette.FLUMIX, 2),
    METALLIX("elemenix.elemenix.metallix", Palette.METALLIX, 3),
    ENERGIX("elemenix.elemenix.energix", Palette.ENERGIX, 4),
    ARCANIX("elemenix.elemenix.arcanix", Palette.ARCANIX, 5);

    private final String name;
    private final int color;
    private final int index;

    Elemenix(String name, int color, int index) {
        this.name = name;
        this.color = color;
        this.index = index;
    }

    public Component getComponent() {
        return Component.translatable(name);
    }

    public String getName() {
        return getComponent().getString();
    }

    public int getColor() {
        return color;
    }

    public int getIndex(){
        return index;
    }
}
