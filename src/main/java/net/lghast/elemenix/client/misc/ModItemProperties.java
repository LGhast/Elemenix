package net.lghast.elemenix.client.misc;

import net.lghast.elemenix.common.system.datacomponent.ElemenicStorage;
import net.lghast.elemenix.common.system.datacomponent.RemoteStorageBinding;
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

    private static void registerHasStorageProperty(){
        ResourceLocation propertyIdHasStorage = ResourceLocation.parse("elemenix:has_storage");
        ItemProperties.register(ModItems.ELEMENIC_STORAGE.get(),
                propertyIdHasStorage,
                (itemStack, clientLevel, livingEntity, seed) -> {
                    ElemenicStorage storage = itemStack.get(ModDataComponents.ELEMENIC_STORAGE.get());
                    if (storage == null) {
                        return 0f;
                    }
                    return storage.isEmpty() ? 0f : 1f;
                }
        );
    }

    private static void registerRemoteBoundProperty(){
        ResourceLocation propertyIdRemoteBound = ResourceLocation.parse("elemenix:remote_bound");
        ItemProperties.register(ModItems.REMOTE_ELEMENIC_STORAGE.get(),
                propertyIdRemoteBound,
                (itemStack, clientLevel, livingEntity, seed) -> {
                    RemoteStorageBinding binding = itemStack.get(ModDataComponents.REMOTE_STORAGE_BINDING.get());
                    if (binding == null) {
                        return 0f;
                    }
                    return binding.isBound() ? 1f : 0f;
                }
        );
    }

    public static void register(){
        registerStyleProperty();
        registerHasStorageProperty();
        registerRemoteBoundProperty();
    }
}
