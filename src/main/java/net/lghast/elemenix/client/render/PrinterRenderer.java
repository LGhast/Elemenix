package net.lghast.elemenix.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.lghast.elemenix.common.content.blockentity.AbstractPrinterBlockEntity;
import net.lghast.elemenix.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class PrinterRenderer implements BlockEntityRenderer<AbstractPrinterBlockEntity> {
    @Override
    public void render(AbstractPrinterBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!ClientConfig.SHOW_PRINTER_ITEM_PREVIEW.get()) return;

        ItemStack stack = blockEntity.getPrintingItemStack();
        if (stack.isEmpty()) return;

        Level level = blockEntity.getLevel();
        if (level == null) return;

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        float angleDegrees = (level.getGameTime() + partialTick) * ClientConfig.PRINTER_ITEM_ROTATION_SPEED.get().floatValue();

        poseStack.pushPose();
        poseStack.translate(0.5, 0.47, 0.5);
        poseStack.scale(0.18f, 0.18f, 0.18f);
        poseStack.mulPose(new Quaternionf().rotationY((float) Math.toRadians(angleDegrees)));
        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY,
                poseStack, bufferSource, level, 0);
        poseStack.popPose();
    }
}
