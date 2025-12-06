package net.lghast.elemenix.datagen;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.register.content.ModBlocks;
import net.lghast.elemenix.register.system.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

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

        tag(ModTags.ELEMENIC_DECONSTRUCTING_BLOCKS)
                .add(ModBlocks.ELEMENIC_INFUSER.get())
                .add(ModBlocks.ELEMENIC_ENRICHER.get())
                .add(ModBlocks.ELEMENIC_EJECTOR.get())
        ;

        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .addTag(ModTags.ELEMENIC_DECONSTRUCTING_BLOCKS)
                .addTag(ModTags.ELEMENIC_TRANSFORMING_BLOCKS)
        ;

        tag(BlockTags.NEEDS_IRON_TOOL)
                .addTag(ModTags.ELEMENIC_DECONSTRUCTING_BLOCKS)
                .addTag(ModTags.ELEMENIC_TRANSFORMING_BLOCKS)
        ;
    }
}
