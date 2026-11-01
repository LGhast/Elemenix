package net.lghast.elemenix.common.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BaseEntityBlock;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class PressurizedOutletBlock extends OutletBlock {
    public static final MapCodec<PressurizedOutletBlock> CODEC = simpleCodec(PressurizedOutletBlock::new);

    public PressurizedOutletBlock(Properties properties) {
        super(properties, 2);
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
}
