package net.lghast.elemenix.compat.jei.recipe;

import net.lghast.elemenix.compat.jei.serializer.EnrichingRecipeSerializer;
import net.lghast.elemenix.register.system.ModRecipes;
import net.lghast.elemenix.utils.Elemenix;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class EnrichingRecipe implements Recipe<RecipeInput> {
    private final Elemenix elemenix;
    private final int amount;
    private final ItemStack output;
    private final boolean portable;

    public EnrichingRecipe(Elemenix elemenix, int amount, ItemStack output, boolean portable) {
        this.elemenix = elemenix;
        this.amount = amount;
        this.output = output;
        this.portable = portable;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return EnrichingRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.ENRICHING.get();
    }

    @Override
    public boolean matches(RecipeInput recipeInput, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput recipeInput, HolderLookup.Provider provider) {
        return null;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return output.copy();
    }

    public boolean portable() {
        return portable;
    }

    public Elemenix elemenix() {
        return elemenix;
    }

    public int amount() {
        return amount;
    }

    public ItemStack output() {
        return output;
    }
}
