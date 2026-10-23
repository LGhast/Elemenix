package net.lghast.elemenix.common.system.menu;

import net.lghast.elemenix.client.screen.BurnerMode;
import net.lghast.elemenix.common.content.item.MemorizerItem;
import net.lghast.elemenix.common.content.item.WitchcraftMemorizerItem;
import net.lghast.elemenix.common.system.datacomponent.MemoryData;
import net.lghast.elemenix.common.system.datacomponent.PotionMemoryData;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.register.system.ModDataComponents;
import net.lghast.elemenix.register.system.ModMenus;
import net.lghast.elemenix.register.system.ModStats;
import net.lghast.elemenix.register.system.ModTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
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
import net.minecraft.world.item.alchemy.PotionContents;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ParametersAreNonnullByDefault
public class BurnerMenu extends AbstractContainerMenu {
    private static final int PRIMARY_SLOT = 0;
    private static final int SECONDARY_SLOT = 1;
    private static final int SLOT_COUNT = 2;

    private static final int PRIMARY_SLOT_X = 22;
    private static final int SECONDARY_SLOT_X = 58;
    private static final int SLOT_Y = 33;

    private final Container burnerContainer;
    private final ContainerData data;
    private BurnerMode currentMode = BurnerMode.MOVE_UP;
    private boolean primaryChanged = false;
    private ItemStack lastPrimaryStack = ItemStack.EMPTY;
    private boolean isInternalOperation = false;
    private final Player player;

    public BurnerMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(SLOT_COUNT), new SimpleContainerData(2));
    }

    public BurnerMenu(int containerId, Inventory playerInventory, Container burnerContainer, ContainerData data) {
        super(ModMenus.BURNER_MENU.get(), containerId);
        this.burnerContainer = burnerContainer;
        this.data = data;
        this.player = playerInventory.player;

        checkContainerSize(burnerContainer, SLOT_COUNT);
        checkContainerDataCount(data, 2);

        this.addSlot(new Slot(burnerContainer, PRIMARY_SLOT, PRIMARY_SLOT_X, SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModTags.MEMORIZER_SLOT_PLACEABLE);
            }

            @Override
            public void setChanged() {
                super.setChanged();
                ItemStack current = getItem();

                if (!isInternalOperation && !ItemStack.matches(lastPrimaryStack, current)) {
                    primaryChanged = true;
                    lastPrimaryStack = current.copy();
                }
            }
        });

        this.addSlot(new Slot(burnerContainer, SECONDARY_SLOT, SECONDARY_SLOT_X, SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModTags.MEMORIZER_SLOT_PLACEABLE);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
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
        data.set(0, currentMode.ordinal());
    }

    public void cycleMode() {
        BurnerMode[] modes = BurnerMode.values();
        currentMode = modes[(currentMode.ordinal() + 1) % modes.length];
        data.set(0, currentMode.ordinal());
        broadcastChanges();
    }

    public BurnerMode getMode() {
        return BurnerMode.values()[data.get(0)];
    }

    public ItemStack getPrimaryMemorizer() {
        return burnerContainer.getItem(PRIMARY_SLOT);
    }

    public ItemStack getSecondaryMemorizer() {
        return burnerContainer.getItem(SECONDARY_SLOT);
    }

    public List<ResourceLocation> getPrimaryMemories() {
        ItemStack memorizer = getPrimaryMemorizer();
        if (memorizer.is(ModItems.ELEMENIC_MEMORIZER)) {
            return MemorizerItem.getOrCreateMemories(memorizer).resolvedItems();
        }
        return new ArrayList<>();
    }

    public boolean isPrimaryPotionMemorizer() {
        return getPrimaryMemorizer().is(ModItems.WITCHCRAFT_MEMORIZER);
    }

    private boolean isInvalidMemorizer(ItemStack stack) {
        return !stack.is(ModItems.ELEMENIC_MEMORIZER) && !stack.is(ModItems.WITCHCRAFT_MEMORIZER);
    }

    public List<PotionMemoryData.Entry> getPrimaryPotionMemories() {
        ItemStack memorizer = getPrimaryMemorizer();
        if (memorizer.is(ModItems.WITCHCRAFT_MEMORIZER)) {
            return WitchcraftMemorizerItem.getOrCreateMemories(memorizer).entries();
        }
        return new ArrayList<>();
    }

    public ItemStack getMemoryItemStack(int index) {
        if (isPrimaryPotionMemorizer()) {
            List<PotionMemoryData.Entry> entries = getPrimaryPotionMemories();
            if (index >= 0 && index < entries.size()) {
                return createPotionStack(entries.get(index));
            }
            return ItemStack.EMPTY;
        }
        List<ResourceLocation> memories = getPrimaryMemories();
        if (index >= 0 && index < memories.size()) {
            return new ItemStack(BuiltInRegistries.ITEM.get(memories.get(index)));
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack createPotionStack(PotionMemoryData.Entry entry) {
        Item item = BuiltInRegistries.ITEM.get(entry.itemId());
        var holder = BuiltInRegistries.POTION.getHolder(ResourceKey.create(Registries.POTION, entry.potionId()));
        return holder.map(p -> PotionContents.createItemStack(item, p)).orElseGet(() -> new ItemStack(item));
    }

    public boolean isMemorizerMismatched() {
        ItemStack primary = getPrimaryMemorizer();
        ItemStack secondary = getSecondaryMemorizer();
        if (primary.isEmpty() || secondary.isEmpty()) {
            return false;
        }
        return primary.is(ModItems.WITCHCRAFT_MEMORIZER) != secondary.is(ModItems.WITCHCRAFT_MEMORIZER);
    }

    public void moveItem(int index, int direction) {
        isInternalOperation = true;
        try {
            ItemStack primary = getPrimaryMemorizer();
            if (primary.isEmpty() || isInvalidMemorizer(primary)) return;

            if (isPrimaryPotionMemorizer()) {
                movePotionItem(primary, index, direction);
            } else {
                moveRegularItem(primary, index, direction);
            }
        } finally {
            isInternalOperation = false;
        }
    }

    private void moveRegularItem(ItemStack primary, int index, int direction) {
        MemoryData memoryData = MemorizerItem.getOrCreateMemories(primary);
        List<ResourceLocation> memories = new ArrayList<>(memoryData.resolvedItems());

        if (direction == -1) {
            if (index > 0) {
                Collections.swap(memories, index, index - 1);
                player.awardStat(ModStats.BURNER_MOVE_UP_ACTIONS.get());
            }
        } else if (direction == 1) {
            if (index < memories.size() - 1) {
                Collections.swap(memories, index, index + 1);
                player.awardStat(ModStats.BURNER_MOVE_DOWN_ACTIONS.get());
            }
        }

        primary.set(ModDataComponents.MEMORY_DATA.get(), memoryData.withResolvedItems(memories));
        slots.getFirst().setChanged();
        broadcastChanges();
    }

    private void movePotionItem(ItemStack primary, int index, int direction) {
        PotionMemoryData memoryData = WitchcraftMemorizerItem.getOrCreateMemories(primary);
        List<PotionMemoryData.Entry> entries = new ArrayList<>(memoryData.entries());

        if (direction == -1) {
            if (index > 0) {
                Collections.swap(entries, index, index - 1);
                player.awardStat(ModStats.BURNER_MOVE_UP_ACTIONS.get());
            }
        } else if (direction == 1) {
            if (index < entries.size() - 1) {
                Collections.swap(entries, index, index + 1);
                player.awardStat(ModStats.BURNER_MOVE_DOWN_ACTIONS.get());
            }
        }

        primary.set(ModDataComponents.POTION_MEMORY_DATA.get(), memoryData.withEntries(entries));
        slots.getFirst().setChanged();
        broadcastChanges();
    }

    public void deleteItem(int index) {
        isInternalOperation = true;
        try {
            ItemStack primary = getPrimaryMemorizer();
            if (primary.isEmpty() || isInvalidMemorizer(primary)) return;

            if (isPrimaryPotionMemorizer()) {
                deletePotionItem(primary, index);
            } else {
                deleteRegularItem(primary, index);
            }
        } finally {
            isInternalOperation = false;
        }
    }

    private void deleteRegularItem(ItemStack primary, int index) {
        MemoryData memoryData = MemorizerItem.getOrCreateMemories(primary);
        List<ResourceLocation> memories = new ArrayList<>(memoryData.resolvedItems());

        if (index >= 0 && index < memories.size()) {
            memories.remove(index);
            primary.set(ModDataComponents.MEMORY_DATA.get(), memoryData.withResolvedItems(memories));
            slots.getFirst().setChanged();
            broadcastChanges();
            player.awardStat(ModStats.BURNER_DELETE_ACTIONS.get());
        }
    }

    private void deletePotionItem(ItemStack primary, int index) {
        PotionMemoryData memoryData = WitchcraftMemorizerItem.getOrCreateMemories(primary);
        List<PotionMemoryData.Entry> entries = new ArrayList<>(memoryData.entries());

        if (index >= 0 && index < entries.size()) {
            entries.remove(index);
            primary.set(ModDataComponents.POTION_MEMORY_DATA.get(), memoryData.withEntries(entries));
            slots.getFirst().setChanged();
            broadcastChanges();
            player.awardStat(ModStats.BURNER_DELETE_ACTIONS.get());
        }
    }

    public void swapItems(int index1, int index2) {
        isInternalOperation = true;
        try {
            ItemStack primary = getPrimaryMemorizer();
            if (primary.isEmpty() || isInvalidMemorizer(primary)) return;

            if (isPrimaryPotionMemorizer()) {
                swapPotionItems(primary, index1, index2);
            } else {
                swapRegularItems(primary, index1, index2);
            }
        } finally {
            isInternalOperation = false;
        }
    }

    private void swapRegularItems(ItemStack primary, int index1, int index2) {
        MemoryData memoryData = MemorizerItem.getOrCreateMemories(primary);
        List<ResourceLocation> memories = new ArrayList<>(memoryData.resolvedItems());

        if (index1 >= 0 && index1 < memories.size() &&
                index2 >= 0 && index2 < memories.size() && index1 != index2) {
            Collections.swap(memories, index1, index2);
            primary.set(ModDataComponents.MEMORY_DATA.get(), memoryData.withResolvedItems(memories));
            slots.getFirst().setChanged();
            broadcastChanges();
            player.awardStat(ModStats.BURNER_SWAP_ACTIONS.get());
        }
    }

    private void swapPotionItems(ItemStack primary, int index1, int index2) {
        PotionMemoryData memoryData = WitchcraftMemorizerItem.getOrCreateMemories(primary);
        List<PotionMemoryData.Entry> entries = new ArrayList<>(memoryData.entries());

        if (index1 >= 0 && index1 < entries.size() &&
                index2 >= 0 && index2 < entries.size() && index1 != index2) {
            Collections.swap(entries, index1, index2);
            primary.set(ModDataComponents.POTION_MEMORY_DATA.get(), memoryData.withEntries(entries));
            slots.getFirst().setChanged();
            broadcastChanges();
            player.awardStat(ModStats.BURNER_SWAP_ACTIONS.get());
        }
    }

    public void insertBefore(int selectedIndex, int targetIndex) {
        isInternalOperation = true;
        try {
            ItemStack primary = getPrimaryMemorizer();
            if (primary.isEmpty() || isInvalidMemorizer(primary)) return;

            if (isPrimaryPotionMemorizer()) {
                insertBeforePotion(primary, selectedIndex, targetIndex);
            } else {
                insertBeforeRegular(primary, selectedIndex, targetIndex);
            }
        } finally {
            isInternalOperation = false;
        }
    }

    private void insertBeforeRegular(ItemStack primary, int selectedIndex, int targetIndex) {
        MemoryData memoryData = MemorizerItem.getOrCreateMemories(primary);
        List<ResourceLocation> memories = new ArrayList<>(memoryData.resolvedItems());

        if (selectedIndex >= 0 && selectedIndex < memories.size() &&
                targetIndex >= 0 && targetIndex < memories.size() && selectedIndex != targetIndex) {
            ResourceLocation selectedItem = memories.remove(selectedIndex);
            if (targetIndex > selectedIndex) {
                targetIndex--;
            }
            memories.add(targetIndex, selectedItem);
            primary.set(ModDataComponents.MEMORY_DATA.get(), memoryData.withResolvedItems(memories));
            slots.getFirst().setChanged();
            broadcastChanges();
            player.awardStat(ModStats.BURNER_INSERT_BEFORE_ACTIONS.get());
        }
    }

    private void insertBeforePotion(ItemStack primary, int selectedIndex, int targetIndex) {
        PotionMemoryData memoryData = WitchcraftMemorizerItem.getOrCreateMemories(primary);
        List<PotionMemoryData.Entry> entries = new ArrayList<>(memoryData.entries());

        if (selectedIndex >= 0 && selectedIndex < entries.size() &&
                targetIndex >= 0 && targetIndex < entries.size() && selectedIndex != targetIndex) {
            PotionMemoryData.Entry selectedItem = entries.remove(selectedIndex);
            if (targetIndex > selectedIndex) {
                targetIndex--;
            }
            entries.add(targetIndex, selectedItem);
            primary.set(ModDataComponents.POTION_MEMORY_DATA.get(), memoryData.withEntries(entries));
            slots.getFirst().setChanged();
            broadcastChanges();
            player.awardStat(ModStats.BURNER_INSERT_BEFORE_ACTIONS.get());
        }
    }

    public void copyMemories() {
        isInternalOperation = true;
        try {
            ItemStack primary = getPrimaryMemorizer();
            ItemStack secondary = getSecondaryMemorizer();
            if (primary.isEmpty() || secondary.isEmpty()) return;

            if (primary.is(ModItems.WITCHCRAFT_MEMORIZER) && secondary.is(ModItems.WITCHCRAFT_MEMORIZER)) {
                PotionMemoryData secondaryData = WitchcraftMemorizerItem.getOrCreateMemories(secondary);
                primary.set(ModDataComponents.POTION_MEMORY_DATA.get(),
                        new PotionMemoryData(new ArrayList<>(secondaryData.entries())));
            } else if (primary.is(ModItems.ELEMENIC_MEMORIZER) && secondary.is(ModItems.ELEMENIC_MEMORIZER)) {
                MemoryData secondaryData = MemorizerItem.getOrCreateMemories(secondary);
                primary.set(ModDataComponents.MEMORY_DATA.get(),
                        new MemoryData(new ArrayList<>(secondaryData.resolvedItems())));
            } else {
                return;
            }

            slots.getFirst().setChanged();
            broadcastChanges();
            player.awardStat(ModStats.BURNER_COPY_ACTIONS.get());
        } finally {
            isInternalOperation = false;
        }
    }

    public void concatenateMemories() {
        isInternalOperation = true;
        try {
            ItemStack primary = getPrimaryMemorizer();
            ItemStack secondary = getSecondaryMemorizer();
            if (primary.isEmpty() || secondary.isEmpty()) return;

            if (primary.is(ModItems.WITCHCRAFT_MEMORIZER) && secondary.is(ModItems.WITCHCRAFT_MEMORIZER)) {
                concatenatePotionMemories(primary, secondary);
            } else if (primary.is(ModItems.ELEMENIC_MEMORIZER) && secondary.is(ModItems.ELEMENIC_MEMORIZER)) {
                concatenateRegularMemories(primary, secondary);
            }
        } finally {
            isInternalOperation = false;
        }
    }

    private void concatenateRegularMemories(ItemStack primary, ItemStack secondary) {
        MemoryData primaryData = MemorizerItem.getOrCreateMemories(primary);
        MemoryData secondaryData = MemorizerItem.getOrCreateMemories(secondary);

        if (primaryData.isFull()) {
            return;
        }

        List<ResourceLocation> combined = new ArrayList<>(primaryData.resolvedItems());
        for (ResourceLocation location : secondaryData.resolvedItems()) {
            if (!combined.contains(location)) {
                combined.add(location);
                if (combined.size() >= MemoryData.MAX) {
                    break;
                }
            }
        }
        if (combined.size() > MemoryData.MAX) {
            combined = combined.subList(0, MemoryData.MAX);
        }

        primary.set(ModDataComponents.MEMORY_DATA.get(), new MemoryData(combined));
        slots.getFirst().setChanged();
        broadcastChanges();
        player.awardStat(ModStats.BURNER_CONCATENATE_ACTIONS.get());
    }

    private void concatenatePotionMemories(ItemStack primary, ItemStack secondary) {
        PotionMemoryData primaryData = WitchcraftMemorizerItem.getOrCreateMemories(primary);
        PotionMemoryData secondaryData = WitchcraftMemorizerItem.getOrCreateMemories(secondary);

        if (primaryData.isFull()) {
            return;
        }

        List<PotionMemoryData.Entry> combined = new ArrayList<>(primaryData.entries());
        for (PotionMemoryData.Entry entry : secondaryData.entries()) {
            if (!combined.contains(entry)) {
                combined.add(entry);
                if (combined.size() >= PotionMemoryData.MAX) {
                    break;
                }
            }
        }
        if (combined.size() > PotionMemoryData.MAX) {
            combined = combined.subList(0, PotionMemoryData.MAX);
        }

        primary.set(ModDataComponents.POTION_MEMORY_DATA.get(), new PotionMemoryData(combined));
        slots.getFirst().setChanged();
        broadcastChanges();
        player.awardStat(ModStats.BURNER_CONCATENATE_ACTIONS.get());
    }

    public boolean isPrimaryMemorizerChanged() {
        return primaryChanged;
    }

    public void resetPrimaryChangedFlag() {
        primaryChanged = false;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (!slot.hasItem()) {
            return stack;
        }

        ItemStack stackInSlot = slot.getItem();
        stack = stackInSlot.copy();

        if (index < SLOT_COUNT) {
            if (!this.moveItemStackTo(stackInSlot, SLOT_COUNT, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (stackInSlot.is(ModTags.MEMORIZER_SLOT_PLACEABLE)) {
                Slot primarySlot = this.slots.getFirst();
                Slot secondarySlot = this.slots.get(SECONDARY_SLOT);

                if (primarySlot.getItem().isEmpty() &&
                        (MemorizerItem.isChangeable(stackInSlot) || WitchcraftMemorizerItem.isChangeable(stackInSlot))) {
                    if (!this.moveItemStackTo(stackInSlot, PRIMARY_SLOT, PRIMARY_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (secondarySlot.getItem().isEmpty()) {
                    if (!this.moveItemStackTo(stackInSlot, SECONDARY_SLOT, SECONDARY_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            } else {
                if (index < SLOT_COUNT + 27) {
                    if (!this.moveItemStackTo(stackInSlot, SLOT_COUNT + 27, this.slots.size(), false)) {
                        return ItemStack.EMPTY;
                    }
                }
                else if (index < this.slots.size()) {
                    if (!this.moveItemStackTo(stackInSlot, SLOT_COUNT, SLOT_COUNT + 27, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        }

        if (stackInSlot.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return stack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);

        if (!player.level().isClientSide) {
            ItemStack primary = burnerContainer.getItem(PRIMARY_SLOT);
            ItemStack secondary = burnerContainer.getItem(SECONDARY_SLOT);

            if (!primary.isEmpty()) {
                if (player.isAlive()) {
                    player.getInventory().placeItemBackInInventory(primary);
                } else {
                    player.drop(primary, false);
                }
            }

            if (!secondary.isEmpty()) {
                if (player.isAlive()) {
                    player.getInventory().placeItemBackInInventory(secondary);
                } else {
                    player.drop(secondary, false);
                }
            }
        }
    }
}
