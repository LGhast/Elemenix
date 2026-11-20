package net.lghast.elemenix.compat.avaritia;

import net.lghast.elemenix.mixin.avaritia.WrappedItemModelAccessor;
import net.minecraft.client.resources.model.BakedModel;

import javax.annotation.Nullable;

/**
 * Resolves the plain base model of an Avaritia custom item model, dropping the halo/cosmic wrappers.
 * <p>Only models from Avaritia's {@code WrappedItemModel} hierarchy are unwrapped; other models return null.
 */
public final class AvaritiaItemModels {
    private static final int MAX_WRAPPER_DEPTH = 4;

    private AvaritiaItemModels() {
    }

    @Nullable
    public static BakedModel getBaseModel(BakedModel model) {
        BakedModel current = model;
        for (int depth = 0; depth < MAX_WRAPPER_DEPTH; depth++) {
            if (!(current instanceof WrappedItemModelAccessor accessor)) {
                break;
            }
            BakedModel wrapped = accessor.elemenix$getWrapped();
            if (wrapped == null || wrapped == current) {
                break;
            }
            current = wrapped;
        }
        return current == model ? null : current;
    }
}
