package net.lghast.elemenix.common.content.blockentity;

import net.lghast.elemenix.common.content.block.InfuserBlock;
import net.lghast.elemenix.common.content.block.TransformerBlock;
import net.lghast.elemenix.common.content.item.AnalyzerItem;
import net.lghast.elemenix.common.system.datacomponent.ElemenicStorage;
import net.lghast.elemenix.common.system.menu.InfuserMenu;
import net.lghast.elemenix.common.system.menu.TransformerMenu;
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
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Arrays;

public class InfuserBlockEntity extends BaseContainerBlockEntity {
    private static final int ANALYZER_SLOT = 0;
    private static final int INPUT_SLOT = 1;
    private static final int SLOT_COUNT = 2;

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
            elemenixStorage[index] = Math.max(0, Math.min(value, Integer.MAX_VALUE));
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
    protected Component getDefaultName() {
        return Component.translatable("container.elemenix.elemenic_infuser");
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
        return new InfuserMenu(containerId, playerInventory, this);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        // 加载元质储存
        if (tag.contains("ElemenixStorage", CompoundTag.TAG_INT_ARRAY)) {
            int[] stored = tag.getIntArray("ElemenixStorage");
            System.arraycopy(stored, 0, elemenixStorage, 0, Math.min(stored.length, 6));
        }

        // 加载计时器
        deconstructionTimer = tag.getInt("DeconstructionTimer");
        infusionTimer = tag.getInt("InfusionTimer");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);

        // 保存元质储存
        tag.putIntArray("ElemenixStorage", elemenixStorage);

        // 保存计时器
        tag.putInt("DeconstructionTimer", deconstructionTimer);
        tag.putInt("InfusionTimer", infusionTimer);
    }

    public void tick(Level level, BlockPos pos, BlockState state, InfuserBlockEntity blockEntity) {
        if (level.isClientSide) return;

        boolean wasWorking = state.getValue(InfuserBlock.WORKING);
        boolean isWorking = false;

        // 解构处理
        if (canDeconstruct()) {
            deconstructionTimer++;
            InfuserBlock block = (InfuserBlock) state.getBlock();

            if (deconstructionTimer >= block.getDcInterval()) {
                deconstructItem();
                deconstructionTimer = 0;
            }
            isWorking = true;
        } else {
            deconstructionTimer = 0;
        }

        // 注入处理
        if (canInfuse()) {
            infusionTimer++;
            InfuserBlock block = (InfuserBlock) state.getBlock();

            if (infusionTimer >= block.getInfusionInterval()) {
                infuseToAnalyzer();
                infusionTimer = 0;
            }
            isWorking = true;
        } else {
            infusionTimer = 0;
        }

        // 更新工作状态
        if (wasWorking != isWorking) {
            level.setBlock(pos, state.setValue(InfuserBlock.WORKING, isWorking), Block.UPDATE_ALL);
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

        // 添加元质到储存
        for (Elemenix elemenix : Elemenix.values()) {
            int value = constituents.get(elemenix);
            if (value > 0) {
                addElemenix(elemenix, value);
            }
        }

        // 减少物品数量
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

        // 对每种元质进行注入
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
            // 更新分析仪数据
            analyzerStack.set(ModDataComponents.ELEMENIC_STORAGE, new ElemenicStorage(newElemenix));
            setChanged();
        }
    }

    // 元质储存操作方法
    public int getStorage(Elemenix elemenix) {
        int index = elemenix.getIndex();
        return index >= 0 && index < 6 ? elemenixStorage[index] : 0;
    }

    public void setStorage(Elemenix elemenix, int value) {
        int index = elemenix.getIndex();
        if (index >= 0 && index < 6) {
            elemenixStorage[index] = Math.max(0, Math.min(value, Integer.MAX_VALUE));
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

    public void consumeElemenix(Elemenix elemenix, int amount) {
        if (amount <= 0) return;
        int index = elemenix.getIndex();
        if (index >= 0 && index < 6) {
            elemenixStorage[index] = Math.max(elemenixStorage[index] - amount, 0);
            setChanged();
        }
    }

    // 获取对应的方块
    public InfuserBlock getInfuserBlock() {
        return getBlockState().getBlock() instanceof InfuserBlock block ? block : null;
    }

    // 槽位验证
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == ANALYZER_SLOT) {
            return stack.is(ModItems.ELEMENIC_ANALYZER);
        } else if (slot == INPUT_SLOT) {
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


}
