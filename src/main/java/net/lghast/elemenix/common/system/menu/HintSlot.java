package net.lghast.elemenix.common.system.menu;

import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

/**
 * A slot that shows a hint tooltip when it is empty.
 */
public class HintSlot extends Slot {
    private final Component hint;

    public HintSlot(Container container, int slot, int x, int y, Component hint) {
        super(container, slot, x, y);
        this.hint = hint;
    }

    public Component getHint() {
        return hint;
    }
}
