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
import net.lghast.elemenix.utils.ElemenixInfo;
import net.lghast.elemenix.utils.RecipeHelper;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod(Elemenics.MOD_ID)
public class Elemenics {
    public static final String MOD_ID = "elemenix";
    public static boolean started = false;
    private static int tickCounter = 0;
    private static boolean hasWorldLoaded = false;

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

        NeoForge.EVENT_BUS.addListener(this::onWorldLoad);
        NeoForge.EVENT_BUS.addListener(this::onServerTick);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLogout);
    }

    private void gatherData(GatherDataEvent event){
        DataGenerators.gatherData(event);
    }

    public void onClientSetup(FMLClientSetupEvent event){
        event.enqueueWork(ModRenders::setItemBlockRenders);
        event.enqueueWork(ModItemProperties::register);
    }

    private void onWorldLoad(ServerStartingEvent event) {
        hasWorldLoaded = true;
        tickCounter = 0;
        started = false;

        ElemenixInfo.clearCaches();
        RecipeHelper.clearCache();
    }

    private void onServerTick(ServerTickEvent.Post event) {
        if (hasWorldLoaded) {
            tickCounter++;

            if (tickCounter >= 40) {
                started = true;
                hasWorldLoaded = false;

                ElemenixInfo.clearCaches();
                RecipeHelper.clearCache();
            }
        }
    }

    private void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            ElemenixInfo.clearCaches();
            RecipeHelper.clearCache();

            hasWorldLoaded = false;
            tickCounter = 0;
            started = false;
        }
    }
}
