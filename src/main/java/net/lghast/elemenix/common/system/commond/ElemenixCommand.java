package net.lghast.elemenix.common.system.commond;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.lghast.elemenix.register.system.ModTags;
import net.lghast.elemenix.utils.Constituents;
import net.lghast.elemenix.utils.ModUtils;
import net.lghast.elemenix.utils.elemenix.ElemenixInfo;
import net.lghast.elemenix.utils.elemenix.FluidElemenixInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforgespi.language.IModInfo;

import java.util.*;
import java.util.stream.Collectors;

@EventBusSubscriber()
@SuppressWarnings("unused")
public class ElemenixCommand {
    private static final int PAGE_SIZE = 50;

    private static final SuggestionProvider<CommandSourceStack> MOD_ID_SUGGESTIONS = (context, builder) -> {
        List<String> modIds = net.neoforged.fml.ModList.get().getMods().stream()
                .map(IModInfo::getModId)
                .collect(Collectors.toList());
        return SharedSuggestionProvider.suggest(modIds, builder);
    };

    private static final SuggestionProvider<CommandSourceStack> ITEM_ID_SUGGESTIONS = (context, builder) -> {
        List<String> ids = BuiltInRegistries.ITEM.keySet().stream()
                .map(ResourceLocation::toString)
                .collect(Collectors.toList());
        return SharedSuggestionProvider.suggest(ids, builder);
    };

    private static final SuggestionProvider<CommandSourceStack> FLUID_ID_SUGGESTIONS = (context, builder) -> {
        List<String> ids = BuiltInRegistries.FLUID.keySet().stream()
                .map(ResourceLocation::toString)
                .collect(Collectors.toList());
        return SharedSuggestionProvider.suggest(ids, builder);
    };

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        LiteralArgumentBuilder<CommandSourceStack> itemCommand = Commands.literal("item")
                .then(Commands.literal("show")
                        .then(Commands.argument("item", StringArgumentType.greedyString())
                                .suggests(ITEM_ID_SUGGESTIONS)
                                .executes(ElemenixCommand::showItem)))
                .then(Commands.literal("all")
                        .executes(ctx -> listItems(ctx, null, 1))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(ctx -> listItems(ctx, null, IntegerArgumentType.getInteger(ctx, "page"))))
                        .then(Commands.argument("modid", StringArgumentType.string())
                                .suggests(MOD_ID_SUGGESTIONS)
                                .executes(ctx -> listItems(ctx, StringArgumentType.getString(ctx, "modid"), 1))
                                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                        .executes(ctx -> listItems(ctx, StringArgumentType.getString(ctx, "modid"),
                                                IntegerArgumentType.getInteger(ctx, "page"))))))
                .then(Commands.literal("unanalysable")
                        .executes(ctx -> listUnanalysableItems(ctx, null, 1))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(ctx -> listUnanalysableItems(ctx, null, IntegerArgumentType.getInteger(ctx, "page"))))
                        .then(Commands.argument("modid", StringArgumentType.string())
                                .suggests(MOD_ID_SUGGESTIONS)
                                .executes(ctx -> listUnanalysableItems(ctx, StringArgumentType.getString(ctx, "modid"), 1))
                                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                        .executes(ctx -> listUnanalysableItems(ctx, StringArgumentType.getString(ctx, "modid"),
                                                IntegerArgumentType.getInteger(ctx, "page"))))));
        LiteralArgumentBuilder<CommandSourceStack> fluidCommand = Commands.literal("fluid")
                .then(Commands.literal("show")
                        .then(Commands.argument("fluid", StringArgumentType.greedyString())
                                .suggests(FLUID_ID_SUGGESTIONS)
                                .executes(ElemenixCommand::showFluid)))
                .then(Commands.literal("all")
                        .executes(ctx -> listFluids(ctx, null, 1))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(ctx -> listFluids(ctx, null, IntegerArgumentType.getInteger(ctx, "page"))))
                        .then(Commands.argument("modid", StringArgumentType.string())
                                .suggests(MOD_ID_SUGGESTIONS)
                                .executes(ctx -> listFluids(ctx, StringArgumentType.getString(ctx, "modid"), 1))
                                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                        .executes(ctx -> listFluids(ctx, StringArgumentType.getString(ctx, "modid"),
                                                IntegerArgumentType.getInteger(ctx, "page"))))))
                .then(Commands.literal("unanalysable")
                        .executes(ctx -> listUnanalysableFluids(ctx, null, 1))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(ctx -> listUnanalysableFluids(ctx, null, IntegerArgumentType.getInteger(ctx, "page"))))
                        .then(Commands.argument("modid", StringArgumentType.string())
                                .suggests(MOD_ID_SUGGESTIONS)
                                .executes(ctx -> listUnanalysableFluids(ctx, StringArgumentType.getString(ctx, "modid"), 1))
                                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                        .executes(ctx -> listUnanalysableFluids(ctx, StringArgumentType.getString(ctx, "modid"),
                                                IntegerArgumentType.getInteger(ctx, "page"))))));
        dispatcher.register(Commands.literal("elemenix")
                .requires(source -> source.hasPermission(0))
                .then(itemCommand)
                .then(fluidCommand));
    }

    private static int showItem(CommandContext<CommandSourceStack> context) {
        String itemId = StringArgumentType.getString(context, "item");
        Item item = ModUtils.getItemFromString(itemId);

        if (item == null) {
            context.getSource().sendFailure(Component.translatable("command.elemenix.item.show.invalid", itemId));
            return 0;
        }

        Constituents constituents = ElemenixInfo.getConstituents(item);
        String itemName = item.getDescription().getString();

        context.getSource().sendSuccess(
                () -> Component.translatable("command.elemenix.item.show.result", itemName, constituents.toString()),
                false
        );
        return 1;
    }

    private static int showFluid(CommandContext<CommandSourceStack> context) {
        String fluidId = StringArgumentType.getString(context, "fluid");
        if (fluidId.startsWith("&")) {
            fluidId = fluidId.substring(1);
        }

        Fluid fluid;
        try {
            fluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(fluidId));
        } catch (Exception e) {
            fluid = null;
        }
        if (fluid == null) {
            context.getSource().sendFailure(Component.translatable("command.elemenix.fluid.show.invalid", fluidId));
            return 0;
        }

        Constituents mapped = FluidElemenixInfo.getMappedConstituents(fluid);
        Constituents display = (mapped == null) ? new Constituents(true) : mapped;
        String fluidName = getFluidDisplayName(fluid);

        context.getSource().sendSuccess(
                () -> Component.translatable("command.elemenix.fluid.show.result", fluidName, display.toString()),
                false
        );
        return 1;
    }

    private static int listItems(CommandContext<CommandSourceStack> context, String modId, int page) {
        List<Item> items = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR) continue;
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (modId != null && !id.getNamespace().equals(modId)) continue;
            items.add(item);
        }

        items.sort(Comparator.comparing(item -> BuiltInRegistries.ITEM.getKey(item).toString()));
        if (items.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.translatable("command.elemenix.item.all.empty"), false);
            return 1;
        }

        int total = items.size();
        int totalPages = (total + PAGE_SIZE - 1) / PAGE_SIZE;
        if (page > totalPages) {
            context.getSource().sendFailure(Component.translatable("command.elemenix.item.all.page_out_of_range", totalPages));
            return 0;
        }

        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, total);
        List<Item> pageItems = items.subList(start, end);
        Map<String, List<Item>> grouped = pageItems.stream()
                .collect(Collectors.groupingBy(
                        item -> BuiltInRegistries.ITEM.getKey(item).getNamespace(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        context.getSource().sendSuccess(() -> Component.translatable("command.elemenix.item.all.header", total), false);

        for (Map.Entry<String, List<Item>> entry : grouped.entrySet()) {
            String namespace = entry.getKey();
            context.getSource().sendSuccess(() -> Component.literal(namespace + "："), false);
            for (Item item : entry.getValue()) {
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
                String itemName = item.getDescription().getString();
                String constituentsStr = ElemenixInfo.getConstituents(item).toString();
                context.getSource().sendSuccess(
                        () -> Component.translatable("command.elemenix.item.all.entry",
                                itemName, id.getPath(), constituentsStr),
                        false);
            }
        }

        context.getSource().sendSuccess(
                () -> Component.translatable("command.elemenix.item.all.page_info", page, totalPages), false);

        if (page < totalPages) {
            String nextCommand = modId == null
                    ? "/elemenix item all " + (page + 1)
                    : "/elemenix item all " + modId + " " + (page + 1);
            Component hint = Component.translatable("command.elemenix.item.all.next_page_hint")
                    .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, nextCommand)))
                    .withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.ITALIC);
            context.getSource().sendSuccess(() -> hint, false);
        }
        return 1;
    }

    private static int listUnanalysableItems(CommandContext<CommandSourceStack> context, String modId, int page) {
        List<Item> items = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR) continue;
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (modId != null && !id.getNamespace().equals(modId)) continue;
            if (ElemenixInfo.isUnanalysable(item) && !isStrictlyUnanalysable(item)) {
                items.add(item);
            }
        }

        items.sort(Comparator.comparing(item -> BuiltInRegistries.ITEM.getKey(item).toString()));
        if (items.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.translatable("command.elemenix.item.unanalysable.empty"), false);
            return 1;
        }

        int total = items.size();
        int totalPages = (total + PAGE_SIZE - 1) / PAGE_SIZE;
        if (page > totalPages) {
            context.getSource().sendFailure(
                    Component.translatable("command.elemenix.item.unanalysable.page_out_of_range", totalPages));
            return 0;
        }

        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, total);
        List<Item> pageItems = items.subList(start, end);
        Map<String, List<Item>> grouped = pageItems.stream()
                .collect(Collectors.groupingBy(
                        item -> BuiltInRegistries.ITEM.getKey(item).getNamespace(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
        context.getSource().sendSuccess(
                () -> Component.translatable("command.elemenix.item.unanalysable.header", total), false);

        for (Map.Entry<String, List<Item>> entry : grouped.entrySet()) {
            String namespace = entry.getKey();
            context.getSource().sendSuccess(() -> Component.literal(namespace + "："), false);
            for (Item item : entry.getValue()) {
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
                String itemName = item.getDescription().getString();
                context.getSource().sendSuccess(
                        () -> Component.translatable("command.elemenix.item.unanalysable.entry",
                                itemName, id.getPath()),
                        false);
            }
        }
        context.getSource().sendSuccess(
                () -> Component.translatable("command.elemenix.item.unanalysable.page_info", page, totalPages), false);

        if (page < totalPages) {
            String nextCommand = modId == null
                    ? "/elemenix item unanalysable " + (page + 1)
                    : "/elemenix item unanalysable " + modId + " " + (page + 1);
            Component hint = Component.translatable("command.elemenix.item.unanalysable.next_page_hint")
                    .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, nextCommand)))
                    .withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.ITALIC);
            context.getSource().sendSuccess(() -> hint, false);
        }
        return 1;
    }

    private static int listFluids(CommandContext<CommandSourceStack> context, String modId, int page) {
        List<Fluid> fluids = new ArrayList<>();
        for (Fluid fluid : BuiltInRegistries.FLUID) {
            if (fluid == Fluids.EMPTY) continue;
            ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
            if (modId != null && !id.getNamespace().equals(modId)) continue;
            fluids.add(fluid);
        }

        fluids.sort(Comparator.comparing(fluid -> BuiltInRegistries.FLUID.getKey(fluid).toString()));
        if (fluids.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.translatable("command.elemenix.fluid.all.empty"), false);
            return 1;
        }

        int total = fluids.size();
        int totalPages = (total + PAGE_SIZE - 1) / PAGE_SIZE;
        if (page > totalPages) {
            context.getSource().sendFailure(
                    Component.translatable("command.elemenix.fluid.all.page_out_of_range", totalPages));
            return 0;
        }

        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, total);
        List<Fluid> pageFluids = fluids.subList(start, end);
        Map<String, List<Fluid>> grouped = pageFluids.stream()
                .collect(Collectors.groupingBy(
                        fluid -> BuiltInRegistries.FLUID.getKey(fluid).getNamespace(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        context.getSource().sendSuccess(() -> Component.translatable("command.elemenix.fluid.all.header", total), false);
        for (Map.Entry<String, List<Fluid>> entry : grouped.entrySet()) {
            String namespace = entry.getKey();
            context.getSource().sendSuccess(() -> Component.literal(namespace + "："), false);
            for (Fluid fluid : entry.getValue()) {
                String fluidName = getFluidDisplayName(fluid);
                String fluidPath = BuiltInRegistries.FLUID.getKey(fluid).getPath();
                Constituents mapped = FluidElemenixInfo.getMappedConstituents(fluid);
                Constituents display = (mapped == null) ? new Constituents(true) : mapped;
                context.getSource().sendSuccess(
                        () -> Component.translatable("command.elemenix.fluid.all.entry",
                                fluidName, fluidPath, display.toString()),
                        false);
            }
        }

        context.getSource().sendSuccess(
                () -> Component.translatable("command.elemenix.fluid.all.page_info", page, totalPages), false);
        if (page < totalPages) {
            String nextCommand = modId == null
                    ? "/elemenix fluid all " + (page + 1)
                    : "/elemenix fluid all " + modId + " " + (page + 1);
            Component hint = Component.translatable("command.elemenix.fluid.all.next_page_hint")
                    .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, nextCommand)))
                    .withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.ITALIC);
            context.getSource().sendSuccess(() -> hint, false);
        }
        return 1;
    }

    private static int listUnanalysableFluids(CommandContext<CommandSourceStack> context, String modId, int page) {
        List<Fluid> fluids = new ArrayList<>();
        for (Fluid fluid : BuiltInRegistries.FLUID) {
            if (fluid == Fluids.EMPTY) continue;
            ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
            if (modId != null && !id.getNamespace().equals(modId)) continue;

            Constituents mapped = FluidElemenixInfo.getMappedConstituents(fluid);
            if (mapped == null || mapped.isUnanalysable()) {
                fluids.add(fluid);
            }
        }

        fluids.sort(Comparator.comparing(fluid -> BuiltInRegistries.FLUID.getKey(fluid).toString()));
        if (fluids.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.translatable("command.elemenix.fluid.unanalysable.empty"), false);
            return 1;
        }

        int total = fluids.size();
        int totalPages = (total + PAGE_SIZE - 1) / PAGE_SIZE;
        if (page > totalPages) {
            context.getSource().sendFailure(
                    Component.translatable("command.elemenix.fluid.unanalysable.page_out_of_range", totalPages));
            return 0;
        }

        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, total);
        List<Fluid> pageFluids = fluids.subList(start, end);
        Map<String, List<Fluid>> grouped = pageFluids.stream()
                .collect(Collectors.groupingBy(
                        fluid -> BuiltInRegistries.FLUID.getKey(fluid).getNamespace(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
        context.getSource().sendSuccess(
                () -> Component.translatable("command.elemenix.fluid.unanalysable.header", total), false);
        for (Map.Entry<String, List<Fluid>> entry : grouped.entrySet()) {
            String namespace = entry.getKey();
            context.getSource().sendSuccess(() -> Component.literal(namespace + "："), false);
            for (Fluid fluid : entry.getValue()) {
                String fluidName = getFluidDisplayName(fluid);
                String fluidPath = BuiltInRegistries.FLUID.getKey(fluid).getPath();
                context.getSource().sendSuccess(
                        () -> Component.translatable("command.elemenix.fluid.unanalysable.entry",
                                fluidName, fluidPath),
                        false);
            }
        }

        context.getSource().sendSuccess(
                () -> Component.translatable("command.elemenix.fluid.unanalysable.page_info", page, totalPages), false);
        if (page < totalPages) {
            String nextCommand = modId == null
                    ? "/elemenix fluid unanalysable " + (page + 1)
                    : "/elemenix fluid unanalysable " + modId + " " + (page + 1);
            Component hint = Component.translatable("command.elemenix.fluid.unanalysable.next_page_hint")
                    .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, nextCommand)))
                    .withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.ITALIC);
            context.getSource().sendSuccess(() -> hint, false);
        }
        return 1;
    }

    private static String getFluidDisplayName(Fluid fluid) {
        try {
            Component description = fluid.getFluidType().getDescription();
            String name = description.getString();
            if (!name.isEmpty()) {
                return name;
            }
        } catch (Exception ignored) {}
        ResourceLocation key = BuiltInRegistries.FLUID.getKey(fluid);
        return key.toString();
    }

    private static boolean isStrictlyUnanalysable(Item item) {
        ItemStack stack = new ItemStack(item);
        if (stack.is(Items.AIR)) {
            return true;
        }
        if (stack.is(ModTags.STRICTLY_UNANALYSABLE)) {
            return true;
        }
        if (ElemenixInfo.isUnanalysableStrictly(item)) {
            return true;
        }
        String fullId = BuiltInRegistries.ITEM.getKey(item).toString();
        return fullId.contains("spawn_egg");
    }
}
