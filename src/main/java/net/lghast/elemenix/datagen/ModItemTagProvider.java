package net.lghast.elemenix.datagen;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.register.content.ModBlocks;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.register.system.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends ItemTagsProvider {
    public ModItemTagProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pLookupProvider, CompletableFuture<TagLookup<Block>> pBlockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(pOutput, pLookupProvider, pBlockTags, Elemenics.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider pProvider) {
        tag(ModTags.ESSENCES)
                .add(ModItems.ORGANIX_ESSENCE.asItem())
                .add(ModItems.TERRIX_ESSENCE.asItem())
                .add(ModItems.FLUMIX_ESSENCE.asItem())
                .add(ModItems.METALLIX_ESSENCE.asItem())
                .add(ModItems.ENERGIX_ESSENCE.asItem())
                .add(ModItems.ARCANIX_ESSENCE.asItem())

                .add(ModItems.ORGANIX_ESSENPLEX.asItem())
                .add(ModItems.TERRIX_ESSENPLEX.asItem())
                .add(ModItems.FLUMIX_ESSENPLEX.asItem())
                .add(ModItems.METALLIX_ESSENPLEX.asItem())
                .add(ModItems.ENERGIX_ESSENPLEX.asItem())
                .add(ModItems.ARCANIX_ESSENPLEX.asItem())
        ;

        tag(ModTags.EQUILIBRIUM)
                .add(ModItems.ELEMENIC_EQUILIBRIUM.asItem())
                .add(ModItems.ELEMENIC_EQUILIPLEX.asItem())
                .add(ModItems.ELEMENIC_CHAOS.asItem())
        ;

        tag(ModTags.ANALYZER_UNRECORDABLE)
                .addTag(ModTags.ESSENCES)
                .addTag(ModTags.EQUILIBRIUM)
                .add(Items.ENCHANTED_BOOK)
                .add(ModItems.ELEMENIC_ANALYZER.asItem())
                .add(ModItems.ANALYSIS_TERMINAL.asItem())
        ;

        tag(ModTags.MEMORIZER_SLOT_PLACEABLE)
                .add(ModItems.ELEMENIC_MEMORIZER.asItem())
                .add(ModItems.FONDANT_CAKE.asItem())
        ;

        tag(ModTags.ELEMENIC_TRANSFORMERS)
                .add(ModBlocks.GEOLOGICAL_SIMULATOR.asItem())
                .add(ModBlocks.METALLURGICAL_ACTIVATOR.asItem())
                .add(ModBlocks.GERMINAL_ACCELERATOR.asItem())
                .add(ModBlocks.TRANSPIRING_INCINERATOR.asItem())
                .add(ModBlocks.OPTICAL_CAPTURER.asItem())
        ;

        tag(ModTags.ELEMENIC_DECONSTRUCTORS)
                .add(ModBlocks.ELEMENIC_INFUSER.asItem())
                .add(ModBlocks.ELEMENIC_ENRICHER.asItem())
                .add(ModBlocks.ELEMENIC_EJECTOR.asItem())
        ;

        tag(ModTags.IGNORED_BY_DECONSTRUCTOR_INPUT)
                .add(ModItems.ELEMENIC_STORAGE.asItem())
                .add(ModItems.ELEMENIC_ANALYZER.asItem())
                .add(ModItems.SCANNING_STORAGE.asItem())
                .add(ModItems.ELEMENIC_MEMORIZER.asItem())
                .add(ModItems.MEMORIZER_BOX.asItem())
                .add(ModItems.REMOTE_ELEMENIC_STORAGE.asItem())
                .add(ModItems.ANALYSIS_TERMINAL.asItem())
        ;

        tag(ModTags.IGNORED_BY_SCANNING)
                .add(ModItems.ELEMENIC_STORAGE.asItem())
                .add(ModItems.ELEMENIC_ANALYZER.asItem())
                .add(ModItems.SCANNING_STORAGE.asItem())
                .add(ModItems.ELEMENIC_MEMORIZER.asItem())
                .add(ModItems.ANALYSIS_TERMINAL.asItem())
                .add(ModItems.MEMORIZER_BOX.asItem())
        ;
    }
}
