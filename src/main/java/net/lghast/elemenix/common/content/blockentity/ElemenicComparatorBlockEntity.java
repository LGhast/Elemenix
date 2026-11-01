package net.lghast.elemenix.common.content.blockentity;

import net.lghast.elemenix.common.content.block.ElemenicComparatorBlock;
import net.lghast.elemenix.common.system.advancement.ComparatorChainTrigger;
import net.lghast.elemenix.register.content.ModBlockEntities;
import net.lghast.elemenix.utils.elemenix.Elemenix;
import net.lghast.elemenix.utils.misc.ElemenixHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@ParametersAreNonnullByDefault
public class ElemenicComparatorBlockEntity extends BlockEntity {
    public static final int MAX_THRESHOLD = 9999999;
    public static final int TARGET_SUM = 6;
    public static final int TARGET_ANY = 7;

    private static final long[] UNIT_MULTIPLIERS = {1L, 10_000L, 100_000_000L, 1_000_000_000_000L};

    private int threshold = 0;
    private int unit = 0;
    private int tickCounter = 0;
    private UUID ownerUuid = null;

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
            if (passing) {
                tryTriggerChainAdvancement(level);
            }
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

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public void setOwnerUuid(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
        setChanged();
    }

    /**
     * Fires the chain advancement when this comparator turns on and the whole chain
     * of at least five serially connected comparators uses pairwise-distinct criteria.
     */
    private void tryTriggerChainAdvancement(Level level) {
        List<ElemenicComparatorBlockEntity> chain = collectChain();
        if (chain.size() < 5) return;

        for (ElemenicComparatorBlockEntity be : chain) {
            if (be == this) continue;
            if (!be.getBlockState().getValue(ElemenicComparatorBlock.POWERED)) return;
        }

        Set<Long> criteria = new HashSet<>();
        for (ElemenicComparatorBlockEntity be : chain) {
            BlockState s = be.getBlockState();
            long key = (long) s.getValue(ElemenicComparatorBlock.TARGET) * 8 + s.getValue(ElemenicComparatorBlock.RULE);
            criteria.add(key);
        }
        if (criteria.size() < 5) return;

        for (ElemenicComparatorBlockEntity be : chain) {
            UUID owner = be.getOwnerUuid();
            if (owner == null) continue;

            var server = level.getServer();
            if (server == null) return;

            ServerPlayer player = server.getPlayerList().getPlayer(owner);
            if (player != null) {
                ComparatorChainTrigger.TRIGGER.get().trigger(player);
            }
        }
    }

    private List<ElemenicComparatorBlockEntity> collectChain() {
        Level level = this.level;
        if (level == null) return List.of();
        Direction facing = getBlockState().getValue(ElemenicComparatorBlock.FACING);
        List<ElemenicComparatorBlockEntity> chain = new ArrayList<>();
        chain.add(this);

        BlockPos cursor = worldPosition.relative(facing.getOpposite());
        for (int i = 0; i < 16; i++) {
            BlockEntity be = level.getBlockEntity(cursor);
            if (be instanceof ElemenicComparatorBlockEntity other && other.getBlockState().getValue(ElemenicComparatorBlock.FACING) == facing) {
                chain.add(other);
                cursor = cursor.relative(facing.getOpposite());
            } else {
                break;
            }
        }

        cursor = worldPosition.relative(facing);
        for (int i = 0; i < 16; i++) {
            BlockEntity be = level.getBlockEntity(cursor);
            if (be instanceof ElemenicComparatorBlockEntity other && other.getBlockState().getValue(ElemenicComparatorBlock.FACING) == facing) {
                chain.add(other);
                cursor = cursor.relative(facing);
            } else {
                break;
            }
        }

        return chain;
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putInt("Threshold", threshold);
        tag.putInt("Unit", unit);
        if (ownerUuid != null) {
            tag.putUUID("Owner", ownerUuid);
        }
        return tag;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Threshold", threshold);
        tag.putInt("Unit", unit);
        if (ownerUuid != null) {
            tag.putUUID("Owner", ownerUuid);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        threshold = tag.getInt("Threshold");
        unit = tag.getInt("Unit");
        ownerUuid = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
    }
}
