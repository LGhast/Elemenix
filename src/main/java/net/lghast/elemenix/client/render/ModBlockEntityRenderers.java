package net.lghast.elemenix.client.render;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.register.content.ModBlockEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = Elemenics.MOD_ID, value = Dist.CLIENT)
public class ModBlockEntityRenderers {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.SUBSTANCE_PRINTER.get(), context -> new PrinterRenderer());
        event.registerBlockEntityRenderer(ModBlockEntities.WITCHCRAFT_PRINTER.get(), context -> new PrinterRenderer());
    }
}
