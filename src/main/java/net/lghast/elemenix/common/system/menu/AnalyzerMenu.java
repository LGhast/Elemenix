package net.lghast.elemenix.common.system.menu;

import net.lghast.elemenix.common.content.item.AnalyzerItem;
import net.lghast.elemenix.common.content.item.MemorizerItem;
import net.lghast.elemenix.common.system.datacomponent.ElemenicStorage;
import net.lghast.elemenix.common.system.datacomponent.MemoryData;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.register.system.ModDataComponents;
import net.lghast.elemenix.register.system.ModMenus;
import net.lghast.elemenix.register.system.ModTags;
import net.lghast.elemenix.utils.Constituents;
import net.lghast.elemenix.utils.Elemenix;
import net.lghast.elemenix.utils.ElemenixInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
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
    private final ContainerData data;
    private final Player player;

    public AnalyzerMenu(int containerId, Inventory playerInventory, ItemStack menuStack) {
        this(containerId, playerInventory, new SimpleContainer(SLOT_COUNT), new SimpleContainerData(6), menuStack);
    }

    public AnalyzerMenu(int containerId, Inventory playerInventory,
                        Container analyzerContainer, ContainerData data, ItemStack menuStack) {
        super(ModMenus.ELEMENIX_ANALYZER_MENU.get(), containerId);
        this.analyzerContainer = analyzerContainer;
        this.analyzerUuid = AnalyzerItem.getOrCreateUuid(menuStack).uuid();
        this.data = data;
        this.player = playerInventory.player;

        loadAnalyzerStorage();
        checkContainerSize(analyzerContainer, SLOT_COUNT);
        checkContainerDataCount(data, 6);

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

    private static boolean isInputSlotPlaceable(ItemStack stack){
        if(stack.is(ModItems.ELEMENIC_MEMORIZER)){
            return MemorizerItem.getOrCreateMemories(stack).resolvedItems().isEmpty();
        }
        return !ElemenixInfo.getConstituents(stack).isUnanalysable() && !(stack.getItem() instanceof AnalyzerItem);
    }

    private void loadAnalyzerStorage() {
        ItemStack analyzerStack = getAnalyzerStack();
        if (analyzerStack.isEmpty()) return;

        ElemenicStorage elemenicStorage = AnalyzerItem.getOrCreateData(analyzerStack);

        data.set(0, (int) Math.min(elemenicStorage.organix(), Integer.MAX_VALUE));
        data.set(1, (int) Math.min(elemenicStorage.terrix(), Integer.MAX_VALUE));
        data.set(2, (int) Math.min(elemenicStorage.flumix(), Integer.MAX_VALUE));
        data.set(3, (int) Math.min(elemenicStorage.metallix(), Integer.MAX_VALUE));
        data.set(4, (int) Math.min(elemenicStorage.energix(), Integer.MAX_VALUE));
        data.set(5, (int) Math.min(elemenicStorage.arcanix(), Integer.MAX_VALUE));
    }

    public void saveStorageAndMemoryData() {
        ItemStack analyzerStack = getAnalyzerStack();
        if (analyzerStack.isEmpty()) return;

        long[] elemenix = new long[]{
                data.get(0) & 0xFFFFFFFFL,
                data.get(1) & 0xFFFFFFFFL,
                data.get(2) & 0xFFFFFFFFL,
                data.get(3) & 0xFFFFFFFFL,
                data.get(4) & 0xFFFFFFFFL,
                data.get(5) & 0xFFFFFFFFL
        };
        analyzerStack.set(ModDataComponents.ELEMENIC_STORAGE.get(),
                new ElemenicStorage(elemenix));

        ItemStack memorizerItem = getMemorizerItem();
        if(memorizerItem.is(ModItems.ELEMENIC_MEMORIZER)) {
            List<ResourceLocation> itemMemory =
                    MemorizerItem.getOrCreateMemories(memorizerItem).resolvedItems();
            memorizerItem.set(ModDataComponents.MEMORY_DATA.get(),
                    new MemoryData(itemMemory));
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

    public ContainerData getData(){
        return data;
    }

    public void decomposeItem() {
        ItemStack inputStack = getInputItem();
        if (inputStack.isEmpty()) return;

        Constituents constituents = ElemenixInfo.getConstituents(inputStack);
        if (constituents.isUnanalysable()) return;

        for (Elemenix type : Elemenix.values()) {
            int amount = constituents.get(type) * inputStack.getCount();
            if (amount > 0) {
                int current = data.get(type.getIndex());
                long newValue = (current & 0xFFFFFFFFL) + amount;
                data.set(type.getIndex(), (int) Math.min(newValue, Integer.MAX_VALUE));
            }
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(inputStack.getItem());

        if(!MemorizerItem.isReadonly(getMemorizerItem())){
            MemoryData currentMemoryData = MemorizerItem.getOrCreateMemories(getMemorizerItem());
            List<ResourceLocation> currentMemories = currentMemoryData.resolvedItems();
            if (!inputStack.is(ModTags.ANALYZER_UNRECORDABLE) && !currentMemories.contains(itemId)) {
                List<ResourceLocation> newMemories = new ArrayList<>(currentMemories);
                newMemories.add(itemId);

                getMemorizerItem().set(ModDataComponents.MEMORY_DATA.get(), currentMemoryData.withResolvedItems(newMemories));
                slots.get(MEMORIZER_SLOT).setChanged();
            }
        }

        analyzerContainer.setItem(INPUT_SLOT, ItemStack.EMPTY);

        saveStorageAndMemoryData();
        broadcastChanges();
    }

    public void reconstructItem(ResourceLocation itemId, boolean shiftClick) {
        Item item = BuiltInRegistries.ITEM.get(itemId);
        if(!getCarried().isEmpty() && !getCarried().is(item)) return;

        ItemStack resultStack = new ItemStack(item);
        if(resultStack.isEmpty()) return;
        Constituents required = ElemenixInfo.getConstituents(resultStack);
        if (required.isUnanalysable()) return;

        int maxStackSize = resultStack.getMaxStackSize();

        int maxAmount = shiftClick ? maxStackSize : 1;
        for (Elemenix type : Elemenix.values()) {
            int req = required.get(type);
            if (req > 0) {
                long available = data.get(type.getIndex()) & 0xFFFFFFFFL;
                maxAmount = (int) Math.min(maxAmount, available / req);
            }
        }

        if (maxAmount <= 0) return;

        for (Elemenix type : Elemenix.values()) {
            int consumption = required.get(type) * maxAmount;
            if (consumption > 0) {
                int current = data.get(type.getIndex());
                data.set(type.getIndex(), Math.max(0, current - consumption));
            }
        }

        resultStack.setCount(maxAmount);
        if (!getCarried().isEmpty() && ItemStack.isSameItemSameComponents(getCarried(), resultStack)) {
            getCarried().grow(maxAmount);
        } else {
            setCarried(resultStack);
        }

        saveStorageAndMemoryData();
        broadcastChanges();
    }

    public List<ResourceLocation> getMemories() {
        ItemStack memorizer = getMemorizerItem();
        if(memorizer.is(ModItems.ELEMENIC_MEMORIZER)) {
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
            }else if (index == MEMORIZER_SLOT) {
                if (!this.moveItemStackTo(stack1, SLOT_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack1, INPUT_SLOT, SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
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

            ItemStack memorizerStack = analyzerContainer.getItem(MEMORIZER_SLOT);
            if (!memorizerStack.isEmpty()) {
                if (player.isAlive()) {
                    player.getInventory().placeItemBackInInventory(memorizerStack);
                } else {
                    player.drop(memorizerStack, false);
                }
                analyzerContainer.setItem(MEMORIZER_SLOT, ItemStack.EMPTY);
            }
        }
    }
}
