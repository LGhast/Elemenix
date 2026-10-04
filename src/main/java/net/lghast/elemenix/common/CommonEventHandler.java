package net.lghast.elemenix.common;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.client.misc.ClientElemenixPrecompute;
import net.lghast.elemenix.utils.elemenix.ElemenixInfo;
import net.lghast.elemenix.utils.recipe.RecipeHelper;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = Elemenics.MOD_ID)
public class CommonEventHandler {
    @SubscribeEvent
    public static void onRecipesUpdated(RecipesUpdatedEvent event) {
        ElemenixInfo.clearCaches();

        if (!Elemenics.started) {
            return;
        }

        Level level = Elemenics.getCurrentLevel();
        if (level == null) {
            return;
        }

        if (level.isClientSide()) {
            ClientElemenixPrecompute.start();
        } else {
            ElemenixInfo.initialize();
            RecipeHelper.precomputeRecipes(level);
        }
    }
}
