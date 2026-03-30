package net.lghast.elemenix;

import net.lghast.elemenix.client.misc.ModItemProperties;
import net.lghast.elemenix.client.misc.ModRenders;
import net.lghast.elemenix.conifig.ClientConfig;
import net.lghast.elemenix.conifig.CommonConfig;
import net.lghast.elemenix.datagen.DataGenerators;
import net.lghast.elemenix.network.SyncModStartedPayload;
import net.lghast.elemenix.register.content.ModBlockEntities;
import net.lghast.elemenix.register.content.ModBlocks;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.register.system.*;
import net.lghast.elemenix.utils.elemenix.ElemenixInfo;
import net.lghast.elemenix.utils.ModUtils;
import net.lghast.elemenix.utils.recipe.RecipeHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import javax.annotation.Nullable;

@Mod(Elemenics.MOD_ID)
public class Elemenics {
    public static final String MOD_ID = "elemenix";
    public static boolean started = false;
    private static int tickCounter = 0;
    private static boolean hasWorldLoaded = false;

    @Nullable
    private static ServerLevel currentServerLevel = null;

    public Elemenics(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModMenus.register(modEventBus);
        ModAdvancementTriggers.register(modEventBus);
        ModRecipes.register(modEventBus);
        ModStats.register(modEventBus);

        modEventBus.addListener(this::gatherData);
        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::onCommonSetup);

        NeoForge.EVENT_BUS.addListener(this::onWorldLoad);
        NeoForge.EVENT_BUS.addListener(this::onServerTick);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLogin);
        NeoForge.EVENT_BUS.addListener(this::onServerStarted);
        NeoForge.EVENT_BUS.addListener(this::onServerStopped);
    }

    private void gatherData(GatherDataEvent event){
        DataGenerators.gatherData(event);
    }

    public void onClientSetup(FMLClientSetupEvent event){
        event.enqueueWork(ModRenders::setItemBlockRenders);
        event.enqueueWork(ModItemProperties::register);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModStats::init);
    }

    private void onWorldLoad(ServerStartingEvent event) {
        hasWorldLoaded = true;
        tickCounter = 0;
        started = false;
        currentServerLevel = event.getServer().overworld();

        ElemenixInfo.clearCaches();
        RecipeHelper.clearCache();
    }

    private void onServerTick(ServerTickEvent.Post event) {
        if (hasWorldLoaded) {
            tickCounter++;

            if (tickCounter >= 60) {
                started = true;
                hasWorldLoaded = false;
                sendSyncToAllPlayers(event.getServer().overworld());

                ElemenixInfo.clearCaches();
                RecipeHelper.clearCache();

                ElemenixInfo.initialize();
                RecipeHelper.precomputeRecipes(getCurrentLevel());
            }
        }
    }

    private void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                sendSyncStartedPacket(serverPlayer, started);
            }
        }
    }

    @Nullable
    public static Level getCurrentLevel() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            return ModUtils.getClientLevel();
        } else {
            return currentServerLevel;
        }
    }

    private void onServerStarted(ServerStartedEvent event) {
        currentServerLevel = event.getServer().overworld();
    }

    private void onServerStopped(ServerStoppedEvent event) {
        currentServerLevel = null;
    }

    private void sendSyncStartedPacket(ServerPlayer player, boolean started) {
        SyncModStartedPayload payload = new SyncModStartedPayload(started);
        player.connection.send(payload);
    }

    private void sendSyncToAllPlayers(ServerLevel level) {
        if (level != null) {
            level.getServer();
            for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                sendSyncStartedPacket(player, true);
            }
        }
    }
}
