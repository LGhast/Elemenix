package net.lghast.elemenix.common.content.blockentity;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.common.content.block.ChannelBlock;
import net.lghast.elemenix.common.content.block.OutletBlock;
import net.lghast.elemenix.config.CommonConfig;
import net.lghast.elemenix.register.content.ModBlockEntities;
import net.lghast.elemenix.utils.misc.ElemenixHolder;
import net.lghast.elemenix.utils.elemenix.Elemenix;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class OutletBlockEntity extends BlockEntity {
    public static final int DEFAULT_OUTPUT_PERCENT = 100;
    public static final int MIN_OUTPUT_PERCENT = 0;
    public static final int MAX_OUTPUT_PERCENT = 100;
    public static final int OUTPUT_INTERVAL = 20;
    public static final int MAX_PATH_LENGTH = 16;

    private int outputPercent = DEFAULT_OUTPUT_PERCENT;
    private int outputTimer = 0;
    private int workGrace = 0;

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return outputPercent;
        }

        @Override
        public void set(int index, int value) {
            outputPercent = clampPercent(value);
        }

        @Override
        public int getCount() {
            return 1;
        }
    };

    public OutletBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.OUTLET.get(), pos, state);
    }

    public void setOutputPercent(int percent) {
        this.outputPercent = clampPercent(percent);
        setChanged();
    }

    public ContainerData getDataAccess() {
        return dataAccess;
    }

    private static int clampPercent(int value) {
        return Math.max(MIN_OUTPUT_PERCENT, Math.min(MAX_OUTPUT_PERCENT, value));
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;
        if (!Elemenics.started) return;

        boolean wasWorking = state.getValue(OutletBlock.WORKING);
        boolean wasDisabled = state.getValue(OutletBlock.DISABLED);
        boolean disabled = level.hasNeighborSignal(pos) || outputPercent == 0;

        boolean isWorking = false;

        if (disabled) {
            outputTimer = 0;
        } else {
            ElemenixHolder source = findSource(level);
            ElemenixHolder target = findTarget(level);
            if (source != null && target != null && canTransfer(source, target)) {
                outputTimer++;
                if (outputTimer >= OUTPUT_INTERVAL) {
                    outputTimer = 0;
                    transfer(source, target);
                }
                isWorking = true;
            } else {
                outputTimer = 0;
            }
        }

        if (isWorking) {
            workGrace = 20;
        } else if (workGrace > 0) {
            workGrace--;
            isWorking = true;
        }

        if (wasWorking != isWorking || wasDisabled != disabled) {
            level.setBlock(pos, state.setValue(OutletBlock.WORKING, isWorking).setValue(OutletBlock.DISABLED, disabled), Block.UPDATE_ALL);
        }
    }

    private ElemenixHolder findSource(Level level) {
        Direction facing = getBlockState().getValue(OutletBlock.FACING);
        BlockPos sourcePos = worldPosition.relative(facing.getOpposite());
        BlockEntity be = level.getBlockEntity(sourcePos);
        return be instanceof ElemenixHolder holder ? holder : null;
    }

    private ElemenixHolder findTarget(Level level) {
        Direction current = getBlockState().getValue(OutletBlock.FACING);
        BlockPos cursor = worldPosition;

        for (int i = 0; i < MAX_PATH_LENGTH; i++) {
            cursor = cursor.relative(current);
            BlockState state = level.getBlockState(cursor);

            if (state.getBlock() instanceof ChannelBlock) {
                if (state.getValue(ChannelBlock.INPUT) != current.getOpposite()) {
                    return null;
                }
                current = state.getValue(ChannelBlock.FACING);
            } else {
                BlockEntity be = level.getBlockEntity(cursor);
                return be instanceof ElemenixHolder holder ? holder : null;
            }
        }
        return null;
    }

    private boolean canTransfer(ElemenixHolder source, ElemenixHolder target) {
        for (Elemenix type : Elemenix.values()) {
            if (source.canExtract(type) && target.canReceive(type) && source.getStored(type) > 0) {
                return true;
            }
        }
        return false;
    }

    private void transfer(ElemenixHolder source, ElemenixHolder target) {
        long cap = (long) CommonConfig.OUTLET_MAX_OUTPUT_PER_TYPE.get() * outputPercent / MAX_OUTPUT_PERCENT;
        for (Elemenix type : Elemenix.values()) {
            if (!source.canExtract(type) || !target.canReceive(type)) continue;
            long available = source.getStored(type);
            if (available <= 0) continue;
            long amount = Math.min(available, cap);
            source.extract(type, amount);
            target.receive(type, amount);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        outputPercent = clampPercent(tag.getInt("OutputPercent"));
        outputTimer = tag.getInt("OutputTimer");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("OutputPercent", outputPercent);
        tag.putInt("OutputTimer", outputTimer);
    }
}
