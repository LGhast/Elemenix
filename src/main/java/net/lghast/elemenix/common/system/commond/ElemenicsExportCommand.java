package net.lghast.elemenix.common.system.commond;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.lghast.elemenix.register.system.ModTags;
import net.lghast.elemenix.utils.elemenix.ElemenixInfo;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
                        .executes(ElemenicsExportCommand::exportIgnored)));
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
            if (ElemenixInfo.isUnanalysableStrictly(item)) continue;

            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (id.toString().contains("spawn_egg")) continue;

            if (stack.is(ModTags.COMMAND_IGNORED)) continue;
            if (ElemenixInfo.isUnanalysable(item)) {
                ids.add(id.toString());
            }
        }
        return writeExport(context, ids, "command_ignored.json");
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
}
