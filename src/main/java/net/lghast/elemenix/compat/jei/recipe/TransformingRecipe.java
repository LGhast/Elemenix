package net.lghast.elemenix.compat.jei.recipe;

import net.lghast.elemenix.compat.jei.serializer.TransformingRecipeSerializer;
import net.lghast.elemenix.register.system.ModRecipes;
import net.lghast.elemenix.utils.elemenix.Elemenix;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public record TransformingRecipe(Ingredient instrument, Elemenix elemenix1, Elemenix elemenix2,
                                 int amount, ItemStack output) implements Recipe<RecipeInput> {

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return TransformingRecipeSerializer.INSTANCE;
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipes.TRANSFORMING.get();
    }

    @Override
    public boolean matches(RecipeInput recipeInput, Level level) {
        return false;
    }

    @Override
    public @NotNull ItemStack assemble(RecipeInput recipeInput, HolderLookup.Provider provider) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.Provider registries) {
        return output.copy();
    }
}
