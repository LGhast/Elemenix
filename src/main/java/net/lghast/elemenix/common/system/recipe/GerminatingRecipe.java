package net.lghast.elemenix.common.system.recipe;

import net.lghast.elemenix.register.system.ModRecipes;
import net.lghast.elemenix.utils.Constituents;
import net.lghast.elemenix.utils.elemenix.ElemenixInfo;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public record GerminatingRecipe(Ingredient template, List<ItemStack> products) implements Recipe<RecipeInput> {
    public static final int MAX_PRODUCTS = 6;

    @Nullable
    public static GerminatingRecipe findRecipe(Level level, ItemStack templateStack) {
        if (templateStack.isEmpty()) return null;
        for (RecipeHolder<GerminatingRecipe> holder :
                level.getRecipeManager().getAllRecipesFor(ModRecipes.GERMINATING.get())) {
            GerminatingRecipe recipe = holder.value();
            if (recipe.template().test(templateStack)) {
                return recipe;
            }
        }
        return null;
    }

    public int[] getCost() {
        long sum = 0;
        for (ItemStack product : products) {
            Constituents constituents = ElemenixInfo.getConstituents(product);
            if (constituents != null && !constituents.isUnanalysable()) {
                sum += constituents.getSum() * Math.max(1, product.getCount());
            }
        }
        int cost = (int) sum;
        return new int[]{cost, cost};
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipes.GERMINATING_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipes.GERMINATING.get();
    }

    @Override
    public boolean matches(RecipeInput input, Level level) {
        if (input.isEmpty()) return false;
        return template.test(input.getItem(0));
    }

    @Override
    public @NotNull ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
        return products.isEmpty() ? ItemStack.EMPTY : products.getFirst().copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.Provider registries) {
        return assemble(null, registries);
    }
}
