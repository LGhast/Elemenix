package net.lghast.elemenix.compat.jech;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class JechCompat {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String JECH_MOD_ID = "jecharacters";

    private static final String TREE_SEARCHER_CLASS_NAME = "me.towdium.pinin.searchers.TreeSearcher";
    private static final String MATCH_CLASS_NAME = "me.towdium.jecharacters.utils.Match";

    private static boolean resolved = false;
    private static boolean available = false;

    @Nullable
    private static Method createSearcherMethod;
    @Nullable
    private static Method putMethod;
    @Nullable
    private static Method searchMethod;

    private static void resolve() {
        if (resolved) {
            return;
        }
        resolved = true;

        if (!ModList.get().isLoaded(JECH_MOD_ID)) {
            return;
        }

        try {
            Class<?> treeSearcherClass = Class.forName(TREE_SEARCHER_CLASS_NAME);
            Class<?> matchClass = Class.forName(MATCH_CLASS_NAME);

            createSearcherMethod = matchClass.getMethod("searcher");
            putMethod = treeSearcherClass.getMethod("put", String.class, Object.class);
            searchMethod = treeSearcherClass.getMethod("search", String.class);

            available = true;
        } catch (ReflectiveOperationException e) {
            LOGGER.warn("Failed to resolve the Just Enough Characters search API. Pinyin search is disabled.", e);
        }
    }

    public static boolean isAvailable() {
        resolve();
        return available;
    }

    @Nullable
    public static Object createSearcher() {
        if (!isAvailable() || createSearcherMethod == null) {
            return null;
        }
        try {
            return createSearcherMethod.invoke(null);
        } catch (ReflectiveOperationException e) {
            LOGGER.warn("Failed to create a Just Enough Characters searcher", e);
            return null;
        }
    }

    public static void putEntry(@Nullable Object searcher, String key, Object value) {
        if (searcher == null || putMethod == null) {
            return;
        }
        try {
            putMethod.invoke(searcher, key, value);
        } catch (ReflectiveOperationException e) {
            LOGGER.warn("Failed to put an entry into the Just Enough Characters searcher", e);
        }
    }

    @Nullable
    public static Set<Object> searchValues(@Nullable Object searcher, String token) {
        if (searcher == null || searchMethod == null || token.isEmpty()) {
            return null;
        }
        try {
            Collection<?> results = (Collection<?>) searchMethod.invoke(searcher, token);
            return results == null ? null : new HashSet<>(results);
        } catch (ReflectiveOperationException e) {
            LOGGER.warn("Failed to query the Just Enough Characters searcher", e);
            return null;
        }
    }
}
