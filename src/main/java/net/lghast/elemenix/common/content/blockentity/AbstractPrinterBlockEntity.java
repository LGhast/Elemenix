package net.lghast.elemenix.common.content.blockentity;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.common.content.block.AbstractPrinterBlock;
import net.lghast.elemenix.common.system.menu.PrinterMenu;
import net.lghast.elemenix.utils.elemenix.Constituents;
import net.lghast.elemenix.utils.elemenix.Elemenix;
import net.lghast.elemenix.utils.elemenix.ElemenixInfo;
import net.lghast.elemenix.utils.misc.ElemenixHolder;
import net.lghast.elemenix.utils.misc.LongContainerData;
import net.lghast.elemenix.utils.misc.SidedInvHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ParametersAreNonnullByDefault
public abstract class AbstractPrinterBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, ElemenixHolder {
    public static final int OUTPUT_SLOT_FIRST = 0;
    public static final int OUTPUT_SLOT_COUNT = 8;
    public static final int MEMORIZER_SLOT = 8;
    public static final int SLOT_COUNT = 9;

    public static final int MODE_FIRST = 0;
    public static final int MODE_SEQUENTIAL = 1;
    public static final int MODE_RANDOM = 2;
    public static final int MODE_COUNT = 3;

    private static final String TAG_STORAGE = "ElemenixStorage";
    private static final String TAG_MODE = "Mode";
    private static final String TAG_RECONSTRUCT_TIMER = "ReconstructTimer";
    private static final String TAG_CURSOR = "ReconstructCursor";
    private static final String TAG_PRINTING = "Printing";
    private static final String TAG_DISABLED = "Disabled";

    private static final int[] ALL_SLOTS = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8};

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final LongContainerData storage = new LongContainerData(6);

    private int mode = MODE_FIRST;
    private int reconstructTimer = 0;
    private int reconstructCursor = 0;
    private int workGrace = 0;

    @Nullable
    private String printingKey = null;

    private final Set<String> disabledKeys = new HashSet<>();

    protected AbstractPrinterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public abstract Item getMemorizerItem();

    protected abstract double getLossMultiplier();

    protected abstract int getReconstructionInterval();

    /** Returns the list of reconstructable entry keys, or null when no memorizer is present. */
    @Nullable
    public abstract List<String> getMemoryKeys();

    /** Creates the result stack for a memory key. */
    public abstract ItemStack createEntryStack(String key);

    @Override
    protected @NotNull NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(@NotNull NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected @NotNull AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory) {
        return new PrinterMenu(containerId, playerInventory, this);
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        super.setItem(slot, stack);
        if (slot == MEMORIZER_SLOT) {
            clearDisabledKeys();
        }
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        ItemStack result = super.removeItem(slot, amount);
        if (slot == MEMORIZER_SLOT) {
            clearDisabledKeys();
        }
        return result;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        ItemStack result = super.removeItemNoUpdate(slot);
        if (slot == MEMORIZER_SLOT) {
            clearDisabledKeys();
        }
        return result;
    }

    public LongContainerData getDataAccess() {
        return storage;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = Math.floorMod(mode, MODE_COUNT);
        this.printingKey = null;
        this.reconstructCursor = 0;
        setChanged();
    }

    public ItemStack getPrintingItemStack() {
        if (printingKey == null) {
            return ItemStack.EMPTY;
        }
        return createEntryStack(printingKey);
    }

    public static String getModeName(int mode) {
        String key = switch (Math.floorMod(mode, MODE_COUNT)) {
            case MODE_SEQUENTIAL -> "gui.elemenix.printer_mode_sequential";
            case MODE_RANDOM -> "gui.elemenix.printer_mode_random";
            default -> "gui.elemenix.printer_mode_first";
        };
        return Component.translatable(key).getString();
    }

    public boolean isDisabled(String key) {
        return disabledKeys.contains(key);
    }

    public void toggleDisabled(String key) {
        if (!disabledKeys.remove(key)) {
            disabledKeys.add(key);
        }
        setChanged();
    }

    private void clearDisabledKeys() {
        if (!disabledKeys.isEmpty()) {
            disabledKeys.clear();
            setChanged();
        }
    }

    public void addElemenix(Elemenix elemenix, long amount) {
        if (amount <= 0) return;
        int index = elemenix.getIndex();
        if (index >= 0 && index < 6) {
            long current = storage.getLong(index);
            storage.setLong(index, Math.min(current + amount, Long.MAX_VALUE));
            setChanged();
        }
    }

    public void deductElemenix(Elemenix elemenix, long amount) {
        if (amount <= 0) return;
        int index = elemenix.getIndex();
        if (index >= 0 && index < 6) {
            long current = storage.getLong(index);
            storage.setLong(index, Math.max(current - amount, 0L));
            setChanged();
        }
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;
        if (!Elemenics.started) return;

        boolean hasMemorizer = getItem(MEMORIZER_SLOT).is(getMemorizerItem());
        List<String> memoryKeys = getMemoryKeys();
        boolean memorizerNonEmpty = memoryKeys != null && !memoryKeys.isEmpty();
        boolean disabled = level.hasNeighborSignal(pos);

        boolean canWork = hasMemorizer && !disabled && canReconstructAny();
        boolean working = canWork;

        if (canWork) {
            reconstructTimer++;
            if (reconstructTimer >= getReconstructionInterval()) {
                reconstructTimer = 0;
                reconstructCurrentAndPrepareNext();
            } else if (printingKey == null) {
                updatePrintingKey();
            }
        } else {
            reconstructTimer = 0;
            if (printingKey != null) {
                printingKey = null;
                setChanged();
            }
        }

        if (working) {
            workGrace = 20;
        } else if (workGrace > 0) {
            if (hasMemorizer && memorizerNonEmpty) {
                workGrace--;
                working = true;
            } else {
                workGrace = 0;
            }
        }

        boolean stateHasMemorizer = state.getValue(AbstractPrinterBlock.HAS_MEMORIZER);
        boolean stateDisabled = state.getValue(AbstractPrinterBlock.DISABLED);
        boolean stateWorking = state.getValue(AbstractPrinterBlock.WORKING);

        if (stateHasMemorizer != hasMemorizer || stateDisabled != disabled || stateWorking != working) {
            level.setBlock(pos, state
                    .setValue(AbstractPrinterBlock.HAS_MEMORIZER, hasMemorizer)
                    .setValue(AbstractPrinterBlock.DISABLED, disabled)
                    .setValue(AbstractPrinterBlock.WORKING, working), Block.UPDATE_ALL);
        }
    }

    private boolean canReconstructAny() {
        List<String> memoryKeys = getMemoryKeys();
        if (memoryKeys == null || memoryKeys.isEmpty()) return false;
        for (String key : memoryKeys) {
            if (isReconstructable(key)) return true;
        }
        return false;
    }

    private void reconstructCurrentAndPrepareNext() {
        if (printingKey != null) {
            reconstruct(printingKey);
        }
        updatePrintingKey();
    }

    private void updatePrintingKey() {
        List<String> memoryKeys = getMemoryKeys();
        if (memoryKeys == null || memoryKeys.isEmpty()) {
            printingKey = null;
            return;
        }
        int index = findTargetIndex(memoryKeys);
        printingKey = index >= 0 ? memoryKeys.get(index) : null;
        setChanged();
    }

    private int findTargetIndex(List<String> memoryKeys) {
        int size = memoryKeys.size();
        switch (mode) {
            case MODE_FIRST: {
                for (int i = 0; i < size; i++) {
                    if (isReconstructable(memoryKeys.get(i))) return i;
                }
                return -1;
            }
            case MODE_SEQUENTIAL: {
                int start = Math.floorMod(reconstructCursor, size);
                for (int i = 0; i < size; i++) {
                    int idx = (start + i) % size;
                    if (isReconstructable(memoryKeys.get(idx))) {
                        reconstructCursor = (idx + 1) % size;
                        return idx;
                    }
                }
                return -1;
            }
            case MODE_RANDOM: {
                List<Integer> candidates = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    if (isReconstructable(memoryKeys.get(i))) {
                        candidates.add(i);
                    }
                }
                if (candidates.isEmpty()) return -1;
                if (level == null) return candidates.getFirst();
                return candidates.get(level.random.nextInt(candidates.size()));
            }
            default:
                return -1;
        }
    }

    private Constituents getPrinterRequired(ItemStack stack) {
        Constituents required = ElemenixInfo.getPremiumAppliedConstituents(stack);
        if (required.isUnanalysable()) return required;
        double loss = getLossMultiplier();
        if (loss != 1.0) {
            required.multiply(loss);
        }
        return required;
    }

    private boolean isReconstructable(String key) {
        if (disabledKeys.contains(key)) return false;
        ItemStack stack = createEntryStack(key);
        if (stack.isEmpty()) return false;
        if (ElemenixInfo.isUnreconstructable(stack)) return false;

        Constituents required = getPrinterRequired(stack);
        if (required.isUnanalysable()) return false;

        for (Elemenix type : Elemenix.values()) {
            long req = required.get(type);
            if (req > 0 && storage.getLong(type.getIndex()) < req) {
                return false;
            }
        }
        return findOutputSlot(stack) >= 0;
    }

    private int findOutputSlot(ItemStack resultStack) {
        for (int i = OUTPUT_SLOT_FIRST; i < OUTPUT_SLOT_FIRST + OUTPUT_SLOT_COUNT; i++) {
            ItemStack slotStack = items.get(i);
            if (!slotStack.isEmpty() && ItemStack.isSameItemSameComponents(slotStack, resultStack)
                    && slotStack.getCount() < slotStack.getMaxStackSize()) {
                return i;
            }
        }
        for (int i = OUTPUT_SLOT_FIRST; i < OUTPUT_SLOT_FIRST + OUTPUT_SLOT_COUNT; i++) {
            if (items.get(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    private void reconstruct(String key) {
        ItemStack resultStack = createEntryStack(key);
        if (resultStack.isEmpty()) return;
        if (ElemenixInfo.isUnreconstructable(resultStack)) return;

        Constituents required = getPrinterRequired(resultStack);
        if (required.isUnanalysable()) return;

        for (Elemenix type : Elemenix.values()) {
            long req = required.get(type);
            if (req > 0 && storage.getLong(type.getIndex()) < req) {
                return;
            }
        }

        int slot = findOutputSlot(resultStack);
        if (slot < 0) return;

        for (Elemenix type : Elemenix.values()) {
            long req = required.get(type);
            if (req > 0) {
                deductElemenix(type, req);
            }
        }

        ItemStack slotStack = items.get(slot);
        if (slotStack.isEmpty()) {
            items.set(slot, resultStack.copyWithCount(1));
        } else {
            slotStack.grow(1);
        }
        setChanged();
    }

    @Override
    public long getStored(Elemenix type) {
        return storage.getLong(type.getIndex());
    }

    @Override
    public boolean canExtract(Elemenix type) {
        return false;
    }

    @Override
    public boolean canReceive(Elemenix type) {
        return true;
    }

    @Override
    public void extract(Elemenix type, long amount) {}

    @Override
    public void receive(Elemenix type, long amount) {
        addElemenix(type, amount);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == MEMORIZER_SLOT && stack.is(getMemorizerItem());
    }

    @Override
    public boolean canTakeItem(Container target, int slot, ItemStack stack) {
        return slot >= OUTPUT_SLOT_FIRST && slot < OUTPUT_SLOT_FIRST + OUTPUT_SLOT_COUNT;
    }

    @Override
    public int @NotNull [] getSlotsForFace(Direction side) {
        return ALL_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return index == MEMORIZER_SLOT && stack.is(getMemorizerItem());
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index >= OUTPUT_SLOT_FIRST && index < OUTPUT_SLOT_FIRST + OUTPUT_SLOT_COUNT;
    }

    public IItemHandler getItemHandler(@Nullable Direction side) {
        return new SidedInvHandler(this, side);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        ContainerHelper.loadAllItems(tag, items, provider);

        if (tag.contains(TAG_STORAGE, CompoundTag.TAG_LONG_ARRAY)) {
            long[] stored = tag.getLongArray(TAG_STORAGE);
            for (int i = 0; i < Math.min(stored.length, 6); i++) {
                storage.setLong(i, stored[i]);
            }
        }

        mode = Math.floorMod(tag.getInt(TAG_MODE), MODE_COUNT);
        reconstructTimer = tag.getInt(TAG_RECONSTRUCT_TIMER);
        reconstructCursor = tag.getInt(TAG_CURSOR);
        printingKey = tag.contains(TAG_PRINTING) ? tag.getString(TAG_PRINTING) : null;

        disabledKeys.clear();
        if (tag.contains(TAG_DISABLED, Tag.TAG_LIST)) {
            ListTag list = tag.getList(TAG_DISABLED, Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                disabledKeys.add(list.getString(i));
            }
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putLongArray(TAG_STORAGE, storage.getData());
        tag.putInt(TAG_MODE, mode);
        if (printingKey != null) {
            tag.putString(TAG_PRINTING, printingKey);
        }
        if (!disabledKeys.isEmpty()) {
            ListTag list = new ListTag();
            for (String key : disabledKeys) {
                list.add(StringTag.valueOf(key));
            }
            tag.put(TAG_DISABLED, list);
        }
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        ContainerHelper.saveAllItems(tag, items, provider);
        tag.putLongArray(TAG_STORAGE, storage.getData());
        tag.putInt(TAG_MODE, mode);
        tag.putInt(TAG_RECONSTRUCT_TIMER, reconstructTimer);
        tag.putInt(TAG_CURSOR, reconstructCursor);
        if (printingKey != null) {
            tag.putString(TAG_PRINTING, printingKey);
        }
        if (!disabledKeys.isEmpty()) {
            ListTag list = new ListTag();
            for (String key : disabledKeys) {
                list.add(StringTag.valueOf(key));
            }
            tag.put(TAG_DISABLED, list);
        }
    }
}
