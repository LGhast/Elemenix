package net.lghast.elemenix.common.system.recipe;

import net.lghast.elemenix.register.system.ModRecipes;
import net.lghast.elemenix.utils.Constituents;
import net.lghast.elemenix.utils.elemenix.ElemenixInfo;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public record MineralizingRecipe(Ingredient mineral, Ingredient medium, ItemStack result, double flumixCost)
        implements Recipe<RecipeInput> {
    public static final double DEFAULT_FLUMIX_COST = 0.1;

    @Nullable
    public static MineralizingRecipe findRecipe(Level level, ItemStack mineralStack, ItemStack mediumStack) {
        if (mineralStack.isEmpty() || mediumStack.isEmpty()) return null;
        for (RecipeHolder<MineralizingRecipe> holder : level.getRecipeManager().getAllRecipesFor(ModRecipes.MINERALIZING.get())) {
            MineralizingRecipe recipe = holder.value();
            if (recipe.mineral().test(mineralStack) && recipe.medium().test(mediumStack)) {
                return recipe;
            }
        }
        return null;
    }

    public int getFlumixCost(ItemStack mineralStack) {
        Constituents constituents = ElemenixInfo.getConstituents(mineralStack);
        if (constituents == null || constituents.isUnanalysable()) return 0;
        long sum = constituents.getSum();
        if (sum <= 0) return 0;
        double ratio = flumixCost > 0 ? flumixCost : DEFAULT_FLUMIX_COST;
        return (int) Math.max(1, Math.ceil(sum * ratio));
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipes.MINERALIZING_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipes.MINERALIZING.get();
    }

    @Override
    public boolean matches(RecipeInput input, Level level) {
        if (input.size() < 2) return false;
        return mineral.test(input.getItem(0)) && medium.test(input.getItem(1));
    }

    @Override
    public @NotNull ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, mineral, medium);
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.Provider registries) {
        return result.copy();
    }
}
