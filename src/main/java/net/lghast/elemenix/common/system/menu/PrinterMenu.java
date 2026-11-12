package net.lghast.elemenix.common.system.menu;

import net.lghast.elemenix.common.content.blockentity.AbstractPrinterBlockEntity;
import net.lghast.elemenix.register.system.ModMenus;
import net.lghast.elemenix.utils.misc.LongContainerData;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class PrinterMenu extends AbstractContainerMenu {
    private static final int OUTPUT_SLOT_X = 10;
    private static final int OUTPUT_SLOT_Y = 110;
    private static final int OUTPUT_COLUMNS = 4;
    private static final int OUTPUT_ROWS = 2;
    private static final int MEMORIZER_SLOT_X = 37;
    private static final int MEMORIZER_SLOT_Y = 156;
    private static final int PLAYER_INV_X = 89;
    private static final int PLAYER_INV_Y = 117;
    private static final int HOTBAR_Y = 175;

    private final AbstractPrinterBlockEntity blockEntity;
    private final LongContainerData data;

    public PrinterMenu(int containerId, Inventory playerInventory, AbstractPrinterBlockEntity blockEntity) {
        super(ModMenus.PRINTER_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = blockEntity.getDataAccess();

        for (int row = 0; row < OUTPUT_ROWS; row++) {
            for (int col = 0; col < OUTPUT_COLUMNS; col++) {
                int slotIndex = col + row * OUTPUT_COLUMNS;
                this.addSlot(new Slot(blockEntity, slotIndex, OUTPUT_SLOT_X + col * 18, OUTPUT_SLOT_Y + row * 18));
            }
        }

        this.addSlot(new Slot(blockEntity, AbstractPrinterBlockEntity.MEMORIZER_SLOT, MEMORIZER_SLOT_X, MEMORIZER_SLOT_Y));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, PLAYER_INV_X + j * 18, PLAYER_INV_Y + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, PLAYER_INV_X + i * 18, HOTBAR_Y));
        }

        this.addDataSlots(data);
    }

    public LongContainerData getData() {
        return data;
    }

    public ItemStack getMemorizerItem() {
        return blockEntity.getItem(AbstractPrinterBlockEntity.MEMORIZER_SLOT);
    }

    @Nullable
    public List<String> getMemoryKeys() {
        return blockEntity.getMemoryKeys();
    }

    public ItemStack createEntryStack(String key) {
        return blockEntity.createEntryStack(key);
    }

    public int getMode() {
        return blockEntity.getMode();
    }

    public void setMode(int mode) {
        blockEntity.setMode(mode);
    }

    public boolean isDisabled(String key) {
        return blockEntity.isDisabled(key);
    }

    public void toggleDisabled(String key) {
        blockEntity.toggleDisabled(key);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();

            int blockSlots = AbstractPrinterBlockEntity.SLOT_COUNT;
            int totalSlots = this.slots.size();

            if (index < blockSlots) {
                if (!this.moveItemStackTo(slotStack, blockSlots, totalSlots, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotStack.is(blockEntity.getMemorizerItem())) {
                if (!this.moveItemStackTo(slotStack, AbstractPrinterBlockEntity.MEMORIZER_SLOT,
                        AbstractPrinterBlockEntity.MEMORIZER_SLOT + 1, false)) {
                    if (!this.moveItemStackTo(slotStack, blockSlots, totalSlots, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else {
                if (!this.moveItemStackTo(slotStack, blockSlots, totalSlots, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }

        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity.stillValid(player);
    }

    public AbstractPrinterBlockEntity getBlockEntity() {
        return blockEntity;
    }
}
