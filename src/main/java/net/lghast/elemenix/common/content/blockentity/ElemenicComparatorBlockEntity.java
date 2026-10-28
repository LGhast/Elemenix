package net.lghast.elemenix.common.content.blockentity;

import net.lghast.elemenix.common.content.block.ElemenicComparatorBlock;
import net.lghast.elemenix.register.content.ModBlockEntities;
import net.lghast.elemenix.utils.elemenix.Elemenix;
import net.lghast.elemenix.utils.misc.ElemenixHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ElemenicComparatorBlockEntity extends BlockEntity {
    public static final int MAX_THRESHOLD = 9999999;
    public static final int TARGET_SUM = 6;
    public static final int TARGET_ANY = 7;

    private static final long[] UNIT_MULTIPLIERS = {1L, 10_000L, 100_000_000L, 1_000_000_000_000L};

    private int threshold = 0;
    private int unit = 0;
    private int tickCounter = 0;

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> threshold;
                case 1 -> unit;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> threshold = Mth.clamp(value, 0, MAX_THRESHOLD);
                case 1 -> unit = Mth.clamp(value, 0, 3);
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public ElemenicComparatorBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.COMPARATOR.get(), pos, blockState);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;
        tickCounter++;
        if (tickCounter < 2) return;
        tickCounter = 0;

        boolean passing = isPassing();
        boolean powered = state.getValue(ElemenicComparatorBlock.POWERED);
        if (passing != powered) {
            level.setBlock(pos, state.setValue(ElemenicComparatorBlock.POWERED, passing), Block.UPDATE_ALL);
        }
    }

    public boolean isPassing() {
        return isPassing(0);
    }

    public boolean isOwnCriterionPassing() {
        ElemenixHolder holder = findStorageHolder();
        return holder != null && checkCriterion(getBlockState(), holder);
    }

    private ElemenixHolder findStorageHolder() {
        Level level = this.level;
        if (level == null) return null;
        BlockState state = getBlockState();
        Direction facing = state.getValue(ElemenicComparatorBlock.FACING);

        // 找到链首比较器后方的储存方块
        BlockPos storage = worldPosition.relative(facing.getOpposite());
        for (int i = 0; i < 16; i++) {
            BlockEntity be = level.getBlockEntity(storage);
            if (be instanceof ElemenicComparatorBlockEntity other
                    && other.getBlockState().getValue(ElemenicComparatorBlock.FACING) == facing) {
                storage = storage.relative(facing.getOpposite());
            } else {
                break;
            }
        }
        return level.getBlockEntity(storage) instanceof ElemenixHolder holder ? holder : null;
    }

    private boolean isPassing(int depth) {
        if (depth > 16) return true;
        Level level = this.level;
        if (level == null) return false;
        BlockState state = getBlockState();
        Direction facing = state.getValue(ElemenicComparatorBlock.FACING);

        ElemenixHolder holder = findStorageHolder();
        if (holder == null) return false;
        if (!checkCriterion(state, holder)) return false;

        // 取“并”：紧邻后方的比较器也必须通过
        BlockPos behind = worldPosition.relative(facing.getOpposite());
        BlockEntity behindBe = level.getBlockEntity(behind);
        if (behindBe instanceof ElemenicComparatorBlockEntity other
                && other.getBlockState().getValue(ElemenicComparatorBlock.FACING) == facing) {
            return other.isPassing(depth + 1);
        }
        return true;
    }

    private boolean checkCriterion(BlockState state, ElemenixHolder holder) {
        int target = state.getValue(ElemenicComparatorBlock.TARGET);
        int rule = state.getValue(ElemenicComparatorBlock.RULE);
        long actualThreshold = (long) threshold * UNIT_MULTIPLIERS[unit];

        if (target == TARGET_ANY) {
            for (Elemenix type : Elemenix.values()) {
                if (matchesRule(holder.getStored(type), actualThreshold, rule)) return true;
            }
            return false;
        }

        long value;
        if (target == TARGET_SUM) {
            long sum = 0;
            for (Elemenix type : Elemenix.values()) {
                sum = saturatingAdd(sum, holder.getStored(type));
            }
            value = sum;
        } else {
            value = holder.getStored(Elemenix.values()[target]);
        }

        return matchesRule(value, actualThreshold, rule);
    }

    private static boolean matchesRule(long value, long threshold, int rule) {
        return switch (rule) {
            case 0 -> value > threshold;
            case 1 -> value < threshold;
            case 2 -> value == threshold;
            case 3 -> value >= threshold;
            case 4 -> value <= threshold;
            default -> false;
        };
    }

    private static long saturatingAdd(long a, long b) {
        if (b > 0 && a > Long.MAX_VALUE - b) return Long.MAX_VALUE;
        return a + b;
    }

    public void setCriterion(int target, int rule, int newThreshold, int newUnit) {
        this.threshold = Mth.clamp(newThreshold, 0, MAX_THRESHOLD);
        this.unit = Mth.clamp(newUnit, 0, 3);
        Level level = this.level;
        if (level != null) {
            BlockState state = getBlockState();
            level.setBlock(worldPosition, state
                    .setValue(ElemenicComparatorBlock.TARGET, Mth.clamp(target, 0, 7))
                    .setValue(ElemenicComparatorBlock.RULE, Mth.clamp(rule, 0, 4)),
                    Block.UPDATE_CLIENTS);
        }
        setChanged();
    }

    public ContainerData getDataAccess() {
        return dataAccess;
    }

    public int getThreshold() {
        return threshold;
    }

    public int getUnit() {
        return unit;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Threshold", threshold);
        tag.putInt("Unit", unit);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        threshold = tag.getInt("Threshold");
        unit = tag.getInt("Unit");
    }
}
