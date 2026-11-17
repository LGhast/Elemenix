package net.lghast.elemenix.common.system.menu;

import net.lghast.elemenix.register.system.ModMenus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RegistrarMenu extends AbstractContainerMenu {
    public RegistrarMenu(int containerId) {
        super(ModMenus.REGISTRAR_MENU.get(), containerId);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }
}
