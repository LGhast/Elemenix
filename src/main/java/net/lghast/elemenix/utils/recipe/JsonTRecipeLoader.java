package net.lghast.elemenix.utils.recipe;

import com.google.gson.*;
import net.lghast.elemenix.utils.Constituents;
import net.lghast.elemenix.utils.elemenix.Elemenix;
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
import java.util.*;

public class JsonTRecipeLoader {
    private static final Gson GSON = new GsonBuilder().setLenient().create();
    private static final Logger LOGGER = LogManager.getLogger();

    public static List<TechnicalRecipe> loadAllRecipes() {
        Map<String, List<TechnicalRecipe>> recipesByMod = new LinkedHashMap<>();
        Map<String, Set<String>> dependencies = new HashMap<>();

        for (IModFileInfo modFileInfo : ModList.get().getModFiles()) {
            String currentModId = modFileInfo.getMods().getFirst().getModId();
            Path recipeFolder = modFileInfo.getFile().findResource("data/elemenix/technical_recipes");
            if (!Files.exists(recipeFolder)) continue;

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(recipeFolder, p -> p.toString().endsWith(".json"))) {
                for (Path file : stream) {
                    parseRecipeFile(file, currentModId, recipesByMod, dependencies);
                }
            } catch (IOException e) {
                LOGGER.error("Failed to read technical recipes folder for mod {}", currentModId, e);
            }
        }

        List<String> sortedMods = topologicalSort(recipesByMod.keySet(), dependencies);

        List<TechnicalRecipe> allRecipes = new ArrayList<>();
        for (String modId : sortedMods) {
            List<TechnicalRecipe> modRecipes = recipesByMod.get(modId);
            if (modRecipes != null) {
                allRecipes.addAll(modRecipes);
            }
        }
        return allRecipes;
    }

    private static void parseRecipeFile(Path file, String defaultModId, Map<String, List<TechnicalRecipe>> recipesByMod, Map<String, Set<String>> dependencies) {
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root == null) return;

            String fileModId = root.has("mod_id") ? root.get("mod_id").getAsString() : defaultModId;

            if (root.has("load_after")) {
                JsonArray afterArr = root.getAsJsonArray("load_after");
                for (JsonElement e : afterArr) {
                    String requiredMod = e.getAsString();
                    dependencies.computeIfAbsent(fileModId, k -> new HashSet<>()).add(requiredMod);
                }
            }

            JsonArray recipesArray = root.getAsJsonArray("recipes");
            if (recipesArray == null) {
                LOGGER.warn("Technical recipe file {} has no 'recipes' array", file);
                return;
            }

            List<TechnicalRecipe> modRecipes = new ArrayList<>();
            for (JsonElement elem : recipesArray) {
                JsonObject obj = elem.getAsJsonObject();
                if (obj.has("preset")) {
                    List<TechnicalRecipe> presetRecipes = createRecipesFromPreset(obj);
                    if (presetRecipes != null) {
                        modRecipes.addAll(presetRecipes);
                    }
                } else {
                    TechnicalRecipe recipe = parseNormalRecipe(obj);
                    if (recipe != null) {
                        modRecipes.add(recipe);
                    }
                }
            }

            if (!modRecipes.isEmpty()) {
                recipesByMod.computeIfAbsent(fileModId, k -> new ArrayList<>()).addAll(modRecipes);
            }

        } catch (Exception e) {
            LOGGER.error("Failed to parse technical recipe file: {}", file, e);
        }
    }

    private static TechnicalRecipe parseNormalRecipe(JsonObject obj) {
        if (!obj.has("output") || !obj.has("count")) {
            LOGGER.warn("Recipe missing 'output' or 'count': {}", obj);
            return null;
        }
        String outputId = obj.get("output").getAsString();
        int count = obj.get("count").getAsInt();
        TechnicalRecipe recipe = new TechnicalRecipe(outputId, count);

        if (obj.has("inputs")) {
            JsonArray inputs = obj.getAsJsonArray("inputs");
            for (JsonElement e : inputs) {
                JsonObject io = e.getAsJsonObject();
                recipe.addInput(io.get("id").getAsString(), io.get("count").getAsInt());
            }
        }
        if (obj.has("offcuts")) {
            JsonArray offcuts = obj.getAsJsonArray("offcuts");
            for (JsonElement e : offcuts) {
                JsonObject off = e.getAsJsonObject();
                recipe.addOffcut(off.get("id").getAsString(), off.get("count").getAsInt());
            }
        }
        if (obj.has("additions")) {
            Constituents adds = parseConstituentsFromArray(obj.getAsJsonArray("additions"));
            recipe.setAdditions(adds);
        }
        if (obj.has("deductions")) {
            Constituents deducts = parseConstituentsFromArray(obj.getAsJsonArray("deductions"));
            recipe.setDeductions(deducts);
        }
        if (obj.has("multiplier")) {
            recipe.setMultiplier(obj.get("multiplier").getAsDouble());
        }
        if (obj.has("container")) {
            recipe.setContainer(obj.get("container").getAsString());
        }
        if (obj.has("removed_elemenix")) {
            String removed = obj.get("removed_elemenix").getAsString().toUpperCase();
            try {
                recipe.setRemovedElemenix(Elemenix.valueOf(removed));
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Invalid Elemenix type: {}", removed);
            }
        }
        if (obj.has("copper_state")) {
            String state = obj.get("copper_state").getAsString().toUpperCase();
            try {
                recipe.setCopperState(TechnicalRecipe.CopperState.valueOf(state));
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Invalid CopperState: {}", state);
            }
        }
        return recipe;
    }

    private static Constituents parseConstituentsFromArray(JsonArray arr) {
        Constituents cons = new Constituents();
        for (JsonElement e : arr) {
            JsonObject obj = e.getAsJsonObject();
            String typeName = obj.get("type").getAsString().toUpperCase();
            int value = obj.get("value").getAsInt();
            try {
                Elemenix type = Elemenix.valueOf(typeName);
                cons.add(type, value);
            } catch (IllegalArgumentException ex) {
                LOGGER.warn("Invalid Elemenix type in additions/deductions: {}", typeName);
            }
        }
        return cons;
    }

    private static List<TechnicalRecipe> createRecipesFromPreset(JsonObject obj) {
        String preset = obj.get("preset").getAsString();
        return switch (preset) {
            case "same" -> createSameRecipe(obj);
            case "mob_bucket" -> createMobBucketRecipes(obj);
            case "template" -> createTemplateRecipe(obj);
            case "copper_oxidizing" -> createCopperOxidizingRecipes(obj);
            case "bucket_elemenix" -> createBucketWithElemenix(obj);
            case "bucket_constituents" -> createBucketWithConstituents(obj);
            case "food_unit" -> createFoodUnitRecipes(obj);
            case "food_unit_bowl" -> createFoodUnitBowlRecipe(obj);
            default -> {
                LOGGER.warn("Unknown preset: {}", preset);
                yield Collections.emptyList();
            }
        };
    }

    private static List<TechnicalRecipe> createMobBucketRecipes(JsonObject obj) {
        if (!obj.has("output")) {
            LOGGER.warn("Mob bucket preset missing 'output'");
            return Collections.emptyList();
        }
        String outputId = obj.get("output").getAsString();
        TechnicalRecipe recipe = new TechnicalRecipe(outputId, 1);

        boolean useWaterBucket = !obj.has("in_water") || obj.get("in_water").getAsBoolean();
        String bucketItem = useWaterBucket ? "minecraft:water_bucket" : "minecraft:bucket";
        recipe.addInput(bucketItem, 1);

        if (obj.has("mob_item")) {
            recipe.addInput(obj.get("mob_item").getAsString(), 1);
        } else if (obj.has("mob_constituents")) {
            JsonArray arr = obj.getAsJsonArray("mob_constituents");
            Constituents cons = parseConstituentsFromArray(arr);
            recipe.setAdditions(cons);
        } else {
            LOGGER.warn("Mob bucket preset for {} missing 'mob_item' or 'mob_constituents'", outputId);
            return Collections.emptyList();
        }
        return Collections.singletonList(recipe);
    }

    private static List<TechnicalRecipe> createTemplateRecipe(JsonObject obj) {
        if (!obj.has("output")) {
            LOGGER.warn("Template preset missing 'output'");
            return Collections.emptyList();
        }
        String outputId = obj.get("output").getAsString();
        TechnicalRecipe recipe = new TechnicalRecipe(outputId, 1);

        String mainItem = obj.has("main_item") ? obj.get("main_item").getAsString() : "minecraft:diamond";
        recipe.addInput(mainItem, 7);

        if (!obj.has("material")) {
            LOGGER.warn("Template preset for {} missing 'material'", outputId);
            return Collections.emptyList();
        }
        String materialItem = obj.get("material").getAsString();
        recipe.addInput(materialItem, 1);

        return Collections.singletonList(recipe);
    }

    private static List<TechnicalRecipe> createCopperOxidizingRecipes(JsonObject obj) {
        if (!obj.has("input")) {
            LOGGER.warn("Copper oxidizing preset missing 'input'");
            return Collections.emptyList();
        }
        String inputId = obj.get("input").getAsString();
        String baseOutput = obj.has("base_output") ? obj.get("base_output").getAsString() : null;
        String outputPrefix = obj.has("output_prefix") ? obj.get("output_prefix").getAsString() : null;

        List<TechnicalRecipe> recipes = new ArrayList<>();
        for (TechnicalRecipe.CopperState state : TechnicalRecipe.CopperState.values()) {
            String outId;
            if (baseOutput != null) {
                String[] parts = baseOutput.split(":", 2);
                if (parts.length == 2) {
                    outId = parts[0] + ":" + state.getId() + "_" + parts[1];
                } else {
                    outId = state.getId() + "_" + baseOutput;
                }
            } else if (outputPrefix != null) {
                outId = buildOxidizedId(outputPrefix, state.getId());
            } else {
                LOGGER.warn("Copper oxidizing preset missing 'base_output' or 'output_prefix'");
                return Collections.emptyList();
            }

            TechnicalRecipe recipe = new TechnicalRecipe(outId, 1);
            recipe.addInput(inputId, 1);
            recipe.setCopperState(state);
            recipes.add(recipe);
        }
        return recipes;
    }

    private static String buildOxidizedId(String prefix, String stateId) {
        String[] parts = prefix.split(":", 2);
        if (parts.length == 2) {
            return parts[0] + ":" + stateId + "_" + parts[1];
        } else {
            return stateId + "_" + prefix;
        }
    }
    private static List<TechnicalRecipe> createSameRecipe(JsonObject obj) {
        if (!obj.has("output") || !obj.has("input")) {
            LOGGER.warn("Same preset missing 'output' or 'input'");
            return Collections.emptyList();
        }
        String outputId = obj.get("output").getAsString();
        String inputId = obj.get("input").getAsString();

        TechnicalRecipe recipe = new TechnicalRecipe(outputId, 1);
        recipe.addInput(inputId, 1);

        if (obj.has("multiplier")) {
            recipe.setMultiplier(obj.get("multiplier").getAsDouble());
        }
        return Collections.singletonList(recipe);
    }

    private static List<TechnicalRecipe> createBucketWithElemenix(JsonObject obj) {
        if (!obj.has("output") || !obj.has("elemenix_type") || !obj.has("value")) {
            LOGGER.warn("Bucket elemenix preset missing 'output', 'elemenix_type' or 'value'");
            return Collections.emptyList();
        }
        String outputId = obj.get("output").getAsString();
        String typeName = obj.get("elemenix_type").getAsString().toUpperCase();
        int value = obj.get("value").getAsInt();
        TechnicalRecipe recipe = new TechnicalRecipe(outputId, 1);
        recipe.addInput("minecraft:bucket", 1);
        try {
            recipe.setAddition(Elemenix.valueOf(typeName), value);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid elemenix_type: {}", typeName);
            return Collections.emptyList();
        }
        return Collections.singletonList(recipe);
    }

    private static List<TechnicalRecipe> createBucketWithConstituents(JsonObject obj) {
        if (!obj.has("output") || !obj.has("constituents")) {
            LOGGER.warn("Bucket constituents preset missing 'output' or 'constituents'");
            return Collections.emptyList();
        }
        String outputId = obj.get("output").getAsString();
        JsonArray arr = obj.getAsJsonArray("constituents");
        Constituents cons = parseConstituentsFromArray(arr);
        TechnicalRecipe recipe = new TechnicalRecipe(outputId, 1);
        recipe.addInput("minecraft:bucket", 1);
        recipe.setAdditions(cons);
        return Collections.singletonList(recipe);
    }

    private static List<TechnicalRecipe> createFoodUnitRecipes(JsonObject obj) {
        if (!obj.has("output") || !obj.has("source")) {
            LOGGER.warn("Food unit preset missing 'output' or 'source'");
            return Collections.emptyList();
        }
        String outputId = obj.get("output").getAsString();
        int count = obj.has("count") ? obj.get("count").getAsInt() : 4;
        String sourceId = obj.get("source").getAsString();

        TechnicalRecipe recipe = new TechnicalRecipe(outputId, count);
        recipe.addInput(sourceId, 1);

        if (obj.has("offcuts")) {
            JsonArray offcutsArr = obj.getAsJsonArray("offcuts");
            for (JsonElement e : offcutsArr) {
                JsonObject offObj = e.getAsJsonObject();
                String offId = offObj.get("id").getAsString();
                int offCount = offObj.has("count") ? offObj.get("count").getAsInt() : 1;
                recipe.addOffcut(offId, offCount);
            }
        }

        if (obj.has("container")) {
            recipe.setContainer(obj.get("container").getAsString());
        }

        return Collections.singletonList(recipe);
    }

    private static List<TechnicalRecipe> createFoodUnitBowlRecipe(JsonObject obj) {
        if (!obj.has("output") || !obj.has("source")) {
            LOGGER.warn("Food unit bowl preset missing 'output' or 'source'");
            return Collections.emptyList();
        }
        String outputId = obj.get("output").getAsString();
        int count = obj.has("count") ? obj.get("count").getAsInt() : 4;
        String sourceId = obj.get("source").getAsString();

        TechnicalRecipe recipe = new TechnicalRecipe(outputId, count);
        recipe.addInput(sourceId, 1);
        recipe.setContainer("minecraft:bowl");
        recipe.addOffcut("minecraft:bowl", 1);

        return Collections.singletonList(recipe);
    }

    private static List<String> topologicalSort(Set<String> mods, Map<String, Set<String>> dependencies) {
        Map<String, Integer> indegree = new HashMap<>();
        Map<String, List<String>> graph = new HashMap<>();
        for (String mod : mods) {
            indegree.put(mod, 0);
            graph.put(mod, new ArrayList<>());
        }
        for (Map.Entry<String, Set<String>> entry : dependencies.entrySet()) {
            String from = entry.getKey();
            for (String to : entry.getValue()) {
                if (mods.contains(to)) {
                    graph.computeIfAbsent(to, k -> new ArrayList<>()).add(from);
                    indegree.merge(from, 1, Integer::sum);
                } else {
                    LOGGER.debug("Mod {} depends on '{}' which is not present, ignored.", from, to);
                }
            }
        }
        Queue<String> queue = new LinkedList<>();
        for (Map.Entry<String, Integer> e : indegree.entrySet()) {
            if (e.getValue() == 0) queue.add(e.getKey());
        }
        List<String> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            String node = queue.poll();
            result.add(node);
            for (String neighbor : graph.getOrDefault(node, Collections.emptyList())) {
                indegree.merge(neighbor, -1, Integer::sum);
                if (indegree.get(neighbor) == 0) queue.add(neighbor);
            }
        }
        if (result.size() != mods.size()) {
            LOGGER.warn("Circular dependency detected in technical recipes mod order! Using original order.");
            return new ArrayList<>(mods);
        }
        return result;
    }
}
