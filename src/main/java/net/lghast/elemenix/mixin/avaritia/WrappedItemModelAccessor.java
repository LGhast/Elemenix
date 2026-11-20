package net.lghast.elemenix.mixin.avaritia;

import net.minecraft.client.resources.model.BakedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Optional accessor for Avaritia's item model wrapper, used by the registrar screen to render the
 * base item model without the halo/cosmic effect layer. The target is pseudo, so the mixin is skipped
 * when Avaritia is not installed.
 */
@Pseudo
@Mixin(targets = "committee.nova.mods.avaritia.api.client.model.bakedmodels.WrappedItemModel")
public interface WrappedItemModelAccessor {
    @Accessor("wrapped")
    BakedModel elemenix$getWrapped();
}
