package net.lghast.elemenix.common.system.menu;

import net.lghast.elemenix.common.content.block.InfuserBlock;
import net.lghast.elemenix.common.content.blockentity.InfuserBlockEntity;
import net.lghast.elemenix.common.content.item.StorageItem;
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
public class InfuserMenu extends AbstractContainerMenu {
    private final InfuserBlockEntity blockEntity;
    private final ContainerData data;

    public InfuserMenu(int containerId, Inventory playerInventory, InfuserBlockEntity blockEntity) {
        super(ModMenus.INFUSER_MENU.get(), containerId);
        this.blockEntity = blockEntity;

        this.data = blockEntity.getDataAccess();

        addSlots(playerInventory);
        addDataSlots(data);
    }

    public int[] getStorage(){
        return blockEntity.getElemenixStorage();
    }

    private void addSlots(Inventory playerInventory) {
        this.addSlot(new Slot(blockEntity, 0, 80, 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                InfuserBlock block = blockEntity.getInfuserBlock();
                if (block == null) return false;

                return stack.getItem() instanceof StorageItem;
            }
        });

        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 88 + i * 18));
            }
        }

        for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 146));
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        ItemStack originalStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            originalStack = slotStack.copy();

            if (index < 1) {
                if (!this.moveItemStackTo(slotStack, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                boolean moved = slotStack.getItem() instanceof StorageItem
                        && this.moveItemStackTo(slotStack, 0, 1, false);

                if (!moved) {
                    if (index < 28) {
                        moved = this.moveItemStackTo(slotStack, 28, 37, false);
                    } else {
                        moved = this.moveItemStackTo(slotStack, 1, 28, false);
                    }
                }

                if (!moved) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == originalStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }

        return originalStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity.stillValid(player);
    }

    public InfuserBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public ContainerData getData() {
        return data;
    }
}
