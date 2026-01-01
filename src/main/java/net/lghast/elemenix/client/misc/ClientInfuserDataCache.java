package net.lghast.elemenix.client.misc;

import net.minecraft.core.GlobalPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ClientInfuserDataCache {
    private static final Map<GlobalPos, CacheEntry> cache = new HashMap<>();
    private static final long CACHE_DURATION = 5000;

    public static void updateCache(GlobalPos pos, long[] data) {
        cache.put(pos, new CacheEntry(data, System.currentTimeMillis()));
    }

    public static Optional<long[]> getCachedData(GlobalPos pos) {
        CacheEntry entry = cache.get(pos);
        if (entry != null && System.currentTimeMillis() - entry.timestamp < CACHE_DURATION) {
            return Optional.of(entry.data);
        }
        cache.remove(pos);
        return Optional.empty();
    }

    private record CacheEntry(long[] data, long timestamp) {}
}
