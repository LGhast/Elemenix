package net.lghast.elemenix.datagen;

import net.lghast.elemenix.register.content.ModBlocks;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.register.system.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.CompletableFuture;

@ParametersAreNonnullByDefault
public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pRegistries) {
        super(pOutput, pRegistries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ANALYZING_CHIP.get(), 1)
                .pattern(" R ")
                .pattern("IDI")
                .pattern(" R ")
                .define('D', Items.DIAMOND)
                .define('I', Items.IRON_INGOT)
                .define('R', Items.REDSTONE)
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ELEMENIC_ANALYZER.get(), 1)
                .pattern("ABC")
                .pattern(" D ")
                .pattern("EFG")
                .define('D', ModItems.ANALYZING_CHIP)
                .define('A', ModTags.TERRIX_MATERIALS)
                .define('B', ModTags.ORGANIX_MATERIALS)
                .define('C', ModTags.ENERGIX_MATERIALS)
                .define('E', ModTags.METALLIX_MATERIALS)
                .define('F', ModTags.ARCANIX_MATERIALS)
                .define('G', ModTags.FLUMIX_MATERIALS)
                .unlockedBy("has_analyzing_chip", has(ModItems.ANALYZING_CHIP))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ELEMENIC_STORAGE.get(), 1)
                .pattern(" B ")
                .pattern("NDN")
                .pattern(" N ")
                .define('D', ModItems.ANALYZING_CHIP)
                .define('N', Items.QUARTZ)
                .define('B', Items.BLAZE_POWDER)
                .unlockedBy("has_analyzing_chip", has(ModItems.ANALYZING_CHIP))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.REMOTE_ELEMENIC_STORAGE.get(), 1)
                .pattern(" B ")
                .pattern("NDN")
                .pattern("ENE")
                .define('D', ModItems.ANALYZING_CHIP)
                .define('N', Items.QUARTZ)
                .define('B', Items.BLAZE_POWDER)
                .define('E', Items.ENDER_EYE)
                .unlockedBy("has_storage", has(ModItems.ELEMENIC_STORAGE))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ELEMENIC_ANALYZER.get(), 1)
                .pattern("ABC")
                .pattern(" D ")
                .pattern("EFG")
                .define('D', ModItems.ANALYZING_CHIP)
                .define('A', ModItems.TERRIX_ESSENCE)
                .define('B', ModItems.ORGANIX_ESSENCE)
                .define('C', ModItems.ENERGIX_ESSENCE)
                .define('E', ModItems.METALLIX_ESSENCE)
                .define('F', ModItems.ARCANIX_ESSENCE)
                .define('G', ModItems.FLUMIX_ESSENCE)
                .unlockedBy("has_analyzing_chip", has(ModItems.ANALYZING_CHIP))
                .save(recipeOutput, "elemenic_analyzer_from_essences");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ELEMENIC_MEMORIZER.get(), 1)
                .pattern("A A")
                .pattern("CRC")
                .pattern("CCC")
                .define('A', Items.AMETHYST_SHARD)
                .define('R', Items.COMPARATOR)
                .define('C', Items.COPPER_INGOT)
                .unlockedBy("has_redstone_comparator", has(Items.COMPARATOR))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MEMORIZER_BOX.get(), 1)
                .pattern("ISI")
                .pattern("CIC")
                .pattern("   ")
                .define('I', Items.IRON_INGOT)
                .define('S', ModTags.C_SLIMEBALLS)
                .define('C', Items.COPPER_INGOT)
                .unlockedBy("has_elemenic_memorizer", has(ModItems.ELEMENIC_MEMORIZER))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MEMORIZER_BOX.get(), 1)
                .pattern("ISI")
                .pattern("CIC")
                .pattern("   ")
                .define('I', Items.IRON_INGOT)
                .define('S', ModTags.C_SLIME_BALLS)
                .define('C', Items.COPPER_INGOT)
                .unlockedBy("has_elemenic_memorizer", has(ModItems.ELEMENIC_MEMORIZER))
                .save(recipeOutput, "memorizer_box_from_slime_balls");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MEMORY_BURNER.get(), 1)
                .pattern("A A")
                .pattern("RDR")
                .pattern(" I ")
                .define('A', Items.AMETHYST_SHARD)
                .define('R', Items.REDSTONE)
                .define('D', Items.DIAMOND)
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_elemenic_memorizer", has(ModItems.ELEMENIC_MEMORIZER))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ORGANIX_ESSENPLEX.get(), 1)
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.ORGANIX_ESSENCE)
                .unlockedBy("has_organix_essence", has(ModItems.ORGANIX_ESSENPLEX))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.TERRIX_ESSENPLEX.get(), 1)
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.TERRIX_ESSENCE)
                .unlockedBy("has_terrix_essence", has(ModItems.TERRIX_ESSENPLEX))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FLUMIX_ESSENPLEX.get(), 1)
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.FLUMIX_ESSENCE)
                .unlockedBy("has_flumix_essence", has(ModItems.FLUMIX_ESSENPLEX))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.METALLIX_ESSENPLEX.get(), 1)
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.METALLIX_ESSENCE)
                .unlockedBy("has_metallix_essence", has(ModItems.METALLIX_ESSENPLEX))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ENERGIX_ESSENPLEX.get(), 1)
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.ENERGIX_ESSENCE)
                .unlockedBy("has_energix_essence", has(ModItems.ENERGIX_ESSENPLEX))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ARCANIX_ESSENPLEX.get(), 1)
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.ARCANIX_ESSENCE)
                .unlockedBy("has_arcanix_essence", has(ModItems.ARCANIX_ESSENPLEX))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ELEMENIC_EQUILIPLEX.get(), 1)
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.ELEMENIC_EQUILIBRIUM)
                .unlockedBy("has_elemenic_equilibrium", has(ModItems.ELEMENIC_EQUILIBRIUM))
                .save(recipeOutput, "elemenic_equiliplex_from_equilibrium");

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.ELEMENIC_EQUILIBRIUM.get(), 1)
                .requires(ModItems.ORGANIX_ESSENCE)
                .requires(ModItems.TERRIX_ESSENCE)
                .requires(ModItems.FLUMIX_ESSENCE)
                .requires(ModItems.METALLIX_ESSENCE)
                .requires(ModItems.ENERGIX_ESSENCE)
                .requires(ModItems.ARCANIX_ESSENCE)
                .unlockedBy("has_elemenic_analyzer", has(ModItems.ELEMENIC_ANALYZER))
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.ELEMENIC_EQUILIPLEX.get(), 1)
                .requires(ModItems.ORGANIX_ESSENPLEX)
                .requires(ModItems.TERRIX_ESSENPLEX)
                .requires(ModItems.FLUMIX_ESSENPLEX)
                .requires(ModItems.METALLIX_ESSENPLEX)
                .requires(ModItems.ENERGIX_ESSENPLEX)
                .requires(ModItems.ARCANIX_ESSENPLEX)
                .unlockedBy("has_elemenic_analyzer", has(ModItems.ELEMENIC_ANALYZER))
                .save(recipeOutput, "elemenic_equiliplex");

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.ELEMENIC_ANALYZER.get(), 1)
                .requires(ModItems.ANALYZING_CHIP, 1)
                .requires(ModItems.ELEMENIC_EQUILIBRIUM)
                .unlockedBy("has_elemenic_equilibrium", has(ModItems.ELEMENIC_EQUILIBRIUM))
                .save(recipeOutput, "elemenic_analyzer_from_equilibrium");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.GEOLOGICAL_SIMULATOR.get(), 1)
                .pattern("IRI")
                .pattern("ABC")
                .pattern("INI")
                .define('I', Items.IRON_INGOT)
                .define('R', Items.REDSTONE)
                .define('N', Items.NETHERITE_SCRAP)
                .define('A', ModItems.METALLIX_ESSENCE)
                .define('B', ModItems.TERRIX_ESSENCE)
                .define('C', ModItems.FLUMIX_ESSENCE)
                .unlockedBy("has_terrix_essence", has(ModItems.TERRIX_ESSENCE))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.METALLURGICAL_ACTIVATOR.get(), 1)
                .pattern("IRI")
                .pattern("ABC")
                .pattern("INI")
                .define('I', Items.IRON_INGOT)
                .define('R', Items.REDSTONE)
                .define('N', Items.NETHERITE_SCRAP)
                .define('A', ModItems.TERRIX_ESSENCE)
                .define('B', ModItems.METALLIX_ESSENCE)
                .define('C', ModItems.ENERGIX_ESSENCE)
                .unlockedBy("has_energix_essence", has(ModItems.ENERGIX_ESSENCE))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.GERMINAL_ACCELERATOR.get(), 1)
                .pattern("IRI")
                .pattern("ABC")
                .pattern("INI")
                .define('I', Items.IRON_INGOT)
                .define('R', Items.REDSTONE)
                .define('N', Items.NETHERITE_SCRAP)
                .define('A', ModItems.TERRIX_ESSENCE)
                .define('B', ModItems.ORGANIX_ESSENCE)
                .define('C', ModItems.FLUMIX_ESSENCE)
                .unlockedBy("has_organix_essence", has(ModItems.ORGANIX_ESSENCE))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.TRANSPIRING_INCINERATOR.get(), 1)
                .pattern("IRI")
                .pattern("ABC")
                .pattern("INI")
                .define('I', Items.IRON_INGOT)
                .define('R', Items.REDSTONE)
                .define('N', Items.NETHERITE_SCRAP)
                .define('A', ModItems.ORGANIX_ESSENCE)
                .define('B', ModItems.FLUMIX_ESSENCE)
                .define('C', ModItems.ENERGIX_ESSENCE)
                .unlockedBy("has_flumix_essence", has(ModItems.FLUMIX_ESSENCE))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.OPTICAL_CAPTURER.get(), 1)
                .pattern("IRI")
                .pattern("ABC")
                .pattern("INI")
                .define('I', Items.IRON_INGOT)
                .define('R', Items.DAYLIGHT_DETECTOR)
                .define('N', Items.NETHERITE_SCRAP)
                .define('A', ModItems.ORGANIX_ESSENCE)
                .define('B', ModItems.ENERGIX_ESSENCE)
                .define('C', ModItems.FLUMIX_ESSENCE)
                .unlockedBy("has_energix_essence", has(ModItems.ENERGIX_ESSENCE))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ELEMENIC_INFUSER.get(), 1)
                .pattern("IHI")
                .pattern("RCR")
                .pattern("INI")
                .define('I', Items.IRON_INGOT)
                .define('R', Items.REDSTONE_LAMP)
                .define('N', Items.NETHERITE_SCRAP)
                .define('H', Items.HOPPER)
                .define('C', ModItems.ANALYZING_CHIP)
                .unlockedBy("has_analyzing_chip", has(ModItems.ANALYZING_CHIP))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ELEMENIC_ENRICHER.get(), 1)
                .pattern(" E ")
                .pattern(" I ")
                .pattern(" C ")
                .define('I', ModBlocks.ELEMENIC_INFUSER)
                .define('E', ModItems.ELEMENIC_EQUILIBRIUM)
                .define('C', Items.CHEST)
                .unlockedBy("has_elemenic_infuser", has(ModBlocks.ELEMENIC_INFUSER))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ELEMENIC_EJECTOR.get(), 1)
                .pattern(" C ")
                .pattern("RER")
                .pattern(" D ")
                .define('E', ModBlocks.ELEMENIC_ENRICHER)
                .define('C', Items.COMPARATOR)
                .define('R', Items.REDSTONE)
                .define('D', Items.DISPENSER)
                .unlockedBy("has_elemenic_enricher", has(ModBlocks.ELEMENIC_ENRICHER))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, ModItems.FONDANT_CAKE.get(), 1)
                .pattern("SPS")
                .pattern("EOE")
                .pattern("WBW")
                .define('S', Items.SUGAR)
                .define('P', Items.PURPLE_DYE)
                .define('E', Items.EGG)
                .define('O', Items.ORANGE_DYE)
                .define('W', Items.WHEAT)
                .define('B', Items.SWEET_BERRIES)
                .unlockedBy("has_energix_essence", has(ModItems.ENERGIX_ESSENCE))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FLOW_STRAIGHTENER.get(), 1)
                .pattern("   ")
                .pattern("IBI")
                .pattern("   ")
                .define('I', Items.IRON_INGOT)
                .define('B', Items.IRON_BARS)
                .unlockedBy("has_iron_bars", has(Items.IRON_BARS))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.THROTTLE_VALVE.get(), 1)
                .pattern(" I ")
                .pattern("IRI")
                .pattern(" B ")
                .define('I', Items.IRON_INGOT)
                .define('B', Items.BUCKET)
                .define('R', Items.REDSTONE)
                .unlockedBy("has_bucket", has(Items.BUCKET))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ELEMENIC_SCANNER.get(), 1)
                .pattern(" G ")
                .pattern("GCG")
                .pattern(" G ")
                .define('G', Items.GOLD_NUGGET)
                .define('C', ModItems.ANALYZING_CHIP)
                .unlockedBy("has_analyzing_chip", has(ModItems.ANALYZING_CHIP))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SCANNING_STORAGE.get(), 1)
                .pattern(" B ")
                .pattern("GSG")
                .pattern(" G ")
                .define('G', Items.GOLD_INGOT)
                .define('B', Items.BLAZE_POWDER)
                .define('S', ModItems.ELEMENIC_SCANNER)
                .unlockedBy("has_scanning_storage", has(ModItems.ELEMENIC_SCANNER))
                .save(recipeOutput);
    }
}
