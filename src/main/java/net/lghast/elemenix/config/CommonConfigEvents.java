package net.lghast.elemenix.config;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.core.resolver.BaseResolver;
import net.lghast.elemenix.core.Constituents;
import net.lghast.elemenix.common.system.misc.ScanHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = Elemenics.MOD_ID)
public class CommonConfigEvents {
    @SubscribeEvent
    public static void onConfigLoaded(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == CommonConfig.SPEC) {
            BaseResolver.loadFromConfig();
            Constituents.loadFromConfig();
            ScanHelper.loadFromConfig();
        }
    }

    @SubscribeEvent
    public static void onConfigReloaded(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == CommonConfig.SPEC) {
            BaseResolver.loadFromConfig();
            Constituents.loadFromConfig();
            ScanHelper.loadFromConfig();
        }
    }
}
