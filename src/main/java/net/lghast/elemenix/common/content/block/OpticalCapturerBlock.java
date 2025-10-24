package net.lghast.elemenix.common.content.block;

import com.mojang.serialization.MapCodec;
import net.lghast.elemenix.common.content.blockentity.TransformerBlockEntity;
import net.lghast.elemenix.conifig.ServerConfig;
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
import org.jetbrains.annotations.Nullable;

public class OpticalCapturerBlock extends TransformerBlock {
    public static final MapCodec<OpticalCapturerBlock> CODEC = simpleCodec(OpticalCapturerBlock::new);

    public OpticalCapturerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Elemenix getInputElemenixA() {
        return Elemenix.ORGANIX;
    }

    @Override
    public Elemenix getInputElemenixB() {
        return Elemenix.FLUMIX;
    }

    @Override
    public Elemenix getOutputElemenix() {
        return Elemenix.ENERGIX;
    }

    @Override
    public Item getOutputItem() {
        return ModItems.ENERGIX_ESSENCE.asItem();
    }

    @Override
    public int getConsumptionA() {
        return ServerConfig.OC_CONSUMPTION.get();
    }

    @Override
    public int getConsumptionB() {
        return ServerConfig.OC_CONSUMPTION.get();
    }

    @Override
    public int getProduction() {
        return ServerConfig.OC_PRODUCTION.get();
    }

    @Override
    public int getRcRequirement() {
        return ElemenixInfo.ESSENCE_VALUE;
    }

    @Override
    public int getDcInterval() {
        return ServerConfig.OC_DC_INTERVAL.get();
    }

    @Override
    public int getRcInterval() {
        return ServerConfig.OC_RC_INTERVAL.get();
    }

    @Override
    public ResourceLocation getGuiTexture() {
        return ResourceLocation.fromNamespaceAndPath("elemenix", "textures/gui/optical_capturer.png");
    }

    @Override
    public Component getGuiTitle() {
        return Component.translatable("container.elemenix.optical_capturer");
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TransformerBlockEntity(pos, state);
    }
}
