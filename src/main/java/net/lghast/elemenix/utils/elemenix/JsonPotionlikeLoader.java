package net.lghast.elemenix.utils.elemenix;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.lghast.elemenix.utils.ModUtils;
import net.minecraft.world.item.Item;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModFileInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps potion-content-bearing items to the ratio between one item and one potion content.
 * Loaded from data/elemenix/potionlike across every mod.
 */
public class JsonPotionlikeLoader {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setLenient().create();

    private static final Map<Item, Double> RATIO_MAP = new HashMap<>();

    public static void loadAll() {
        RATIO_MAP.clear();

        for (IModFileInfo modFileInfo : ModList.get().getModFiles()) {
            Path folder = modFileInfo.getFile().findResource("data/elemenix/potionlike");
            if (folder == null || !Files.exists(folder)) continue;

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder, p -> p.toString().endsWith(".json"))) {
                for (Path file : stream) {
                    parseFile(file);
                }
            } catch (IOException e) {
                LOGGER.error("Failed to read potion content ratios folder for mod {}", modFileInfo.getMods().getFirst().getModId(), e);
            }
        }
    }

    private static void parseFile(Path file) {
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root == null) return;

            if (root.has("mod_id")) {
                String modId = root.get("mod_id").getAsString();
                if (!ModUtils.hasServerMod(modId)) return;
            }

            JsonArray entries = root.getAsJsonArray("entries");
            if (entries == null) return;

            for (JsonElement elem : entries) {
                JsonObject obj = elem.getAsJsonObject();
                String itemId = obj.get("item").getAsString();
                double ratio = obj.get("content_ratio").getAsDouble();

                Item item = ModUtils.getItemFromString(itemId);
                if (item != null) {
                    RATIO_MAP.put(item, ratio);
                } else {
                    LOGGER.warn("Unknown item ID in potion content ratios: {}", itemId);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to parse potion content ratio file: {}", file, e);
        }
    }

    public static double getContentRatio(Item item) {
        return RATIO_MAP.getOrDefault(item, 1.0);
    }

    public static void clear() {
        RATIO_MAP.clear();
    }
}
