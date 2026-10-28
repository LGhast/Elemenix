package net.lghast.elemenix.common.system.menu;

import net.lghast.elemenix.common.content.block.ElemenicComparatorBlock;
import net.lghast.elemenix.common.content.blockentity.ElemenicComparatorBlockEntity;
import net.lghast.elemenix.register.system.ModMenus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ElemenicComparatorMenu extends AbstractContainerMenu {
    private final ElemenicComparatorBlockEntity blockEntity;
    private final ContainerData data;

    public ElemenicComparatorMenu(int containerId, ElemenicComparatorBlockEntity blockEntity) {
        super(ModMenus.ELEMENIC_COMPARATOR_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = blockEntity.getDataAccess();
        addDataSlots(data);
    }

    public int getTarget() {
        return blockEntity.getBlockState().getValue(ElemenicComparatorBlock.TARGET);
    }

    public int getRule() {
        return blockEntity.getBlockState().getValue(ElemenicComparatorBlock.RULE);
    }

    public int getThreshold() {
        return data.get(0);
    }

    public int getUnit() {
        return data.get(1);
    }

    public ContainerData getData() {
        return data;
    }

    public void update(int target, int rule, int threshold, int unit) {
        blockEntity.setCriterion(target, rule, threshold, unit);
    }

    public ElemenicComparatorBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        if (blockEntity == null || blockEntity.getLevel() == null) return false;
        return blockEntity.getLevel().getBlockEntity(blockEntity.getBlockPos()) == blockEntity;
    }
}
