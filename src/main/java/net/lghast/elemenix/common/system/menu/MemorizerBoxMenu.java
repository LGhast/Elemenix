package net.lghast.elemenix.common.system.menu;

import net.lghast.elemenix.register.system.ModMenus;
import net.lghast.elemenix.register.system.ModTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
public class MemorizerBoxMenu extends AbstractContainerMenu {
    private static final int SLOT_COUNT = 18;
    private final Container container;
    private final ItemStack boxStack;

    public MemorizerBoxMenu(int containerId, Inventory playerInventory, ItemStack boxStack) {
        this(containerId, playerInventory, new SimpleContainer(SLOT_COUNT), new
                SimpleContainerData(SLOT_COUNT), boxStack);
    }

    public MemorizerBoxMenu(int containerId, Inventory playerInventory, Container container, ContainerData data, ItemStack boxStack) {
        super(ModMenus.MEMORIZER_BOX_MENU.get(), containerId);
        this.container = container;
        this.boxStack = boxStack;

        loadFromItemStack();

        checkContainerSize(container, SLOT_COUNT);
        checkContainerDataCount(data, SLOT_COUNT);

        for (int i = 0; i < SLOT_COUNT; i++) {
            int row = i / 9;
            int col = i % 9;

            int x = 15 + col * 18;
            int y = (row == 0) ? 33 : 64;

            this.addSlot(new Slot(container, i, x, y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.is(ModTags.MEMORIZER_SLOT_PLACEABLE);
                }
            });
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 15 + j * 18, 95 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 15 + i * 18, 153));
        }

        this.addDataSlots(data);
    }

    private void loadFromItemStack() {
        ItemContainerContents containerContents = boxStack.get(DataComponents.CONTAINER);
        if (containerContents != null) {
            for (int i = 0; i < Math.min(containerContents.getSlots(), SLOT_COUNT);
                 i++) {
                ItemStack stack = containerContents.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    container.setItem(i, stack);
                }
            }
        }
    }

    public void saveToItemStack() {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < SLOT_COUNT; i++) {
            items.add(container.getItem(i).copy());
        }

        ItemContainerContents containerContents =
                ItemContainerContents.fromItems(items);
        boxStack.set(DataComponents.CONTAINER, containerContents);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack stack1 = slot.getItem();
            stack = stack1.copy();

            if (index < SLOT_COUNT) {
                if (!this.moveItemStackTo(stack1, SLOT_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (stack1.is(ModTags.MEMORIZER_SLOT_PLACEABLE)) {
                if (!this.moveItemStackTo(stack1, 0, SLOT_COUNT, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }

            if (stack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return stack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide) {
            saveToItemStack();
        }
    }
}
