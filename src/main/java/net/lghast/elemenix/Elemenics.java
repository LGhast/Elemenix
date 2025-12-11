package net.lghast.elemenix;

import net.lghast.elemenix.client.misc.ModItemProperties;
import net.lghast.elemenix.client.misc.ModRenders;
import net.lghast.elemenix.conifig.ClientConfig;
import net.lghast.elemenix.conifig.ServerConfig;
import net.lghast.elemenix.datagen.DataGenerators;
import net.lghast.elemenix.register.content.ModBlockEntities;
import net.lghast.elemenix.register.content.ModBlocks;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.register.system.*;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(Elemenics.MOD_ID)
public class Elemenics {
    public static final String MOD_ID = "elemenix";

    public Elemenics(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModMenus.register(modEventBus);
        ModAdvancementTriggers.register(modEventBus);
        ModRecipes.register(modEventBus);

        modEventBus.addListener(this::gatherData);
        modEventBus.addListener(this::onClientSetup);
    }

    private void gatherData(GatherDataEvent event){
        DataGenerators.gatherData(event);
    }

    public void onClientSetup(FMLClientSetupEvent event){
        event.enqueueWork(ModRenders::setItemBlockRenders);
        event.enqueueWork(ModItemProperties::register);
    }
}
