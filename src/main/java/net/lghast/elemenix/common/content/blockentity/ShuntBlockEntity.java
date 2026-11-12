package net.lghast.elemenix.common.content.blockentity;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.common.content.block.ShuntBlock;
import net.lghast.elemenix.common.system.advancement.ShuntBindTrigger;
import net.lghast.elemenix.register.content.ModBlockEntities;
import net.lghast.elemenix.utils.elemenix.Elemenix;
import net.lghast.elemenix.utils.misc.ElemenixHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * An advanced six-block version of the {@link EjectorBlockEntity}. Six adjacent, co-facing,
 * co-planar shunts each picking a distinct elemenix type bind into one group that shares a
 * single int[6] storage (kept on the master). Front faces only output their own type; all
 * other faces output the remaining types and also accept input.
 */
@ParametersAreNonnullByDefault
public class ShuntBlockEntity extends BlockEntity implements ElemenixHolder {
    public static final int FILTER_UNSPECIFIED = 6;

    private static final String TAG_STORAGE = "ElemenixStorage";
    private static final String TAG_MASTER = "Master";
    private static final String TAG_GROUP = "Group";
    private static final String TAG_POS = "Pos";
    private static final String TAG_OWNER = "Owner";

    private final int[] elemenixStorage = new int[6];
    private BlockPos masterPos = null;
    private List<BlockPos> groupMembers = null;
    private UUID ownerUuid = null;

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            if (index < 6) return getMaster().elemenixStorage[index];
            return getFilterIndex();
        }

        @Override
        public void set(int index, int value) {
            if (index < 6) {
                getMaster().elemenixStorage[index] = Math.max(0, value);
            } else {
                setFilterIndex(value);
            }
        }

        @Override
        public int getCount() {
            return 7;
        }
    };

    public ShuntBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SHUNT.get(), pos, state);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;
        if (!Elemenics.started) return;

        boolean bound = masterPos != null;
        if (!bound) {
            bound = tryBind();
        }

        boolean disabled = isGroupDisabled();
        boolean working = bound && !disabled;

        boolean wasWorking = state.getValue(ShuntBlock.WORKING);
        boolean wasDisabled = state.getValue(ShuntBlock.DISABLED);

        if (wasWorking != working || wasDisabled != disabled) {
            level.setBlock(pos, state.setValue(ShuntBlock.WORKING, working).setValue(ShuntBlock.DISABLED, disabled), Block.UPDATE_ALL);
        }
    }

    private boolean tryBind() {
        if (getFilterIndex() == FILTER_UNSPECIFIED) return false;

        List<ShuntBlockEntity> group = findGroup();
        if (group.size() != 6) return false;

        Set<Integer> filters = new HashSet<>();
        for (ShuntBlockEntity be : group) {
            if (be.getFilterIndex() == FILTER_UNSPECIFIED || !filters.add(be.getFilterIndex())) {
                return false;
            }
        }

        ShuntBlockEntity master = group.stream()
                .min(Comparator.comparingLong(be -> be.worldPosition.asLong()))
                .orElse(this);

        List<BlockPos> members = new ArrayList<>();
        for (ShuntBlockEntity be : group) {
            members.add(be.worldPosition);
        }

        for (ShuntBlockEntity be : group) {
            be.masterPos = master.worldPosition;
            be.groupMembers = be == master ? members : null;
            be.setChanged();
        }
        triggerBindAdvancement(group);
        return true;
    }

    private void triggerBindAdvancement(List<ShuntBlockEntity> group) {
        if (level == null) return;
        var server = level.getServer();
        if (server == null) return;
        Set<UUID> owners = new HashSet<>();
        for (ShuntBlockEntity be : group) {
            if (be.getOwnerUuid() != null) {
                owners.add(be.getOwnerUuid());
            }
        }
        for (UUID owner : owners) {
            ServerPlayer player = server.getPlayerList().getPlayer(owner);
            if (player != null) {
                ShuntBindTrigger.TRIGGER.get().trigger(player);
            }
        }
    }

    private List<ShuntBlockEntity> findGroup() {
        Direction front = getFront();
        int planeCoord = worldPosition.get(front.getAxis());
        List<ShuntBlockEntity> group = new ArrayList<>();
        Deque<ShuntBlockEntity> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(this);
        visited.add(worldPosition);

        while (!queue.isEmpty()) {
            ShuntBlockEntity current = queue.poll();
            group.add(current);
            if (group.size() >= 6) break;
            if (level == null) continue;
            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = current.worldPosition.relative(dir);
                if (visited.contains(neighborPos)) continue;
                if (level.getBlockEntity(neighborPos) instanceof ShuntBlockEntity neighbor
                        && neighbor.masterPos == null
                        && neighbor.getFront() == front
                        && neighbor.worldPosition.get(front.getAxis()) == planeCoord) {
                    visited.add(neighborPos);
                    queue.add(neighbor);
                }
            }
        }
        return group;
    }

    private boolean isGroupDisabled() {
        if (level == null) return false;
        ShuntBlockEntity master = getMaster();
        if (master.groupMembers != null) {
            for (BlockPos memberPos : master.groupMembers) {
                if (level.hasNeighborSignal(memberPos)) {
                    return true;
                }
            }
            return false;
        }
        return level.hasNeighborSignal(worldPosition);
    }

    private ShuntBlockEntity getMaster() {
        if (level == null || masterPos == null || masterPos.equals(worldPosition)) {
            return this;
        }
        if (level.getBlockEntity(masterPos) instanceof ShuntBlockEntity master) {
            return master;
        }
        return this;
    }

    public void unbindGroup() {
        if (level == null) return;
        List<ShuntBlockEntity> members = new ArrayList<>();
        ShuntBlockEntity master = getMaster();
        if (master.groupMembers != null) {
            for (BlockPos p : master.groupMembers) {
                if (level.getBlockEntity(p) instanceof ShuntBlockEntity be) {
                    members.add(be);
                }
            }
        } else if (this.masterPos != null) {
            members.add(this);
        }
        for (ShuntBlockEntity be : members) {
            be.masterPos = null;
            be.groupMembers = null;
            be.setChanged();
        }
    }

    public Direction getFront() {
        return getBlockState().getValue(BlockStateProperties.ORIENTATION).front();
    }

    public int getFilterIndex() {
        return getBlockState().getValue(ShuntBlock.FILTER);
    }

    public boolean isBound() {
        return masterPos != null;
    }

    public List<BlockPos> getGroupMembers() {
        ShuntBlockEntity master = getMaster();
        if (master.groupMembers != null) {
            return List.copyOf(master.groupMembers);
        }
        return List.of();
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public void setOwnerUuid(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
        setChanged();
    }

    public void setFilterIndex(int index) {
        int clamped = index < 0 || index > FILTER_UNSPECIFIED ? FILTER_UNSPECIFIED : index;
        unbindGroup();
        Level level = this.level;
        if (level != null) {
            BlockState state = getBlockState();
            if (state.getValue(ShuntBlock.FILTER) != clamped) {
                level.setBlock(worldPosition, state.setValue(ShuntBlock.FILTER, clamped), Block.UPDATE_CLIENTS);
            }
        }
        setChanged();
    }

    public ContainerData getDataAccess() {
        return dataAccess;
    }

    public int[] getElemenixStorage() {
        return getMaster().elemenixStorage.clone();
    }

    @Override
    public long getStored(Elemenix type) {
        return getMaster().elemenixStorage[type.getIndex()];
    }

    @Override
    public boolean canExtract(Elemenix type) {
        return masterPos != null;
    }

    @Override
    public boolean canReceive(Elemenix type) {
        return masterPos != null;
    }

    @Override
    public void extract(Elemenix type, long amount) {
        if (amount <= 0) return;
        ShuntBlockEntity master = getMaster();
        int index = type.getIndex();
        master.elemenixStorage[index] = (int) Math.max(0L, master.elemenixStorage[index] - amount);
        master.setChanged();
    }

    @Override
    public void receive(Elemenix type, long amount) {
        if (amount <= 0) return;
        ShuntBlockEntity master = getMaster();
        int index = type.getIndex();
        master.elemenixStorage[index] = (int) Math.min((long) master.elemenixStorage[index] + amount, Integer.MAX_VALUE);
        master.setChanged();
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putIntArray(TAG_STORAGE, elemenixStorage);
        if (masterPos != null) {
            tag.putLong(TAG_MASTER, masterPos.asLong());
        }
        if (groupMembers != null) {
            ListTag list = new ListTag();
            for (BlockPos p : groupMembers) {
                CompoundTag pt = new CompoundTag();
                pt.putLong(TAG_POS, p.asLong());
                list.add(pt);
            }
            tag.put(TAG_GROUP, list);
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
    protected void loadAdditional(CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains(TAG_STORAGE, Tag.TAG_INT_ARRAY)) {
            int[] stored = tag.getIntArray(TAG_STORAGE);
            System.arraycopy(stored, 0, elemenixStorage, 0, Math.min(stored.length, 6));
        }
        if (tag.contains(TAG_MASTER, Tag.TAG_LONG)) {
            masterPos = BlockPos.of(tag.getLong(TAG_MASTER));
        }
        if (tag.contains(TAG_GROUP, Tag.TAG_LIST)) {
            groupMembers = new ArrayList<>();
            ListTag list = tag.getList(TAG_GROUP, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                groupMembers.add(BlockPos.of(list.getCompound(i).getLong(TAG_POS)));
            }
        }
        if (tag.hasUUID(TAG_OWNER)) {
            ownerUuid = tag.getUUID(TAG_OWNER);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putIntArray(TAG_STORAGE, elemenixStorage);
        if (masterPos != null) {
            tag.putLong(TAG_MASTER, masterPos.asLong());
        }
        if (groupMembers != null) {
            ListTag list = new ListTag();
            for (BlockPos p : groupMembers) {
                CompoundTag pt = new CompoundTag();
                pt.putLong(TAG_POS, p.asLong());
                list.add(pt);
            }
            tag.put(TAG_GROUP, list);
        }
        if (ownerUuid != null) {
            tag.putUUID(TAG_OWNER, ownerUuid);
        }
    }
}
