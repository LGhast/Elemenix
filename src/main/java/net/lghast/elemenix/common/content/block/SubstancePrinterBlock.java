package net.lghast.elemenix.common.content.block;

import com.mojang.serialization.MapCodec;
import net.lghast.elemenix.common.content.blockentity.AbstractPrinterBlockEntity;
import net.lghast.elemenix.common.content.blockentity.SubstancePrinterBlockEntity;
import net.lghast.elemenix.register.content.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class SubstancePrinterBlock extends AbstractPrinterBlock {
    public static final MapCodec<SubstancePrinterBlock> CODEC = simpleCodec(SubstancePrinterBlock::new);

    public SubstancePrinterBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockEntityType<? extends AbstractPrinterBlockEntity> getBlockEntityType() {
        return ModBlockEntities.SUBSTANCE_PRINTER.get();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SubstancePrinterBlockEntity(pos, state);
    }
}
