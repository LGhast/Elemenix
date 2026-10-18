package net.lghast.elemenix.mixin;

import net.lghast.elemenix.utils.imitforge.Imitforging;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true)
    private ItemStack elemenix$render(ItemStack stack) {
        return Imitforging.getAppearanceStackOrOriginal(stack);
    }

    @ModifyVariable(method = "getModel", at = @At("HEAD"), argsOnly = true)
    private ItemStack elemenix$getModel(ItemStack stack) {
        return Imitforging.getAppearanceStackOrOriginal(stack);
    }

    @ModifyVariable(method = "renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V", at = @At("HEAD"), argsOnly = true)
    private ItemStack elemenix$renderStatic(ItemStack stack) {
        return Imitforging.getAppearanceStackOrOriginal(stack);
    }
}
