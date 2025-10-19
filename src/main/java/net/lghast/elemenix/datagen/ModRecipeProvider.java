package net.lghast.elemenix.datagen;

import net.lghast.elemenix.register.content.ModBlocks;
import net.lghast.elemenix.register.content.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pRegistries) {
        super(pOutput, pRegistries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ELEMENIC_ANALYZER.get(), 1)
                .pattern("ABC")
                .pattern("NDN")
                .pattern("EFG")
                .define('D', Items.DIAMOND)
                .define('N', Items.QUARTZ)
                .define('A', Items.FLINT)
                .define('B', Items.WHEAT)
                .define('C', Items.BLAZE_ROD)
                .define('E', Items.GOLD_NUGGET)
                .define('F', Items.ENDER_PEARL)
                .define('G', Items.SNOWBALL)
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ELEMENIC_ANALYZER.get(), 1)
                .pattern("ABC")
                .pattern("NDN")
                .pattern("EFG")
                .define('D', Items.DIAMOND)
                .define('N', Items.QUARTZ)
                .define('A', ModItems.TERRIX_ESSENCE)
                .define('B', ModItems.ORGANIX_ESSENCE)
                .define('C', ModItems.ENERGIX_ESSENCE)
                .define('E', ModItems.METALLIX_ESSENCE)
                .define('F', ModItems.ARCANIX_ESSENCE)
                .define('G', ModItems.FLUMIX_ESSENCE)
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(recipeOutput, "organix_essence_from_essences");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ELEMENIC_MEMORIZER.get(), 1)
                .pattern("A A")
                .pattern("CRC")
                .pattern("CCC")
                .define('A', Items.AMETHYST_SHARD)
                .define('R', Items.COMPARATOR)
                .define('C', Items.COPPER_INGOT)
                .unlockedBy("has_redstone_comparator", has(Items.COMPARATOR))
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

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.ELEMENIC_EQUILIBRIUM.get(), 1)
                .requires(ModItems.ORGANIX_ESSENCE)
                .requires(ModItems.TERRIX_ESSENCE)
                .requires(ModItems.FLUMIX_ESSENCE)
                .requires(ModItems.METALLIX_ESSENCE)
                .requires(ModItems.ENERGIX_ESSENCE)
                .requires(ModItems.ARCANIX_ESSENCE)
                .unlockedBy("has_elemenic_analyzer", has(ModItems.ELEMENIC_ANALYZER))
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.ELEMENIC_EQUILIBRIUM.get(), 9)
                .requires(ModItems.ORGANIX_ESSENPLEX)
                .requires(ModItems.TERRIX_ESSENPLEX)
                .requires(ModItems.FLUMIX_ESSENPLEX)
                .requires(ModItems.METALLIX_ESSENPLEX)
                .requires(ModItems.ENERGIX_ESSENPLEX)
                .requires(ModItems.ARCANIX_ESSENPLEX)
                .unlockedBy("has_elemenic_analyzer", has(ModItems.ELEMENIC_ANALYZER))
                .save(recipeOutput, "elemenic_equilibrium_from_essenplexes");

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.ELEMENIC_ANALYZER.get(), 1)
                .requires(Items.DIAMOND, 1)
                .requires(Items.QUARTZ, 2)
                .requires(ModItems.ELEMENIC_EQUILIBRIUM)
                .unlockedBy("has_elemenic_equilibrium", has(ModItems.ELEMENIC_EQUILIBRIUM))
                .save(recipeOutput, "organix_essence_from_equilibrium");

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
    }
}
