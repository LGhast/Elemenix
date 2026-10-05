package net.lghast.elemenix.compat.jei.category;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.lghast.elemenix.common.system.recipe.MineralizingRecipe;
import net.lghast.elemenix.register.content.ModBlocks;
import net.lghast.elemenix.utils.ModUtils;
import net.lghast.elemenix.utils.elemenix.Elemenix;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class MineralizingRecipeCategory implements IRecipeCategory<MineralizingRecipe> {
    public static final RecipeType<MineralizingRecipe> TYPE =
            RecipeType.create("elemenix", "mineralizing", MineralizingRecipe.class);

    private static final ResourceLocation BACKGROUND_LOCATION =
            ResourceLocation.fromNamespaceAndPath("elemenix", "textures/gui/mineralizing_jei.png");

    private static final int WIDTH = 176;
    private static final int HEIGHT = 74;

    private static final int MINERAL_X = 26;
    private static final int MINERAL_Y = 37;
    private static final int OUTPUT_X = 81;
    private static final int OUTPUT_Y = 37;
    private static final int MEDIUM_X = 134;
    private static final int MEDIUM_Y = 37;

    private static final int FLUMIX_TEXT_X = 66;
    private static final int FLUMIX_TEXT_Y = 14;

    private final IDrawable background;
    private final IDrawable icon;
    private final Component localizedName;

    public MineralizingRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(BACKGROUND_LOCATION, 0, 0, WIDTH, HEIGHT)
                .setTextureSize(WIDTH, HEIGHT)
                .build();
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.GEOLOGICAL_SIMULATOR.get()));
        this.localizedName = Component.translatable("jei.category.elemenix.mineralizing");
    }

    @Override
    public @NotNull RecipeType<MineralizingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return localizedName;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    @Nullable
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MineralizingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, MINERAL_X, MINERAL_Y)
                .addIngredients(recipe.mineral())
                .setSlotName("mineral");

        builder.addSlot(RecipeIngredientRole.INPUT, MEDIUM_X, MEDIUM_Y)
                .addIngredients(recipe.medium())
                .setSlotName("medium");

        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X, OUTPUT_Y)
                .addItemStack(recipe.result())
                .setSlotName("output");
    }

    @Override
    public void draw(MineralizingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (background != null) {
            background.draw(guiGraphics, 0, 0);
        }

        ItemStack[] minerals = recipe.mineral().getItems();
        int cost = minerals.length > 0 ? recipe.getFlumixCost(minerals[0]) : 0;

        Font font = Minecraft.getInstance().font;
        String text = String.valueOf(cost);
        guiGraphics.drawString(font, text, FLUMIX_TEXT_X, FLUMIX_TEXT_Y, Elemenix.FLUMIX.getColor(), false);
    }
}
