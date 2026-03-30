package net.lghast.elemenix.common.system;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.register.system.ModTags;
import net.lghast.elemenix.utils.Constituents;
import net.lghast.elemenix.utils.ModUtils;
import net.lghast.elemenix.utils.elemenix.ElemenixInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforgespi.language.IModInfo;

import java.util.*;
import java.util.stream.Collectors;

@EventBusSubscriber()
@SuppressWarnings("unused")
public class ElemenicsCommand {
    private static final int PAGE_SIZE = 50;

    private static final SuggestionProvider<CommandSourceStack> MOD_ID_SUGGESTIONS = (context, builder) -> {
        List<String> modIds = net.neoforged.fml.ModList.get().getMods().stream()
                .map(IModInfo::getModId)
                .collect(Collectors.toList());
        return SharedSuggestionProvider.suggest(modIds, builder);
    };

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("elemenix")
                .requires(source -> source.hasPermission(0))
                .then(Commands.literal("show")
                        .then(Commands.argument("item", StringArgumentType.greedyString())
                                .suggests((context, builder) -> {
                                    List<String> items = BuiltInRegistries.ITEM.keySet().stream()
                                            .map(ResourceLocation::toString)
                                            .collect(Collectors.toList());
                                    return SharedSuggestionProvider.suggest(items, builder);
                                })
                                .executes(ElemenicsCommand::showItem)))
                .then(Commands.literal("unanalysable")
                        .then(Commands.literal("all")
                                .executes(ctx -> listUnanalysable(ctx, "all", null, 1))
                                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                        .executes(ctx -> listUnanalysable(ctx, "all", null,
                                                IntegerArgumentType.getInteger(ctx, "page")))))
                        .then(Commands.literal("all_strict")
                                .executes(ctx -> listUnanalysable(ctx, "all_strict", null, 1))
                                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                        .executes(ctx -> listUnanalysable(ctx, "all_strict", null,
                                                IntegerArgumentType.getInteger(ctx, "page")))))
                        .then(Commands.literal("only")
                                .then(Commands.argument("mod", StringArgumentType.string())
                                        .suggests(MOD_ID_SUGGESTIONS)
                                        .executes(ctx -> listUnanalysable(ctx, "only",
                                                StringArgumentType.getString(ctx, "mod"), 1))
                                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                                .executes(ctx -> listUnanalysable(ctx, "only",
                                                        StringArgumentType.getString(ctx, "mod"),
                                                        IntegerArgumentType.getInteger(ctx, "page"))))))
                        .then(Commands.literal("only_strict")
                                .then(Commands.argument("mod", StringArgumentType.string())
                                        .suggests(MOD_ID_SUGGESTIONS)
                                        .executes(ctx -> listUnanalysable(ctx, "only_strict",
                                                StringArgumentType.getString(ctx, "mod"), 1))
                                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                                .executes(ctx -> listUnanalysable(ctx, "only_strict",
                                                        StringArgumentType.getString(ctx, "mod"),
                                                        IntegerArgumentType.getInteger(ctx, "page")))))));

        dispatcher.register(command);
    }

    private static int showItem(CommandContext<CommandSourceStack> context) {
        String itemId = StringArgumentType.getString(context, "item");
        Item item = ModUtils.getItemFromString(itemId);
        if (item == null) {
            context.getSource().sendFailure(Component.translatable("command.elemenix.show.invalid_item", itemId));
            return 0;
        }

        Constituents constituents = ElemenixInfo.getConstituents(item);
        String constituentsStr = constituents.toString();
        String itemName = item.getDescription().getString();

        context.getSource().sendSuccess(
                () -> Component.translatable("command.elemenix.show.result", itemName, constituentsStr),
                false
        );
        return 1;
    }

    private static int listUnanalysable(CommandContext<CommandSourceStack> context, String mode, String modId, int page) {
        List<Item> filtered = filterUnanalysable(mode, modId);

        if (filtered.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.translatable("command.elemenix.unanalysable.empty"), false);
            return 1;
        }

        int total = filtered.size();
        int totalPages = (total + PAGE_SIZE - 1) / PAGE_SIZE;

        if (page > totalPages) {
            context.getSource().sendFailure(Component.translatable("command.elemenix.unanalysable.page_out_of_range", totalPages));
            return 0;
        }

        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, total);
        List<Item> pageItems = filtered.subList(start, end);

        Map<String, List<Item>> grouped = pageItems.stream()
                .collect(Collectors.groupingBy(
                        item -> BuiltInRegistries.ITEM.getKey(item).getNamespace(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        context.getSource().sendSuccess(() -> Component.translatable("command.elemenix.unanalysable.header", total), false);

        for (Map.Entry<String, List<Item>> entry : grouped.entrySet()) {
            String namespace = entry.getKey();
            List<Item> items = entry.getValue();
            context.getSource().sendSuccess(() -> Component.literal(namespace + "："), false);
            for (Item item : items) {
                ResourceLocation regName = BuiltInRegistries.ITEM.getKey(item);
                String itemName = item.getDescription().getString();
                String idWithoutNamespace = regName.getPath();
                context.getSource().sendSuccess(() -> Component.translatable("command.elemenix.unanalysable.entry", itemName, idWithoutNamespace), false);
            }
        }

        context.getSource().sendSuccess(() -> Component.translatable("command.elemenix.unanalysable.page_info", page, totalPages), false);

        if (page < totalPages) {
            Component nextPageHint = Component.translatable("command.elemenix.unanalysable.next_page_hint")
                    .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, buildNextPageCommand(context, mode, modId, page + 1))))
                    .withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.ITALIC);
            context.getSource().sendSuccess(() -> nextPageHint, false);
        }

        return 1;
    }

    private static List<Item> filterUnanalysable(String mode, String modId) {
        Set<Item> allUnanalysable = new TreeSet<>(Comparator.comparing(a -> BuiltInRegistries.ITEM.getKey(a).toString()));

        for (Item item : BuiltInRegistries.ITEM) {
            if (ElemenixInfo.isUnanalysable(item)) {
                allUnanalysable.add(item);
            }
        }

        List<Item> filtered = new ArrayList<>();
        for (Item item : allUnanalysable) {
            ResourceLocation regName = BuiltInRegistries.ITEM.getKey(item);

            if (mode.equals("all_strict") || mode.equals("only_strict")) {
                if (isStrictlyUnanalysable(item)) {
                    continue;
                }
            }

            if (mode.equals("only") || mode.equals("only_strict")) {
                if (!regName.getNamespace().equals(modId)) {
                    continue;
                }
            }

            filtered.add(item);
        }
        return filtered;
    }

    private static String buildNextPageCommand(CommandContext<CommandSourceStack> context, String mode, String modId, int nextPage) {
        StringBuilder cmd = new StringBuilder("/elemenix unanalysable ");
        cmd.append(mode);
        if (mode.equals("only") || mode.equals("only_strict")) {
            cmd.append(" ").append(modId);
        }
        cmd.append(" ").append(nextPage);
        return cmd.toString();
    }

    private static boolean isStrictlyUnanalysable(Item item){
        ItemStack stack = new ItemStack(item);
        if(stack.is(Items.AIR)){
            return true;
        }

        if(stack.is(ModTags.STRICTLY_UNANALYSABLE)){
            return true;
        }

        String fullId = BuiltInRegistries.ITEM.getKey(item).toString();
        return fullId.contains("spawner") || fullId.contains("spawn_egg");
    }
}
