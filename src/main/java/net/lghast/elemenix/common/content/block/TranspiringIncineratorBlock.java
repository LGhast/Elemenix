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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TranspiringIncineratorBlock extends TransformerBlock {
    public static final MapCodec<TranspiringIncineratorBlock> CODEC = simpleCodec(TranspiringIncineratorBlock::new);

    public TranspiringIncineratorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Elemenix getInputElemenixA() {
        return Elemenix.ORGANIX;
    }

    @Override
    public Elemenix getInputElemenixB() {
        return Elemenix.ENERGIX;
    }

    @Override
    public Elemenix getOutputElemenix() {
        return Elemenix.FLUMIX;
    }

    @Override
    public Item getOutputItem() {
        return ModItems.FLUMIX_ESSENCE.asItem();
    }

    @Override
    public int getConsumptionA() {
        return ServerConfig.TI_CONSUMPTION.get();
    }

    @Override
    public int getConsumptionB() {
        return ServerConfig.TI_CONSUMPTION.get();
    }

    @Override
    public int getProduction() {
        return ServerConfig.TI_PRODUCTION.get();
    }

    @Override
    public int getRcRequirement() {
        return ElemenixInfo.ESSENCE_VALUE;
    }

    @Override
    public int getDcInterval() {
        return ServerConfig.TI_DC_INTERVAL.get();
    }

    @Override
    public int getRcInterval() {
        return ServerConfig.TI_RC_INTERVAL.get();
    }

    @Override
    public ResourceLocation getGuiTexture() {
        return ResourceLocation.fromNamespaceAndPath("elemenix", "textures/gui/transpiring_incinerator.png");
    }

    @Override
    public Component getGuiTitle() {
        return Component.translatable("container.elemenix.transpiring_incinerator");
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
