package net.lghast.elemenix.common.system.recipe;

import net.lghast.elemenix.common.content.item.MemorizerItem;
import net.lghast.elemenix.common.system.datacomponent.Style;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.register.system.ModDataComponents;
import net.lghast.elemenix.register.system.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class MemorizerStylingRecipe extends CustomRecipe {
    public MemorizerStylingRecipe(CraftingBookCategory category) {
        super(category);
    }
    @Override
    public boolean matches(CraftingInput input, Level level) {
        ItemStack memorizer = ItemStack.EMPTY;
        ItemStack styleItem = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(ModItems.ELEMENIC_MEMORIZER)) {
                if (!memorizer.isEmpty()) {
                    return false;
                }
                memorizer = stack;
            } else if (MemorizerItem.getStyleIndexFor(stack.getItem()) != null) {
                if (!styleItem.isEmpty()) {
                    return false;
                }
                styleItem = stack;
            } else {
                return false;
            }
        }
        if (memorizer.isEmpty() || styleItem.isEmpty()) {
            return false;
        }
        Integer targetStyle = MemorizerItem.getStyleIndexFor(styleItem.getItem());
        return targetStyle != null && targetStyle != MemorizerItem.getOrCreateStyle(memorizer).style();
    }
    @Override
    public @NotNull ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack memorizer = ItemStack.EMPTY;
        ItemStack styleItem = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(ModItems.ELEMENIC_MEMORIZER) && memorizer.isEmpty()) {
                memorizer = stack;
            } else if (MemorizerItem.getStyleIndexFor(stack.getItem()) != null && styleItem.isEmpty()) {
                styleItem = stack;
            }
        }
        if (memorizer.isEmpty() || styleItem.isEmpty()) {
            return ItemStack.EMPTY;
        }
        Integer targetStyle = MemorizerItem.getStyleIndexFor(styleItem.getItem());
        if (targetStyle == null) {
            return ItemStack.EMPTY;
        }

        ItemStack result = memorizer.copy();
        result.set(ModDataComponents.STYLE, new Style(targetStyle));
        return result;
    }
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }
    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipes.MEMORIZER_STYLING.get();
    }
}
