package net.lghast.elemenix.common.content.blockentity;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.common.content.block.EjectorBlock;
import net.lghast.elemenix.common.content.block.OutletBlock;
import net.lghast.elemenix.register.content.ModBlockEntities;
import net.lghast.elemenix.utils.elemenix.Elemenix;
import net.lghast.elemenix.utils.misc.ElemenixHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class EjectorBlockEntity extends BlockEntity implements ElemenixHolder {
    public static final int FILTER_ALL = 6;

    private static final String TAG_STORAGE = "ElemenixStorage";

    private final int[] elemenixStorage = new int[6];
    private int workGrace = 0;

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            if (index < 6) return elemenixStorage[index];
            return getFilterIndex();
        }

        @Override
        public void set(int index, int value) {
            if (index < 6) {
                elemenixStorage[index] = Math.max(0, value);
            } else {
                setFilterIndex(value);
            }
        }

        @Override
        public int getCount() {
            return 7;
        }
    };

    public EjectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.EJECTOR.get(), pos, state);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;
        if (!Elemenics.started) return;

        boolean wasWorking = state.getValue(EjectorBlock.WORKING);
        boolean wasDisabled = state.getValue(EjectorBlock.DISABLED);
        boolean disabled = level.hasNeighborSignal(pos);

        boolean isWorking = !disabled && isFrontOutletWorking(level);

        if (isWorking) {
            workGrace = 20;
        } else if (workGrace > 0) {
            workGrace--;
            isWorking = true;
        }

        if (wasWorking != isWorking || wasDisabled != disabled) {
            level.setBlock(pos, state.setValue(EjectorBlock.WORKING, isWorking).setValue(EjectorBlock.DISABLED, disabled), Block.UPDATE_ALL);
        }
    }

    private boolean isFrontOutletWorking(Level level) {
        Direction front = getFront();
        BlockState frontState = level.getBlockState(worldPosition.relative(front));
        return frontState.getBlock() instanceof OutletBlock && frontState.getValue(OutletBlock.WORKING);
    }

    public Direction getFront() {
        return getBlockState().getValue(BlockStateProperties.ORIENTATION).front();
    }

    public boolean isDisabled() {
        return level != null && level.hasNeighborSignal(worldPosition);
    }

    public int getFilterIndex() {
        return getBlockState().getValue(EjectorBlock.FILTER);
    }

    public void setFilterIndex(int index) {
        int clamped = index < 0 || index > FILTER_ALL ? FILTER_ALL : index;
        Level level = this.level;
        if (level != null) {
            BlockState state = getBlockState();
            if (state.getValue(EjectorBlock.FILTER) != clamped) {
                level.setBlock(worldPosition, state.setValue(EjectorBlock.FILTER, clamped), Block.UPDATE_CLIENTS);
            }
        }
        setChanged();
    }

    public ContainerData getDataAccess() {
        return dataAccess;
    }

    public int[] getElemenixStorage() {
        return elemenixStorage.clone();
    }

    @Override
    public long getStored(Elemenix type) {
        return elemenixStorage[type.getIndex()];
    }

    @Override
    public boolean canExtract(Elemenix type) {
        return true;
    }

    @Override
    public boolean canReceive(Elemenix type) {
        return true;
    }

    @Override
    public void extract(Elemenix type, long amount) {
        if (amount <= 0) return;
        int index = type.getIndex();
        elemenixStorage[index] = (int) Math.max(0L, elemenixStorage[index] - amount);
        setChanged();
    }

    @Override
    public void receive(Elemenix type, long amount) {
        if (amount <= 0) return;
        int index = type.getIndex();
        elemenixStorage[index] = (int) Math.min((long) elemenixStorage[index] + amount, Integer.MAX_VALUE);
        setChanged();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains(TAG_STORAGE, CompoundTag.TAG_INT_ARRAY)) {
            int[] stored = tag.getIntArray(TAG_STORAGE);
            System.arraycopy(stored, 0, elemenixStorage, 0, Math.min(stored.length, 6));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putIntArray(TAG_STORAGE, elemenixStorage);
    }
}
