package net.lghast.elemenix.common.content.block;

import com.mojang.serialization.MapCodec;
import net.lghast.elemenix.common.content.blockentity.TransformerBlockEntity;
import net.lghast.elemenix.common.system.recipe.GerminatingRecipe;
import net.lghast.elemenix.conifig.CommonConfig;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.utils.elemenix.Elemenix;
import net.lghast.elemenix.utils.elemenix.ElemenixInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class GerminalAcceleratorBlock extends TransformerBlock {
    public static final MapCodec<GerminalAcceleratorBlock> CODEC = simpleCodec(GerminalAcceleratorBlock::new);

    public GerminalAcceleratorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Elemenix getInputElemenixA() {
        return Elemenix.TERRIX;
    }

    @Override
    public Elemenix getInputElemenixB() {
        return Elemenix.FLUMIX;
    }

    @Override
    public Elemenix getOutputElemenix() {
        return Elemenix.ORGANIX;
    }

    @Override
    public Item getOutputItem() {
        return ModItems.ORGANIX_ESSENCE.asItem();
    }

    @Override
    public int getConsumptionA() {
        return CommonConfig.GA_CONSUMPTION.get();
    }

    @Override
    public int getConsumptionB() {
        return CommonConfig.GA_CONSUMPTION.get();
    }

    @Override
    public int getProduction() {
        return CommonConfig.GA_PRODUCTION.get();
    }

    @Override
    public int getRcRequirement() {
        return ElemenixInfo.ESSENCE_VALUE;
    }

    @Override
    public int getDcInterval() {
        return CommonConfig.GA_DC_INTERVAL.get();
    }

    @Override
    public int getRcInterval() {
        return CommonConfig.GA_RC_INTERVAL.get();
    }

    @Override
    public ResourceLocation getGuiTexture() {
        return ResourceLocation.fromNamespaceAndPath("elemenix", "textures/gui/germinal_accelerator.png");
    }

    @Override
    public ResourceLocation getSecondaryGuiTexture() {
        return ResourceLocation.fromNamespaceAndPath("elemenix", "textures/gui/germinal_accelerator_germinating.png");
    }

    @Override
    public Component getGuiTitle() {
        return Component.translatable("container.elemenix.germinal_accelerator");
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TransformerBlockEntity(pos, state);
    }

    @Override
    public int getPageCount() {
        return 2;
    }

    public int getGerminationInterval() {
        return Math.max(1, CommonConfig.GA_GERMINATION_INTERVAL.get());
    }

    @Override
    public boolean shouldSuppressTransformingConsumption(TransformerBlockEntity blockEntity) {
        return blockEntity.getGerminatingProgress() > 0;
    }

    @Override
    public void tickSecondPage(Level level, BlockPos pos, BlockState state, TransformerBlockEntity blockEntity) {
        GerminatingRecipe recipe = GerminatingRecipe.findRecipe(level,
                blockEntity.getItem(TransformerBlockEntity.TEMPLATE_SLOT));

        if (recipe == null) {
            if (blockEntity.getGerminatingProgress() != 0) {
                blockEntity.setGerminatingProgress(0);
                blockEntity.setChanged();
            }
            return;
        }

        int[] cost = recipe.getCost();
        if (blockEntity.getInputA() < cost[0] || blockEntity.getInputB() < cost[1]
                || !blockEntity.canGenerateProducts(recipe)) {
            if (blockEntity.getGerminatingProgress() != 0) {
                blockEntity.setGerminatingProgress(0);
                blockEntity.setChanged();
            }
            return;
        }

        int progress = blockEntity.getGerminatingProgress() + 1;
        if (progress >= getGerminationInterval()) {
            blockEntity.setInputA(blockEntity.getInputA() - cost[0]);
            blockEntity.setInputB(blockEntity.getInputB() - cost[1]);
            blockEntity.generateProducts(recipe);
            blockEntity.setGerminatingProgress(0);
        } else {
            blockEntity.setGerminatingProgress(progress);
        }
        blockEntity.setChanged();
    }
}
