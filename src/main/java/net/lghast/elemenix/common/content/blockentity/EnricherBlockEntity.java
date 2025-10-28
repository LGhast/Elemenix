package net.lghast.elemenix.common.content.blockentity;

import net.lghast.elemenix.common.content.block.EnricherBlock;
import net.lghast.elemenix.common.content.block.EnricherBlock;
import net.lghast.elemenix.common.content.block.TransformerBlock;
import net.lghast.elemenix.common.content.item.AnalyzerItem;
import net.lghast.elemenix.common.system.datacomponent.ElemenicStorage;
import net.lghast.elemenix.common.system.menu.EnricherMenu;
import net.lghast.elemenix.common.system.menu.InfuserMenu;
import net.lghast.elemenix.register.content.ModBlockEntities;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.register.system.ModDataComponents;
import net.lghast.elemenix.utils.Constituents;
import net.lghast.elemenix.utils.Elemenix;
import net.lghast.elemenix.utils.ElemenixInfo;
import net.lghast.elemenix.utils.ModUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;

public class EnricherBlockEntity extends BaseContainerBlockEntity {
    private static final int INPUT_SLOT = 6;
    private static final int SLOT_COUNT = 7;

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final int[] elemenixStorage = new int[6];

    private int deconstructionTimer = 0;
    private int enrichingTimer = 0;

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

    public EnricherBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENRICHER.get(), pos, state);
        Arrays.fill(elemenixStorage, 0);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.elemenix.elemenic_enricher");
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory playerInventory) {
        return new EnricherMenu(containerId, playerInventory, this);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        ContainerHelper.loadAllItems(tag, items, provider);

        if (tag.contains("ElemenixStorage", CompoundTag.TAG_INT_ARRAY)) {
            int[] stored = tag.getIntArray("ElemenixStorage");
            System.arraycopy(stored, 0, elemenixStorage, 0, Math.min(stored.length, 6));
        }

        deconstructionTimer = tag.getInt("DeconstructionTimer");
        enrichingTimer = tag.getInt("EnrichingTimer");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        ContainerHelper.saveAllItems(tag, items, provider);

        tag.putIntArray("ElemenixStorage", elemenixStorage);

        tag.putInt("DeconstructionTimer", deconstructionTimer);
        tag.putInt("EnrichingTimer", enrichingTimer);
    }

    public void tick(Level level, BlockPos pos, BlockState state, EnricherBlockEntity blockEntity) {
        if (level.isClientSide) return;

        boolean wasWorking = state.getValue(EnricherBlock.WORKING);
        boolean isWorking = false;

        if (canDeconstruct()) {
            deconstructionTimer++;
            EnricherBlock block = getEnricherBlock();

            if (deconstructionTimer >= block.getDcInterval()) {
                deconstructItem();
                deconstructionTimer = 0;
            }
            isWorking = true;
        } else {
            deconstructionTimer = 0;
        }

        boolean anyCanEnrich = false;
        for(int i = 0; i < elemenixStorage.length; i++) {
            if (canEnrich(i)) {
                anyCanEnrich = true;
                break;
            }
        }

        if (anyCanEnrich) {
            enrichingTimer++;
            isWorking = true;

            EnricherBlock block = getEnricherBlock();
            if (enrichingTimer >= block.getEnrichingInterval()) {
                for(int i = 0; i < elemenixStorage.length; i++) {
                    if (canEnrich(i)) {
                        enrichElemenix(i);
                    }
                }
                enrichingTimer = 0;
            }
        } else {
            enrichingTimer = 0;
        }

        if (wasWorking != isWorking) {
            level.setBlock(pos, state.setValue(EnricherBlock.WORKING, isWorking), Block.UPDATE_ALL);
        }

        setChanged(level, pos, state);
    }

    private boolean canDeconstruct() {
        ItemStack inputStack = getItem(INPUT_SLOT);
        return !inputStack.isEmpty() && !ElemenixInfo.getConstituents(inputStack).isUnanalysable();
    }

    private void deconstructItem() {
        ItemStack inputStack = getItem(INPUT_SLOT);
        if (inputStack.isEmpty()) return;

        Constituents constituents = ElemenixInfo.getConstituents(inputStack);
        if (constituents.isUnanalysable()) return;

        for (Elemenix elemenix : Elemenix.values()) {
            int value = constituents.get(elemenix);
            if (value > 0) {
                addElemenix(elemenix, value);
            }
        }

        inputStack.shrink(1);
        setChanged();
    }

    private boolean canEnrich(int index) {
        if(index >= 0 && index < elemenixStorage.length){
            return elemenixStorage[index] >= ElemenixInfo.ESSENCE_VALUE && isSlotAvailable(index);
        }
        return false;
    }

    private boolean isSlotAvailable(int index){
        return items.get(index).isEmpty() || items.get(index).getCount() < items.get(index).getMaxStackSize();
    }

    private void enrichElemenix(int index) {
        ItemStack essenceStack = getItem(index);

        int consumption = ElemenixInfo.ESSENCE_VALUE;
        Elemenix type = Elemenix.values()[index];

        consumeElemenix(type, consumption);
        if(essenceStack.isEmpty()){
            ItemStack newStack = switch (index){
                case 0 -> new ItemStack(ModItems.ORGANIX_ESSENCE.asItem());
                case 1 -> new ItemStack(ModItems.TERRIX_ESSENCE.asItem());
                case 2 -> new ItemStack(ModItems.FLUMIX_ESSENCE.asItem());
                case 3 -> new ItemStack(ModItems.METALLIX_ESSENCE.asItem());
                case 4 -> new ItemStack(ModItems.ENERGIX_ESSENCE.asItem());
                default -> new ItemStack(ModItems.ARCANIX_ESSENCE.asItem());
            };
            setItem(index, newStack);
        }else {
            essenceStack.setCount(essenceStack.getCount() + 1);
        }
        setChanged();
    }

    public void addElemenix(Elemenix elemenix, int amount) {
        if (amount <= 0) return;
        int index = elemenix.getIndex();
        if (index >= 0 && index < 6) {
            elemenixStorage[index] = (int) Math.min((long) elemenixStorage[index] + amount, Integer.MAX_VALUE);
            setChanged();
        }
    }

    public void consumeElemenix(Elemenix elemenix, int amount) {
        if (amount <= 0) return;
        int index = elemenix.getIndex();
        if (index >= 0 && index < 6) {
            elemenixStorage[index] = Math.max(elemenixStorage[index] - amount, 0);
            setChanged();
        }
    }

    public EnricherBlock getEnricherBlock() {
        return getBlockState().getBlock() instanceof EnricherBlock block ? block : null;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == INPUT_SLOT) {
            return !ElemenixInfo.getConstituents(stack).isUnanalysable();
        }
        return false;
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

    @Override
    public boolean canTakeItem(Container target, int slot, ItemStack stack) {
        return slot >= 0 && slot < INPUT_SLOT;
    }
}
