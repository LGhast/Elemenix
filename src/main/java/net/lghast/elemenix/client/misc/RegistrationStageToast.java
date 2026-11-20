package net.lghast.elemenix.client.misc;

import net.lghast.elemenix.client.Palette;
import net.lghast.elemenix.config.ClientConfig;
import net.lghast.elemenix.register.content.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class RegistrationStageToast implements Toast {
    private static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("toast/advancement");
    private static final long DISPLAY_TIME = 5000L;

    private final Component title;
    private final Component description;
    private final ItemStack icon;

    private RegistrationStageToast(int count) {
        this.title = Component.translatable("toast.elemenix.registration_stage.title");
        this.description = Component.translatable("toast.elemenix.registration_stage.description", count);
        this.icon = new ItemStack(ModBlocks.MEMORY_REGISTRAR.get());
    }

    public static void show(int count) {
        if (!ClientConfig.SHOW_REGISTRATION_STAGE_TOAST.get()) return;
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getToasts().addToast(new RegistrationStageToast(count));
    }

    @Override
    public @NotNull Visibility render(GuiGraphics graphics, ToastComponent toastComponent, long timeSinceLastVisible) {
        graphics.blitSprite(BACKGROUND_SPRITE, 0, 0, this.width(), this.height());
        graphics.drawString(toastComponent.getMinecraft().font, this.title, 30, 7, Palette.YELLOW, false);
        graphics.drawString(toastComponent.getMinecraft().font, this.description, 30, 18, Palette.WHITE, false);
        graphics.renderFakeItem(this.icon, 8, 8);
        return timeSinceLastVisible >= DISPLAY_TIME * toastComponent.getNotificationDisplayTimeMultiplier()
                ? Visibility.HIDE : Visibility.SHOW;
    }
}
