package net.lghast.elemenix.common.content.blockentity;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.common.content.block.EjectorBlock;
import net.lghast.elemenix.common.content.item.ThrottleValveItem;
import net.lghast.elemenix.common.system.datacomponent.ValveOpenness;
import net.lghast.elemenix.common.system.menu.EjectorMenu;
import net.lghast.elemenix.register.content.ModBlockEntities;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.register.system.ModTags;
import net.lghast.elemenix.utils.Constituents;
import net.lghast.elemenix.utils.elemenix.Elemenix;
import net.lghast.elemenix.utils.elemenix.ElemenixInfo;
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
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@ParametersAreNonnullByDefault
public class EjectorBlockEntity extends BaseContainerBlockEntity {
    private static final int INPUT_SLOT = 6;
    private static final int VALVE_SLOT = 7;
    private static final int STRAIGHTENER_SLOT = 8;
    private static final int SLOT_COUNT = 9;

    private final String TAG_INDEX = "EjectedIndex";
    private final String TAG_STORAGE = "ElemenixStorage";
    private final String TAG_DC_TIMER = "DeconstructionTimer";
    private final String TAG_ENRICHING_TIMER = "EnrichingTimer";
    private final String TAG_EJECTING_TIMER = "EjectingTimer";

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final int[] elemenixStorage = new int[6];

    private int deconstructionTimer = 0;
    private int enrichingTimer = 0;
    private int ejectingTimer = 0;
    private int ejectedIndex = 0;

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

    public EjectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.EJECTOR.get(), pos, state);
        Arrays.fill(elemenixStorage, 0);
    }

    @Override
    protected @NotNull Component getDefaultName() {
        return Component.translatable("container.elemenix.elemenic_ejector");
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
        return new EjectorMenu(containerId, playerInventory, this);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        ContainerHelper.loadAllItems(tag, items, provider);

        if (tag.contains(TAG_STORAGE, CompoundTag.TAG_INT_ARRAY)) {
            int[] stored = tag.getIntArray(TAG_STORAGE);
            System.arraycopy(stored, 0, elemenixStorage, 0, Math.min(stored.length, 6));
        }

        deconstructionTimer = tag.getInt(TAG_DC_TIMER);
        enrichingTimer = tag.getInt(TAG_ENRICHING_TIMER);
        ejectingTimer = tag.getInt(TAG_EJECTING_TIMER);
        ejectedIndex = tag.getInt(TAG_INDEX);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        ContainerHelper.saveAllItems(tag, items, provider);

        tag.putIntArray(TAG_STORAGE, elemenixStorage);

        tag.putInt(TAG_DC_TIMER, deconstructionTimer);
        tag.putInt(TAG_ENRICHING_TIMER, enrichingTimer);
        tag.putInt(TAG_EJECTING_TIMER, ejectingTimer);
        tag.putInt(TAG_INDEX, ejectedIndex);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;
        if(!Elemenics.started) return;

        boolean wasWorking = state.getValue(EjectorBlock.WORKING);
        boolean isWorking = false;
        boolean enrichingDone = false;

        if (canDeconstruct()) {
            deconstructionTimer++;
            EjectorBlock block = getEjectorBlock();

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

            EjectorBlock block = getEjectorBlock();
            if (enrichingTimer >= block.getEnrichingInterval()) {
                for(int i = 0; i < elemenixStorage.length; i++) {
                    if (canEnrich(i)) {
                        enrichElemenix(i);
                    }
                }
                enrichingTimer = 0;
                enrichingDone = true;
            }
        } else {
            enrichingTimer = 0;
        }

        if((!enrichingDone || hasStraightener()) && canEject()){
            ejectingTimer++;
            EjectorBlock block = getEjectorBlock();

            if(ejectingTimer >= block.getEjectingInterval()){
                ejectItem();
                ejectingTimer = 0;
            }
            isWorking = true;
        }else{
            ejectingTimer = 0;
        }

        if (wasWorking != isWorking) {
            level.setBlock(pos, state.setValue(EjectorBlock.WORKING, isWorking), Block.UPDATE_ALL);
        }

        setChanged(level, pos, state);
    }

    private boolean canDeconstruct() {
        ItemStack inputStack = getItem(INPUT_SLOT);
        return !inputStack.isEmpty() && !ElemenixInfo.isUndeconstructable(inputStack);
    }

    private void deconstructItem() {
        ItemStack inputStack = getItem(INPUT_SLOT);
        if (inputStack.isEmpty()) return;

        Constituents constituents = ElemenixInfo.getDiscountAppliedConstituents(inputStack);
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

    private boolean canEject() {
        for(int i = 0; i < INPUT_SLOT; i++){
            if(!items.get(i).isEmpty()){
                return true;
            }
        }
        return false;
    }

    private List<Integer> getEjectableIndexes() {
        List<Integer> indexes = new ArrayList<>();
        for(int i = 0; i < INPUT_SLOT; i++){
            if(!items.get(i).isEmpty()){
                indexes.add(i);
            }
        }
        return indexes;
    }

    private void ejectItem(){
        int index = getIndexToEject();
        if(index < 0 || index >= items.size()) return;

        ItemStack stackToEject = items.get(getIndexToEject());
        if(stackToEject.isEmpty()) return;

        int amount = getEjectionAmount(index);
        if(amount <= 0) return;

        ItemStack copiedStack = stackToEject.copy();
        copiedStack.setCount(amount);
        EjectorBlock block = getEjectorBlock();

        if (level != null) {
            block.popOutItem(level, getBlockPos(), getBlockState(), copiedStack);
        }

        stackToEject.shrink(amount);
        setChanged();
        ejectedIndex = index;
    }

    private boolean hasStraightener(){
        return items.get(STRAIGHTENER_SLOT).is(ModItems.FLOW_STRAIGHTENER);
    }

    private int getEjectionAmount(int index){
        int maxSize = items.get(index).getMaxStackSize();
        ItemStack valveStack = items.get(VALVE_SLOT);
        if(!valveStack.is(ModItems.THROTTLE_VALVE)) return Math.min(items.get(index).getCount(),maxSize);
        ValveOpenness openness = ThrottleValveItem.getOrCreateOpenness(valveStack);
        float amount = switch (openness.openness()){
            case 0 -> 0f;
            case 1 -> maxSize * 0.02f;
            case 2 -> maxSize * 0.125f;
            case 3 -> maxSize * 0.25f;
            case 4 -> maxSize * 0.5f;
            default -> maxSize;
        };
        return Math.min(items.get(index).getCount(), (int)Math.floor(amount));
    }

    private int getIndexToEject(){
        List<Integer> ejectableIndexes = getEjectableIndexes();
        if(ejectableIndexes.isEmpty()) return -1;
        if(ejectableIndexes.size() == 1){
            return ejectableIndexes.getFirst();
        }
        if(hasStraightener()){
            for(int i = ejectedIndex + 1; i < INPUT_SLOT; i++){
                if(ejectableIndexes.contains(i)){
                    return i;
                }
            }
            for(int j = 0; j <= ejectedIndex; j++){
                if(ejectableIndexes.contains(j)){
                    return j;
                }
            }
            return -1;
        }
        Random random = new Random();
        return ejectableIndexes.get(random.nextInt(0, ejectableIndexes.size()));
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

    public EjectorBlock getEjectorBlock() {
        return getBlockState().getBlock() instanceof EjectorBlock block ? block : null;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == INPUT_SLOT) {
            return !ElemenixInfo.isUndeconstructable(stack) && !stack.is(ModTags.IGNORED_BY_DECONSTRUCTOR_INPUT);
        }
        if (slot == VALVE_SLOT) {
            return stack.is(ModItems.THROTTLE_VALVE);
        }
        if (slot == STRAIGHTENER_SLOT) {
            return stack.is(ModItems.FLOW_STRAIGHTENER);
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
