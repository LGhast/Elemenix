package net.lghast.elemenix.client.render;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.register.content.ModBlocks;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
@EventBusSubscriber(modid = Elemenics.MOD_ID, value = Dist.CLIENT)
public class ItemBlockRenders {
    private ItemBlockRenders() {}

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        event.getModels().replaceAll((location, model) ->
                isCutoutBlock(location.id()) ? new CutoutModel(model) : model);
    }

    private static boolean isCutoutBlock(ResourceLocation location) {
        Block block = BuiltInRegistries.BLOCK.get(location);
        return block == ModBlocks.GEOLOGICAL_SIMULATOR.get()
                || block == ModBlocks.METALLURGICAL_ACTIVATOR.get()
                || block == ModBlocks.TRANSPIRING_INCINERATOR.get()
                || block == ModBlocks.GERMINAL_ACCELERATOR.get()
                || block == ModBlocks.OPTICAL_CAPTURER.get()
                || block == ModBlocks.ELEMENIC_COMPARATOR.get()
                || block == ModBlocks.SUBSTANCE_PRINTER.get()
                || block == ModBlocks.WITCHCRAFT_PRINTER.get()
                || block == ModBlocks.MEMORIZER_REPLICATOR.get();
    }

    private static final class CutoutModel extends BakedModelWrapper<BakedModel> {
        private CutoutModel(BakedModel original) {
            super(original);
        }

        @Override
        public @NotNull ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource random, ModelData data) {
            return ChunkRenderTypeSet.of(RenderType.cutout());
        }
    }
}
