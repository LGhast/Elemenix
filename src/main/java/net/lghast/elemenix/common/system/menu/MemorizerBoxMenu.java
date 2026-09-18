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
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MemorizerBoxMenu extends AbstractContainerMenu {
    private static final int SLOT_COUNT = 18;

    private final Container container;
    private final Inventory playerInventory;
    private final Player owner;

    private final ItemStack originalBoxStack;
    private final UuidData boxUuid;

    private final int lockedPlayerInventorySlot;

    public MemorizerBoxMenu(int containerId, Inventory playerInventory, ItemStack boxStack) {
        this(containerId, playerInventory, new SimpleContainer(SLOT_COUNT), new SimpleContainerData(SLOT_COUNT), boxStack);
    }

    public MemorizerBoxMenu(int containerId, Inventory playerInventory, Container container, ContainerData data, ItemStack boxStack) {
        super(ModMenus.MEMORIZER_BOX_MENU.get(), containerId);
        checkContainerSize(container, SLOT_COUNT);

        this.container = container;
        this.playerInventory = playerInventory;
        this.owner = playerInventory.player;
        this.originalBoxStack = boxStack;
        this.boxUuid = getOrCreateBoxUuid(boxStack);
        this.lockedPlayerInventorySlot = findPlayerInventorySlotByUuid(playerInventory, this.boxUuid);

        loadFromItemStack(boxStack);

        for (int i = 0; i < SLOT_COUNT; i++) {
            int row = i / 9;
            int col = i % 9;

            int x = 15 + col * 18;
            int y = row == 0 ? 33 : 64;

            this.addSlot(new Slot(container, i, x, y) {

                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return canPlaceInBox(stack);
                }

                @Override
                public void setChanged() {
                    super.setChanged();
                    MemorizerBoxMenu.this.saveToCurrentHolder();
                }
            });
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                int slotIndex = j + i * 9 + 9;
                this.addSlot(createPlayerInventorySlot(
                        playerInventory,
                        slotIndex,
                        15 + j * 18,
                        95 + i * 18
                ));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlot(createPlayerInventorySlot(
                    playerInventory,
                    i,
                    15 + i * 18,
                    153
            ));
        }

        this.addDataSlots(data);
    }

    private Slot createPlayerInventorySlot(Inventory inventory, int slotIndex, int x, int y) {
        if (slotIndex == lockedPlayerInventorySlot) {
            return new Slot(inventory, slotIndex, x, y) {

                @Override
                public boolean mayPickup(@NotNull Player player) {
                    return false;
                }

                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return false;
                }
            };
        }

        return new Slot(inventory, slotIndex, x, y);
    }

    private boolean canPlaceInBox(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        if (!stack.is(ModTags.MEMORIZER_SLOT_PLACEABLE)) {
            return false;
        }

        return !(stack.getItem() instanceof MemorizerBoxItem);
    }

    private UuidData getOrCreateBoxUuid(ItemStack stack) {
        UuidData uuid = stack.get(ModDataComponents.BOX_UUID.get());

        if (uuid == null) {
            uuid = UuidData.createRandom();
            stack.set(ModDataComponents.BOX_UUID.get(), uuid);
        }

        return uuid;
    }

    private void loadFromItemStack(ItemStack boxStack) {
        for (int i = 0; i < SLOT_COUNT; i++) {
            container.setItem(i, ItemStack.EMPTY);
        }

        ItemContainerContents containerContents = boxStack.get(DataComponents.CONTAINER);

        if (containerContents == null) {
            return;
        }

        int max = Math.min(containerContents.getSlots(), SLOT_COUNT);

        for (int i = 0; i < max; i++) {
            ItemStack stack = containerContents.getStackInSlot(i);

            if (!stack.isEmpty()) {
                container.setItem(i, stack.copy());
            }
        }
    }

    public void saveToItemStack(ItemStack targetBoxStack) {
        if (targetBoxStack.isEmpty()) {
            return;
        }

        if (!(targetBoxStack.getItem() instanceof MemorizerBoxItem)) {
            return;
        }

        List<ItemStack> items = new ArrayList<>();

        for (int i = 0; i < SLOT_COUNT; i++) {
            items.add(container.getItem(i).copy());
        }

        ItemContainerContents containerContents = ItemContainerContents.fromItems(items);
        targetBoxStack.set(DataComponents.CONTAINER, containerContents);
    }

    private void saveToCurrentHolder() {
        if (owner.level().isClientSide) {
            return;
        }

        ItemStack targetBoxStack = findBoxStackByUuid(owner, boxUuid);

        if (targetBoxStack.isEmpty() && isSameBox(originalBoxStack, boxUuid)) {
            targetBoxStack = originalBoxStack;
        }

        if (targetBoxStack.isEmpty()) {
            return;
        }

        saveToItemStack(targetBoxStack);

        playerInventory.setChanged();
        broadcastChanges();

    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);

        if (!player.level().isClientSide) {
            saveToCurrentHolder();
        }
    }

    private int findPlayerInventorySlotByUuid(Inventory inventory, UuidData uuid) {
        if (uuid == null) {
            return -1;
        }

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);

            if (isSameBox(stack, uuid)) {
                return i;
            }
        }

        return -1;
    }

    private ItemStack findBoxStackByUuid(Player player, UuidData uuid) {
        if (uuid == null) {
            return ItemStack.EMPTY;
        }

        Inventory inv = player.getInventory();

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);

            if (isSameBox(stack, uuid)) {
                return stack;
            }
        }

        ItemStack main = player.getMainHandItem();
        if (isSameBox(main, uuid)) {
            return main;
        }

        ItemStack off = player.getOffhandItem();
        if (isSameBox(off, uuid)) {
            return off;
        }

        Optional<ICuriosItemHandler> optional = CuriosApi.getCuriosInventory(player);

        if (optional.isPresent()) {
            ICuriosItemHandler handler = optional.get();

            Optional<SlotResult> result = handler.findFirstCurio(stack -> isSameBox(stack, uuid));

            if (result.isPresent()) {
                return result.get().stack();
            }
        }

        return ItemStack.EMPTY;
    }

    private boolean isSameBox(ItemStack stack, UuidData uuid) {
        if (stack.isEmpty()) {
            return false;
        }

        if (!(stack.getItem() instanceof MemorizerBoxItem)) {
            return false;
        }

        UuidData currentUuid = stack.get(ModDataComponents.BOX_UUID.get());

        return currentUuid != null && currentUuid.equals(uuid);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        if (player != owner) {
            return false;
        }

        return !findBoxStackByUuid(player, boxUuid).isEmpty();
    }

    private boolean isLockedPlayerInventoryMenuSlot(int menuIndex) {
        if (lockedPlayerInventorySlot < 0) {
            return false;
        }

        if (menuIndex < SLOT_COUNT || menuIndex >= this.slots.size()) {
            return false;
        }

        Slot slot = this.slots.get(menuIndex);

        return slot.getSlotIndex() == lockedPlayerInventorySlot;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        if (index < 0 || index >= this.slots.size()) {
            return ItemStack.EMPTY;
        }

        if (isLockedPlayerInventoryMenuSlot(index)) {
            return ItemStack.EMPTY;
        }

        ItemStack result;
        Slot slot = this.slots.get(index);

        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stackInSlot = slot.getItem();
        result = stackInSlot.copy();

        if (index < SLOT_COUNT) {
            if (!this.moveItemStackTo(stackInSlot, SLOT_COUNT, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (canPlaceInBox(stackInSlot)) {
            if (!this.moveItemStackTo(stackInSlot, 0, SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (stackInSlot.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        saveToCurrentHolder();
        return result;
    }
}
