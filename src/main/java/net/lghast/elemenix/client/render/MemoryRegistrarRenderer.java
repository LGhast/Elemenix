package net.lghast.elemenix.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.lghast.elemenix.common.content.block.MemoryRegistrarBlock;
import net.lghast.elemenix.common.content.blockentity.MemoryRegistrarBlockEntity;
import net.lghast.elemenix.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class MemoryRegistrarRenderer implements BlockEntityRenderer<MemoryRegistrarBlockEntity> {
    @Override
    public void render(MemoryRegistrarBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack memorizer = blockEntity.getMemorizer();
        if (memorizer.isEmpty()) return;

        Level level = blockEntity.getLevel();
        if (level == null) return;

        Direction facing = blockEntity.getBlockState().getValue(MemoryRegistrarBlock.FACING);
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        poseStack.pushPose();
        poseStack.translate(0.5, 0.14, 0.5);
        poseStack.mulPose(new Quaternionf().rotationY((float) Math.toRadians(facingToYRot(facing))));
        poseStack.mulPose(new Quaternionf().rotationX((float) Math.toRadians(-90)));
        poseStack.scale(0.5f, 0.5f, 0.5f);
        itemRenderer.renderStatic(memorizer, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY,
                poseStack, bufferSource, level, 0);
        poseStack.popPose();

        ItemStack current = blockEntity.getCurrentRegisteringStack();
        if (!current.isEmpty()) {
            float angleDegrees = (level.getGameTime() + partialTick) * ClientConfig.REGISTRAR_ITEM_ROTATION_SPEED.get().floatValue();
            poseStack.pushPose();
            poseStack.translate(0.5, 0.55, 0.5);
            poseStack.mulPose(new Quaternionf().rotationY((float) Math.toRadians(angleDegrees)));
            poseStack.scale(0.25f, 0.25f, 0.25f);
            itemRenderer.renderStatic(current, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY,
                    poseStack, bufferSource, level, 0);
            poseStack.popPose();
        }
    }

    private static float facingToYRot(Direction facing) {
        return switch (facing) {
            case EAST -> 90f;
            case SOUTH -> 180f;
            case WEST -> 270f;
            default -> 0f;
        };
    }
}
