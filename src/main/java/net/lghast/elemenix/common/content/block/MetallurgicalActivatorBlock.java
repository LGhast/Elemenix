package net.lghast.elemenix.common.content.block;

import com.mojang.serialization.MapCodec;
import net.lghast.elemenix.common.content.blockentity.TransformerBlockEntity;
import net.lghast.elemenix.conifig.CommonConfig;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.utils.elemenix.Elemenix;
import net.lghast.elemenix.utils.elemenix.ElemenixInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class MetallurgicalActivatorBlock extends TransformerBlock {
    public static final MapCodec<MetallurgicalActivatorBlock> CODEC = simpleCodec(MetallurgicalActivatorBlock::new);

    public MetallurgicalActivatorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Elemenix getInputElemenixA() {
        return Elemenix.TERRIX;
    }

    @Override
    public Elemenix getInputElemenixB() {
        return Elemenix.ENERGIX;
    }

    @Override
    public Elemenix getOutputElemenix() {
        return Elemenix.METALLIX;
    }

    @Override
    public Item getOutputItem() {
        return ModItems.METALLIX_ESSENCE.asItem();
    }

    @Override
    public int getConsumptionA() {
        return CommonConfig.MA_CONSUMPTION.get();
    }

    @Override
    public int getConsumptionB() {
        return CommonConfig.MA_CONSUMPTION.get();
    }

    @Override
    public int getProduction() {
        return CommonConfig.MA_PRODUCTION.get();
    }

    @Override
    public int getRcRequirement() {
        return ElemenixInfo.ESSENCE_VALUE;
    }

    @Override
    public int getDcInterval() {
        return CommonConfig.MA_DC_INTERVAL.get();
    }

    @Override
    public int getRcInterval() {
        return CommonConfig.MA_RC_INTERVAL.get();
    }

    @Override
    public ResourceLocation getGuiTexture() {
        return ResourceLocation.fromNamespaceAndPath("elemenix", "textures/gui/metallurgical_activator.png");
    }

    @Override
    public Component getGuiTitle() {
        return Component.translatable("container.elemenix.metallurgical_activator");
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
}
