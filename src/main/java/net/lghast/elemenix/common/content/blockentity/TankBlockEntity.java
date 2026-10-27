package net.lghast.elemenix.common.content.blockentity;

import net.lghast.elemenix.register.content.ModBlockEntities;
import net.lghast.elemenix.utils.misc.ElemenixHolder;
import net.lghast.elemenix.utils.misc.LongContainerData;
import net.lghast.elemenix.utils.elemenix.Elemenix;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TankBlockEntity extends BlockEntity implements ElemenixHolder {
    private static final String TAG_STORAGE = "ElemenixStorage";

    private final LongContainerData storage = new LongContainerData(6);

    public TankBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TANK.get(), pos, state);
    }

    @Override
    public long getStored(Elemenix elemenix) {
        return storage.getLong(elemenix.getIndex());
    }

    @Override
    public boolean canExtract(Elemenix type) {
        return true;
    }

    @Override
    public boolean canReceive(Elemenix type) {
        return level == null || !level.hasNeighborSignal(worldPosition);
    }

    @Override
    public void extract(Elemenix type, long amount) {
        deductElemenix(type, amount);
    }

    @Override
    public void receive(Elemenix type, long amount) {
        if (level != null && level.hasNeighborSignal(worldPosition)) return;
        addElemenix(type, amount);
    }

    public LongContainerData getDataAccess() {
        return storage;
    }

    public long[] getStoredAmounts() {
        return storage.getData();
    }

    public boolean isEmpty() {
        for (long value : storage.getData()) {
            if (value != 0L) {
                return false;
            }
        }
        return true;
    }

    private boolean skipDrop = false;

    public void setSkipDrop(boolean skipDrop) {
        this.skipDrop = skipDrop;
    }

    public boolean shouldSkipDrop() {
        return skipDrop;
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
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains(TAG_STORAGE, CompoundTag.TAG_LONG_ARRAY)) {
            long[] stored = tag.getLongArray(TAG_STORAGE);
            for (int i = 0; i < Math.min(stored.length, 6); i++) {
                storage.setLong(i, stored[i]);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putLongArray(TAG_STORAGE, storage.getData());
    }
}
