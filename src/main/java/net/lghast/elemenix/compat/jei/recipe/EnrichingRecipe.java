package net.lghast.elemenix.compat.jei.recipe;

import net.lghast.elemenix.compat.jei.serializer.EnrichingRecipeSerializer;
import net.lghast.elemenix.register.system.ModRecipes;
import net.lghast.elemenix.utils.elemenix.Elemenix;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public record EnrichingRecipe(Elemenix elemenix, int amount, ItemStack output, boolean portable) implements Recipe<RecipeInput> {

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return EnrichingRecipeSerializer.INSTANCE;
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipes.ENRICHING.get();
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
