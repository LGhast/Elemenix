package net.lghast.elemenix.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.lghast.elemenix.common.system.recipe.GerminatingRecipe;
import net.lghast.elemenix.common.system.recipe.MineralizingRecipe;
import net.lghast.elemenix.compat.jei.category.*;
import net.lghast.elemenix.compat.jei.recipe.*;
import net.lghast.elemenix.register.content.ModBlocks;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.register.system.ModRecipes;
import net.lghast.elemenix.utils.elemenix.Elemenix;
import net.lghast.elemenix.utils.elemenix.ElemenixInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
@SuppressWarnings("unused")
public class JEIPlugin implements IModPlugin {

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath("elemenix", "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new EnrichingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new TransformingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new MineralizingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new GerminatingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(EnrichingRecipeCategory.TYPE, getEnrichingRecipes());
        registration.addRecipes(TransformingRecipeCategory.TYPE, getTransformingRecipes());
        registration.addRecipes(MineralizingRecipeCategory.TYPE, getMineralizingRecipes());
        registration.addRecipes(GerminatingRecipeCategory.TYPE, getGerminatingRecipes());

        registration.addItemStackInfo(new ItemStack(ModBlocks.ELEMENIC_INFUSER.get()),
                Component.translatable("jei.info.elemenic_infuser"));
        registration.addItemStackInfo(new ItemStack(ModBlocks.ELEMENIC_ENRICHER.get()),
                Component.translatable("jei.info.elemenic_enricher"));
        registration.addItemStackInfo(new ItemStack(ModBlocks.ELEMENIC_EJECTOR.get()),
                Component.translatable("jei.info.elemenic_ejector"));

        registration.addItemStackInfo(new ItemStack(ModBlocks.GEOLOGICAL_SIMULATOR.get()),
                Component.translatable("jei.info.geological_simulator"));
        registration.addItemStackInfo(new ItemStack(ModBlocks.METALLURGICAL_ACTIVATOR.get()),
                Component.translatable("jei.info.metallurgical_activator"));
        registration.addItemStackInfo(new ItemStack(ModBlocks.GERMINAL_ACCELERATOR.get()),
                Component.translatable("jei.info.germinal_accelerator"));
        registration.addItemStackInfo(new ItemStack(ModBlocks.TRANSPIRING_INCINERATOR.get()),
                Component.translatable("jei.info.transpiring_incinerator"));
        registration.addItemStackInfo(new ItemStack(ModBlocks.OPTICAL_CAPTURER.get()),
                Component.translatable("jei.info.optical_capturer"));

        registration.addItemStackInfo(new ItemStack(ModItems.THROTTLE_VALVE.get()),
                Component.translatable("jei.info.throttle_valve"));
        registration.addItemStackInfo(new ItemStack(ModItems.FLOW_STRAIGHTENER.get()),
                Component.translatable("jei.info.flow_straightener"));
        registration.addItemStackInfo(new ItemStack(ModItems.MEMORY_BURNER.get()),
                Component.translatable("jei.info.memory_burner"));
        registration.addItemStackInfo(new ItemStack(ModItems.ELEMENIC_STORAGE.get()),
                Component.translatable("jei.info.elemenic_storage"));
        registration.addItemStackInfo(new ItemStack(ModItems.REMOTE_ELEMENIC_STORAGE.get()),
                Component.translatable("jei.info.remote_elemenic_storage"));
        registration.addItemStackInfo(new ItemStack(ModItems.ELEMENIC_SCANNER.get()),
                Component.translatable("jei.info.elemenic_scanner"));
        registration.addItemStackInfo(new ItemStack(ModItems.SCANNING_STORAGE.get()),
                Component.translatable("jei.info.scanning_storage"));
    }

    private List<MineralizingRecipe> getMineralizingRecipes() {
        List<MineralizingRecipe> recipes = new ArrayList<>();
        Level level = Minecraft.getInstance().level;
        if (level != null) {
            for (RecipeHolder<MineralizingRecipe> holder :
                    level.getRecipeManager().getAllRecipesFor(ModRecipes.MINERALIZING.get())) {
                recipes.add(holder.value());
            }
        }
        return recipes;
    }

    private List<GerminatingRecipe> getGerminatingRecipes() {
        List<GerminatingRecipe> recipes = new ArrayList<>();
        Level level = Minecraft.getInstance().level;
        if (level != null) {
            for (RecipeHolder<GerminatingRecipe> holder :
                    level.getRecipeManager().getAllRecipesFor(ModRecipes.GERMINATING.get())) {
                recipes.add(holder.value());
            }
        }
        return recipes;
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
