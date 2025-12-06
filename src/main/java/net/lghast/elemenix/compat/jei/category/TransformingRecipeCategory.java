package net.lghast.elemenix.compat.jei.category;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.lghast.elemenix.compat.jei.recipe.TransformingRecipe;
import net.lghast.elemenix.register.content.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class TransformingRecipeCategory implements IRecipeCategory<TransformingRecipe> {
    public static final RecipeType<TransformingRecipe> TYPE =
            RecipeType.create("elemenix", "transforming", TransformingRecipe.class);

    private static final ResourceLocation BACKGROUND_LOCATION =
            ResourceLocation.fromNamespaceAndPath("elemenix", "textures/gui/transforming_jei.png");

    private static final int WIDTH = 120;
    private static final int HEIGHT = 60;

    private static final int INSTRUMENT_X = 16;
    private static final int INSTRUMENT_Y = 22;
    private static final int OUTPUT_X = 88;
    private static final int OUTPUT_Y = 22;

    private static final int TEXT_ELEMENIX1_Y = 8;
    private static final int TEXT_ELEMENIX2_Y = 43;

    private final IDrawable background;
    private final IDrawable icon;
    private final Component localizedName;

    public TransformingRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(BACKGROUND_LOCATION, 0, 0, WIDTH, HEIGHT)
                .setTextureSize(WIDTH, HEIGHT)
                .build();

        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.GEOLOGICAL_SIMULATOR.get()));
        this.localizedName = Component.translatable("jei.category.elemenix.transforming");
    }

    @Override
    public RecipeType<TransformingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, TransformingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, INSTRUMENT_X, INSTRUMENT_Y)
                .addIngredients(recipe.instrument())
                .setSlotName("instrument");

        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X, OUTPUT_Y)
                .addItemStack(recipe.output())
                .setSlotName("output");
    }

    @Override
    public void draw(TransformingRecipe recipe, IRecipeSlotsView recipeSlotsView,
                     GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (background != null) {
            background.draw(guiGraphics, 0, 0);
        }

        Font font = Minecraft.getInstance().font;

        String text1 = recipe.elemenix1().getName() + ": " + formatNumber(recipe.amount());
        int text1Width = font.width(text1);
        int text1X = (WIDTH - text1Width) / 2 + 5;
        guiGraphics.drawString(font, text1, text1X, TEXT_ELEMENIX1_Y,
                recipe.elemenix1().getColor(), false);

        String text2 = recipe.elemenix2().getName() + ": " + formatNumber(recipe.amount());
        int text2Width = font.width(text2);
        int text2X = (WIDTH - text2Width) / 2 + 5;
        guiGraphics.drawString(font, text2, text2X, TEXT_ELEMENIX2_Y,
                recipe.elemenix2().getColor(), false);
    }

    @Override
    public boolean isHandled(TransformingRecipe recipe) {
        return true;
    }

    private String formatNumber(int number) {
        return String.valueOf(number);
    }
}