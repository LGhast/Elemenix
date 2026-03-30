package net.lghast.elemenix.utils.elemenix;

import net.minecraft.network.chat.Component;

public enum Elemenix {
    ORGANIX("elemenix.elemenix.organix", 0xFF12c414, 0),
    TERRIX("elemenix.elemenix.terrix", 0xFFc44a12, 1),
    FLUMIX("elemenix.elemenix.flumix", 0xFF2a86e2, 2),
    METALLIX("elemenix.elemenix.metallix", 0xFF7c8a9e, 3),
    ENERGIX("elemenix.elemenix.energix", 0xFFfd9942, 4),
    ARCANIX("elemenix.elemenix.arcanix", 0xFFc146eb, 5);

    private final String name;
    private final int color;
    private final int index;

    Elemenix(String name, int color, int index) {
        this.name = name;
        this.color = color;
        this.index = index;
    }

    public String getName() {
        return Component.translatable(name).getString();
    }

    public int getColor() {
        return color;
    }

    public int getIndex(){
        return index;
    }
}
