package net.lghast.elemenix.client.misc;

import net.lghast.elemenix.register.content.ModBlocks;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;

public class ModRenders {
    public static void setItemBlockRenders(){
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.GEOLOGICAL_SIMULATOR.get(), RenderType.cutout());
    }
}
