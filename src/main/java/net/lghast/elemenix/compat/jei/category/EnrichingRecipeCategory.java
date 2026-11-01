package net.lghast.elemenix.compat.jei.category;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.lghast.elemenix.compat.jei.recipe.EnrichingRecipe;
import net.lghast.elemenix.register.content.ModBlocks;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.utils.misc.ModUtils;
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
public class EnrichingRecipeCategory implements IRecipeCategory<EnrichingRecipe> {
    public static final RecipeType<EnrichingRecipe> TYPE =
            RecipeType.create("elemenix", "enriching", EnrichingRecipe.class);

    private static final ResourceLocation BACKGROUND_SINGLE =
            ResourceLocation.fromNamespaceAndPath("elemenix", "textures/gui/jei/enriching_jei.png");
    private static final ResourceLocation BACKGROUND_DOUBLE =
            ResourceLocation.fromNamespaceAndPath("elemenix", "textures/gui/jei/enriching_jei_double.png");

    private static final int WIDTH = 120;
    private static final int HEIGHT = 59;

    private static final int OUTPUT_X = 90;
    private static final int OUTPUT_Y = 23;

    private static final int ENRICHER_X = 16;
    private static final int ENRICHER_Y = 13;

    private static final int ANALYZER_X = 16;
    private static final int ANALYZER_Y = 32;
    private static final int ANALYZER_SINGLE_Y = 23;

    private final IDrawable backgroundSingle;
    private final IDrawable backgroundDouble;
    private final IDrawable icon;
    private final Component localizedName;

    public EnrichingRecipeCategory(IGuiHelper guiHelper) {
        this.backgroundSingle = guiHelper.drawableBuilder(BACKGROUND_SINGLE, 0, 0, WIDTH, HEIGHT)
                .setTextureSize(WIDTH, HEIGHT)
                .build();
        this.backgroundDouble = guiHelper.drawableBuilder(BACKGROUND_DOUBLE, 0, 0, WIDTH, HEIGHT)
                .setTextureSize(WIDTH, HEIGHT)
                .build();

        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.ELEMENIC_ENRICHER.get()));
        this.localizedName = Component.translatable("jei.category.elemenix.enriching");
    }

    @Override
    public @NotNull RecipeType<EnrichingRecipe> getRecipeType() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, EnrichingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X, OUTPUT_Y)
                .addItemStack(recipe.output());

        if (!recipe.portable()) {
            builder.addSlot(RecipeIngredientRole.INPUT, ENRICHER_X, ENRICHER_Y)
                    .addItemStack(new ItemStack(ModBlocks.ELEMENIC_ENRICHER.get()))
                    .setSlotName("enricher");

            builder.addSlot(RecipeIngredientRole.INPUT, ANALYZER_X, ANALYZER_Y)
                    .addItemStack(new ItemStack(ModItems.ELEMENIC_ANALYZER.get()))
                    .setSlotName("analyzer");
        } else {
            builder.addSlot(RecipeIngredientRole.INPUT, ANALYZER_X, ANALYZER_SINGLE_Y)
                    .addItemStack(new ItemStack(ModItems.ELEMENIC_ANALYZER.get()))
                    .setSlotName("analyzer");
        }
    }

    @Override
    public void draw(EnrichingRecipe recipe, IRecipeSlotsView recipeSlotsView,
                     GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IDrawable background = recipe.portable() ? backgroundSingle : backgroundDouble;
        if (background != null) {
            background.draw(guiGraphics, 0, 0);
        }

        String text = recipe.elemenix().getName() + ": " + ModUtils.formatNumber(recipe.amount());
        Font font = Minecraft.getInstance().font;

        int textX = (WIDTH - font.width(text)) / 2;
        guiGraphics.drawString(font, text, textX, 4, recipe.elemenix().getColor(), false);
    }
}
