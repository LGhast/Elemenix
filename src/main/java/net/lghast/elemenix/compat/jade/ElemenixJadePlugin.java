package net.lghast.elemenix.compat.jade;

import net.lghast.elemenix.common.content.block.ElemenicComparatorBlock;
import net.lghast.elemenix.common.content.block.EjectorBlock;
import net.lghast.elemenix.common.content.block.OutletBlock;
import net.lghast.elemenix.common.content.block.ShuntBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.BaseEntityBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class ElemenixJadePlugin implements IWailaPlugin {
    public static final ResourceLocation UID_ELEMENIX_STORAGE =
            ResourceLocation.fromNamespaceAndPath("elemenix", "elemenix_storage");
    public static final ResourceLocation UID_OUTLET =
            ResourceLocation.fromNamespaceAndPath("elemenix", "outlet_openness");
    public static final ResourceLocation UID_COMPARATOR =
            ResourceLocation.fromNamespaceAndPath("elemenix", "elemenic_comparator");
    public static final ResourceLocation UID_EJECTOR_FILTER =
            ResourceLocation.fromNamespaceAndPath("elemenix", "ejector_filter");
    public static final ResourceLocation UID_SHUNT_FILTER =
            ResourceLocation.fromNamespaceAndPath("elemenix", "shunt_filter");

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(ElemenixHolderProvider.INSTANCE, BaseEntityBlock.class);
        registration.registerBlockDataProvider(OutletProvider.INSTANCE, OutletBlock.class);
        registration.registerBlockDataProvider(ComparatorProvider.INSTANCE, ElemenicComparatorBlock.class);
        registration.registerBlockDataProvider(EjectorProvider.INSTANCE, EjectorBlock.class);
        registration.registerBlockDataProvider(ShuntProvider.INSTANCE, ShuntBlock.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(ElemenixHolderProvider.INSTANCE, BaseEntityBlock.class);
        registration.registerBlockComponent(OutletProvider.INSTANCE, OutletBlock.class);
        registration.registerBlockComponent(ComparatorProvider.INSTANCE, ElemenicComparatorBlock.class);
        registration.registerBlockComponent(EjectorProvider.INSTANCE, EjectorBlock.class);
        registration.registerBlockComponent(ShuntProvider.INSTANCE, ShuntBlock.class);
    }
}
