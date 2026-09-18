package net.lghast.elemenix.common.system.menu;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.common.content.blockentity.InfuserBlockEntity;
import net.lghast.elemenix.common.content.item.AnalyzerItem;
import net.lghast.elemenix.common.content.item.MemorizerItem;
import net.lghast.elemenix.common.content.item.RemoteStorageItem;
import net.lghast.elemenix.common.content.item.StorageItem;
import net.lghast.elemenix.common.system.datacomponent.ElemenicStorage;
import net.lghast.elemenix.common.system.datacomponent.MemoryData;
import net.lghast.elemenix.common.system.datacomponent.RemoteStorageBinding;
import net.lghast.elemenix.common.system.datacomponent.UuidData;
import net.lghast.elemenix.compat.ftbquests.util.AnalyzerTaskHelper;
import net.lghast.elemenix.conifig.CommonConfig;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.register.system.ModDataComponents;
import net.lghast.elemenix.register.system.ModMenus;
import net.lghast.elemenix.register.system.ModStats;
import net.lghast.elemenix.register.system.ModTags;
import net.lghast.elemenix.utils.Constituents;
import net.lghast.elemenix.utils.elemenix.Elemenix;
import net.lghast.elemenix.utils.elemenix.ElemenixInfo;
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
    private final ItemStack originalAnalyzerStack;
    private final int lockedPlayerInventorySlot;

    public AnalyzerMenu(int containerId, Inventory playerInventory, ItemStack menuStack) {
        this(containerId, playerInventory, new SimpleContainer(SLOT_COUNT), new LongContainerData(12), menuStack);
    }

    public AnalyzerMenu(int containerId, Inventory playerInventory, Container analyzerContainer, LongContainerData data, ItemStack menuStack) {
        super(ModMenus.ELEMENIX_ANALYZER_MENU.get(), containerId);

        checkContainerSize(analyzerContainer, SLOT_COUNT);
        checkContainerDataCount(data, 12);

        this.analyzerContainer = analyzerContainer;
        this.data = data;
        this.player = playerInventory.player;
        this.originalAnalyzerStack = menuStack;

        this.analyzerUuid = AnalyzerItem.getOrCreateUuid(menuStack).uuid();
        this.lockedPlayerInventorySlot = findPlayerInventorySlotByAnalyzerUuid(playerInventory, analyzerUuid);

        loadMemorizerItem(menuStack);
        loadAnalyzerStorage();

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
                return isMemorizerSlotPlaceable(stack);
            }

            @Override
            public void setChanged() {
                super.setChanged();

                AnalyzerMenu.this.slotsChanged(analyzerContainer);
                AnalyzerMenu.this.saveMemorySlotChangedOnly();
            }
        });

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                int slotIndex = j + i * 9 + 9;

                this.addSlot(createPlayerInventorySlot(
                        playerInventory,
                        slotIndex,
                        89 + j * 18,
                        117 + i * 18
                ));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlot(createPlayerInventorySlot(
                    playerInventory,
                    i,
                    89 + i * 18,
                    175
            ));
        }
        this.addDataSlots(data);
    }

    private static boolean isMemorizerSlotPlaceable(ItemStack stack) {
        return stack.is(ModItems.ELEMENIC_MEMORIZER);
    }

    private void loadMemorizerItem(ItemStack analyzerStack) {
        analyzerContainer.setItem(MEMORIZER_SLOT, ItemStack.EMPTY);
        ItemContainerContents containerContents = analyzerStack.get(DataComponents.CONTAINER);

        if (containerContents == null || containerContents.getSlots() <= 0) {
            return;
        }

        ItemStack memorizerStack = containerContents.getStackInSlot(0);
        if (!memorizerStack.isEmpty()) {
            analyzerContainer.setItem(MEMORIZER_SLOT, memorizerStack.copy());
        }
    }

    private void saveMemorizerItem(ItemStack analyzerStack) {
        if (analyzerStack.isEmpty()) {
            return;
        }
        if (!(analyzerStack.getItem() instanceof AnalyzerItem)) {
            return;
        }

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

    private void saveAnalyzerStorageOnly() {
        ItemStack analyzerStack = getAnalyzerStackForSave();

        if (analyzerStack.isEmpty()) {
            return;
        }

        long[] elemenix = new long[] {
                data.getLong(0),
                data.getLong(1),
                data.getLong(2),
                data.getLong(3),
                data.getLong(4),
                data.getLong(5)
        };

        analyzerStack.set(
                ModDataComponents.ELEMENIC_STORAGE.get(),
                new ElemenicStorage(elemenix)
        );
    }

    private void saveAnalyzerMemorizerSlotOnly() {
        ItemStack analyzerStack = getAnalyzerStackForSave();

        if (analyzerStack.isEmpty()) {
            return;
        }

        saveMemorizerItem(analyzerStack);
    }

    private void syncInputSlotToClient() {
        if (player.level().isClientSide) {
            return;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        serverPlayer.connection.send(
                new ClientboundContainerSetSlotPacket(
                        containerId,
                        serverPlayer.containerMenu.incrementStateId(),
                        INPUT_SLOT,
                        analyzerContainer.getItem(INPUT_SLOT)
                )
        );
    }

    public void saveStorageAndMemoryData() {
        saveAnalyzerStorageOnly();
        saveAnalyzerMemorizerSlotOnly();
        syncInputSlotToClient();
    }

    private void saveMemorySlotChangedOnly() {
        saveAnalyzerMemorizerSlotOnly();
        broadcastChanges();
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

    private ItemStack getAnalyzerStackForSave() {
        ItemStack current = AnalyzerItem.findAnalyzerByUuid(player, analyzerUuid);

        if (!current.isEmpty()) {
            return current;
        }

        if (isSameAnalyzer(originalAnalyzerStack, analyzerUuid)) {
            return originalAnalyzerStack;
        }

        return ItemStack.EMPTY;
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

        if (inputStack.is(ModItems.ELEMENIC_STORAGE) || inputStack.is(ModItems.SCANNING_STORAGE)) {
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
        if(Elemenics.isFtbQuestsLoaded() && player instanceof ServerPlayer serverPlayer) {
            AnalyzerTaskHelper.handleDeconstructProgress(serverPlayer, inputStack.copy(), inputStack.getCount());
        }

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

            if(Elemenics.isFtbQuestsLoaded() && player instanceof ServerPlayer serverPlayer) {
                AnalyzerTaskHelper.handleMemorizeProgress(serverPlayer, inputStack.copy());
            }

            slots.get(MEMORIZER_SLOT).setChanged();
        }
    }

    public boolean canRecordInputItem(ItemStack inputStack, ItemStack memorizerStack) {
        if (inputStack.isEmpty() || inputStack.is(ModTags.ANALYZER_UNRECORDABLE)) {
            return false;
        }

        if (!CommonConfig.ALLOW_NON_CREATIVE_RECORDING.get() && !player.getAbilities().instabuild) {
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
        if(Elemenics.isFtbQuestsLoaded() && player instanceof ServerPlayer serverPlayer) {
            AnalyzerTaskHelper.handleReconstructProgress(serverPlayer, resultStack.copy(), maxAmount);
        }

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
        if (player != this.player) {
            return false;
        }

        return !AnalyzerItem.findAnalyzerByUuid(player, analyzerUuid).isEmpty();
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= this.slots.size()) {
            return ItemStack.EMPTY;
        }

        if (isLockedPlayerInventoryMenuSlot(index)) {
            return ItemStack.EMPTY;
        }

        ItemStack result;
        Slot slot = this.slots.get(index);

        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stackInSlot = slot.getItem();
        result = stackInSlot.copy();

        if (index == INPUT_SLOT) {
            if (!this.moveItemStackTo(stackInSlot, SLOT_COUNT, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (index == MEMORIZER_SLOT) {
            if (!this.moveItemStackTo(stackInSlot, SLOT_COUNT, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (isMemorizerSlotPlaceable(stackInSlot)) {
            if (!this.moveItemStackTo(stackInSlot, MEMORIZER_SLOT, MEMORIZER_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (isInputSlotPlaceable(stackInSlot)) {
            if (!this.moveItemStackTo(stackInSlot, INPUT_SLOT, INPUT_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (stackInSlot.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        saveMemorySlotChangedOnly();
        return result;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);

        if (player.level().isClientSide) {
            return;
        }

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

    private int findPlayerInventorySlotByAnalyzerUuid(Inventory inventory, UUID uuid) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);

            if (isSameAnalyzer(stack, uuid)) {
                return i;
            }
        }

        return -1;
    }

    private boolean isSameAnalyzer(ItemStack stack, UUID uuid) {
        if (stack.isEmpty()) {
            return false;
        }

        if (!(stack.getItem() instanceof AnalyzerItem)) {
            return false;
        }

        UuidData currentUuid = stack.get(ModDataComponents.ANALYZER_UUID.get());

        return currentUuid != null && uuid.equals(currentUuid.uuid());
    }

    private Slot createPlayerInventorySlot(Inventory inventory, int slotIndex, int x, int y) {
        if (slotIndex == lockedPlayerInventorySlot) {
            return new Slot(inventory, slotIndex, x, y) {

                @Override
                public boolean mayPickup(Player player) {
                    return false;
                }

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            };
        }

        return new Slot(inventory, slotIndex, x, y);
    }

    private boolean isLockedPlayerInventoryMenuSlot(int menuIndex) {
        if (lockedPlayerInventorySlot < 0) {
            return false;
        }

        if (menuIndex < SLOT_COUNT || menuIndex >= this.slots.size()) {
            return false;
        }

        Slot slot = this.slots.get(menuIndex);

        return slot.getSlotIndex() == lockedPlayerInventorySlot;
    }
}
