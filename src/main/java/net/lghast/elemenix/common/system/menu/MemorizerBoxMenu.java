package net.lghast.elemenix.common.system.menu;

import net.lghast.elemenix.common.content.item.MemorizerBoxItem;
import net.lghast.elemenix.common.system.datacomponent.UuidData;
import net.lghast.elemenix.register.system.ModDataComponents;
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

import java.util.ArrayList;
import java.util.List;

public class MemorizerBoxMenu extends AbstractContainerMenu {
    private static final int SLOT_COUNT = 18;
    private final Container container;
    private final boolean isCurios;
    private final ItemStack originalBoxStack; 
    private final UuidData boxUuid;

    public MemorizerBoxMenu(int containerId, Inventory playerInventory, ItemStack boxStack) {
        this(containerId, playerInventory, new SimpleContainer(SLOT_COUNT), new SimpleContainerData(SLOT_COUNT), boxStack);
    }

    public MemorizerBoxMenu(int containerId, Inventory playerInventory, Container container, ContainerData data, ItemStack boxStack) {
        super(ModMenus.MEMORIZER_BOX_MENU.get(), containerId);
        this.container = container;
        this.originalBoxStack = boxStack;
        
        this.isCurios = !isInPlayerInventory(playerInventory.player, boxStack);
        this.boxUuid = isCurios ? null : MemorizerBoxItem.getOrCreateUuid(boxStack);

        loadFromItemStack(boxStack);
        
        for (int i = 0; i < SLOT_COUNT; i++) {
            int row = i / 9;
            int col = i % 9;
            int x = 15 + col * 18;
            int y = (row == 0) ? 33 : 64;
            this.addSlot(new Slot(container, i, x, y) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
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
    
    private boolean isInPlayerInventory(Player player, ItemStack stack) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i) == stack) return true;
        }
        if (player.getMainHandItem() == stack) return true;
        return player.getOffhandItem() == stack;
    }

    private void loadFromItemStack(ItemStack boxStack) {
        ItemContainerContents containerContents = boxStack.get(DataComponents.CONTAINER);
        if (containerContents != null) {
            for (int i = 0; i < Math.min(containerContents.getSlots(), SLOT_COUNT); i++) {
                ItemStack stack = containerContents.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    container.setItem(i, stack);
                }
            }
        }
    }

    public void saveToItemStack(ItemStack targetBoxStack) {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < SLOT_COUNT; i++) {
            items.add(container.getItem(i).copy());
        }
        ItemContainerContents containerContents = ItemContainerContents.fromItems(items);
        targetBoxStack.set(DataComponents.CONTAINER, containerContents);
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        if (!player.level().isClientSide) {
            if (isCurios) {
                saveToItemStack(originalBoxStack);
            } else {
                ItemStack currentBox = findBoxStackByUuid(player, boxUuid);
                if (!currentBox.isEmpty()) {
                    saveToItemStack(currentBox);
                }
            }
        }
    }

    private ItemStack findBoxStackByUuid(Player player, UuidData uuid) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof MemorizerBoxItem) {
                UuidData u = stack.get(ModDataComponents.BOX_UUID.get());
                if (u != null && u.equals(uuid)) return stack;
            }
        }
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof MemorizerBoxItem) {
            UuidData u = main.get(ModDataComponents.BOX_UUID.get());
            if (u != null && u.equals(uuid)) return main;
        }
        ItemStack off = player.getOffhandItem();
        if (off.getItem() instanceof MemorizerBoxItem) {
            UuidData u = off.get(ModDataComponents.BOX_UUID.get());
            if (u != null && u.equals(uuid)) return off;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
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
}
