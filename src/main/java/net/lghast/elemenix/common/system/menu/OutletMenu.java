package net.lghast.elemenix.common.system.menu;

import net.lghast.elemenix.common.content.blockentity.OutletBlockEntity;
import net.lghast.elemenix.register.system.ModMenus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class OutletMenu extends AbstractContainerMenu {
    private final OutletBlockEntity blockEntity;
    private final ContainerData data;

    public OutletMenu(int containerId, OutletBlockEntity blockEntity) {
        super(ModMenus.OUTLET_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = blockEntity.getDataAccess();
        addDataSlots(data);
    }

    public int getOutputPercent() {
        return data.get(0);
    }

    public void setOutputPercent(int percent) {
        blockEntity.setOutputPercent(percent);
    }

    public ContainerData getData() {
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

    public OutletBlockEntity getBlockEntity() {
        return blockEntity;
    }
}
