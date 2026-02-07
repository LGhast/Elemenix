package net.lghast.elemenix.common.system.menu;

import net.lghast.elemenix.common.content.blockentity.InfuserBlockEntity;
import net.lghast.elemenix.common.content.item.AnalyzerItem;
import net.lghast.elemenix.common.content.item.MemorizerItem;
import net.lghast.elemenix.common.content.item.RemoteStorageItem;
import net.lghast.elemenix.common.content.item.StorageItem;
import net.lghast.elemenix.common.system.datacomponent.ElemenicStorage;
import net.lghast.elemenix.common.system.datacomponent.MemoryData;
import net.lghast.elemenix.common.system.datacomponent.RemoteStorageBinding;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.register.system.ModDataComponents;
import net.lghast.elemenix.register.system.ModMenus;
import net.lghast.elemenix.register.system.ModStats;
import net.lghast.elemenix.register.system.ModTags;
import net.lghast.elemenix.utils.Constituents;
import net.lghast.elemenix.utils.Elemenix;
import net.lghast.elemenix.utils.ElemenixInfo;
import net.lghast.elemenix.utils.LongContainerData;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
public class AnalyzerMenu extends AbstractContainerMenu {
    private static final int INPUT_SLOT = 0;
    private static final int MEMORIZER_SLOT = 1;
    private static final int SLOT_COUNT = 2;

    private static final int INPUT_SLOT_X = 22;
    private static final int MEMORIZER_SLOT_X = 52;
    private static final int SLOT_Y = 130;

    private final Container analyzerContainer;
    private final UUID analyzerUuid;
    private final LongContainerData data;
    private final Player player;

    public AnalyzerMenu(int containerId, Inventory playerInventory, ItemStack menuStack) {
        this(containerId, playerInventory, new SimpleContainer(SLOT_COUNT), new LongContainerData(12), menuStack);
    }

    public AnalyzerMenu(int containerId, Inventory playerInventory, Container analyzerContainer, LongContainerData data, ItemStack menuStack) {
        super(ModMenus.ELEMENIX_ANALYZER_MENU.get(), containerId);
        this.analyzerContainer = analyzerContainer;
        this.analyzerUuid = AnalyzerItem.getOrCreateUuid(menuStack).uuid();
        this.data = data;
        this.player = playerInventory.player;

        loadMemorizerItem(menuStack);
        loadAnalyzerStorage();
        checkContainerSize(analyzerContainer, SLOT_COUNT);
        checkContainerDataCount(data, 12);

        this.addSlot(new Slot(analyzerContainer, INPUT_SLOT, INPUT_SLOT_X, SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isInputSlotPlaceable(stack);
            }

            @Override
            public void setChanged() {
                super.setChanged();
                AnalyzerMenu.this.slotsChanged(analyzerContainer);
            }
        });

        this.addSlot(new Slot(analyzerContainer, MEMORIZER_SLOT, MEMORIZER_SLOT_X, SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModTags.MEMORIZER_SLOT_PLACEABLE);
            }

            @Override
            public void setChanged() {
                super.setChanged();
                AnalyzerMenu.this.slotsChanged(analyzerContainer);
            }
        });

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 89 + j * 18, 117 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 89 + i * 18, 175));
        }

        this.addDataSlots(data);
    }

    private void loadMemorizerItem(ItemStack analyzerStack) {
        ItemContainerContents containerContents = analyzerStack.get(DataComponents.CONTAINER);
        if (containerContents != null && containerContents.getSlots() > 0) {
            ItemStack memorizerStack = containerContents.getStackInSlot(0);
            if (!memorizerStack.isEmpty()) {
                analyzerContainer.setItem(MEMORIZER_SLOT, memorizerStack);
            }
        }
    }

    private void saveMemorizerItem() {
        ItemStack analyzerStack = getAnalyzerStack();
        if (analyzerStack.isEmpty()) return;

        ItemStack memorizerStack = getMemorizerItem();
        List<ItemStack> items = new ArrayList<>();
        items.add(memorizerStack.copy());

        ItemContainerContents containerContents = ItemContainerContents.fromItems(items);
        analyzerStack.set(DataComponents.CONTAINER, containerContents);
    }

    public LongContainerData getLongContainerData() {
        return data;
    }

    private static boolean isInputSlotPlaceable(ItemStack stack) {
        if (stack.is(ModItems.ELEMENIC_MEMORIZER)) {
            return MemorizerItem.getOrCreateMemories(stack).resolvedItems().isEmpty();
        }
        return !ElemenixInfo.isUndeconstructable(stack) && !(stack.getItem() instanceof AnalyzerItem);
    }

    private void loadAnalyzerStorage() {
        ItemStack analyzerStack = getAnalyzerStack();
        if (analyzerStack.isEmpty()) return;

        ElemenicStorage elemenicStorage = AnalyzerItem.getOrCreateData(analyzerStack);

        data.setLong(0, elemenicStorage.organix());
        data.setLong(1, elemenicStorage.terrix());
        data.setLong(2, elemenicStorage.flumix());
        data.setLong(3, elemenicStorage.metallix());
        data.setLong(4, elemenicStorage.energix());
        data.setLong(5, elemenicStorage.arcanix());
    }

    public void saveStorageAndMemoryData() {
        ItemStack analyzerStack = getAnalyzerStack();
        if (analyzerStack.isEmpty()) return;

        long[] elemenix = new long[] {
                data.getLong(0),
                data.getLong(1),
                data.getLong(2),
                data.getLong(3),
                data.getLong(4),
                data.getLong(5)
        };
        analyzerStack.set(ModDataComponents.ELEMENIC_STORAGE.get(), new ElemenicStorage(elemenix));

        ItemStack memorizerItem = getMemorizerItem();
        if (memorizerItem.is(ModItems.ELEMENIC_MEMORIZER)) {
            List<ResourceLocation> itemMemory = MemorizerItem.getOrCreateMemories(memorizerItem).resolvedItems();
            memorizerItem.set(ModDataComponents.MEMORY_DATA.get(), new MemoryData(itemMemory));
        }

        saveMemorizerItem();

        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(
                    new ClientboundContainerSetSlotPacket(
                            containerId,
                            serverPlayer.containerMenu.incrementStateId(),
                            INPUT_SLOT,
                            analyzerContainer.getItem(INPUT_SLOT)
                    )
            );
        }
    }

    public ItemStack getInputItem() {
        return analyzerContainer.getItem(INPUT_SLOT);
    }

    public ItemStack getMemorizerItem() {
        return analyzerContainer.getItem(MEMORIZER_SLOT);
    }

    private ItemStack getAnalyzerStack() {
        return AnalyzerItem.findAnalyzerByUuid(player, analyzerUuid);
    }

    public LongContainerData getData() {
        return data;
    }

    public void deconstructItem() {
        ItemStack inputStack = getInputItem();
        if (inputStack.isEmpty()) return;

        if (inputStack.getItem() instanceof RemoteStorageItem) {
            if (RemoteStorageItem.isBound(inputStack)) {
                if (player.level() instanceof ServerLevel serverLevel) {
                    ElemenicStorage remoteStorage = RemoteStorageItem.getRemoteStorage(inputStack, serverLevel);
                    if (!remoteStorage.isEmpty()) {
                        transferFromRemoteStorage(remoteStorage);
                    }
                }
            } else {
                regularlyDeconstruct(inputStack);
            }
            return;
        }

        if (inputStack.is(ModItems.ELEMENIC_STORAGE)) {
            ElemenicStorage storageData = StorageItem.getOrCreateData(inputStack);

            if (storageData.isEmpty()) {
                regularlyDeconstruct(inputStack);
            } else {
                transferStorage(inputStack, storageData);
            }
            return;
        }

        regularlyDeconstruct(inputStack);
    }

    private void regularlyDeconstruct(ItemStack inputStack) {
        Constituents constituents = ElemenixInfo.getDiscountAppliedConstituents(inputStack);
        if (constituents.isUnanalysable()) return;

        for (Elemenix type : Elemenix.values()) {
            long amount = (long) constituents.get(type) * inputStack.getCount();
            if (amount > 0) {
                long current = data.getLong(type.getIndex());
                long newValue = current + amount;
                data.setLong(type.getIndex(), newValue);
            }
        }
        recordToMemorizer(inputStack);
        player.awardStat(ModStats.DECONSTRUCTED_ITEMS.get(), inputStack.getCount());

        analyzerContainer.setItem(INPUT_SLOT, ItemStack.EMPTY);

        saveStorageAndMemoryData();
        broadcastChanges();
    }

    private void transferStorage(ItemStack storageStack, ElemenicStorage storageData) {
        long[] storageElemenix = storageData.elemenix();
        long[] analyzerElemenix = new long[6];

        for (int i = 0; i < 6; i++) {
            analyzerElemenix[i] = data.getLong(i);
        }

        boolean anyTransferred = false;

        for (int i = 0; i < 6; i++) {
            long storageAmount = storageElemenix[i];
            if (storageAmount <= 0) continue;

            long analyzerAmount = analyzerElemenix[i];
            long maxTransfer = Long.MAX_VALUE - analyzerAmount;

            if (maxTransfer <= 0) {
                continue;
            }

            long transferAmount = Math.min(storageAmount, maxTransfer);
            data.setLong(i, analyzerAmount + transferAmount);
            storageElemenix[i] -= transferAmount;

            anyTransferred = true;
        }

        if (anyTransferred) {
            storageStack.set(ModDataComponents.ELEMENIC_STORAGE.get(), new ElemenicStorage(storageElemenix));
            player.awardStat(ModStats.STORAGE_TRANSFERS.get());

            getSlot(INPUT_SLOT).setChanged();
            saveStorageAndMemoryData();
            broadcastChanges();
        }
    }

    private void transferFromRemoteStorage(ElemenicStorage remoteStorage) {
        long[] remoteElemenix = remoteStorage.elemenix();
        long[] analyzerElemenix = new long[6];

        for (int i = 0; i < 6; i++) {
            analyzerElemenix[i] = data.getLong(i);
        }

        boolean anyTransferred = false;

        for (int i = 0; i < 6; i++) {
            long remoteAmount = remoteElemenix[i];
            if (remoteAmount <= 0) continue;

            long analyzerAmount = analyzerElemenix[i];
            long maxTransfer = Long.MAX_VALUE - analyzerAmount;

            if (maxTransfer <= 0) {
                continue;
            }

            long transferAmount = Math.min(remoteAmount, maxTransfer);
            data.setLong(i, analyzerAmount + transferAmount);
            updateRemoteStorage(i, transferAmount);

            anyTransferred = true;
        }

        if (anyTransferred) {
            player.awardStat(ModStats.REMOTE_STORAGE_TRANSFERS.get());
            saveStorageAndMemoryData();
            broadcastChanges();
        }
    }

    private void updateRemoteStorage(int index, long amount) {
        ItemStack remoteStack = getInputItem();
        RemoteStorageBinding binding = remoteStack.get(ModDataComponents.REMOTE_STORAGE_BINDING.get());

        if (binding == null || !binding.isBound() || !(player.level() instanceof ServerLevel serverLevel)) return;

        Optional<GlobalPos> globalPosOptional = binding.boundPos();
        if (globalPosOptional.isEmpty()) return;

        GlobalPos globalPos = globalPosOptional.get();

        if (serverLevel.dimension() != globalPos.dimension()) return;

        BlockEntity blockEntity = serverLevel.getBlockEntity(globalPos.pos());
        if (!(blockEntity instanceof InfuserBlockEntity infuser)) return;

        ItemStack storageStack = infuser.getItem(0);
        if (!(storageStack.getItem() instanceof StorageItem)) return;

        ElemenicStorage currentStorage = StorageItem.getOrCreateData(storageStack);
        long[] newElemenix = currentStorage.elemenix().clone();

        if (newElemenix[index] >= amount) {
            newElemenix[index] -= amount;
            storageStack.set(ModDataComponents.ELEMENIC_STORAGE.get(), new ElemenicStorage(newElemenix));
            infuser.setChanged();
        }
    }

    private void recordToMemorizer(ItemStack inputStack) {
        ItemStack memorizerStack = getMemorizerItem();
        if (canRecordInputItem(inputStack, memorizerStack)) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(inputStack.getItem());
            MemoryData currentMemoryData = MemorizerItem.getOrCreateMemories(memorizerStack);
            List<ResourceLocation> currentMemories = currentMemoryData.resolvedItems();
            List<ResourceLocation> newMemories = new ArrayList<>(currentMemories);

            newMemories.add(itemId);
            player.awardStat(ModStats.MEMORIZER_RECORDS_ADDED.get());
            memorizerStack.set(ModDataComponents.MEMORY_DATA.get(), currentMemoryData.withResolvedItems(newMemories));
            slots.get(MEMORIZER_SLOT).setChanged();
        }
    }

    public boolean canRecordInputItem(ItemStack inputStack, ItemStack memorizerStack) {
        if (inputStack.isEmpty() || inputStack.is(ModTags.ANALYZER_UNRECORDABLE)) {
            return false;
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(inputStack.getItem());
        List<ResourceLocation> memories = MemorizerItem.getOrCreateMemories(memorizerStack).resolvedItems();

        return !MemorizerItem.isReadonly(memorizerStack) && MemorizerItem.isNotFull(memorizerStack) && !memories.contains(itemId);
    }

    public void reconstructItem(ResourceLocation itemId, boolean shiftClick) {
        Item item = BuiltInRegistries.ITEM.get(itemId);
        if (!getCarried().isEmpty() && !getCarried().is(item)) return;

        ItemStack resultStack = new ItemStack(item);
        if (resultStack.isEmpty() || ElemenixInfo.isUnreconstructable(resultStack)) return;

        Constituents required = ElemenixInfo.getPremiumAppliedConstituents(resultStack);
        if (required.isUnanalysable()) return;

        int maxStackSize = resultStack.getMaxStackSize();

        int maxAmount = shiftClick ? maxStackSize : 1;
        for (Elemenix type : Elemenix.values()) {
            int req = required.get(type);
            if (req > 0) {
                long available = data.getLong(type.getIndex()) & 0xFFFFFFFFL;
                maxAmount = (int) Math.min(maxAmount, available / req);
            }
        }

        if (maxAmount <= 0) return;

        for (Elemenix type : Elemenix.values()) {
            int consumption = required.get(type) * maxAmount;
            if (consumption > 0) {
                long current = data.getLong(type.getIndex());
                data.setLong(type.getIndex(), Math.max(0, current - consumption));
            }
        }

        resultStack.setCount(maxAmount);
        if (!getCarried().isEmpty() && ItemStack.isSameItemSameComponents(getCarried(), resultStack)) {
            getCarried().grow(maxAmount);
        } else {
            setCarried(resultStack);
        }
        player.awardStat(ModStats.RECONSTRUCTED_ITEMS.get(), maxAmount);

        saveStorageAndMemoryData();
        broadcastChanges();
    }

    public List<ResourceLocation> getMemories() {
        ItemStack memorizer = getMemorizerItem();
        if (memorizer.is(ModItems.ELEMENIC_MEMORIZER)) {
            MemoryData memoryDate = MemorizerItem.getOrCreateMemories(memorizer);
            return memoryDate.resolvedItems();
        }
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack stack1 = slot.getItem();
            stack = stack1.copy();

            if (index == INPUT_SLOT) {
                if (!this.moveItemStackTo(stack1, SLOT_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index == MEMORIZER_SLOT) {
                if (!this.moveItemStackTo(stack1, SLOT_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (stack1.getItem() instanceof MemorizerItem) {
                if (!this.moveItemStackTo(stack1, MEMORIZER_SLOT, MEMORIZER_SLOT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(stack1, INPUT_SLOT, SLOT_COUNT, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return stack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);

        if (!player.level().isClientSide) {
            saveStorageAndMemoryData();

            ItemStack inputStack = analyzerContainer.getItem(INPUT_SLOT);
            if (!inputStack.isEmpty()) {
                if (player.isAlive()) {
                    player.getInventory().placeItemBackInInventory(inputStack);
                } else {
                    player.drop(inputStack, false);
                }
                analyzerContainer.setItem(INPUT_SLOT, ItemStack.EMPTY);
            }
        }
    }
}
