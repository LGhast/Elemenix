package net.lghast.elemenix.common.content.blockentity;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.common.content.block.TransformerBlock;
import net.lghast.elemenix.common.system.menu.TransformerMenu;
import net.lghast.elemenix.common.system.recipe.GerminatingRecipe;
import net.lghast.elemenix.register.content.ModBlockEntities;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class TransformerBlockEntity extends BaseContainerBlockEntity {
    public static final int MODE_TRANSFORMING = 0;
    public static final int MODE_MINERALIZING = 1;
    public static final int TEMPLATE_SLOT = 3;
    public static final int PRODUCT_SLOT_FIRST = 4;
    public static final int PRODUCT_SLOT_COUNT = 6;

    private int inputA = 0;
    private int inputB = 0;
    private int outputC = 0;
    private int dcTimer = 0;
    private int rcTimer = 0;
    private int mode = MODE_TRANSFORMING;
    private int germinatingProgress = 0;

    private boolean transformEnabled = false;
    private boolean enrichEnabled = false;

    private NonNullList<ItemStack> items = NonNullList.withSize(10, ItemStack.EMPTY);

    public TransformerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRANSFORMER.get(), pos, state);
    }

    public void setInputA(int inputA) {
        this.inputA = inputA;
    }

    public void setInputB(int inputB) {
        this.inputB = inputB;
    }

    public void setOutputC(int outputC) {
        this.outputC = outputC;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getGerminatingProgress() {
        return germinatingProgress;
    }

    public void setGerminatingProgress(int germinatingProgress) {
        this.germinatingProgress = germinatingProgress;
    }

    public boolean isTransformEnabled() {
        return transformEnabled;
    }

    public boolean isEnrichEnabled() {
        return enrichEnabled;
    }

    public void setTransformEnabled(boolean transformEnabled) {
        this.transformEnabled = transformEnabled;
    }

    public void setEnrichEnabled(boolean enrichEnabled) {
        this.enrichEnabled = enrichEnabled;
    }

    public void toggleTransform() {
        transformEnabled = !transformEnabled;
        setChanged();
    }

    public void toggleEnrich() {
        enrichEnabled = !enrichEnabled;
        setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TransformerBlockEntity blockEntity) {
        if (level.isClientSide) return;
        if (!Elemenics.started) return;
        TransformerBlock block = (TransformerBlock) state.getBlock();
        if (blockEntity.mode != MODE_TRANSFORMING && block.getPageCount() > 1) {
            block.tickSecondPage(level, pos, state, blockEntity);
        } else if (blockEntity.germinatingProgress != 0) {
            blockEntity.setGerminatingProgress(0);
        }
        boolean suppressConsumption = block.shouldSuppressTransformingConsumption(blockEntity);
        boolean isWorking = blockEntity.tickTransforming(block, suppressConsumption);
        if (state.getValue(TransformerBlock.WORKING) != isWorking) {
            level.setBlock(pos, state.setValue(TransformerBlock.WORKING, isWorking), 3);
        }
    }

    private boolean tickTransforming(TransformerBlock block, boolean suppressConsumption) {
        dcTimer++;
        if (dcTimer >= block.getDcInterval()) {
            dcTimer = 0;
            deconstructInput(block);
        }
        if (!suppressConsumption) {
            rcTimer++;
            if (rcTimer >= block.getRcInterval()) {
                rcTimer = 0;
                if (transformEnabled) {
                    convertInput(block);
                }
                if (enrichEnabled) {
                    enrichOutput(block);
                }
            }
        }
        return (transformEnabled && inputA >= block.getConsumptionA() && inputB >= block.getConsumptionB())
                || (enrichEnabled && outputC >= block.getRcRequirement());
    }

    private void deconstructInput(TransformerBlock block) {
        ItemStack inputStackA = items.get(0);
        ItemStack inputStackB = items.get(1);
        if (!inputStackA.isEmpty() && !ElemenixInfo.isUnreconstructable(inputStackA)) {
            Constituents constituents = ElemenixInfo.getDiscountAppliedConstituents(inputStackA);
            if (constituents.isPure(block.getInputElemenixA())) {
                inputStackA.shrink(1);
                inputA += constituents.get(block.getInputElemenixA());
                setChanged();
            }
        }
        if (!inputStackB.isEmpty() && !ElemenixInfo.isUnreconstructable(inputStackB)) {
            Constituents constituents = ElemenixInfo.getDiscountAppliedConstituents(inputStackB);
            if (constituents.isPure(block.getInputElemenixB())) {
                inputStackB.shrink(1);
                inputB += constituents.get(block.getInputElemenixB());
                setChanged();
            }
        }
    }

    private void convertInput(TransformerBlock block) {
        if (inputA >= block.getConsumptionA() && inputB >= block.getConsumptionB()) {
            inputA -= block.getConsumptionA();
            inputB -= block.getConsumptionB();
            outputC += block.getProduction();
            setChanged();
        }
    }

    private void enrichOutput(TransformerBlock block) {
        if (outputC >= block.getRcRequirement() && canOutput(block)) {
            outputC -= block.getRcRequirement();
            ItemStack outputStack = new ItemStack(block.getOutputItem());
            if (items.get(2).isEmpty()) {
                items.set(2, outputStack);
            } else {
                items.get(2).grow(1);
            }
            setChanged();
        }
    }

    private boolean canOutput(TransformerBlock block) {
        ItemStack outputSlot = items.get(2);
        return outputSlot.isEmpty() || (outputSlot.getCount() < outputSlot.getMaxStackSize() && outputSlot.getItem() == block.getOutputItem());
    }

    public boolean canGenerateProducts(GerminatingRecipe recipe) {
        List<ItemStack> products = recipe.products();
        for (int i = 0; i < products.size(); i++) {
            ItemStack product = products.get(i);
            ItemStack slotStack = items.get(PRODUCT_SLOT_FIRST + i);
            if (slotStack.isEmpty()) continue;
            if (!ItemStack.isSameItemSameComponents(slotStack, product)) return false;
            if (slotStack.getCount() + product.getCount() > slotStack.getMaxStackSize()) return false;
        }
        return true;
    }

    public void generateProducts(GerminatingRecipe recipe) {
        List<ItemStack> products = recipe.products();
        for (int i = 0; i < Math.min(products.size(), PRODUCT_SLOT_COUNT); i++) {
            ItemStack product = products.get(i).copy();
            ItemStack slotStack = items.get(PRODUCT_SLOT_FIRST + i);
            if (slotStack.isEmpty()) {
                items.set(PRODUCT_SLOT_FIRST + i, product);
            } else {
                slotStack.grow(product.getCount());
            }
        }
        setChanged();
    }

    public int getInputA() { return inputA; }

    public int getInputB() { return inputB; }

    public int getOutputC() { return outputC; }

    public static TransformerBlockEntity getBlockEntity(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof TransformerBlockEntity blockEntity) {
            return blockEntity;
        }
        return null;
    }

    public TransformerBlock getTransformerBlock() {
        if (getBlockState().getBlock() instanceof TransformerBlock block) {
            return block;
        }
        return null;
    }

    @Override
    protected @NotNull Component getDefaultName() {
        TransformerBlock block = getTransformerBlock();
        return block != null ? block.getGuiTitle() : Component.translatable("container.elemenix.transformer");
    }

    @Override
    protected @NotNull NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> nonNullList) {
        items = nonNullList;
    }

    @Override
    protected @NotNull AbstractContainerMenu createMenu(int containerId, Inventory playerInventory) {
        return new TransformerMenu(containerId, playerInventory, this);
    }

    @Override
    public int getContainerSize() {
        return 10;
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(items, slot, amount);
        if (!stack.isEmpty()) {
            setChanged();
        }
        return stack;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5
        ) <= 64.0;
    }

    @Override
    public void clearContent() {
        items.clear();
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putLong("InputA", inputA);
        tag.putLong("InputB", inputB);
        tag.putLong("OutputC", outputC);
        tag.putInt("InputTimer", dcTimer);
        tag.putInt("OutputTimer", rcTimer);
        tag.putInt("Mode", mode);
        tag.putInt("GerminatingProgress", germinatingProgress);
        tag.putBoolean("TransformEnabled", transformEnabled);
        tag.putBoolean("EnrichEnabled", enrichEnabled);
        ContainerHelper.saveAllItems(tag, items, provider);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        inputA = tag.getInt("InputA");
        inputB = tag.getInt("InputB");
        outputC = tag.getInt("OutputC");
        dcTimer = tag.getInt("InputTimer");
        rcTimer = tag.getInt("OutputTimer");
        mode = tag.getInt("Mode");
        germinatingProgress = tag.getInt("GerminatingProgress");
        transformEnabled = tag.getBoolean("TransformEnabled");
        enrichEnabled = tag.getBoolean("EnrichEnabled");
        ContainerHelper.loadAllItems(tag, items, provider);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot < 2) {
            TransformerBlock block = getTransformerBlock();
            if (block == null) return false;
            if (ElemenixInfo.isUndeconstructable(stack) || stack.is(ModTags.IGNORED_BY_DECONSTRUCTOR_INPUT)) return false;
            Elemenix requiredType = (slot == 0) ? block.getInputElemenixA() : block.getInputElemenixB();
            Constituents constituents = ElemenixInfo.getConstituents(stack);
            return constituents.isPure(requiredType);
        }
        if (slot == TEMPLATE_SLOT) {
            return !ElemenixInfo.isUndeconstructable(stack) && !stack.is(ModTags.IGNORED_BY_DECONSTRUCTOR_INPUT);
        }
        return slot == 4 && !ElemenixInfo.isUndeconstructable(stack) && !stack.is(ModTags.IGNORED_BY_DECONSTRUCTOR_INPUT);
    }

    @Override
    public boolean canTakeItem(Container target, int slot, ItemStack stack) {
        return slot == 2;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
    }
}
