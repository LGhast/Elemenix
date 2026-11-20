package net.lghast.elemenix.common.system.command;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.lghast.elemenix.core.resolver.BaseResolver;
import net.lghast.elemenix.register.system.ModTags;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@EventBusSubscriber
@SuppressWarnings("unused")
public class ElemenicsExportCommand {
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("elemenicsexport")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("spawneggs")
                        .executes(ElemenicsExportCommand::exportSpawnEggs))
                .then(Commands.literal("ignored")
                        .executes(ElemenicsExportCommand::exportIgnored))
                .then(Commands.literal("itemtags")
                        .executes(ElemenicsExportCommand::exportItemTags))
                .then(Commands.literal("entitytags")
                        .executes(ElemenicsExportCommand::exportEntityTags)));
    }

    private static int exportSpawnEggs(CommandContext<CommandSourceStack> context) {
        List<String> ids = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (!id.toString().contains("spawn_egg")) continue;
            if (BuiltInRegistries.ITEM.wrapAsHolder(item).is(ModTags.SPAWN_EGGS)) continue;
            ids.add(id.toString());
        }
        return writeExport(context, ids, "spawneggs.json");
    }

    private static int exportIgnored(CommandContext<CommandSourceStack> context) {
        List<String> ids = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack stack = new ItemStack(item);
            if (stack.is(Items.AIR)) continue;
            if (stack.is(ModTags.STRICTLY_UNANALYSABLE)) continue;
            if (BaseResolver.isUnanalysableStrictly(item)) continue;

            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (id.toString().contains("spawn_egg")) continue;

            if (stack.is(ModTags.COMMAND_IGNORED)) continue;
            if (BaseResolver.isUnanalysable(item)) {
                ids.add(id.toString());
            }
        }
        return writeExport(context, ids, "command_ignored.json");
    }

    private static int exportItemTags(CommandContext<CommandSourceStack> context) {
        Registry<Item> registry = context.getSource().getServer().registryAccess().registryOrThrow(Registries.ITEM);
        Set<ResourceLocation> existing = loadRepresentativeKeys("item.json");
        List<String> ids = registry.getTags()
                .map(pair -> pair.getFirst().location())
                .filter(loc -> !existing.contains(loc))
                .map(ResourceLocation::toString)
                .sorted()
                .toList();
        return writeTagListExport(context, ids, "item_tags.json");
    }

    private static int exportEntityTags(CommandContext<CommandSourceStack> context) {
        Registry<EntityType<?>> registry = context.getSource().getServer().registryAccess().registryOrThrow(Registries.ENTITY_TYPE);
        Set<ResourceLocation> existing = loadRepresentativeKeys("entity.json");
        List<String> ids = registry.getTags()
                .map(pair -> pair.getFirst().location())
                .filter(loc -> !existing.contains(loc))
                .map(ResourceLocation::toString)
                .sorted()
                .toList();
        return writeTagListExport(context, ids, "entity_tags.json");
    }

    private static Set<ResourceLocation> loadRepresentativeKeys(String fileName) {
        Set<ResourceLocation> keys = new HashSet<>();
        try (InputStream in = ElemenicsExportCommand.class.getClassLoader()
                .getResourceAsStream("assets/elemenix/representatives/" + fileName)) {
            if (in == null) return keys;
            try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    if (entry.getKey().equals("replace")) continue;
                    ResourceLocation id = ResourceLocation.tryParse(entry.getKey());
                    if (id != null) keys.add(id);
                }
            }
        } catch (IOException ignored) {
        }
        return keys;
    }

    private static int writeExport(CommandContext<CommandSourceStack> context, List<String> ids, String fileName) {
        if (ids.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.translatable("command.elemenix.export.empty"), false);
            return 0;
        }

        ids.sort(Comparator.naturalOrder());

        StringBuilder builder = new StringBuilder("[\n");
        for (int i = 0; i < ids.size(); i++) {
            builder.append("  {\n")
                    .append("    \"id\": \"").append(ids.get(i)).append("\",\n")
                    .append("    \"required\": false\n")
                    .append("  }");
            if (i < ids.size() - 1) {
                builder.append(",");
            }
            builder.append("\n");
        }
        builder.append("]\n");

        Path dir = FMLPaths.GAMEDIR.get().resolve("elemenicsexport");
        Path output = dir.resolve(fileName);
        try {
            Files.createDirectories(dir);
            Files.writeString(output, builder.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            context.getSource().sendFailure(Component.translatable("command.elemenix.export.failed", e.getMessage()));
            return 0;
        }

        context.getSource().sendSuccess(
                () -> Component.translatable("command.elemenix.export.success", ids.size(), output.toString()),
                false
        );
        return ids.size();
    }

    private static int writeTagListExport(CommandContext<CommandSourceStack> context, List<String> ids, String fileName) {
        if (ids.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.translatable("command.elemenix.export.tag_empty"), false);
            return 0;
        }

        StringBuilder builder = new StringBuilder("[\n");
        for (int i = 0; i < ids.size(); i++) {
            builder.append("  \"").append(ids.get(i)).append("\"");
            if (i < ids.size() - 1) {
                builder.append(",");
            }
            builder.append("\n");
        }
        builder.append("]\n");

        Path dir = FMLPaths.GAMEDIR.get().resolve("elemenicsexport");
        Path output = dir.resolve(fileName);
        try {
            Files.createDirectories(dir);
            Files.writeString(output, builder.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            context.getSource().sendFailure(Component.translatable("command.elemenix.export.failed", e.getMessage()));
            return 0;
        }

        context.getSource().sendSuccess(
                () -> Component.translatable("command.elemenix.export.tag_success", ids.size(), output.toString()),
                false
        );
        return ids.size();
    }
}
