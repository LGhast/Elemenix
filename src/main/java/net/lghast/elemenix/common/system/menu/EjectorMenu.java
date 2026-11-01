package net.lghast.elemenix.common.system.menu;

import net.lghast.elemenix.common.content.blockentity.EjectorBlockEntity;
import net.lghast.elemenix.register.system.ModMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class EjectorMenu extends AbstractContainerMenu {
    private final EjectorBlockEntity blockEntity;
    private final ContainerData data;

    public EjectorMenu(int containerId, Inventory playerInventory, EjectorBlockEntity blockEntity) {
        super(ModMenus.EJECTOR_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = blockEntity.getDataAccess();

        addSlots(playerInventory);
        addDataSlots(data);
    }

    public int[] getStorage() {
        return blockEntity.getElemenixStorage();
    }

    public int getFilterIndex() {
        return data.get(6);
    }

    public void setFilterIndex(int index) {
        blockEntity.setFilterIndex(index);
    }

    private void addSlots(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 88 + i * 18));
            }
        }
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 146));
        }
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

    public EjectorBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public ContainerData getData() {
        return data;
    }
}
