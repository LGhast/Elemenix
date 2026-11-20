package net.lghast.elemenix.common.content.blockentity;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.common.content.block.ReplicatorBlock;
import net.lghast.elemenix.common.content.item.MemorizerItem;
import net.lghast.elemenix.common.content.item.WitchcraftMemorizerItem;
import net.lghast.elemenix.common.system.datacomponent.ItemMemories;
import net.lghast.elemenix.common.system.datacomponent.PotionMemories;
import net.lghast.elemenix.common.system.datacomponent.Style;
import net.lghast.elemenix.common.system.menu.ReplicatorMenu;
import net.lghast.elemenix.config.CommonConfig;
import net.lghast.elemenix.register.content.ModBlockEntities;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.register.system.ModDataComponents;
import net.lghast.elemenix.register.system.ModTags;
import net.lghast.elemenix.core.Elemenix;
import net.lghast.elemenix.common.system.transfer.ElemenixHolder;
import net.lghast.elemenix.common.system.menu.LongContainerData;
import net.lghast.elemenix.common.system.transfer.SidedInvHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class ReplicatorBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, ElemenixHolder {
    public static final int MASTER_SLOT = 0;
    public static final int STYLE_SLOT = 1;
    public static final int INPUT_SLOT_FIRST = 2;
    public static final int INPUT_SLOT_COUNT = 4;
    public static final int OUTPUT_SLOT_FIRST = 6;
    public static final int OUTPUT_SLOT_COUNT = 4;
    public static final int SLOT_COUNT = 10;

    private static final String TAG_STORAGE = "EnergixStorage";
    private static final String TAG_REPLICATE_TIMER = "ReplicateTimer";
    private static final String TAG_REPLICATING = "Replicating";

    private static final long MAX_ENERGIX = Integer.MAX_VALUE;

    private static final int[] HOPPER_SLOTS = {1, 2, 3, 4, 5, 6, 7, 8, 9};

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final LongContainerData storage = new LongContainerData(6);

    private int replicateTimer = 0;
    private ItemStack replicatingStack = ItemStack.EMPTY;

    public ReplicatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MEMORIZER_REPLICATOR.get(), pos, state);
    }

    @Override
    protected @NotNull Component getDefaultName() {
        return Component.translatable("container.elemenix.memorizer_replicator");
    }

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
        return new ReplicatorMenu(containerId, playerInventory, this);
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    public LongContainerData getDataAccess() {
        return storage;
    }

    public ItemStack getMasterStack() {
        return getItem(MASTER_SLOT);
    }

    @Nullable
    public List<String> getMemoryKeys() {
        ItemStack master = getItem(MASTER_SLOT);
        if (master.is(ModItems.ELEMENIC_MEMORIZER)) {
            ItemMemories itemMemories = MemorizerItem.getOrCreateMemories(master);
            List<String> keys = new ArrayList<>();
            for (ResourceLocation id : itemMemories.memories()) {
                keys.add(id.toString());
            }
            return keys;
        }
        if (master.is(ModItems.WITCHCRAFT_MEMORIZER)) {
            List<PotionMemories.Entry> entries = WitchcraftMemorizerItem.getOrCreateMemories(master).entries();
            List<String> keys = new ArrayList<>();
            for (PotionMemories.Entry entry : entries) {
                keys.add(entry.itemId() + "|" + entry.potionId());
            }
            return keys;
        }
        return null;
    }

    public ItemStack createEntryStack(String key) {
        int separator = key.indexOf('|');
        if (separator > 0 && separator < key.length() - 1) {
            ResourceLocation itemId = ResourceLocation.tryParse(key.substring(0, separator));
            ResourceLocation potionId = ResourceLocation.tryParse(key.substring(separator + 1));
            if (itemId == null || potionId == null) return ItemStack.EMPTY;
            Item item = BuiltInRegistries.ITEM.get(itemId);
            Optional<Holder.Reference<Potion>> potion = BuiltInRegistries.POTION
                    .getHolder(ResourceKey.create(Registries.POTION, potionId));
            return potion.map(p -> PotionContents.createItemStack(item, p)).orElse(ItemStack.EMPTY);
        }
        ResourceLocation id = ResourceLocation.tryParse(key);
        if (id == null) return ItemStack.EMPTY;
        return new ItemStack(BuiltInRegistries.ITEM.get(id));
    }

    private boolean isInputSlot(int slot) {
        return slot >= INPUT_SLOT_FIRST && slot < INPUT_SLOT_FIRST + INPUT_SLOT_COUNT;
    }

    public ItemStack getReplicatingItemStack() {
        return replicatingStack;
    }

    private int getReplicationInterval() {
        return CommonConfig.REPLICATOR_REPLICATION_INTERVAL.get();
    }

    private int getMemoryCount(ItemStack master) {
        if (master.is(ModItems.ELEMENIC_MEMORIZER)) {
            return MemorizerItem.getOrCreateMemories(master).memories().size();
        }
        return WitchcraftMemorizerItem.getOrCreateMemories(master).entries().size();
    }

    public int getMasterMemoryCount() {
        ItemStack master = getItem(MASTER_SLOT);
        if (master.is(ModItems.ELEMENIC_MEMORIZER)) {
            return MemorizerItem.getOrCreateMemories(master).memories().size();
        }
        if (master.is(ModItems.WITCHCRAFT_MEMORIZER)) {
            return WitchcraftMemorizerItem.getOrCreateMemories(master).entries().size();
        }
        return 0;
    }

    private boolean canReplicate() {
        ItemStack master = getItem(MASTER_SLOT);
        if (master.isEmpty()) return false;

        boolean elemenic = master.is(ModItems.ELEMENIC_MEMORIZER);
        boolean witchcraft = master.is(ModItems.WITCHCRAFT_MEMORIZER);
        if (!elemenic && !witchcraft) return false;

        int memoryCount = getMemoryCount(master);
        if (memoryCount <= 0) return false;

        if (CommonConfig.REPLICATOR_FORBID_READONLY_REPLICATION.get()) {
            boolean readonly = elemenic ? MemorizerItem.isReadonly(master) : WitchcraftMemorizerItem.isReadonly(master);
            if (readonly) return false;
        }

        if (findEmptyInputMemorizer() < 0) return false;
        if (findEmptyOutputSlot() < 0) return false;

        long required = (long) memoryCount * CommonConfig.REPLICATOR_REPLICATION_ENERGIX_COST.get();
        return storage.getLong(Elemenix.ENERGIX.getIndex()) >= required;
    }

    private int findEmptyInputMemorizer() {
        ItemStack master = getItem(MASTER_SLOT);
        for (int i = INPUT_SLOT_FIRST; i < INPUT_SLOT_FIRST + INPUT_SLOT_COUNT; i++) {
            ItemStack stack = getItem(i);
            if (stack.isEmpty()) continue;
            if (master.is(ModItems.ELEMENIC_MEMORIZER)) {
                if (stack.is(ModItems.ELEMENIC_MEMORIZER) && MemorizerItem.getOrCreateMemories(stack).isEmpty()) {
                    return i;
                }
            } else if (master.is(ModItems.WITCHCRAFT_MEMORIZER)) {
                if (stack.is(ModItems.WITCHCRAFT_MEMORIZER) && WitchcraftMemorizerItem.getOrCreateMemories(stack).isEmpty()) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int findEmptyOutputSlot() {
        for (int i = OUTPUT_SLOT_FIRST; i < OUTPUT_SLOT_FIRST + OUTPUT_SLOT_COUNT; i++) {
            if (getItem(i).isEmpty()) return i;
        }
        return -1;
    }

    private ItemStack createReplicaPreview() {
        ItemStack master = getItem(MASTER_SLOT);
        ItemStack preview = master.is(ModItems.ELEMENIC_MEMORIZER)
                ? new ItemStack(ModItems.ELEMENIC_MEMORIZER.get())
                : new ItemStack(ModItems.WITCHCRAFT_MEMORIZER.get());
        applyStyle(preview);
        return preview;
    }

    private void applyStyle(ItemStack stack) {
        ItemStack styleStack = getItem(STYLE_SLOT);
        if (styleStack.isEmpty()) return;
        Integer styleIndex = MemorizerItem.getStyleIndexFor(styleStack.getItem());
        if (styleIndex != null) {
            stack.set(ModDataComponents.STYLE, new Style(styleIndex));
        }
    }

    private ItemStack createReplica() {
        ItemStack master = getItem(MASTER_SLOT);
        if (master.is(ModItems.ELEMENIC_MEMORIZER)) {
            ItemStack replica = new ItemStack(ModItems.ELEMENIC_MEMORIZER.get());
            ItemMemories itemMemories = MemorizerItem.getOrCreateMemories(master);
            replica.set(ModDataComponents.ITEM_MEMORIES, itemMemories.withMemories(itemMemories.memories()));
            applyStyle(replica);
            return replica;
        }
        ItemStack replica = new ItemStack(ModItems.WITCHCRAFT_MEMORIZER.get());
        PotionMemories memoryData = WitchcraftMemorizerItem.getOrCreateMemories(master);
        replica.set(ModDataComponents.POTION_MEMORIES, memoryData.withEntries(memoryData.entries()));
        applyStyle(replica);
        return replica;
    }

    private boolean processReplication() {
        if (!canReplicate()) {
            resetReplication();
            return false;
        }
        if (replicatingStack.isEmpty()) {
            replicatingStack = createReplicaPreview();
            setChanged();
        }
        replicateTimer++;
        if (replicateTimer >= getReplicationInterval()) {
            produceReplica();
            replicateTimer = 0;
            replicatingStack = ItemStack.EMPTY;
            setChanged();
        }
        return true;
    }

    private void produceReplica() {
        int inputSlot = findEmptyInputMemorizer();
        int outputSlot = findEmptyOutputSlot();
        if (inputSlot < 0 || outputSlot < 0) return;

        ItemStack master = getItem(MASTER_SLOT);
        ItemStack replica = createReplica();

        getItem(inputSlot).shrink(1);

        long cost = (long) getMemoryCount(master) * CommonConfig.REPLICATOR_REPLICATION_ENERGIX_COST.get();
        storage.setLong(Elemenix.ENERGIX.getIndex(), storage.getLong(Elemenix.ENERGIX.getIndex()) - cost);

        ItemStack styleStack = getItem(STYLE_SLOT);
        if (!styleStack.isEmpty()) {
            styleStack.shrink(1);
        }

        setItem(outputSlot, replica);
        setChanged();
    }

    private void resetReplication() {
        if (replicateTimer != 0 || !replicatingStack.isEmpty()) {
            replicateTimer = 0;
            replicatingStack = ItemStack.EMPTY;
            setChanged();
        }
    }

    private boolean isOutputSlot(int slot) {
        return slot >= OUTPUT_SLOT_FIRST && slot < OUTPUT_SLOT_FIRST + OUTPUT_SLOT_COUNT;
    }

    private boolean isInputStackAllowed(ItemStack stack) {
        ItemStack master = getItem(MASTER_SLOT);
        if (master.is(ModItems.ELEMENIC_MEMORIZER)) {
            return stack.is(ModItems.ELEMENIC_MEMORIZER);
        }
        if (master.is(ModItems.WITCHCRAFT_MEMORIZER)) {
            return stack.is(ModItems.WITCHCRAFT_MEMORIZER);
        }
        return false;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == MASTER_SLOT) {
            return stack.is(ModItems.ELEMENIC_MEMORIZER) || stack.is(ModItems.WITCHCRAFT_MEMORIZER);
        }
        if (slot == STYLE_SLOT) {
            return stack.is(ModTags.MEMORIZER_STYLE_MATERIALS);
        }
        if (isInputSlot(slot)) {
            return isInputStackAllowed(stack);
        }
        return false;
    }

    @Override
    public boolean canTakeItem(Container target, int slot, ItemStack stack) {
        return isOutputSlot(slot);
    }

    @Override
    public int @NotNull [] getSlotsForFace(Direction side) {
        return HOPPER_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return isOutputSlot(index);
    }

    public IItemHandler getItemHandler(@Nullable Direction side) {
        return new SidedInvHandler(this, side);
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
        return type == Elemenix.ENERGIX;
    }

    @Override
    public void extract(Elemenix type, long amount) {}

    @Override
    public void receive(Elemenix type, long amount) {
        if (type != Elemenix.ENERGIX || amount <= 0) return;
        int index = Elemenix.ENERGIX.getIndex();
        long current = storage.getLong(index);
        storage.setLong(index, Math.min(current + amount, MAX_ENERGIX));
        setChanged();
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;
        if (!Elemenics.started) return;

        ItemStack master = getItem(MASTER_SLOT);
        boolean hasMemorizer = master.is(ModItems.ELEMENIC_MEMORIZER) || master.is(ModItems.WITCHCRAFT_MEMORIZER);
        boolean witchcraft = master.is(ModItems.WITCHCRAFT_MEMORIZER);
        boolean disabled = level.hasNeighborSignal(pos);
        boolean working = !disabled && processReplication();

        if (state.getValue(ReplicatorBlock.HAS_MEMORIZER) != hasMemorizer
                || state.getValue(ReplicatorBlock.WITCHCRAFT) != witchcraft
                || state.getValue(ReplicatorBlock.DISABLED) != disabled
                || state.getValue(ReplicatorBlock.WORKING) != working) {
            level.setBlock(pos, state
                    .setValue(ReplicatorBlock.HAS_MEMORIZER, hasMemorizer)
                    .setValue(ReplicatorBlock.WITCHCRAFT, witchcraft)
                    .setValue(ReplicatorBlock.DISABLED, disabled)
                    .setValue(ReplicatorBlock.WORKING, working), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        ContainerHelper.loadAllItems(tag, items, provider);
        if (tag.contains(TAG_STORAGE, CompoundTag.TAG_LONG)) {
            storage.setLong(Elemenix.ENERGIX.getIndex(), tag.getLong(TAG_STORAGE));
        }
        replicateTimer = tag.getInt(TAG_REPLICATE_TIMER);
        if (tag.contains(TAG_REPLICATING)) {
            replicatingStack = ItemStack.parseOptional(provider, tag.getCompound(TAG_REPLICATING));
        } else {
            replicatingStack = ItemStack.EMPTY;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        ContainerHelper.saveAllItems(tag, items, provider);
        tag.putLong(TAG_STORAGE, storage.getLong(Elemenix.ENERGIX.getIndex()));
        tag.putInt(TAG_REPLICATE_TIMER, replicateTimer);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putLong(TAG_STORAGE, storage.getLong(Elemenix.ENERGIX.getIndex()));
        tag.putInt(TAG_REPLICATE_TIMER, replicateTimer);
        if (!replicatingStack.isEmpty()) {
            tag.put(TAG_REPLICATING, replicatingStack.saveOptional(registries));
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
}
