package net.lghast.elemenix.compat.jei.category;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.lghast.elemenix.common.system.recipe.GerminatingRecipe;
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
public class GerminatingRecipeCategory implements IRecipeCategory<GerminatingRecipe> {
    public static final RecipeType<GerminatingRecipe> TYPE =
            RecipeType.create("elemenix", "germinating", GerminatingRecipe.class);

    private static final ResourceLocation BACKGROUND_LOCATION =
            ResourceLocation.fromNamespaceAndPath("elemenix", "textures/gui/germinating_jei.png");

    private static final int WIDTH = 176;
    private static final int HEIGHT = 74;

    private static final int TEMPLATE_X = 47;
    private static final int TEMPLATE_Y = 18;
    private static final int OUTPUT_FIRST_X = 50;
    private static final int OUTPUT_Y = 48;
    private static final int FLUMIX_TEXT_X = 91;
    private static final int FLUMIX_TEXT_Y = 16;
    private static final int TERRIX_TEXT_X = 91;
    private static final int TERRIX_TEXT_Y = 26;

    private final IDrawable background;
    private final IDrawable icon;
    private final Component localizedName;

    public GerminatingRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(BACKGROUND_LOCATION, 0, 0, WIDTH, HEIGHT)
                .setTextureSize(WIDTH, HEIGHT)
                .build();
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.GERMINAL_ACCELERATOR.get()));
        this.localizedName = Component.translatable("jei.category.elemenix.germinating");
    }

    @Override
    public @NotNull RecipeType<GerminatingRecipe> getRecipeType() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, GerminatingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, TEMPLATE_X, TEMPLATE_Y)
                .addIngredients(recipe.template())
                .setSlotName("template");

        for (int i = 0; i < recipe.products().size(); i++) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_FIRST_X + i * 18, OUTPUT_Y)
                    .addItemStack(recipe.products().get(i))
                    .setSlotName("product_" + i);
        }
    }

    @Override
    public void draw(GerminatingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (background != null) {
            background.draw(guiGraphics, 0, 0);
        }

        int[] cost = recipe.getCost();
        Font font = Minecraft.getInstance().font;

        String flumixText = ModUtils.formatNumber(cost[1]);
        guiGraphics.drawString(font, flumixText, FLUMIX_TEXT_X, FLUMIX_TEXT_Y, Elemenix.FLUMIX.getColor(), false);

        String terrixText = ModUtils.formatNumber(cost[0]);
        guiGraphics.drawString(font, terrixText, TERRIX_TEXT_X, TERRIX_TEXT_Y, Elemenix.TERRIX.getColor(), false);
    }
}
