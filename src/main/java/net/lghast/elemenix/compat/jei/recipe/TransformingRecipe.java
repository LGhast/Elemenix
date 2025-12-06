package net.lghast.elemenix.compat.jei.recipe;

import net.lghast.elemenix.compat.jei.serializer.TransformingRecipeSerializer;
import net.lghast.elemenix.register.system.ModRecipes;
import net.lghast.elemenix.utils.Elemenix;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class TransformingRecipe implements Recipe<RecipeInput> {
    private final Ingredient instrument; // 新增：仪器输入
    private final Elemenix elemenix1;
    private final Elemenix elemenix2;
    private final int amount;
    private final ItemStack output;

    public TransformingRecipe(Ingredient instrument, Elemenix elemenix1, Elemenix elemenix2, int amount, ItemStack output) {
        this.instrument = instrument;
        this.elemenix1 = elemenix1;
        this.elemenix2 = elemenix2;
        this.amount = amount;
        this.output = output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TransformingRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.TRANSFORMING.get();
    }

    @Override
    public boolean matches(RecipeInput recipeInput, Level level) {
        // 需要根据实际逻辑实现
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput recipeInput, HolderLookup.Provider provider) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return output.copy();
    }

    public Ingredient instrument() {
        return instrument;
    }

    public Elemenix elemenix1() {
        return elemenix1;
    }

    public Elemenix elemenix2() {
        return elemenix2;
    }

    public int amount() {
        return amount;
    }

    public ItemStack output() {
        return output;
    }
}
