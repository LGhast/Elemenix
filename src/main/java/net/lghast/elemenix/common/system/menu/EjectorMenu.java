package net.lghast.elemenix.common.system.menu;

import net.lghast.elemenix.common.content.block.EjectorBlock;
import net.lghast.elemenix.common.content.blockentity.EjectorBlockEntity;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.register.system.ModMenus;
import net.lghast.elemenix.utils.ElemenixInfo;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

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

    public int[] getStorage(){
        return blockEntity.getElemenixStorage();
    }

    private void addSlots(Inventory playerInventory) {
        for(int i = 0; i < 6; i++){
            this.addSlot(new Slot(blockEntity, i, 34 + 18 * i, 22) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
        }

        this.addSlot(new Slot(blockEntity, 6, 170, 20) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                EjectorBlock block = blockEntity.getEjectorBlock();
                if (block == null) return false;

                return isValidForInputSlot(stack);
            }
        });

        this.addSlot(new Slot(blockEntity, 7, 170, 45) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                EjectorBlock block = blockEntity.getEjectorBlock();
                if (block == null) return false;

                return stack.is(ModItems.THROTTLE_VALVE);
            }
        });

        this.addSlot(new Slot(blockEntity, 8, 170, 66) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                EjectorBlock block = blockEntity.getEjectorBlock();
                if (block == null) return false;

                return stack.is(ModItems.FLOW_STRAIGHTENER);
            }
        });

        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 20 + j * 18, 94 + i * 18));
            }
        }

        for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 20 + k * 18, 152));
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

    public EjectorBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public ContainerData getData() {
        return data;
    }
}