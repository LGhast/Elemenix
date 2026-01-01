package net.lghast.elemenix.conifig;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.utils.ElemenixInfo;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = Elemenics.MOD_ID, value = Dist.CLIENT)
public class ConfigEventHandler {
    @SubscribeEvent
    public static void onConfigLoaded(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == ServerConfig.SPEC) {
            ElemenixInfo.reloadFromConfig();
        }
    }

    @SubscribeEvent
    public static void onConfigReloaded(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == ServerConfig.SPEC) {
            ElemenixInfo.reloadFromConfig();
        }
    }
}
