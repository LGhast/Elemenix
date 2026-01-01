package net.lghast.elemenix.client.misc;

import com.mojang.blaze3d.platform.InputConstants;
import net.lghast.elemenix.Elemenics;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
@SuppressWarnings("unused")
@EventBusSubscriber(modid = Elemenics.MOD_ID, value = Dist.CLIENT)
public class ModKeyBindings {
    public static final KeyMapping OPEN_MEMORIZER_BOX = new KeyMapping(
            "key.elemenix.open_memorizer_box",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            "key.categories.elemenics"
    );

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_MEMORIZER_BOX);
    }
}