package net.lghast.elemenix.common.system.menu;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.common.content.blockentity.InfuserBlockEntity;
import net.lghast.elemenix.common.content.item.*;
import net.lghast.elemenix.common.system.datacomponent.ElemenicStorage;
import net.lghast.elemenix.common.system.datacomponent.MemoryData;
import net.lghast.elemenix.common.system.datacomponent.RemoteStorageBinding;
import net.lghast.elemenix.common.system.datacomponent.UuidData;
import net.lghast.elemenix.compat.ftbquests.util.AnalyzerTaskHelper;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.register.system.ModDataComponents;
import net.lghast.elemenix.register.system.ModMenus;
import net.lghast.elemenix.register.system.ModStats;
import net.lghast.elemenix.utils.Constituents;
import net.lghast.elemenix.utils.LongContainerData;
import net.lghast.elemenix.utils.elemenix.Elemenix;
import net.lghast.elemenix.utils.elemenix.ElemenixInfo;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AnalysisTerminalMenu extends AbstractContainerMenu {
    private static final int BOX_SLOTS = 18;
    private static final int BOX_SLOT_HIDDEN_X = -1000;
    private static final int BOX_SLOT_HIDDEN_Y = -1000;

    private static final int INV_X = 88;
    private static final int INV_Y = 145;
    private static final int HOTBAR_Y = 203;

    private final Container boxContainer;
    private final LongContainerData data;
    private final Player player;

    @Nullable
    private final UUID analyzerUuid;
    @Nullable
    private final UUID boxUuid;

    private final int lockedTerminalSlot;

    private int selectedMemorizer = -1;
    private boolean allowDeconstruct = true;
    private boolean allowMemory = true;
    private boolean allowTransfer = false;

    public AnalysisTerminalMenu(int containerId, Inventory playerInventory, ItemStack analyzerStack, ItemStack boxStack) {
        this(containerId, playerInventory, analyzerStack, boxStack, -1);
    }

    public AnalysisTerminalMenu(int containerId, Inventory playerInventory, ItemStack analyzerStack, ItemStack boxStack,
                                int lockedTerminalSlot) {
        this(containerId, playerInventory, new SimpleContainer(BOX_SLOTS), new LongContainerData(6),
                analyzerStack, boxStack, lockedTerminalSlot);
    }

    public AnalysisTerminalMenu(int containerId, Inventory playerInventory, Container boxContainer, LongContainerData data,
                                ItemStack analyzerStack, ItemStack boxStack, int lockedTerminalSlot) {
        super(ModMenus.ANALYSIS_TERMINAL_MENU.get(), containerId);
        checkContainerSize(boxContainer, BOX_SLOTS);
        checkContainerDataCount(data, 6);

        this.boxContainer = boxContainer;
        this.data = data;
        this.player = playerInventory.player;
        this.lockedTerminalSlot = lockedTerminalSlot;

        if (!analyzerStack.isEmpty()) {
            this.analyzerUuid = AnalyzerItem.getOrCreateUuid(analyzerStack).uuid();
        } else {
            this.analyzerUuid = null;
        }

        if (!boxStack.isEmpty()) {
            MemorizerBoxItem.createUuid(boxStack);
            UuidData boxUuidData = boxStack.get(ModDataComponents.BOX_UUID.get());
            this.boxUuid = boxUuidData != null ? boxUuidData.uuid() : null;
            loadBoxContainer(boxStack);
        } else {
            this.boxUuid = null;
        }

        loadAnalyzerStorage(analyzerStack);

        for (int i = 0; i < BOX_SLOTS; i++) {
            this.addSlot(new Slot(boxContainer, i, BOX_SLOT_HIDDEN_X, BOX_SLOT_HIDDEN_Y) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return false;
                }

                @Override
                public boolean mayPickup(@NotNull Player player) {
                    return false;
                }
            });
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(createPlayerInventorySlot(playerInventory, j + i * 9 + 9, INV_X + j * 18, INV_Y + i * 18));
            }
        }
        for (int j = 0; j < 9; j++) {
            this.addSlot(createPlayerInventorySlot(playerInventory, j, INV_X + j * 18, HOTBAR_Y));
        }

        this.addDataSlots(data);
    }

    private Slot createPlayerInventorySlot(Inventory inventory, int slotIndex, int x, int y) {
        if (slotIndex == lockedTerminalSlot) {
            return new Slot(inventory, slotIndex, x, y) {
                @Override
                public boolean mayPickup(@NotNull Player player) {
                    return false;
                }

                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return false;
                }
            };
        }
        return new Slot(inventory, slotIndex, x, y);
    }

    private boolean isLockedTerminalMenuSlot(int menuIndex) {
        if (lockedTerminalSlot < 0) {
            return false;
        }
        if (menuIndex < BOX_SLOTS || menuIndex >= this.slots.size()) {
            return false;
        }
        return this.slots.get(menuIndex).getSlotIndex() == lockedTerminalSlot;
    }

    private boolean isPlayerInventoryMenuIndex(int menuIndex) {
        return menuIndex >= BOX_SLOTS && menuIndex < this.slots.size();
    }

    public LongContainerData getLongContainerData() {
        return data;
    }

    public ItemStack getBoxItem(int index) {
        return boxContainer.getItem(index);
    }

    public List<ItemStack> getMemorizerStacks() {
        List<ItemStack> memorizers = new ArrayList<>();
        for (int i = 0; i < BOX_SLOTS; i++) {
            ItemStack stack = boxContainer.getItem(i);
            if (stack.getItem() instanceof MemorizerItem) {
                memorizers.add(stack);
            }
        }
        return memorizers;
    }

    private ItemStack getMemorizerByIndex(int index) {
        List<ItemStack> memorizers = getMemorizerStacks();
        return (index >= 0 && index < memorizers.size()) ? memorizers.get(index) : ItemStack.EMPTY;
    }

    public List<ResourceLocation> getVisibleMemories() {
        List<ItemStack> memorizers = getMemorizerStacks();
        if (selectedMemorizer >= 0) {
            if (selectedMemorizer >= memorizers.size()) {
                return List.of();
            }
            return MemorizerItem.getOrCreateMemories(memorizers.get(selectedMemorizer)).resolvedItems();
        }
        LinkedHashSet<ResourceLocation> merged = new LinkedHashSet<>();
        for (ItemStack memorizer : memorizers) {
            merged.addAll(MemorizerItem.getOrCreateMemories(memorizer).resolvedItems());
        }
        return new ArrayList<>(merged);
    }

    public void setSelectedMemorizer(int index) {
        this.selectedMemorizer = index;
        if (index >= 0 && getMemorizerByIndex(index).isEmpty()) {
            this.selectedMemorizer = -1;
        }
    }

    public void setFlags(boolean allowDeconstruct, boolean allowMemory, boolean allowTransfer) {
        this.allowDeconstruct = allowDeconstruct;
        this.allowMemory = allowMemory;
        this.allowTransfer = allowTransfer;
    }

    private void loadBoxContainer(ItemStack boxStack) {
        for (int i = 0; i < BOX_SLOTS; i++) {
            boxContainer.setItem(i, ItemStack.EMPTY);
        }
        ItemContainerContents contents = boxStack.get(DataComponents.CONTAINER);
        if (contents == null) {
            return;
        }
        int max = Math.min(contents.getSlots(), BOX_SLOTS);
        for (int i = 0; i < max; i++) {
            ItemStack stack = contents.getStackInSlot(i);
            if (!stack.isEmpty()) {
                boxContainer.setItem(i, stack.copy());
            }
        }
    }

    private void saveBoxToItemStack(ItemStack targetBoxStack) {
        if (targetBoxStack.isEmpty() || !(targetBoxStack.getItem() instanceof MemorizerBoxItem)) {
            return;
        }
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < BOX_SLOTS; i++) {
            items.add(boxContainer.getItem(i).copy());
        }
        targetBoxStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
    }

    private void saveBoxContainer() {
        if (boxUuid == null) {
            return;
        }
        ItemStack box = findBoxByUuid(player, boxUuid);
        if (!box.isEmpty()) {
            saveBoxToItemStack(box);
        }
    }

    private void loadAnalyzerStorage(ItemStack analyzerStack) {
        if (analyzerStack.isEmpty()) {
            return;
        }
        ElemenicStorage storage = AnalyzerItem.getOrCreateData(analyzerStack);
        data.setLong(0, storage.organix());
        data.setLong(1, storage.terrix());
        data.setLong(2, storage.flumix());
        data.setLong(3, storage.metallix());
        data.setLong(4, storage.energix());
        data.setLong(5, storage.arcanix());
    }

    private void saveAnalyzerStorage() {
        if (analyzerUuid == null) {
            return;
        }
        ItemStack analyzerStack = AnalyzerItem.findAnalyzerByUuid(player, analyzerUuid);
        if (analyzerStack.isEmpty()) {
            return;
        }
        long[] elemenix = new long[]{
                data.getLong(0), data.getLong(1), data.getLong(2),
                data.getLong(3), data.getLong(4), data.getLong(5)
        };
        analyzerStack.set(ModDataComponents.ELEMENIC_STORAGE.get(), new ElemenicStorage(elemenix));
    }

    private ItemStack findBoxByUuid(Player target, UUID uuid) {
        if (uuid == null) {
            return ItemStack.EMPTY;
        }
        Inventory inv = target.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (isSameBox(stack, uuid)) {
                return stack;
            }
        }
        if (isSameBox(target.getMainHandItem(), uuid)) {
            return target.getMainHandItem();
        }
        if (isSameBox(target.getOffhandItem(), uuid)) {
            return target.getOffhandItem();
        }
        Optional<ICuriosItemHandler> optional = CuriosApi.getCuriosInventory(target);
        if (optional.isPresent()) {
            Optional<SlotResult> result = optional.get().findFirstCurio(stack -> isSameBox(stack, uuid));
            if (result.isPresent()) {
                return result.get().stack();
            }
        }
        return ItemStack.EMPTY;
    }

    private boolean isSameBox(ItemStack stack, UUID uuid) {
        if (stack.isEmpty() || !(stack.getItem() instanceof MemorizerBoxItem)) {
            return false;
        }
        UuidData currentUuid = stack.get(ModDataComponents.BOX_UUID.get());
        return currentUuid != null && currentUuid.uuid().equals(uuid);
    }

    @Override
    public void clicked(int slotId, int button, @NotNull ClickType clickType, @NotNull Player player) {
        if (!player.level().isClientSide
                && clickType == ClickType.QUICK_MOVE
                && isPlayerInventoryMenuIndex(slotId)
                && !isLockedTerminalMenuSlot(slotId)
                && getCarried().isEmpty()
                && allowDeconstruct) {
            Slot slot = this.slots.get(slotId);
            if (slot.hasItem()) {
                handleShiftDeconstruct(slot);
                return;
            }
        }
        super.clicked(slotId, button, clickType, player);
    }

    private void handleShiftDeconstruct(Slot slot) {
        ItemStack stack = slot.getItem();
        if (stack.getItem() instanceof RemoteStorageItem && RemoteStorageItem.isBound(stack)) {
            if (allowTransfer) {
                transferFromRemoteStorage(stack);
            }
            return;
        }
        if (stack.is(ModItems.ELEMENIC_STORAGE) || stack.is(ModItems.SCANNING_STORAGE)) {
            ElemenicStorage storageData = StorageItem.getOrCreateData(stack);
            if (!storageData.isEmpty()) {

                if (allowTransfer) {
                    transferStorageFromSlot(slot, storageData);
                }
                return;
            }
        }
        regularlyDeconstruct(slot);
    }

    private void regularlyDeconstruct(Slot slot) {
        ItemStack inputStack = slot.getItem();
        if (inputStack.isEmpty()) {
            return;
        }
        if (inputStack.getItem() instanceof AnalyzerItem) {
            return;
        }
        Constituents constituents = ElemenixInfo.getDiscountAppliedConstituents(inputStack);
        if (constituents.isUnanalysable()) {
            return;
        }
        int count = inputStack.getCount();
        if (failToAddEssences(constituents, count)) {
            return;
        }
        addEssences(constituents, count);
        tryRecordItem(inputStack);
        player.awardStat(ModStats.DECONSTRUCTED_ITEMS.get(), count);
        if (Elemenics.isFtbQuestsLoaded() && player instanceof ServerPlayer serverPlayer) {
            AnalyzerTaskHelper.handleDeconstructProgress(serverPlayer, inputStack.copy(), count);
        }
        slot.set(ItemStack.EMPTY);
        saveAnalyzerStorage();
        saveBoxContainer();
        broadcastChanges();
    }

    public void deconstructCarriedItem() {
        if (!allowDeconstruct) {
            return;
        }
        ItemStack carried = getCarried();
        if (carried.isEmpty()) {
            return;
        }
        if (carried.getItem() instanceof RemoteStorageItem && RemoteStorageItem.isBound(carried)) {
            if (allowTransfer) {
                transferFromRemoteStorage(carried);
            }
            return;
        }
        if (carried.is(ModItems.ELEMENIC_STORAGE) || carried.is(ModItems.SCANNING_STORAGE)) {
            ElemenicStorage storageData = StorageItem.getOrCreateData(carried);
            if (!storageData.isEmpty()) {
                if (allowTransfer) {
                    transferStorageFromCarried(carried, storageData);
                }
                return;
            }
        }
        if (carried.getItem() instanceof AnalyzerItem) {
            return;
        }
        Constituents constituents = ElemenixInfo.getDiscountAppliedConstituents(carried);
        if (constituents.isUnanalysable()) {
            return;
        }
        if (failToAddEssences(constituents, 1)) {
            return;
        }
        addEssences(constituents, 1);
        tryRecordItem(carried);
        player.awardStat(ModStats.DECONSTRUCTED_ITEMS.get(), 1);
        if (Elemenics.isFtbQuestsLoaded() && player instanceof ServerPlayer serverPlayer) {
            ItemStack single = carried.copy();
            single.setCount(1);
            AnalyzerTaskHelper.handleDeconstructProgress(serverPlayer, single, 1);
        }
        if (carried.getCount() > 1) {
            carried.shrink(1);
        } else {
            carried = ItemStack.EMPTY;
        }
        setCarried(carried);
        saveAnalyzerStorage();
        saveBoxContainer();
        broadcastChanges();
    }

    private boolean failToAddEssences(Constituents constituents, int count) {
        for (Elemenix type : Elemenix.values()) {
            long amount = (long) constituents.get(type) * count;
            if (amount > 0) {
                long current = data.getLong(type.getIndex());
                if (current > Long.MAX_VALUE - amount) {
                    return true;
                }
            }
        }
        return false;
    }

    private void addEssences(Constituents constituents, int count) {
        for (Elemenix type : Elemenix.values()) {
            long amount = (long) constituents.get(type) * count;
            if (amount > 0) {
                data.setLong(type.getIndex(), data.getLong(type.getIndex()) + amount);
            }
        }
    }

    private boolean transferIntoAnalyzer(long[] sourceElemenix) {
        boolean anyTransferred = false;
        for (int i = 0; i < 6; i++) {
            long amount = sourceElemenix[i];
            if (amount <= 0) {
                continue;
            }
            long current = data.getLong(i);
            long maxTransfer = Long.MAX_VALUE - current;
            if (maxTransfer <= 0) {
                continue;
            }
            long transferAmount = Math.min(amount, maxTransfer);
            data.setLong(i, current + transferAmount);
            sourceElemenix[i] -= transferAmount;
            anyTransferred = true;
        }
        return anyTransferred;
    }

    private void transferStorageFromSlot(Slot slot, ElemenicStorage storageData) {
        long[] elemenix = storageData.elemenix().clone();
        if (transferIntoAnalyzer(elemenix)) {
            slot.getItem().set(ModDataComponents.ELEMENIC_STORAGE.get(), new ElemenicStorage(elemenix));
            slot.setChanged();
            player.awardStat(ModStats.STORAGE_TRANSFERS.get());
            saveAnalyzerStorage();
            broadcastChanges();
        }
    }

    private void transferStorageFromCarried(ItemStack carried, ElemenicStorage storageData) {
        long[] elemenix = storageData.elemenix().clone();
        if (transferIntoAnalyzer(elemenix)) {
            carried.set(ModDataComponents.ELEMENIC_STORAGE.get(), new ElemenicStorage(elemenix));
            setCarried(carried);
            player.awardStat(ModStats.STORAGE_TRANSFERS.get());
            saveAnalyzerStorage();
            broadcastChanges();
        }
    }

    private void transferFromRemoteStorage(ItemStack remoteStack) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        ElemenicStorage remoteStorage = RemoteStorageItem.getRemoteStorage(remoteStack, serverLevel);
        if (remoteStorage.isEmpty()) {
            return;
        }
        long[] elemenix = remoteStorage.elemenix().clone();
        long[] before = elemenix.clone();
        if (transferIntoAnalyzer(elemenix)) {
            for (int i = 0; i < 6; i++) {
                long delta = before[i] - elemenix[i];
                if (delta > 0) {
                    updateRemoteStorage(remoteStack, i, delta);
                }
            }
            player.awardStat(ModStats.REMOTE_STORAGE_TRANSFERS.get());
            saveAnalyzerStorage();
            broadcastChanges();
        }
    }

    private void updateRemoteStorage(ItemStack remoteStack, int index, long amount) {
        RemoteStorageBinding binding = remoteStack.get(ModDataComponents.REMOTE_STORAGE_BINDING.get());
        if (binding == null || !binding.isBound() || !(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Optional<GlobalPos> globalPosOptional = binding.boundPos();
        if (globalPosOptional.isEmpty()) {
            return;
        }
        GlobalPos globalPos = globalPosOptional.get();
        if (serverLevel.dimension() != globalPos.dimension()) {
            return;
        }
        BlockEntity blockEntity = serverLevel.getBlockEntity(globalPos.pos());
        if (!(blockEntity instanceof InfuserBlockEntity infuser)) {
            return;
        }
        ItemStack storageStack = infuser.getItem(0);
        if (!(storageStack.getItem() instanceof StorageItem)) {
            return;
        }
        ElemenicStorage currentStorage = StorageItem.getOrCreateData(storageStack);
        long[] newElemenix = currentStorage.elemenix().clone();
        if (newElemenix[index] >= amount) {
            newElemenix[index] -= amount;
            storageStack.set(ModDataComponents.ELEMENIC_STORAGE.get(), new ElemenicStorage(newElemenix));
            infuser.setChanged();
        }
    }

    private void tryRecordItem(ItemStack inputStack) {
        if (!allowMemory) {
            return;
        }
        if (selectedMemorizer >= 0) {
            int slotIndex = findMemorizerSlot(selectedMemorizer);
            if (slotIndex >= 0) {
                ItemStack memorizer = boxContainer.getItem(slotIndex);
                if (canRecordInto(memorizer, inputStack)) {
                    recordInto(slotIndex, memorizer, inputStack);
                }
            }
            return;
        }
        for (int i = 0; i < BOX_SLOTS; i++) {
            ItemStack memorizer = boxContainer.getItem(i);
            if (memorizer.getItem() instanceof MemorizerItem && canRecordInto(memorizer, inputStack)) {
                recordInto(i, memorizer, inputStack);
                return;
            }
        }
    }

    private int findMemorizerSlot(int memorizerIndex) {
        int found = -1;
        for (int i = 0; i < BOX_SLOTS; i++) {
            if (boxContainer.getItem(i).getItem() instanceof MemorizerItem) {
                found++;
                if (found == memorizerIndex) {
                    return i;
                }
            }
        }
        return -1;
    }

    private boolean canRecordInto(ItemStack memorizer, ItemStack inputStack) {
        if (memorizer.isEmpty() || inputStack.isEmpty()) {
            return false;
        }
        if (MemorizerItem.isReadonly(memorizer) || !MemorizerItem.isNotFull(memorizer)) {
            return false;
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(inputStack.getItem());
        return !MemorizerItem.getOrCreateMemories(memorizer).resolvedItems().contains(itemId);
    }

    private void recordInto(int slotIndex, ItemStack memorizer, ItemStack inputStack) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(inputStack.getItem());
        MemoryData current = MemorizerItem.getOrCreateMemories(memorizer);
        List<ResourceLocation> newMemories = new ArrayList<>(current.resolvedItems());
        newMemories.add(itemId);
        memorizer.set(ModDataComponents.MEMORY_DATA.get(), current.withResolvedItems(newMemories));
        player.awardStat(ModStats.MEMORIZER_RECORDS_ADDED.get());
        if (Elemenics.isFtbQuestsLoaded() && player instanceof ServerPlayer serverPlayer) {
            AnalyzerTaskHelper.handleMemorizeProgress(serverPlayer, inputStack.copy());
        }
        slots.get(slotIndex).setChanged();
    }

    public void reconstructItem(ResourceLocation itemId, boolean shiftClick) {
        List<ResourceLocation> visible = getVisibleMemories();
        if (!visible.contains(itemId)) {
            return;
        }
        ItemStack resultStack = new ItemStack(BuiltInRegistries.ITEM.get(itemId));
        if (resultStack.isEmpty() || ElemenixInfo.isUnreconstructable(resultStack)) {
            return;
        }
        if (!getCarried().isEmpty() && !getCarried().is(resultStack.getItem())) {
            return;
        }
        Constituents required = ElemenixInfo.getPremiumAppliedConstituents(resultStack);
        if (required.isUnanalysable()) {
            return;
        }
        int maxStackSize = resultStack.getMaxStackSize();

        int maxAmount = shiftClick ? maxStackSize : 1;
        for (Elemenix type : Elemenix.values()) {
            int req = required.get(type);
            if (req > 0) {
                long available = data.getLong(type.getIndex()) & 0xFFFFFFFFL;
                maxAmount = (int) Math.min(maxAmount, available / req);
            }
        }
        if (maxAmount <= 0) {
            return;
        }

        int actualAmount = maxAmount;
        if (!getCarried().isEmpty()) {
            int room = maxStackSize - getCarried().getCount();
            if (room <= 0) {
                return;
            }
            actualAmount = Math.min(actualAmount, room);
        }

        for (Elemenix type : Elemenix.values()) {
            int consumption = required.get(type) * actualAmount;
            if (consumption > 0) {
                long current = data.getLong(type.getIndex());
                data.setLong(type.getIndex(), Math.max(0, current - consumption));
            }
        }

        resultStack.setCount(actualAmount);
        if (!getCarried().isEmpty() && ItemStack.isSameItemSameComponents(getCarried(), resultStack)) {
            getCarried().grow(actualAmount);
            setCarried(getCarried());
        } else {
            setCarried(resultStack);
        }

        player.awardStat(ModStats.RECONSTRUCTED_ITEMS.get(), actualAmount);
        if (Elemenics.isFtbQuestsLoaded() && player instanceof ServerPlayer serverPlayer) {
            AnalyzerTaskHelper.handleReconstructProgress(serverPlayer, resultStack.copy(), actualAmount);
        }
        saveAnalyzerStorage();
        broadcastChanges();
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        if (player != this.player) {
            return false;
        }
        if (analyzerUuid == null || boxUuid == null) {
            return true;
        }
        return !AnalyzerItem.findAnalyzerByUuid(player, analyzerUuid).isEmpty()
                && !findBoxByUuid(player, boxUuid).isEmpty();
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        if (!player.level().isClientSide) {
            saveAnalyzerStorage();
            saveBoxContainer();
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }
}
