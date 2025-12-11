package net.lghast.elemenix.client.misc;

import net.lghast.elemenix.common.system.datacomponent.Style;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.register.system.ModDataComponents;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;

public class ModItemProperties {
    private static void registerStyleProperty(){
        ResourceLocation propertyIdStyle = ResourceLocation.parse("elemenix:style");
        ItemProperties.register(ModItems.ELEMENIC_MEMORIZER.get(),
                propertyIdStyle,
                (itemStack, clientLevel, livingEntity, seed) -> {
                    Style style = itemStack.get(ModDataComponents.STYLE.get());
                    if (style != null) {
                        return style.style();
                    }
                    return 0f;
                }
        );
    }

    public static void register(){
        registerStyleProperty();
    }
}
