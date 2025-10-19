package net.lghast.elemenix.common.content.blockentity;

import net.lghast.elemenix.common.content.block.TransformerBlock;
import net.lghast.elemenix.common.system.menu.TransformerMenu;
import net.lghast.elemenix.register.content.ModBlockEntities;
import net.lghast.elemenix.utils.Constituents;
import net.lghast.elemenix.utils.Elemenix;
import net.lghast.elemenix.utils.ElemenixInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

public class TransformerBlockEntity extends BaseContainerBlockEntity {
    private long inputA = 0;
    private long inputB = 0;
    private long outputC = 0;

    private int inputTimer = 0;
    private int outputTimer = 0;

    private NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);

    public TransformerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRANSFORMER.get(), pos, state);
    }

    public void setInputA(long inputA) {
        this.inputA = inputA;
    }

    public void setInputB(long inputB) {
        this.inputB = inputB;
    }

    public void setOutputC(long outputC) {
        this.outputC = outputC;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TransformerBlockEntity blockEntity) {
        if (level.isClientSide) return;

        TransformerBlock block = (TransformerBlock) state.getBlock();

        blockEntity.inputTimer++;
        if (blockEntity.inputTimer >= block.getInputAbsorbInterval()) {
            blockEntity.inputTimer = 0;
            blockEntity.processInputItems(block);
        }

        blockEntity.outputTimer++;
        if (blockEntity.outputTimer >= block.getOutputGenerateInterval()) {
            blockEntity.outputTimer = 0;
            blockEntity.processOutputGeneration(block);
        }

        boolean wasWorking = state.getValue(TransformerBlock.WORKING);
        boolean isWorking = blockEntity.outputC > block.getOutputRequirement() ||
                (blockEntity.inputA >= block.getConsumptionA() && blockEntity.inputB >= block.getConsumptionB());

        if (wasWorking != isWorking) {
            level.setBlock(pos, state.setValue(TransformerBlock.WORKING, isWorking), 3);
        }
    }

    private void processInputItems(TransformerBlock block) {
        if (!items.get(0).isEmpty()) {
            Constituents constituents = ElemenixInfo.getConstituents(items.get(0));
            if (constituents.isPure(block.getInputTypeA())) {
                items.get(0).shrink(1);
                inputA += constituents.get(block.getInputTypeA());
                setChanged();
            }
        }

        if (!items.get(1).isEmpty()) {
            Constituents constituents = ElemenixInfo.getConstituents(items.get(1));
            if (constituents.isPure(block.getInputTypeB())) {
                items.get(1).shrink(1);
                inputB += constituents.get(block.getInputTypeB());
                setChanged();
            }
        }
    }

    private void processOutputGeneration(TransformerBlock block) {
        if (inputA >= block.getConsumptionA() && inputB >= block.getConsumptionB()) {
            inputA -= block.getConsumptionA();
            inputB -= block.getConsumptionB();
            outputC += block.getProductionC();
            setChanged();
        }

        if (outputC >= block.getOutputRequirement() && canOutputItem(block)) {
            outputC -= block.getOutputRequirement();
            ItemStack outputStack = new ItemStack(block.getOutputItem());
            if (items.get(2).isEmpty()) {
                items.set(2, outputStack);
            } else {
                items.get(2).grow(1);
            }
            setChanged();
        }
    }

    private boolean canOutputItem(TransformerBlock block) {
        ItemStack outputSlot = items.get(2);
        return outputSlot.isEmpty() ||
                (outputSlot.getCount() < outputSlot.getMaxStackSize() &&
                        outputSlot.getItem() == block.getOutputItem());
    }

    public long getInputA() { return inputA; }
    public long getInputB() { return inputB; }
    public long getOutputC() { return outputC; }

    public static TransformerBlockEntity getBlockEntity(Level level, BlockPos pos) {
        if (level != null && level.getBlockEntity(pos) instanceof TransformerBlockEntity blockEntity) {
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
    protected Component getDefaultName() {
        TransformerBlock block = getTransformerBlock();
        return block != null ? block.getGuiTitle() : Component.translatable("container.elemenix.transformer");
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> nonNullList) {
        items = nonNullList;
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory playerInventory) {
        return new TransformerMenu(containerId, playerInventory, this);
    }

    @Override
    public int getContainerSize() {
        return 3;
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(items, slot, amount);
        if (!stack.isEmpty()) {
            setChanged();
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
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
        if (level.getBlockEntity(worldPosition) != this) {
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
        tag.putInt("InputTimer", inputTimer);
        tag.putInt("OutputTimer", outputTimer);
        ContainerHelper.saveAllItems(tag, items, provider);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        inputA = tag.getLong("InputA");
        inputB = tag.getLong("InputB");
        outputC = tag.getLong("OutputC");
        inputTimer = tag.getInt("InputTimer");
        outputTimer = tag.getInt("OutputTimer");
        ContainerHelper.loadAllItems(tag, items, provider);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot < 2) {
            TransformerBlock block = getTransformerBlock();
            if (block == null) return false;

            Elemenix requiredType = (slot == 0) ? block.getInputTypeA() : block.getInputTypeB();
            Constituents constituents = ElemenixInfo.getConstituents(stack);
            return constituents.isPure(requiredType);
        }
        return false;
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
