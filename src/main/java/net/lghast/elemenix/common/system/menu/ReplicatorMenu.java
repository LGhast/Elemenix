package net.lghast.elemenix.common.system.menu;

import net.lghast.elemenix.common.content.blockentity.ReplicatorBlockEntity;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.register.system.ModMenus;
import net.lghast.elemenix.register.system.ModTags;
import net.minecraft.network.chat.Component;
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
public class ReplicatorMenu extends AbstractContainerMenu {
    private static final int MASTER_X = 17;
    private static final int MASTER_Y = 30;
    private static final int STYLE_X = 61;
    private static final int STYLE_Y = 30;
    private static final int INPUT_X = 12;
    private static final int INPUT_Y = 71;
    private static final int OUTPUT_X = 12;
    private static final int OUTPUT_Y = 94;
    private static final int PLAYER_INV_X = 40;
    private static final int PLAYER_INV_Y = 119;
    private static final int HOTBAR_Y = 177;

    private final ReplicatorBlockEntity blockEntity;
    private final LongContainerData data;

    public ReplicatorMenu(int containerId, Inventory playerInventory, ReplicatorBlockEntity blockEntity) {
        super(ModMenus.MEMORIZER_REPLICATOR_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = blockEntity.getDataAccess();

        this.addSlot(new HintSlot(blockEntity, ReplicatorBlockEntity.MASTER_SLOT, MASTER_X, MASTER_Y,
                Component.translatable("gui.elemenix.replicator.master_hint")));
        this.addSlot(new HintSlot(blockEntity, ReplicatorBlockEntity.STYLE_SLOT, STYLE_X, STYLE_Y,
                Component.translatable("gui.elemenix.replicator.style_hint")));

        for (int i = 0; i < ReplicatorBlockEntity.INPUT_SLOT_COUNT; i++) {
            this.addSlot(new Slot(blockEntity,
                    ReplicatorBlockEntity.INPUT_SLOT_FIRST + i,
                    INPUT_X + i * 18, INPUT_Y));
        }

        for (int i = 0; i < ReplicatorBlockEntity.OUTPUT_SLOT_COUNT; i++) {
            this.addSlot(new Slot(blockEntity,
                    ReplicatorBlockEntity.OUTPUT_SLOT_FIRST + i,
                    OUTPUT_X + i * 18, OUTPUT_Y));
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, PLAYER_INV_X + col * 18, PLAYER_INV_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, PLAYER_INV_X + col * 18, HOTBAR_Y));
        }

        this.addDataSlots(data);
    }

    public LongContainerData getData() {
        return data;
    }

    public ReplicatorBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public ItemStack getMasterStack() {
        return blockEntity.getMasterStack();
    }

    @Nullable
    public List<String> getMemoryKeys() {
        return blockEntity.getMemoryKeys();
    }

    public ItemStack createEntryStack(String key) {
        return blockEntity.createEntryStack(key);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();

            int blockSlots = ReplicatorBlockEntity.SLOT_COUNT;
            int totalSlots = this.slots.size();

            if (index < blockSlots) {
                if (!this.moveItemStackTo(slotStack, blockSlots, totalSlots, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotStack.is(ModItems.ELEMENIC_MEMORIZER) || slotStack.is(ModItems.WITCHCRAFT_MEMORIZER)) {
                if (!this.moveItemStackTo(slotStack, ReplicatorBlockEntity.MASTER_SLOT,
                        ReplicatorBlockEntity.MASTER_SLOT + 1, false)) {
                    if (!this.moveItemStackTo(slotStack, ReplicatorBlockEntity.INPUT_SLOT_FIRST,
                            ReplicatorBlockEntity.INPUT_SLOT_FIRST + ReplicatorBlockEntity.INPUT_SLOT_COUNT, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (slotStack.is(ModTags.MEMORIZER_STYLE_MATERIALS)) {
                if (!this.moveItemStackTo(slotStack, ReplicatorBlockEntity.STYLE_SLOT,
                        ReplicatorBlockEntity.STYLE_SLOT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
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
}
