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
import net.lghast.elemenix.utils.ModUtils;
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
public class EnrichingRecipeCategory implements IRecipeCategory<EnrichingRecipe> {
    public static final RecipeType<EnrichingRecipe> TYPE =
            RecipeType.create("elemenix", "enriching", EnrichingRecipe.class);

    private static final ResourceLocation BACKGROUND_LOCATION =
            ResourceLocation.fromNamespaceAndPath("elemenix", "textures/gui/enriching_jei.png");

    private static final int WIDTH = 120;
    private static final int HEIGHT = 80;

    private static final int OUTPUT_X = 88;
    private static final int OUTPUT_Y = 32;

    private static final int ENRICHER_X = 16;
    private static final int ENRICHER_Y = 14;

    private static final int ANALYZER_X = 16;
    private static final int ANALYZER_Y = 32;

    private static final int EJECTOR_X = 16;
    private static final int EJECTOR_Y = 50;

    private static final int TEXT_Y = 55;
    private static final int TEXT_X = 47;

    private final IDrawable background;
    private final IDrawable icon;
    private final Component localizedName;

    public EnrichingRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(BACKGROUND_LOCATION, 0, 0, WIDTH, HEIGHT)
                .setTextureSize(WIDTH, HEIGHT)
                .build();

        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.ELEMENIC_ENRICHER.get()));
        this.localizedName = Component.translatable("jei.category.elemenix.enriching");
    }

    @Override
    public RecipeType<EnrichingRecipe> getRecipeType() {
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

            builder.addSlot(RecipeIngredientRole.INPUT, EJECTOR_X, EJECTOR_Y)
                    .addItemStack(new ItemStack(ModBlocks.ELEMENIC_EJECTOR.get()))
                    .setSlotName("ejector");
        } else {
            builder.addSlot(RecipeIngredientRole.INPUT, ANALYZER_X, ANALYZER_Y)
                    .addItemStack(new ItemStack(ModItems.ELEMENIC_ANALYZER.get()))
                    .setSlotName("analyzer");
        }
    }

    @Override
    public void draw(EnrichingRecipe recipe, IRecipeSlotsView recipeSlotsView,
                     GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (background != null) {
            background.draw(guiGraphics, 0, 0);
        }

        String text = recipe.elemenix().getName() + ": " + ModUtils.formatNumber(recipe.amount());
        Font font = Minecraft.getInstance().font;

        guiGraphics.drawString(font, text, TEXT_X, TEXT_Y, recipe.elemenix().getColor(), false);
    }
}