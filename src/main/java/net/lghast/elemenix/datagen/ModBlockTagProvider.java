package net.lghast.elemenix.datagen;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.register.content.ModBlocks;
import net.lghast.elemenix.register.system.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.CompletableFuture;

@ParametersAreNonnullByDefault
public class ModBlockTagProvider extends BlockTagsProvider {
    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper){
        super(output,lookupProvider, Elemenics.MOD_ID,existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        tag(ModTags.ELEMENIC_TRANSFORMING_BLOCKS)
                .add(ModBlocks.GEOLOGICAL_SIMULATOR.get())
                .add(ModBlocks.METALLURGICAL_ACTIVATOR.get())
                .add(ModBlocks.GERMINAL_ACCELERATOR.get())
                .add(ModBlocks.TRANSPIRING_INCINERATOR.get())
                .add(ModBlocks.OPTICAL_CAPTURER.get())
        ;

        tag(ModTags.ELEMENIX_OUTPUTS)
                .add(ModBlocks.ELEMENIC_DECONSTRUCTOR.get())
                .add(ModBlocks.ELEMENIC_TANK.get())
                .addTag(ModTags.ELEMENIC_TRANSFORMING_BLOCKS)
        ;

        tag(ModTags.ELEMENIX_INPUTS)
                .add(ModBlocks.ELEMENIC_TANK.get())
                .add(ModBlocks.ELEMENIC_INFUSER.get())
                .add(ModBlocks.ELEMENIC_ENRICHER.get())
                .add(ModBlocks.ELEMENIC_EJECTOR.get())
                .addTag(ModTags.ELEMENIC_TRANSFORMING_BLOCKS)
        ;

        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .addTag(ModTags.ELEMENIX_OUTPUTS)
                .addTag(ModTags.ELEMENIX_INPUTS)
                .add(ModBlocks.ELEMENIC_OUTLET.get())
                .add(ModBlocks.ELEMENIC_CHANNEL.get())
        ;

        tag(BlockTags.NEEDS_IRON_TOOL)
                .addTag(ModTags.ELEMENIX_OUTPUTS)
                .addTag(ModTags.ELEMENIX_INPUTS)
                .add(ModBlocks.ELEMENIC_OUTLET.get())
        ;
    }
}
