package net.lghast.elemenix.register.system;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.common.content.blockentity.EnricherBlockEntity;
import net.lghast.elemenix.common.content.blockentity.InfuserBlockEntity;
import net.lghast.elemenix.common.content.blockentity.SubstancePrinterBlockEntity;
import net.lghast.elemenix.common.content.blockentity.TransformerBlockEntity;
import net.lghast.elemenix.common.content.blockentity.WitchcraftPrinterBlockEntity;
import net.lghast.elemenix.register.content.ModBlockEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = Elemenics.MOD_ID)
public class ModCapabilities {
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.TRANSFORMER.get(),
                TransformerBlockEntity::getItemHandler
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.INFUSER.get(),
                InfuserBlockEntity::getItemHandler
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.ENRICHER.get(),
                EnricherBlockEntity::getItemHandler
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.SUBSTANCE_PRINTER.get(),
                SubstancePrinterBlockEntity::getItemHandler
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.WITCHCRAFT_PRINTER.get(),
                WitchcraftPrinterBlockEntity::getItemHandler
        );
    }
}
