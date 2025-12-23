package net.lghast.elemenix.common.content.blockentity;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.common.content.block.InfuserBlock;
import net.lghast.elemenix.common.content.item.AnalyzerItem;
import net.lghast.elemenix.common.system.datacomponent.ElemenicStorage;
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
import java.util.Arrays;

@ParametersAreNonnullByDefault
public class InfuserBlockEntity extends BaseContainerBlockEntity {
    private static final int ANALYZER_SLOT = 0;
    private static final int INPUT_SLOT = 1;
    private static final int SLOT_COUNT = 2;

    private final static String TAG_STORAGE = "ElemenixStorage";
    private final String TAG_DC_TIMER = "DeconstructionTimer";
    private final String TAG_INFUSION_TIMER = "InfusionTimer";

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final int[] elemenixStorage = new int[6];

    private int deconstructionTimer = 0;
    private int infusionTimer = 0;

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

        deconstructionTimer = tag.getInt(TAG_DC_TIMER);
        infusionTimer = tag.getInt(TAG_INFUSION_TIMER);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        ContainerHelper.saveAllItems(tag, items, provider);

        tag.putIntArray(TAG_STORAGE, elemenixStorage);

        tag.putInt(TAG_DC_TIMER, deconstructionTimer);
        tag.putInt(TAG_INFUSION_TIMER, infusionTimer);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;
        if(!Elemenics.started) return;

        boolean wasWorking = state.getValue(InfuserBlock.WORKING);
        boolean isWorking = false;

        if (canDeconstruct()) {
            deconstructionTimer++;
            InfuserBlock block = getInfuserBlock();

            if (deconstructionTimer >= block.getDcInterval()) {
                deconstructItem();
                deconstructionTimer = 0;
            }
            isWorking = true;
        } else {
            deconstructionTimer = 0;
        }

        if (canInfuse()) {
            infusionTimer++;
            InfuserBlock block = getInfuserBlock();

            if (infusionTimer >= block.getInfusionInterval()) {
                infuseToAnalyzer();
                infusionTimer = 0;
            }
            isWorking = true;
        } else {
            infusionTimer = 0;
        }

        if (wasWorking != isWorking) {
            level.setBlock(pos, state.setValue(InfuserBlock.WORKING, isWorking), Block.UPDATE_ALL);
        }

        setChanged(level, pos, state);
    }

    private boolean canDeconstruct() {
        ItemStack inputStack = getItem(INPUT_SLOT);
        return !inputStack.isEmpty() && !ElemenixInfo.isUnanalysable(inputStack);
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

    private boolean canInfuse() {
        ItemStack analyzerStack = getItem(ANALYZER_SLOT);
        return !analyzerStack.isEmpty() &&
                analyzerStack.getItem() instanceof AnalyzerItem &&
                hasElemenixToInfuse();
    }

    private boolean hasElemenixToInfuse() {
        for (int value : elemenixStorage) {
            if (value > 0) return true;
        }
        return false;
    }

    private void infuseToAnalyzer() {
        ItemStack analyzerStack = getItem(ANALYZER_SLOT);
        if (analyzerStack.isEmpty() || !(analyzerStack.getItem() instanceof AnalyzerItem)) return;

        InfuserBlock block = (InfuserBlock) getBlockState().getBlock();
        int maxInfusion = block.getMaxInfusion();

        ElemenicStorage currentStorage = AnalyzerItem.getOrCreateData(analyzerStack);
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
            analyzerStack.set(ModDataComponents.ELEMENIC_STORAGE, new ElemenicStorage(newElemenix));
            setChanged();
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
        if (slot == ANALYZER_SLOT) {
            return stack.is(ModItems.ELEMENIC_ANALYZER);
        } else if (slot == INPUT_SLOT) {
            return !ElemenixInfo.isUnanalysable(stack);
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
}
