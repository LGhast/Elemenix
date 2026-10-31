package net.lghast.elemenix.common.content.blockentity;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.common.content.block.DeconstructorBlock;
import net.lghast.elemenix.common.system.menu.DeconstructorMenu;
import net.lghast.elemenix.register.content.ModBlockEntities;
import net.lghast.elemenix.register.system.ModTags;
import net.lghast.elemenix.utils.elemenix.Constituents;
import net.lghast.elemenix.utils.misc.ElemenixHolder;
import net.lghast.elemenix.utils.misc.LongContainerData;
import net.lghast.elemenix.utils.elemenix.Elemenix;
import net.lghast.elemenix.utils.elemenix.ElemenixInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class DeconstructorBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, ElemenixHolder {
    public static final int INPUT_SLOT_FIRST = 0;
    public static final int SLOT_COUNT = 3;

    private static final String TAG_STORAGE = "ElemenixStorage";
    private static final String TAG_DC_TIMER = "DeconstructionTimer";

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final LongContainerData storage = new LongContainerData(6);

    private int deconstructionTimer = 0;
    private int workGrace = 0;

    public DeconstructorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DECONSTRUCTOR.get(), pos, state);
    }

    @Override
    protected @NotNull Component getDefaultName() {
        return Component.translatable("container.elemenix.elemenic_deconstructor");
    }

    @Override
    protected @NotNull NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(@NotNull NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected @NotNull AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory) {
        return new DeconstructorMenu(containerId, playerInventory, this);
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

        deconstructionTimer = tag.getInt(TAG_DC_TIMER);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        ContainerHelper.saveAllItems(tag, items, provider);

        tag.putLongArray(TAG_STORAGE, storage.getData());
        tag.putInt(TAG_DC_TIMER, deconstructionTimer);
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;
        if (!Elemenics.started) return;

        boolean wasWorking = state.getValue(DeconstructorBlock.WORKING);
        boolean wasDisabled = state.getValue(DeconstructorBlock.DISABLED);
        boolean redstone = level.hasNeighborSignal(pos);

        boolean hasItem = hasAnyItem();
        boolean canWork = !redstone && hasDeconstructable();

        boolean isWorking = false;
        boolean isDisabled = redstone || (hasItem && !hasDeconstructable());

        if (canWork) {
            deconstructionTimer++;
            if (deconstructionTimer >= getDeconstructorBlock().getDcInterval()) {
                deconstructionTimer = 0;
                deconstructAll();
            }
            isWorking = true;
        } else {
            deconstructionTimer = 0;
        }

        if (isWorking) {
            workGrace = 20;
        } else if (workGrace > 0) {
            workGrace--;
            isWorking = true;
        }

        if (wasWorking != isWorking || wasDisabled != isDisabled) {
            level.setBlock(pos, state
                    .setValue(DeconstructorBlock.WORKING, isWorking)
                    .setValue(DeconstructorBlock.DISABLED, isDisabled), Block.UPDATE_ALL);
        }

        setChanged(level, pos, state);
    }

    private boolean hasAnyItem() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (!getItem(i).isEmpty()) return true;
        }
        return false;
    }

    private boolean hasDeconstructable() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (isDeconstructable(i)) return true;
        }
        return false;
    }

    private boolean isDeconstructable(int slot) {
        ItemStack stack = getItem(slot);
        if (stack.isEmpty()) return false;
        if (ElemenixInfo.isUndeconstructable(stack)) return false;

        Constituents constituents = ElemenixInfo.getDiscountAppliedConstituents(stack);
        return !constituents.isUnanalysable();
    }

    private void deconstructAll() {
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            if (isDeconstructable(slot)) {
                deconstructItem(slot);
            }
        }
    }

    private void deconstructItem(int slot) {
        ItemStack stack = getItem(slot);
        Constituents constituents = ElemenixInfo.getDiscountAppliedConstituents(stack);

        for (Elemenix elemenix : Elemenix.values()) {
            long value = constituents.get(elemenix);
            if (value > 0) {
                addElemenix(elemenix, value);
            }
        }

        stack.shrink(1);
        setChanged();
    }

    public DeconstructorBlock getDeconstructorBlock() {
        return getBlockState().getBlock() instanceof DeconstructorBlock block ? block : null;
    }

    public LongContainerData getDataAccess() {
        return storage;
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

    @Override
    public long getStored(Elemenix type) {
        return storage.getLong(type.getIndex());
    }

    @Override
    public boolean canExtract(Elemenix type) {
        return true;
    }

    @Override
    public boolean canReceive(Elemenix type) {
        return false;
    }

    @Override
    public void extract(Elemenix type, long amount) {
        deductElemenix(type, amount);
    }

    @Override
    public void receive(Elemenix type, long amount) {}

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot >= INPUT_SLOT_FIRST && slot < SLOT_COUNT) {
            return !ElemenixInfo.isUndeconstructable(stack) && !stack.is(ModTags.IGNORED_BY_DECONSTRUCTOR_INPUT);
        }
        return false;
    }

    @Override
    public boolean canTakeItem(Container target, int slot, ItemStack stack) {
        return false;
    }

    @Override
    public int @NotNull [] getSlotsForFace(Direction side) {
        if (side == Direction.DOWN) {
            return new int[0];
        }
        return new int[]{INPUT_SLOT_FIRST, INPUT_SLOT_FIRST + 1, INPUT_SLOT_FIRST + 2};
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @org.jetbrains.annotations.Nullable Direction direction) {
        if (direction == null || direction == Direction.DOWN) {
            return false;
        }
        return index >= INPUT_SLOT_FIRST && index < SLOT_COUNT && canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return false;
    }
}
