package net.lghast.elemenix.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.lghast.elemenix.compat.jei.category.EnrichingRecipeCategory;
import net.lghast.elemenix.compat.jei.category.TransformingRecipeCategory;
import net.lghast.elemenix.compat.jei.recipe.EnrichingRecipe;
import net.lghast.elemenix.compat.jei.recipe.TransformingRecipe;
import net.lghast.elemenix.register.content.ModBlocks;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.utils.Elemenix;
import net.lghast.elemenix.utils.ElemenixInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath("elemenix", "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new EnrichingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new TransformingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(EnrichingRecipeCategory.TYPE, getEnrichingRecipes());
        registration.addRecipes(TransformingRecipeCategory.TYPE, getTransformingRecipes());
    }

    private List<TransformingRecipe> getTransformingRecipes() {
        List<TransformingRecipe> recipes = new ArrayList<>();
        int essenceValue = ElemenixInfo.ESSENCE_VALUE;

        recipes.add(new TransformingRecipe(
                Ingredient.of(ModBlocks.GEOLOGICAL_SIMULATOR.asItem()),
                Elemenix.METALLIX,
                Elemenix.FLUMIX,
                essenceValue,
                new ItemStack(ModItems.TERRIX_ESSENCE.asItem())
        ));

        recipes.add(new TransformingRecipe(
                Ingredient.of(ModBlocks.METALLURGICAL_ACTIVATOR.asItem()),
                Elemenix.TERRIX,
                Elemenix.ENERGIX,
                essenceValue,
                new ItemStack(ModItems.METALLIX_ESSENCE.asItem())
        ));

        recipes.add(new TransformingRecipe(
                Ingredient.of(ModBlocks.GERMINAL_ACCELERATOR.asItem()),
                Elemenix.FLUMIX,
                Elemenix.TERRIX,
                essenceValue,
                new ItemStack(ModItems.ORGANIX_ESSENCE.asItem())
        ));

        recipes.add(new TransformingRecipe(
                Ingredient.of(ModBlocks.TRANSPIRING_INCINERATOR.asItem()),
                Elemenix.ORGANIX,
                Elemenix.ENERGIX,
                essenceValue,
                new ItemStack(ModItems.FLUMIX_ESSENCE.asItem())
        ));

        recipes.add(new TransformingRecipe(
                Ingredient.of(ModBlocks.OPTICAL_CAPTURER.asItem()),
                Elemenix.FLUMIX,
                Elemenix.ORGANIX,
                essenceValue,
                new ItemStack(ModItems.ENERGIX_ESSENCE.asItem())
        ));

        return recipes;
    }

    private List<EnrichingRecipe> getEnrichingRecipes() {
        List<EnrichingRecipe> recipes = new ArrayList<>();
        int essenceValue = ElemenixInfo.ESSENCE_VALUE;
        int essenplexValue = essenceValue * 9;

        recipes.add(new EnrichingRecipe(
                Elemenix.ORGANIX,
                essenceValue,
                new ItemStack(ModItems.ORGANIX_ESSENCE.get()),
                false
        ));
        recipes.add(new EnrichingRecipe(
                Elemenix.TERRIX,
                essenceValue,
                new ItemStack(ModItems.TERRIX_ESSENCE.get()),
                false
        ));

        recipes.add(new EnrichingRecipe(
                Elemenix.FLUMIX,
                essenceValue,
                new ItemStack(ModItems.FLUMIX_ESSENCE.get()),
                false
        ));

        recipes.add(new EnrichingRecipe(
                Elemenix.METALLIX,
                essenceValue,
                new ItemStack(ModItems.METALLIX_ESSENCE.get()),
                false
        ));

        recipes.add(new EnrichingRecipe(
                Elemenix.ENERGIX,
                essenceValue,
                new ItemStack(ModItems.ENERGIX_ESSENCE.get()),
                false
        ));

        recipes.add(new EnrichingRecipe(
                Elemenix.ARCANIX,
                essenceValue,
                new ItemStack(ModItems.ARCANIX_ESSENCE.get()),
                false
        ));

        recipes.add(new EnrichingRecipe(
                Elemenix.ORGANIX,
                essenplexValue,
                new ItemStack(ModItems.ORGANIX_ESSENPLEX.get()),
                true
        ));

        recipes.add(new EnrichingRecipe(
                Elemenix.TERRIX,
                essenplexValue,
                new ItemStack(ModItems.TERRIX_ESSENPLEX.get()),
                true
        ));

        recipes.add(new EnrichingRecipe(
                Elemenix.FLUMIX,
                essenplexValue,
                new ItemStack(ModItems.FLUMIX_ESSENPLEX.get()),
                true
        ));

        recipes.add(new EnrichingRecipe(
                Elemenix.METALLIX,
                essenplexValue,
                new ItemStack(ModItems.METALLIX_ESSENPLEX.get()),
                true
        ));

        recipes.add(new EnrichingRecipe(
                Elemenix.ENERGIX,
                essenplexValue,
                new ItemStack(ModItems.ENERGIX_ESSENPLEX.get()),
                true
        ));

        recipes.add(new EnrichingRecipe(
                Elemenix.ARCANIX,
                essenplexValue,
                new ItemStack(ModItems.ARCANIX_ESSENPLEX.get()),
                true
        ));

        return recipes;
    }
}
