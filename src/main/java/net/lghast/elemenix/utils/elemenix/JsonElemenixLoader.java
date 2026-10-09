package net.lghast.elemenix.utils.elemenix;

import com.google.gson.Gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.utils.Constituents;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModFileInfo;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class JsonElemenixLoader {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new Gson();

    public static Map<String, Constituents> loadAllMappings() {
        Map<String, Constituents> result = new LinkedHashMap<>();

        FluidElemenixInfo.clearAll();

        IModFileInfo modFileInfo = ModList.get().getModFileById(Elemenics.MOD_ID);
        if (modFileInfo == null) return result;

        Path root = modFileInfo.getFile().findResource("data/elemenix/mod_constituents");
        if (root == null || !Files.exists(root)) {
            LOGGER.warn("Compat mappings folder not found.");
            return result;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(root,
                entry -> entry.getFileName().toString().endsWith(".json"))) {
            for (Path entry : stream) {
                try {
                    Map<String, Constituents> map = parseFile(entry);
                    if (!map.isEmpty()) {
                        result.putAll(map);
                        LOGGER.debug("Parse compat mapping file successfully: {}", entry);
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to parse compat mapping file: {}", entry, e);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Error reading compat mappings directory", e);
        }
        return result;
    }

    private static Map<String, Constituents> parseFile(Path file) throws IOException {
        Map<String, Constituents> map = new HashMap<>();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);

            if (root.has("mod_id")) {
                String modId = root.get("mod_id").getAsString();
                if (!ModList.get().isLoaded(modId)) {
                    return map;
                }
            }
            JsonArray entries = root.getAsJsonArray("entries");
            if (entries == null) return map;

            for (JsonElement elem : entries) {
                JsonObject obj = elem.getAsJsonObject();
                String id = obj.get("id").getAsString();

                Constituents constituents = parseEntryValue(obj, id);
                if (constituents == null) continue;

                if (id.startsWith("&")) {
                    String fluidId = id.substring(1);
                    if (fluidId.startsWith("#")) {
                        FluidElemenixInfo.putDirectTag(fluidId.substring(1), constituents);
                    } else {
                        FluidElemenixInfo.putDirect(fluidId, constituents);
                    }
                } else {
                    map.put(id, constituents);
                }
            }
        }
        return map;
    }

    @Nullable
    private static Constituents parseEntryValue(JsonObject obj, String id) {
        if (obj.has("unanalysable") && obj.get("unanalysable").getAsBoolean()) {
            return new Constituents(true);
        }

        if (obj.has("preset")) {
            String preset = obj.get("preset").getAsString();
            Constituents constituents = resolvePreset(preset, obj);
            if (constituents == null) {
                LOGGER.warn("Unknown preset '{}' for entry {}", preset, id);
            }
            return constituents;
        }

        if (obj.has("elemenix")) {
            String typeName = obj.get("elemenix").getAsString().toUpperCase();
            try {
                Elemenix type = Elemenix.valueOf(typeName);
                int value = obj.get("value").getAsInt();
                return new Constituents(type, value);
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Invalid elemenix type '{}' for entry {}", typeName, id);
                return null;
            }
        }

        JsonArray arr = obj.getAsJsonArray("constituents");
        if (arr == null) {
            LOGGER.warn("Entry {} has no recognizable format", id);
            return null;
        }
        int o = arr.get(0).getAsInt();
        int t = arr.get(1).getAsInt();
        int f = arr.get(2).getAsInt();
        int m = arr.get(3).getAsInt();
        int e = arr.get(4).getAsInt();
        int a = arr.get(5).getAsInt();
        return new Constituents(o, t, f, m, e, a);
    }

    private static Constituents resolvePreset(String preset, JsonObject obj) {
        switch (preset) {
            case "rawMeat": {
                int hunger = obj.has("hunger") ? obj.get("hunger").getAsInt() : 0;
                if (obj.has("flumix_addition")) {
                    int flumixAdd = obj.get("flumix_addition").getAsInt();
                    return Constituents.rawMeat(hunger, flumixAdd);
                }
                return Constituents.rawMeat(hunger);
            }
            case "rawFish": {
                int hunger = obj.has("hunger") ? obj.get("hunger").getAsInt() : 0;
                return Constituents.rawFish(hunger);
            }
            case "stone":
                return Constituents.stone();
            case "grassVineLeaves":
                return Constituents.grassVineLeaves();
            case "seagrass":
                return Constituents.seagrass();
            case "flower":
                return Constituents.flower();
            case "flowerLarge":
                return Constituents.flowerLarge();
            default:
                return null;
        }
    }
}
