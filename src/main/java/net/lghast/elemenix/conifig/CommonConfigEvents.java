package net.lghast.elemenix.conifig;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.utils.Constituents;
import net.lghast.elemenix.utils.ElemenixInfo;
import net.lghast.elemenix.utils.ScanHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = Elemenics.MOD_ID)
public class CommonConfigEvents {
    @SubscribeEvent
    public static void onConfigLoaded(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == CommonConfig.SPEC) {
            ElemenixInfo.loadFromConfig();
            Constituents.loadFromConfig();
            ScanHelper.loadFromConfig();
        }
    }

    @SubscribeEvent
    public static void onConfigReloaded(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == CommonConfig.SPEC) {
            ElemenixInfo.loadFromConfig();
            Constituents.loadFromConfig();
            ScanHelper.loadFromConfig();
        }
    }
}
