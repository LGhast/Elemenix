package net.lghast.elemenix.common.system.menu;

import net.lghast.elemenix.common.content.blockentity.TankBlockEntity;
import net.lghast.elemenix.register.system.ModMenus;
import net.lghast.elemenix.utils.misc.LongContainerData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TankMenu extends AbstractContainerMenu {
    private final TankBlockEntity blockEntity;
    private final LongContainerData data;

    public TankMenu(int containerId, TankBlockEntity blockEntity) {
        super(ModMenus.TANK_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = blockEntity.getDataAccess();
        addDataSlots(data);
    }

    public LongContainerData getData() {
        return data;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        if (blockEntity == null || blockEntity.getLevel() == null) return false;
        return blockEntity.getLevel().getBlockEntity(blockEntity.getBlockPos()) == blockEntity;
    }

    public TankBlockEntity getBlockEntity() {
        return blockEntity;
    }
}
