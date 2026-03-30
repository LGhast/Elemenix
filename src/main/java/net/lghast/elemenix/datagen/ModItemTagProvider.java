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
        tag(ModTags.DYED_WOOLS)
                .add(Items.ORANGE_WOOL)
                .add(Items.RED_WOOL)
                .add(Items.BLACK_WOOL)
                .add(Items.YELLOW_WOOL)
                .add(Items.LIME_WOOL)
                .add(Items.GREEN_WOOL)
                .add(Items.BLUE_WOOL)
                .add(Items.CYAN_WOOL)
                .add(Items.LIGHT_BLUE_WOOL)
                .add(Items.LIGHT_GRAY_WOOL)
                .add(Items.GRAY_WOOL)
                .add(Items.BROWN_WOOL)
                .add(Items.PINK_WOOL)
                .add(Items.PURPLE_WOOL)
                .add(Items.MAGENTA_WOOL)
        ;

        tag(ModTags.DYED_BEDS)
                .add(Items.ORANGE_BED)
                .add(Items.RED_BED)
                .add(Items.BLACK_BED)
                .add(Items.YELLOW_BED)
                .add(Items.LIME_BED)
                .add(Items.GREEN_BED)
                .add(Items.BLUE_BED)
                .add(Items.CYAN_BED)
                .add(Items.LIGHT_BLUE_BED)
                .add(Items.LIGHT_GRAY_BED)
                .add(Items.GRAY_BED)
                .add(Items.BROWN_BED)
                .add(Items.PINK_BED)
                .add(Items.PURPLE_BED)
                .add(Items.MAGENTA_BED)
        ;

        tag(ModTags.CONCRETE_POWDER)
                .add(Items.WHITE_CONCRETE_POWDER)
                .add(Items.ORANGE_CONCRETE_POWDER)
                .add(Items.RED_CONCRETE_POWDER)
                .add(Items.BLACK_CONCRETE_POWDER)
                .add(Items.YELLOW_CONCRETE_POWDER)
                .add(Items.LIME_CONCRETE_POWDER)
                .add(Items.GREEN_CONCRETE_POWDER)
                .add(Items.BLUE_CONCRETE_POWDER)
                .add(Items.CYAN_CONCRETE_POWDER)
                .add(Items.LIGHT_BLUE_CONCRETE_POWDER)
                .add(Items.LIGHT_GRAY_CONCRETE_POWDER)
                .add(Items.GRAY_CONCRETE_POWDER)
                .add(Items.BROWN_CONCRETE_POWDER)
                .add(Items.PINK_CONCRETE_POWDER)
                .add(Items.PURPLE_CONCRETE_POWDER)
                .add(Items.MAGENTA_CONCRETE_POWDER)
        ;

        tag(ModTags.CONCRETE)
                .add(Items.WHITE_CONCRETE)
                .add(Items.ORANGE_CONCRETE)
                .add(Items.RED_CONCRETE)
                .add(Items.BLACK_CONCRETE)
                .add(Items.YELLOW_CONCRETE)
                .add(Items.LIME_CONCRETE)
                .add(Items.GREEN_CONCRETE)
                .add(Items.BLUE_CONCRETE)
                .add(Items.CYAN_CONCRETE)
                .add(Items.LIGHT_BLUE_CONCRETE)
                .add(Items.LIGHT_GRAY_CONCRETE)
                .add(Items.GRAY_CONCRETE)
                .add(Items.BROWN_CONCRETE)
                .add(Items.PINK_CONCRETE)
                .add(Items.PURPLE_CONCRETE)
                .add(Items.MAGENTA_CONCRETE)
        ;

        tag(ModTags.BIG_FLOWERS)
                .add(Items.SUNFLOWER)
                .add(Items.LILAC)
                .add(Items.PEONY)
                .add(Items.ROSE_BUSH)
                .add(Items.PITCHER_PLANT)
        ;

        tag(ModTags.FROGLIGHTS)
                .add(Items.OCHRE_FROGLIGHT)
                .add(Items.VERDANT_FROGLIGHT)
                .add(Items.PEARLESCENT_FROGLIGHT)
        ;

        tag(ModTags.CORALS)
                .add(Items.BRAIN_CORAL)
                .add(Items.BUBBLE_CORAL)
                .add(Items.FIRE_CORAL)
                .add(Items.HORN_CORAL)
                .add(Items.TUBE_CORAL)
                .add(Items.BRAIN_CORAL_FAN)
                .add(Items.BUBBLE_CORAL_FAN)
                .add(Items.FIRE_CORAL_FAN)
                .add(Items.HORN_CORAL_FAN)
                .add(Items.TUBE_CORAL_FAN)
        ;

        tag(ModTags.DEAD_CORALS)
                .add(Items.DEAD_BRAIN_CORAL)
                .add(Items.DEAD_BUBBLE_CORAL)
                .add(Items.DEAD_FIRE_CORAL)
                .add(Items.DEAD_HORN_CORAL)
                .add(Items.DEAD_TUBE_CORAL)
                .add(Items.DEAD_BRAIN_CORAL_FAN)
                .add(Items.DEAD_BUBBLE_CORAL_FAN)
                .add(Items.DEAD_FIRE_CORAL_FAN)
                .add(Items.DEAD_HORN_CORAL_FAN)
                .add(Items.DEAD_TUBE_CORAL_FAN)
        ;

        tag(ModTags.CORAL_BLOCKS)
                .add(Items.BRAIN_CORAL_BLOCK)
                .add(Items.BUBBLE_CORAL_BLOCK)
                .add(Items.FIRE_CORAL_BLOCK)
                .add(Items.HORN_CORAL_BLOCK)
                .add(Items.TUBE_CORAL_BLOCK)
        ;

        tag(ModTags.DEAD_CORAL_BLOCKS)
                .add(Items.DEAD_BRAIN_CORAL_BLOCK)
                .add(Items.DEAD_BUBBLE_CORAL_BLOCK)
                .add(Items.DEAD_FIRE_CORAL_BLOCK)
                .add(Items.DEAD_HORN_CORAL_BLOCK)
                .add(Items.DEAD_TUBE_CORAL_BLOCK)
        ;

        tag(ModTags.DYED_SHULKER_BOXES)
                .add(Items.WHITE_SHULKER_BOX)
                .add(Items.ORANGE_SHULKER_BOX)
                .add(Items.RED_SHULKER_BOX)
                .add(Items.BLACK_SHULKER_BOX)
                .add(Items.YELLOW_SHULKER_BOX)
                .add(Items.LIME_SHULKER_BOX)
                .add(Items.GREEN_SHULKER_BOX)
                .add(Items.BLUE_SHULKER_BOX)
                .add(Items.CYAN_SHULKER_BOX)
                .add(Items.LIGHT_BLUE_SHULKER_BOX)
                .add(Items.LIGHT_GRAY_SHULKER_BOX)
                .add(Items.GRAY_SHULKER_BOX)
                .add(Items.BROWN_SHULKER_BOX)
                .add(Items.PINK_SHULKER_BOX)
                .add(Items.PURPLE_SHULKER_BOX)
                .add(Items.MAGENTA_SHULKER_BOX)
        ;

        tag(ModTags.INFESTED_STONES)
                .add(Items.INFESTED_COBBLESTONE)
                .add(Items.INFESTED_CHISELED_STONE_BRICKS)
                .add(Items.INFESTED_DEEPSLATE)
                .add(Items.INFESTED_STONE)
                .add(Items.INFESTED_MOSSY_STONE_BRICKS)
                .add(Items.INFESTED_CRACKED_STONE_BRICKS)
                .add(Items.INFESTED_STONE_BRICKS)
        ;

        tag(ModTags.VINYL_DISCS)
                .add(Items.MUSIC_DISC_11)
                .add(Items.MUSIC_DISC_13)
                .add(Items.MUSIC_DISC_BLOCKS)
                .add(Items.MUSIC_DISC_CAT)
                .add(Items.MUSIC_DISC_CHIRP)
                .add(Items.MUSIC_DISC_FAR)
                .add(Items.MUSIC_DISC_MALL)
                .add(Items.MUSIC_DISC_MELLOHI)
                .add(Items.MUSIC_DISC_OTHERSIDE)
                .add(Items.MUSIC_DISC_STAL)
                .add(Items.MUSIC_DISC_STRAD)
                .add(Items.MUSIC_DISC_WAIT)
                .add(Items.MUSIC_DISC_WARD)
                .addTag(ItemTags.CREEPER_DROP_MUSIC_DISCS)
        ;

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
        ;

        tag(ModTags.ANALYZER_UNRECORDABLE)
                .addTag(ModTags.ESSENCES)
                .addTag(ModTags.EQUILIBRIUM)
                .add(Items.ENCHANTED_BOOK)
                .add(ModItems.ELEMENIC_ANALYZER.asItem())
        ;

        tag(ModTags.MEMORIZER_SLOT_PLACEABLE)
                .add(ModItems.ELEMENIC_MEMORIZER.asItem())
                .add(ModItems.FONDANT_CAKE.asItem())
        ;

        tag(ModTags.EGGS_FORGE)
                .add(Items.EGG)
                .add(Items.TURTLE_EGG)
                .add(Items.SNIFFER_EGG)
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
        ;

        tag(ModTags.IGNORED_BY_SCANNING)
                .add(ModItems.ELEMENIC_STORAGE.asItem())
                .add(ModItems.ELEMENIC_ANALYZER.asItem())
                .add(ModItems.SCANNING_STORAGE.asItem())
                .add(ModItems.ELEMENIC_MEMORIZER.asItem())
        ;
    }
}
