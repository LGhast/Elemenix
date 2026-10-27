package net.lghast.elemenix.common.system.menu;

import net.lghast.elemenix.common.content.blockentity.DeconstructorBlockEntity;
import net.lghast.elemenix.register.system.ModMenus;
import net.lghast.elemenix.register.system.ModTags;
import net.lghast.elemenix.utils.misc.LongContainerData;
import net.lghast.elemenix.utils.elemenix.ElemenixInfo;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class DeconstructorMenu extends AbstractContainerMenu {
    private final DeconstructorBlockEntity blockEntity;
    private final LongContainerData data;

    public DeconstructorMenu(int containerId, Inventory playerInventory, DeconstructorBlockEntity blockEntity) {
        super(ModMenus.DECONSTRUCTOR_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = blockEntity.getDataAccess();

        addSlots(playerInventory);
        addDataSlots(data);
    }

    public LongContainerData getData() {
        return data;
    }

    private void addSlots(Inventory playerInventory) {
        for (int i = 0; i < 3; i++) {
            this.addSlot(new Slot(blockEntity, i, 63 + 18 * i, 19) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return !ElemenixInfo.isUndeconstructable(stack) && !stack.is(ModTags.IGNORED_BY_DECONSTRUCTOR_INPUT);
                }
            });
        }

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
        ItemStack originalStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            originalStack = slotStack.copy();

            if (index < 3) {
                if (!this.moveItemStackTo(slotStack, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                boolean moved = false;
                if (!ElemenixInfo.isUndeconstructable(slotStack) && !slotStack.is(ModTags.IGNORED_BY_DECONSTRUCTOR_INPUT)) {
                    moved = this.moveItemStackTo(slotStack, 0, 3, false);
                }
                if (!moved) {
                    if (index < 30) {
                        moved = this.moveItemStackTo(slotStack, 30, 39, false);
                    } else {
                        moved = this.moveItemStackTo(slotStack, 3, 30, false);
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

    public DeconstructorBlockEntity getBlockEntity() {
        return blockEntity;
    }
}
