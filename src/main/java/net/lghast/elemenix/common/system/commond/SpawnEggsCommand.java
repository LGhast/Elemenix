package net.lghast.elemenix.common.system.commond;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.lghast.elemenix.register.system.ModTags;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Scans every registered item whose id contains "spawn_egg" and is not part of
 * ModTags.SPAWN_EGGS, then writes them to a JSON array file,
 * making it easier to build the spawn egg tag JSON.
 */
@EventBusSubscriber
@SuppressWarnings("unused")
public class SpawnEggsCommand {
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("spawneggs")
                .requires(source -> source.hasPermission(2))
                .executes(SpawnEggsCommand::generate));
    }

    private static int generate(CommandContext<CommandSourceStack> context) {
        List<String> ids = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (!id.toString().contains("spawn_egg")) {
                continue;
            }
            if (BuiltInRegistries.ITEM.wrapAsHolder(item).is(ModTags.SPAWN_EGGS)) {
                continue;
            }
            ids.add(id.toString());
        }

        if (ids.isEmpty()) {
            context.getSource().sendSuccess(
                    () -> Component.translatable("command.elemenix.spawneggs.empty"),
                    false
            );
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

        Path output = FMLPaths.GAMEDIR.get().resolve("spawn_eggs_output.json");
        try {
            Files.writeString(output, builder.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            context.getSource().sendFailure(Component.translatable("command.elemenix.spawneggs.failed", e.getMessage()));
            return 0;
        }

        context.getSource().sendSuccess(
                () -> Component.translatable("command.elemenix.spawneggs.success", ids.size(), output.toString()),
                false
        );
        return ids.size();
    }
}
