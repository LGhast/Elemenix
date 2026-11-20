package net.lghast.elemenix.client.render;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.lghast.elemenix.compat.avaritia.AvaritiaItemModels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

/**
 * Renders item icons without Avaritia's halo/cosmic layers by using the wrapped base model when the
 * item model is an Avaritia custom wrapper. Mirrors {@link GuiGraphics#renderItem(ItemStack, int, int)}.
 */
public final class ItemIconRenderer {
    private static final boolean AVARITIA_LOADED = ModList.get().isLoaded("avaritia");

    private ItemIconRenderer() {
    }

    public static void render(GuiGraphics graphics, ItemStack stack, int x, int y) {
        if (stack.isEmpty()) {
            return;
        }
        if (!AVARITIA_LOADED) {
            graphics.renderItem(stack, x, y);
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ItemRenderer renderer = minecraft.getItemRenderer();
        BakedModel model = renderer.getModel(stack, minecraft.level, minecraft.player, 0);
        BakedModel base = AvaritiaItemModels.getBaseModel(model);
        renderModel(graphics, renderer, stack, x, y, base != null ? base : model);
    }

    private static void renderModel(GuiGraphics graphics, ItemRenderer renderer, ItemStack stack,
                                    int x, int y, BakedModel model) {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        boolean flat = !model.usesBlockLight();
        try {
            pose.translate(x + 8.0F, y + 8.0F, 150.0F);
            pose.scale(16.0F, -16.0F, 16.0F);
            if (flat) {
                Lighting.setupForFlatItems();
            }
            renderer.render(stack, ItemDisplayContext.GUI, false, pose, graphics.bufferSource(),
                    15728880, OverlayTexture.NO_OVERLAY, model);
            graphics.flush();
        } finally {
            if (flat) {
                Lighting.setupFor3DItems();
            }
            pose.popPose();
        }
    }
}
