package net.lghast.elemenix.common.content.block;

import com.mojang.serialization.MapCodec;
import net.lghast.elemenix.common.content.blockentity.TransformerBlockEntity;
import net.lghast.elemenix.conifig.CommonConfig;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.utils.Elemenix;
import net.lghast.elemenix.utils.ElemenixInfo;
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
public class GeologicalSimulatorBlock extends TransformerBlock {
    public static final MapCodec<GeologicalSimulatorBlock> CODEC = simpleCodec(GeologicalSimulatorBlock::new);

    public GeologicalSimulatorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Elemenix getInputElemenixA() {
        return Elemenix.METALLIX;
    }

    @Override
    public Elemenix getInputElemenixB() {
        return Elemenix.FLUMIX;
    }

    @Override
    public Elemenix getOutputElemenix() {
        return Elemenix.TERRIX;
    }

    @Override
    public Item getOutputItem() {
        return ModItems.TERRIX_ESSENCE.asItem();
    }

    @Override
    public int getConsumptionA() {
        return CommonConfig.GS_CONSUMPTION.get();
    }

    @Override
    public int getConsumptionB() {
        return CommonConfig.GS_CONSUMPTION.get();
    }

    @Override
    public int getProduction() {
        return CommonConfig.GS_PRODUCTION.get();
    }

    @Override
    public int getRcRequirement() {
        return ElemenixInfo.ESSENCE_VALUE;
    }

    @Override
    public int getDcInterval() {
        return CommonConfig.GS_DC_INTERVAL.get();
    }

    @Override
    public int getRcInterval() {
        return CommonConfig.GS_RC_INTERVAL.get();
    }

    @Override
    public ResourceLocation getGuiTexture() {
        return ResourceLocation.fromNamespaceAndPath("elemenix", "textures/gui/geological_simulator.png");
    }

    @Override
    public Component getGuiTitle() {
        return Component.translatable("container.elemenix.geological_simulator");
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
