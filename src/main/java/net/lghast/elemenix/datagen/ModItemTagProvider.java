package net.lghast.elemenix.datagen;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.register.content.ModBlocks;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.register.system.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
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
        tag(ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "tools/wrench")))
                .add(ModItems.WRENCH.asItem())
                .add(ModItems.ANALYTICAL_WRENCH.asItem())
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
                .add(ModItems.ELEMENIC_CHAOS.asItem())
        ;

        tag(ModTags.MEMORIZER_SLOT_PLACEABLE)
                .add(ModItems.ELEMENIC_MEMORIZER.asItem())
                .add(ModItems.WITCHCRAFT_MEMORIZER.asItem())
                .add(ModItems.FONDANT_CAKE.asItem())
        ;

        tag(ModTags.ELEMENIC_TRANSFORMERS)
                .add(ModBlocks.GEOLOGICAL_SIMULATOR.asItem())
                .add(ModBlocks.METALLURGICAL_ACTIVATOR.asItem())
                .add(ModBlocks.GERMINAL_ACCELERATOR.asItem())
                .add(ModBlocks.TRANSPIRING_INCINERATOR.asItem())
                .add(ModBlocks.OPTICAL_CAPTURER.asItem())
        ;

        tag(ModTags.ELEMENIC_OUTLETS)
                .add(ModBlocks.ELEMENIC_OUTLET.asItem())
                .add(ModBlocks.ELEMENIC_PRESSURIZED_OUTLET.asItem())
        ;

        tag(ModTags.PRINTERS)
                .add(ModBlocks.SUBSTANCE_PRINTER.asItem())
                .add(ModBlocks.WITCHCRAFT_PRINTER.asItem())
        ;

        tag(ModTags.ELEMENIC_RECEPTACLES)
                .add(ModItems.ELEMENIC_STORAGE.asItem())
                .add(ModItems.SCANNING_STORAGE.asItem())
                .add(ModItems.ELEMENIC_ANALYZER.asItem())
                .add(ModBlocks.ELEMENIC_TANK.asItem())
        ;

        tag(ModTags.ELEMENIX_TRANSFERABLE)
                .add(ModItems.ELEMENIC_STORAGE.asItem())
                .add(ModItems.SCANNING_STORAGE.asItem())
                .add(ModBlocks.ELEMENIC_TANK.asItem())
                .add(ModItems.REMOTE_ELEMENIC_STORAGE.asItem())
        ;

        tag(ModTags.IGNORED_BY_DECONSTRUCTOR_INPUT)
                .addTag(ModTags.ELEMENIC_RECEPTACLES)
                .add(ModItems.ELEMENIC_MEMORIZER.asItem())
                .add(ModItems.WITCHCRAFT_MEMORIZER.asItem())
                .add(ModItems.MEMORIZER_BOX.asItem())
                .add(ModItems.ANALYSIS_TERMINAL.asItem())
        ;

        tag(ModTags.IGNORED_BY_SCANNING)
                .addTag(ModTags.ELEMENIC_RECEPTACLES)
                .add(ModItems.ELEMENIC_MEMORIZER.asItem())
                .add(ModItems.WITCHCRAFT_MEMORIZER.asItem())
                .add(ModItems.MEMORIZER_BOX.asItem())
        ;

        tag(ModTags.REGISTRAR_HIDDEN)
                .add(Items.BUDDING_AMETHYST)
                .add(Items.FROGSPAWN)
                .add(Items.BEDROCK)
                .add(Items.REINFORCED_DEEPSLATE)
                .add(ModItems.NULLVOID.asItem())
                .addTag(ItemTags.create(ResourceLocation.fromNamespaceAndPath("elemenix", "infested_stones")))
        ;

        tag(ModTags.ELEMENIX_OUTPUTS)
                .add(ModBlocks.ELEMENIC_DECONSTRUCTOR.get().asItem())
                .add(ModBlocks.ELEMENIC_TANK.get().asItem())
                .add(ModBlocks.ELEMENIC_EJECTOR.get().asItem())
                .add(ModBlocks.ELEMENIC_SHUNT.get().asItem())
                .addTag(ModTags.ELEMENIC_TRANSFORMERS)
        ;

        tag(ModTags.ELEMENIX_INPUTS)
                .add(ModBlocks.ELEMENIC_TANK.get().asItem())
                .add(ModBlocks.ELEMENIC_INFUSER.get().asItem())
                .add(ModBlocks.ELEMENIC_ENRICHER.get().asItem())
                .add(ModBlocks.ELEMENIC_EJECTOR.get().asItem())
                .add(ModBlocks.ELEMENIC_SHUNT.get().asItem())
                .add(ModBlocks.MEMORIZER_REPLICATOR.get().asItem())
                .addTag(ModTags.PRINTERS)
                .addTag(ModTags.ELEMENIC_TRANSFORMERS)
        ;

        tag(ItemTags.TRIM_MATERIALS)
                .add(ModItems.ORGANIX_ESSENCE.asItem())
                .add(ModItems.TERRIX_ESSENCE.asItem())
                .add(ModItems.FLUMIX_ESSENCE.asItem())
                .add(ModItems.METALLIX_ESSENCE.asItem())
                .add(ModItems.ENERGIX_ESSENCE.asItem())
                .add(ModItems.ARCANIX_ESSENCE.asItem())
                .add(ModItems.WITCHCRAFT_TROVE.asItem())
        ;

        tag(ModTags.MEMORIZER_STYLE_MATERIALS)
                .add(Items.COPPER_INGOT)
                .add(Items.WHITE_DYE)
                .add(Items.RED_DYE)
                .add(Items.ORANGE_DYE)
                .add(Items.YELLOW_DYE)
                .add(Items.GREEN_DYE)
                .add(Items.CYAN_DYE)
                .add(Items.BLUE_DYE)
                .add(Items.PURPLE_DYE)
                .add(Items.BLACK_DYE)
                .add(Items.GRAY_DYE)
                .add(Items.LIME_DYE)
                .add(Items.PINK_DYE)
                .add(Items.MAGENTA_DYE)
                .add(Items.LIGHT_GRAY_DYE)
                .add(Items.LIGHT_BLUE_DYE)
                .add(Items.BROWN_DYE)
                .add(Items.PRISMARINE_SHARD)
                .add(Items.BREEZE_ROD)
                .add(Items.POPPED_CHORUS_FRUIT)
                .add(Items.GOLD_INGOT)
                .add(Items.DIAMOND)
        ;

        tag(ModTags.ELEMENIC_INSTRUMENTS_POTABLE)
                .add(ModItems.ELEMENIC_ANALYZER.asItem())
                .add(ModItems.MEMORIZER_BOX.asItem())
                .add(ModItems.MEMORY_BURNER.asItem())
                .add(ModItems.ANALYSIS_TERMINAL.asItem())
        ;

        tag(ModTags.ELEMENIC_INSTRUMENTS)
                .addTag(ModTags.ELEMENIC_INSTRUMENTS_POTABLE)
                .add(ModItems.ELEMENIC_SCANNER.asItem())
                .add(ModItems.MEMORY_REGISTRAR.asItem())
                .add(ModItems.ELEMENIC_STORAGE.asItem())
                .add(ModItems.SCANNING_STORAGE.asItem())
                .add(ModItems.REMOTE_ELEMENIC_STORAGE.asItem())
                .add(ModItems.ANALYTICAL_WRENCH.asItem())
        ;
    }
}
