package net.lghast.elemenix.common.content.blockentity;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.common.content.block.InfuserBlock;
import net.lghast.elemenix.common.content.item.RemoteStorageItem;
import net.lghast.elemenix.common.content.item.StorageItem;
import net.lghast.elemenix.common.system.datacomponent.ElemenicStorage;
import net.lghast.elemenix.common.system.datacomponent.RemoteStorageBinding;
import net.lghast.elemenix.common.system.menu.InfuserMenu;
import net.lghast.elemenix.network.infuser.InfuserDataUpdatePayload;
import net.lghast.elemenix.register.content.ModBlockEntities;
import net.lghast.elemenix.register.system.ModDataComponents;
import net.lghast.elemenix.utils.misc.ElemenixHolder;
import net.lghast.elemenix.utils.misc.SidedInvHandler;
import net.lghast.elemenix.utils.elemenix.Elemenix;
import net.lghast.elemenix.utils.misc.ModUtils;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Objects;

@ParametersAreNonnullByDefault
public class InfuserBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, ElemenixHolder {
    private static final int STORAGE_SLOT = 0;
    private static final int SLOT_COUNT = 1;

    private final static String TAG_STORAGE = "ElemenixStorage";
    private final String TAG_INFUSION_TIMER = "InfusionTimer";

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final int[] elemenixStorage = new int[6];

    private int infusionTimer = 0;
    private int workGrace = 0;

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return elemenixStorage[index];
        }

        @Override
        public void set(int index, int value) {
            elemenixStorage[index] = Math.max(0, value);
        }

        @Override
        public int getCount() {
            return 6;
        }
    };

    public InfuserBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INFUSER.get(), pos, state);
        Arrays.fill(elemenixStorage, 0);
    }

    @Override
    protected @NotNull Component getDefaultName() {
        return Component.translatable("container.elemenix.elemenic_infuser");
    }

    @Override
    protected @NotNull NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected @NotNull AbstractContainerMenu createMenu(int containerId, Inventory playerInventory) {
        return new InfuserMenu(containerId, playerInventory, this);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        ContainerHelper.loadAllItems(tag, items, provider);

        if (tag.contains(TAG_STORAGE, CompoundTag.TAG_INT_ARRAY)) {
            int[] stored = tag.getIntArray(TAG_STORAGE);
            System.arraycopy(stored, 0, elemenixStorage, 0, Math.min(stored.length, 6));
        }

        infusionTimer = tag.getInt(TAG_INFUSION_TIMER);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        ContainerHelper.saveAllItems(tag, items, provider);

        tag.putIntArray(TAG_STORAGE, elemenixStorage);

        tag.putInt(TAG_INFUSION_TIMER, infusionTimer);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;
        if(!Elemenics.started) return;

        boolean wasWorking = state.getValue(InfuserBlock.WORKING);
        boolean wasDisabled = state.getValue(InfuserBlock.DISABLED);
        boolean disabled = level.hasNeighborSignal(pos);
        boolean isWorking = false;

        if (disabled) {
            infusionTimer = 0;
        } else if (canInfuse()) {
            infusionTimer++;
            InfuserBlock block = getInfuserBlock();

            if (infusionTimer >= block.getInfusionInterval()) {
                infuse();
                infusionTimer = 0;
            }
            isWorking = true;
        } else {
            infusionTimer = 0;
        }

        if (isWorking) {
            workGrace = 20;
        } else if (workGrace > 0) {
            workGrace--;
            isWorking = true;
        }

        if (wasWorking != isWorking || wasDisabled != disabled) {
            level.setBlock(pos, state.setValue(InfuserBlock.WORKING, isWorking).setValue(InfuserBlock.DISABLED, disabled), Block.UPDATE_ALL);
        }

        setChanged(level, pos, state);
    }

    private boolean canInfuse() {
        ItemStack storageStack = getItem(STORAGE_SLOT);
        return !storageStack.isEmpty() && storageStack.getItem() instanceof StorageItem && hasElemenixToInfuse();
    }

    private boolean hasElemenixToInfuse() {
        for (int value : elemenixStorage) {
            if (value > 0) return true;
        }
        return false;
    }

    private void infuse() {
        ItemStack storageStack = getItem(STORAGE_SLOT);
        if (storageStack.isEmpty() || !(storageStack.getItem() instanceof StorageItem)) return;

        InfuserBlock block = (InfuserBlock) getBlockState().getBlock();
        int maxInfusion = block.getMaxInfusion();

        ElemenicStorage currentStorage = StorageItem.getOrCreateData(storageStack);
        long[] newElemenix = currentStorage.elemenix().clone();

        boolean infused = false;

        for (Elemenix elemenix : Elemenix.values()) {
            int index = elemenix.getIndex();
            int available = elemenixStorage[index];

            if (available > 0) {
                int toInfuse = Math.min(available, maxInfusion);
                long newValue = ModUtils.safeAdd(newElemenix[index], toInfuse);

                if (newValue != newElemenix[index]) {
                    newElemenix[index] = newValue;
                    elemenixStorage[index] -= toInfuse;
                    infused = true;
                }
            }
        }

        if (infused) {
            storageStack.set(ModDataComponents.ELEMENIC_STORAGE, new ElemenicStorage(newElemenix));
            setChanged();

            broadcastStorageUpdate();
        }
    }

    public void addElemenix(Elemenix elemenix, int amount) {
        if (amount <= 0) return;
        int index = elemenix.getIndex();
        if (index >= 0 && index < 6) {
            elemenixStorage[index] = (int) Math.min((long) elemenixStorage[index] + amount, Integer.MAX_VALUE);
            setChanged();
        }
    }

    public InfuserBlock getInfuserBlock() {
        return getBlockState().getBlock() instanceof InfuserBlock block ? block : null;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == STORAGE_SLOT && stack.getItem() instanceof StorageItem;
    }

    @Override
    public int @NotNull [] getSlotsForFace(Direction side) {
        return new int[0];
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @org.jetbrains.annotations.Nullable Direction direction) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return false;
    }

    @Override
    public boolean canTakeItem(Container target, int slot, ItemStack stack) {
        return false;
    }

    @Override
    public long getStored(Elemenix type) {
        return elemenixStorage[type.getIndex()];
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
    public void extract(Elemenix type, long amount) {
        // no-op: the infuser is a target only
    }

    @Override
    public void receive(Elemenix type, long amount) {
        addElemenix(type, (int) amount);
    }

    public IItemHandler getItemHandler(@org.jetbrains.annotations.Nullable Direction side) {
        return new SidedInvHandler(this, side);
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    public ContainerData getDataAccess() {
        return dataAccess;
    }

    public int[] getElemenixStorage() {
        return elemenixStorage.clone();
    }

    public void broadcastStorageUpdate() {
        if (level != null && !level.isClientSide) {
            ItemStack storageStack = getItem(0);
            if (storageStack.getItem() instanceof StorageItem) {
                ElemenicStorage storage = StorageItem.getOrCreateData(storageStack);

                InfuserDataUpdatePayload payload = new InfuserDataUpdatePayload(
                        GlobalPos.of(level.dimension(), worldPosition),
                        storage.elemenix()
                );

                for (ServerPlayer player : Objects.requireNonNull(level.getServer()).getPlayerList().getPlayers()) {
                    if (playerHasBoundRemoteStorage(player, worldPosition)) {
                        PacketDistributor.sendToPlayer(player, payload);
                    }
                }
            }
        }
    }

    private boolean playerHasBoundRemoteStorage(ServerPlayer player, BlockPos pos) {
        for (ItemStack stack : player.getInventory().items) {
            if (isRemoteStorageBoundTo(stack, pos, player.level().dimension())) {
                return true;
            }
        }

        if (isRemoteStorageBoundTo(player.getOffhandItem(), pos, player.level().dimension())) {
            return true;
        }

        for (ItemStack stack : player.getArmorSlots()) {
            if (isRemoteStorageBoundTo(stack, pos, player.level().dimension())) {
                return true;
            }
        }

        return false;
    }

    private boolean isRemoteStorageBoundTo(ItemStack stack, BlockPos pos, ResourceKey<Level> dimension) {
        if (stack.getItem() instanceof RemoteStorageItem) {
            RemoteStorageBinding binding = stack.get(ModDataComponents.REMOTE_STORAGE_BINDING.get());
            if (binding != null && binding.isBound() && binding.boundPos().isPresent()) {
                GlobalPos boundPos = binding.boundPos().get();
                return boundPos.pos().equals(pos) && boundPos.dimension() == dimension;
            }
        }
        return false;
    }
}
