package net.lghast.elemenix.common.system.menu;

import net.lghast.elemenix.common.content.block.InfuserBlock;
import net.lghast.elemenix.common.content.block.TransformerBlock;
import net.lghast.elemenix.common.content.blockentity.InfuserBlockEntity;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.register.system.ModMenus;
import net.lghast.elemenix.utils.Constituents;
import net.lghast.elemenix.utils.Elemenix;
import net.lghast.elemenix.utils.ElemenixInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class InfuserMenu extends AbstractContainerMenu {
    private final InfuserBlockEntity blockEntity;
    private final ContainerData data;


    public InfuserMenu(int containerId, Inventory playerInventory, InfuserBlockEntity blockEntity) {
        super(ModMenus.INFUSER_MENU.get(), containerId);
        this.blockEntity = blockEntity;

        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return blockEntity.getStorage(Elemenix.values()[index]);
            }

            @Override
            public void set(int index, int value) {
                blockEntity.setStorage(Elemenix.values()[index], value);
            }

            @Override
            public int getCount() {
                return 6;
            }
        };

        addSlots(playerInventory);
        addDataSlots(data);
    }

    public int[] getStorage(){
        return new int[]{data.get(0), data.get(1), data.get(2), data.get(3), data.get(4), data.get(5)};
    }

    private void addSlots(Inventory playerInventory) {
        this.addSlot(new Slot(blockEntity, 0, 80, 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                InfuserBlock block = blockEntity.getInfuserBlock();
                if (block == null) return false;

                return stack.is(ModItems.ELEMENIC_ANALYZER);
            }
        });

        this.addSlot(new Slot(blockEntity, 1, 129, 15) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                InfuserBlock block = blockEntity.getInfuserBlock();
                if (block == null) return false;

                return !ElemenixInfo.getConstituents(stack).isUnanalysable();
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
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < 2) {
                if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(itemstack1, 0, 2, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
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