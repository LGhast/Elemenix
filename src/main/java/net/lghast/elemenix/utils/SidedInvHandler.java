package net.lghast.elemenix.utils;

import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class SidedInvHandler implements IItemHandler {
    private final WorldlyContainer container;

    @Nullable
    private final Direction side;

    public SidedInvHandler(WorldlyContainer container, @Nullable Direction side) {
        this.container = container;
        this.side = side;
    }

    @Override
    public int getSlots() {
        return container.getContainerSize();
    }

    private boolean isExposed(int slot) {
        if (slot < 0 || slot >= getSlots()) {
            return false;
        }
        if (side == null) {
            return true;
        }
        for (int exposed : container.getSlotsForFace(side)) {
            if (exposed == slot) {
                return true;
            }
        }
        return false;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return isExposed(slot) ? container.getItem(slot).copy() : ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || !isExposed(slot)) {
            return stack;
        }
        boolean placeable = side == null
                ? container.canPlaceItem(slot, stack)
                : container.canPlaceItemThroughFace(slot, stack, side);
        if (!placeable) {
            return stack;
        }
        ItemStack existing = container.getItem(slot);
        int limit = getSlotLimit(slot);
        if (!existing.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(existing, stack)) {
                return stack;
            }
            limit -= existing.getCount();
        }
        if (limit <= 0) {
            return stack;
        }
        int toInsert = Math.min(stack.getCount(), limit);
        if (!simulate) {
            if (existing.isEmpty()) {
                ItemStack copy = stack.copy();
                copy.setCount(toInsert);
                container.setItem(slot, copy);
            } else {
                existing.grow(toInsert);
                container.setChanged();
            }
        }
        if (toInsert >= stack.getCount()) {
            return ItemStack.EMPTY;
        }
        ItemStack remainder = stack.copy();
        remainder.shrink(toInsert);
        return remainder;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0 || !isExposed(slot)) {
            return ItemStack.EMPTY;
        }
        ItemStack existing = container.getItem(slot);
        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }
        boolean takeable = side == null
                ? container.canTakeItem(container, slot, existing)
                : container.canTakeItemThroughFace(slot, existing, side);
        if (!takeable) {
            return ItemStack.EMPTY;
        }
        int toExtract = Math.min(amount, existing.getCount());
        if (simulate) {
            ItemStack copy = existing.copy();
            copy.setCount(toExtract);
            return copy;
        }
        return container.removeItem(slot, toExtract);
    }

    @Override
    public int getSlotLimit(int slot) {
        return container.getMaxStackSize();
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if (stack.isEmpty() || !isExposed(slot)) {
            return false;
        }
        return side == null
                ? container.canPlaceItem(slot, stack)
                : container.canPlaceItemThroughFace(slot, stack, side);
    }
}
