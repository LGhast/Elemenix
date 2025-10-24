package net.lghast.elemenix.common.system.menu;

import net.lghast.elemenix.common.content.block.EnricherBlock;
import net.lghast.elemenix.common.content.block.InfuserBlock;
import net.lghast.elemenix.common.content.blockentity.EnricherBlockEntity;
import net.lghast.elemenix.common.content.blockentity.InfuserBlockEntity;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.register.system.ModMenus;
import net.lghast.elemenix.utils.Elemenix;
import net.lghast.elemenix.utils.ElemenixInfo;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class EnricherMenu extends AbstractContainerMenu {
    private final EnricherBlockEntity blockEntity;
    private final ContainerData data;


    public EnricherMenu(int containerId, Inventory playerInventory, EnricherBlockEntity blockEntity) {
        super(ModMenus.ENRICHER_MENU.get(), containerId);
        this.blockEntity = blockEntity;

        this.data = blockEntity.getDataAccess();

        addSlots(playerInventory);
        addDataSlots(data);
    }

    public int[] getStorage(){
        return blockEntity.getElemenixStorage();
    }

    private void addSlots(Inventory playerInventory) {
        for(int i = 0; i < 6; i++){
            this.addSlot(new Slot(blockEntity, i, 16 + 18 * i, 22) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
        }

        this.addSlot(new Slot(blockEntity, 6, 142, 20) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                EnricherBlock block = blockEntity.getEnricherBlock();
                if (block == null) return false;

                return isValidForInputSlot(stack);
            }
        });

        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 94 + i * 18));
            }
        }

        for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 152));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack originalStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            originalStack = slotStack.copy();

            boolean moved = false;

            if (index < 7) {
                moved = this.moveItemStackTo(slotStack, 7, 43, true);
            } else {
                if (isValidForInputSlot(slotStack)) {
                    moved = this.moveItemStackTo(slotStack, 6, 7, false);
                }
                if (!moved && !slotStack.isEmpty()) {
                    if (index < 34) {
                        moved = this.moveItemStackTo(slotStack, 34, 43, false);
                    } else {
                        moved = this.moveItemStackTo(slotStack, 7, 34, false);
                    }
                }
            }

            if (!moved) {
                return ItemStack.EMPTY;
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

    private boolean isValidForInputSlot(ItemStack stack) {
        return !ElemenixInfo.getConstituents(stack).isUnanalysable();
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity.stillValid(player);
    }

    public EnricherBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public ContainerData getData() {
        return data;
    }
}