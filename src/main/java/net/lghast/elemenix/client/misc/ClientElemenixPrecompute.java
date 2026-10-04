package net.lghast.elemenix.client.misc;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.utils.ModUtils;
import net.lghast.elemenix.utils.elemenix.ElemenixInfo;
import net.lghast.elemenix.utils.recipe.RecipeHelper;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@OnlyIn(Dist.CLIENT)
public class ClientElemenixPrecompute {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "Elemenix-Client-Precompute");
        thread.setDaemon(true);
        thread.setPriority(Thread.MIN_PRIORITY + 1);
        return thread;
    });

    private static final AtomicBoolean WARM_UP_SCHEDULED = new AtomicBoolean(false);

    public static void start() {
        Level level = ModUtils.getClientLevel();
        if (level == null || !Elemenics.started) {
            return;
        }

        if (!RecipeHelper.areRecipesLoaded(level)) {
            return;
        }
        if (!WARM_UP_SCHEDULED.compareAndSet(false, true)) {
            return;
        }
        EXECUTOR.execute(() -> {
            try {
                warmUp(level);
            } finally {
                WARM_UP_SCHEDULED.set(false);
            }
        });
    }

    private static void warmUp(Level level) {
        try {
            ElemenixInfo.initialize();
            RecipeHelper.precomputeRecipes(level);
        } catch (Throwable t) {
            LOGGER.error("Client-side elemenix precomputation failed", t);
        }
    }
}
