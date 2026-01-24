package net.lghast.elemenix.common;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.utils.ElemenixInfo;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = Elemenics.MOD_ID)
public class CommonEventHandler {
    @SubscribeEvent
    public static void onRecipesUpdated(RecipesUpdatedEvent event) {
        ElemenixInfo.clearCaches();
    }
}
