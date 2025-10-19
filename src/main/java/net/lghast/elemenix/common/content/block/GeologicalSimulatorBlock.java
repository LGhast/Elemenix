package net.lghast.elemenix.common.content.block;

import com.mojang.serialization.MapCodec;
import net.lghast.elemenix.common.content.blockentity.TransformerBlockEntity;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.utils.Elemenix;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class GeologicalSimulatorBlock extends TransformerBlock {
    public static final MapCodec<GeologicalSimulatorBlock> CODEC = simpleCodec(GeologicalSimulatorBlock::new);

    public GeologicalSimulatorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Elemenix getInputTypeA() {
        return Elemenix.METALLIX;
    }

    @Override
    public Elemenix getInputTypeB() {
        return Elemenix.FLUMIX;
    }

    @Override
    public Elemenix getOutputType() {
        return Elemenix.TERRIX;
    }

    @Override
    public Item getOutputItem() {
        return ModItems.TERRIX_ESSENCE.asItem();
    }

    @Override
    public long getConsumptionA() {
        return 72;
    }

    @Override
    public long getConsumptionB() {
        return 72;
    }

    @Override
    public long getProductionC() {
        return 72;
    }

    @Override
    public long getOutputRequirement() {
        return 486;
    }

    @Override
    public int getInputAbsorbInterval() {
        return 20;
    }

    @Override
    public int getOutputGenerateInterval() {
        return 10;
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
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TransformerBlockEntity(pos, state);
    }
}
