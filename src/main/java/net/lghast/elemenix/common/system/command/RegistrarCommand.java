package net.lghast.elemenix.common.system.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.lghast.elemenix.utils.misc.RegistrationStageNotifier;
import net.lghast.elemenix.common.content.blockentity.MemoryRegistrarBlockEntity;
import net.lghast.elemenix.common.system.attachment.RegistrarCollection;
import net.lghast.elemenix.common.system.datacomponent.PotionMemoryData;
import net.lghast.elemenix.register.system.ModAttachments;
import net.lghast.elemenix.register.system.ModTags;
import net.lghast.elemenix.utils.elemenix.ElemenixInfo;
import net.lghast.elemenix.utils.misc.ModUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@EventBusSubscriber()
@SuppressWarnings("unused")
public class RegistrarCommand {
    private static final int PAGE_SIZE = 50;
    private static final ResourceLocation WATER_POTION_ID = ResourceLocation.parse("minecraft:water");
    private static final String ARG_TARGET = "target";
    private static final String ARG_ITEM = "item";
    private static final String ARG_POTION = "potion";
    private static final String ARG_PAGE = "page";

    private record Entry(String name, String id) {}

    private static final SuggestionProvider<CommandSourceStack> ITEM_ID_SUGGESTIONS = (context, builder) -> {
        List<String> ids = BuiltInRegistries.ITEM.keySet().stream()
                .map(ResourceLocation::toString)
                .collect(Collectors.toList());
        return SharedSuggestionProvider.suggest(ids, builder);
    };

    private static final SuggestionProvider<CommandSourceStack> POTION_ID_SUGGESTIONS = (context, builder) -> {
        List<String> ids = BuiltInRegistries.POTION.keySet().stream()
                .map(ResourceLocation::toString)
                .collect(Collectors.toList());
        return SharedSuggestionProvider.suggest(ids, builder);
    };

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("registrar")
                .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument(ARG_TARGET, EntityArgument.player())
                        .then(addCommand())
                        .then(removeCommand())
                        .then(listCommand()))
                .then(addCommand())
                .then(removeCommand())
                .then(listCommand()));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> addCommand() {
        return Commands.literal("add")
                .then(Commands.literal("all")
                        .executes(ctx -> addAll(ctx, getTarget(ctx))))
                .then(Commands.argument(ARG_ITEM, StringArgumentType.string())
                        .suggests(ITEM_ID_SUGGESTIONS)
                        .executes(ctx -> addItem(ctx, getTarget(ctx), null))
                        .then(Commands.argument(ARG_POTION, StringArgumentType.string())
                                .suggests(POTION_ID_SUGGESTIONS)
                                .executes(ctx -> addItem(ctx, getTarget(ctx),
                                        StringArgumentType.getString(ctx, ARG_POTION)))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> removeCommand() {
        return Commands.literal("remove")
                .then(Commands.literal("all")
                        .executes(ctx -> removeAll(ctx, getTarget(ctx))))
                .then(Commands.argument(ARG_ITEM, StringArgumentType.string())
                        .suggests(ITEM_ID_SUGGESTIONS)
                        .executes(ctx -> removeItem(ctx, getTarget(ctx), null))
                        .then(Commands.argument(ARG_POTION, StringArgumentType.string())
                                .suggests(POTION_ID_SUGGESTIONS)
                                .executes(ctx -> removeItem(ctx, getTarget(ctx),
                                        StringArgumentType.getString(ctx, ARG_POTION)))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> listCommand() {
        return Commands.literal("list")
                .executes(ctx -> listRegistrations(ctx, getTarget(ctx), 1))
                .then(Commands.argument(ARG_PAGE, IntegerArgumentType.integer(1))
                        .executes(ctx -> listRegistrations(ctx, getTarget(ctx),
                                IntegerArgumentType.getInteger(ctx, ARG_PAGE))));
    }

    @Nullable
    private static ServerPlayer getTarget(CommandContext<CommandSourceStack> ctx) {
        try {
            return EntityArgument.getPlayer(ctx, ARG_TARGET);
        } catch (IllegalArgumentException e) {
            return ctx.getSource().getPlayer();
        } catch (CommandSyntaxException e) {
            return null;
        }
    }

    private static int addItem(CommandContext<CommandSourceStack> ctx, ServerPlayer target, String potionArg) {
        if (target == null) {
            ctx.getSource().sendFailure(Component.translatable("command.elemenix.registrar.player_only"));
            return 0;
        }

        String itemIdStr = StringArgumentType.getString(ctx, ARG_ITEM);
        Item item = ModUtils.getItemFromString(itemIdStr);
        if (item == null || item == Items.AIR) {
            ctx.getSource().sendFailure(Component.translatable("command.elemenix.registrar.invalid_item", itemIdStr));
            return 0;
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);

        RegistrarCollection collection = target.getData(ModAttachments.REGISTRAR_COLLECTION);

        if (potionArg == null) {
            target.setData(ModAttachments.REGISTRAR_COLLECTION, collection.withItem(itemId));
            RegistrationStageNotifier.check(target);
            ctx.getSource().sendSuccess(
                    () -> Component.translatable("command.elemenix.registrar.add.success", itemId.toString()), false);
            return 1;
        }

        if (!new ItemStack(item).is(ModTags.POTIONLIKE)) {
            ctx.getSource().sendFailure(Component.translatable("command.elemenix.registrar.not_potionlike", itemIdStr));
            return 0;
        }

        ResourceLocation potionId;
        try {
            potionId = ResourceLocation.parse(potionArg);
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.translatable("command.elemenix.registrar.invalid_potion", potionArg));
            return 0;
        }

        Optional<Holder.Reference<Potion>> potion = BuiltInRegistries.POTION
                .getHolder(ResourceKey.create(Registries.POTION, potionId));
        if (potion.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable("command.elemenix.registrar.invalid_potion", potionArg));
            return 0;
        }

        PotionMemoryData.Entry entry = new PotionMemoryData.Entry(itemId, potionId);
        target.setData(ModAttachments.REGISTRAR_COLLECTION, collection.withPotion(entry));
        RegistrationStageNotifier.check(target);
        ctx.getSource().sendSuccess(
                () -> Component.translatable("command.elemenix.registrar.add.success", itemId + " " + potionId), false);
        return 1;
    }

    private static int addAll(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        if (target == null) {
            ctx.getSource().sendFailure(Component.translatable("command.elemenix.registrar.player_only"));
            return 0;
        }

        RegistrarCollection collection = target.getData(ModAttachments.REGISTRAR_COLLECTION);
        int before = collection.items().size() + collection.potions().size();
        Set<ResourceLocation> allItems = new HashSet<>(collection.items());
        Set<PotionMemoryData.Entry> allPotions = new HashSet<>(collection.potions());
        for (Item item : BuiltInRegistries.ITEM) {
            if (shouldSkip(item)) continue;
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
            if (new ItemStack(item).is(ModTags.POTIONLIKE)) {
                allItems.remove(itemId);
                allPotions.add(new PotionMemoryData.Entry(itemId, WATER_POTION_ID));
            } else {
                allItems.add(itemId);
            }
        }
        int added = Math.max(0, allItems.size() + allPotions.size() - before);
        target.setData(ModAttachments.REGISTRAR_COLLECTION, new RegistrarCollection(allItems, allPotions));
        RegistrationStageNotifier.check(target);
        ctx.getSource().sendSuccess(
                () -> Component.translatable("command.elemenix.registrar.add.all.success", added), false);
        return 1;
    }

    private static boolean shouldSkip(Item item) {
        if (item == Items.AIR) return true;
        if (MemoryRegistrarBlockEntity.isBuiltInMemoryItem(item)) return false;
        ItemStack stack = new ItemStack(item);
        if (stack.is(ModTags.ANALYZER_UNRECORDABLE)) return true;
        if (stack.is(ModTags.REGISTRAR_HIDDEN)) return true;
        return ElemenixInfo.isUnanalysable(item);
    }

    private static int removeItem(CommandContext<CommandSourceStack> ctx, ServerPlayer target, String potionArg) {
        if (target == null) {
            ctx.getSource().sendFailure(Component.translatable("command.elemenix.registrar.player_only"));
            return 0;
        }

        String itemIdStr = StringArgumentType.getString(ctx, ARG_ITEM);
        Item item = ModUtils.getItemFromString(itemIdStr);
        if (item == null || item == Items.AIR) {
            ctx.getSource().sendFailure(Component.translatable("command.elemenix.registrar.invalid_item", itemIdStr));
            return 0;
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);

        RegistrarCollection collection = target.getData(ModAttachments.REGISTRAR_COLLECTION);

        if (potionArg == null) {
            if (!collection.containsItem(itemId)) {
                ctx.getSource().sendFailure(Component.translatable("command.elemenix.registrar.remove.missing", itemId.toString()));
                return 0;
            }
            Set<ResourceLocation> newItems = new HashSet<>(collection.items());
            newItems.remove(itemId);
            target.setData(ModAttachments.REGISTRAR_COLLECTION, new RegistrarCollection(newItems, collection.potions()));
            ctx.getSource().sendSuccess(
                    () -> Component.translatable("command.elemenix.registrar.remove.success", itemId.toString()), false);
            return 1;
        }

        ResourceLocation potionId;
        try {
            potionId = ResourceLocation.parse(potionArg);
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.translatable("command.elemenix.registrar.invalid_potion", potionArg));
            return 0;
        }

        PotionMemoryData.Entry entry = new PotionMemoryData.Entry(itemId, potionId);
        if (!collection.containsPotion(entry)) {
            ctx.getSource().sendFailure(Component.translatable("command.elemenix.registrar.remove.missing", itemId + " " + potionId));
            return 0;
        }
        Set<PotionMemoryData.Entry> newPotions = new HashSet<>(collection.potions());
        newPotions.remove(entry);
        target.setData(ModAttachments.REGISTRAR_COLLECTION, new RegistrarCollection(collection.items(), newPotions));
        ctx.getSource().sendSuccess(
                () -> Component.translatable("command.elemenix.registrar.remove.success", itemId + " " + potionId), false);
        return 1;
    }

    private static int removeAll(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        if (target == null) {
            ctx.getSource().sendFailure(Component.translatable("command.elemenix.registrar.player_only"));
            return 0;
        }

        target.setData(ModAttachments.REGISTRAR_COLLECTION, RegistrarCollection.empty());
        ctx.getSource().sendSuccess(
                () -> Component.translatable("command.elemenix.registrar.remove.all.success"), false);
        return 1;
    }

    private static int listRegistrations(CommandContext<CommandSourceStack> ctx, ServerPlayer target, int page) {
        if (target == null) {
            ctx.getSource().sendFailure(Component.translatable("command.elemenix.registrar.player_only"));
            return 0;
        }

        RegistrarCollection collection = target.getData(ModAttachments.REGISTRAR_COLLECTION);
        List<Entry> entries = new ArrayList<>();
        for (ResourceLocation itemId : collection.items()) {
            entries.add(new Entry(getItemDisplayName(itemId), itemId.toString()));
        }
        for (PotionMemoryData.Entry potion : collection.potions()) {
            String id = potion.itemId() + " " + potion.potionId();
            entries.add(new Entry(getPotionDisplayName(potion), id));
        }
        entries.sort(Comparator.comparing(Entry::id));

        if (entries.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.translatable("command.elemenix.registrar.list.empty"), false);
            return 1;
        }

        int total = entries.size();
        int totalPages = (total + PAGE_SIZE - 1) / PAGE_SIZE;
        if (page > totalPages) {
            ctx.getSource().sendFailure(Component.translatable("command.elemenix.registrar.list.page_out_of_range", totalPages));
            return 0;
        }

        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, total);
        List<Entry> pageEntries = entries.subList(start, end);

        ctx.getSource().sendSuccess(
                () -> Component.translatable("command.elemenix.registrar.list.header", total), false);
        for (Entry entry : pageEntries) {
            ctx.getSource().sendSuccess(
                    () -> Component.translatable("command.elemenix.registrar.list.entry", entry.name(), entry.id()), false);
        }
        ctx.getSource().sendSuccess(
                () -> Component.translatable("command.elemenix.registrar.list.page_info", page, totalPages), false);

        if (page < totalPages) {
            String nextCommand = "/registrar " + target.getGameProfile().getName() + " list " + (page + 1);
            Component hint = Component.translatable("command.elemenix.registrar.list.next_page_hint")
                    .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, nextCommand)))
                    .withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.ITALIC);
            ctx.getSource().sendSuccess(() -> hint, false);
        }
        return 1;
    }

    private static String getItemDisplayName(ResourceLocation id) {
        try {
            Item item = BuiltInRegistries.ITEM.get(id);
            if (item != Items.AIR) {
                return item.getDescription().getString();
            }
        } catch (Exception ignored) {
        }
        return id.toString();
    }

    private static String getPotionDisplayName(PotionMemoryData.Entry entry) {
        try {
            Item item = BuiltInRegistries.ITEM.get(entry.itemId());
            Optional<Holder.Reference<Potion>> potion = BuiltInRegistries.POTION
                    .getHolder(ResourceKey.create(Registries.POTION, entry.potionId()));
            if (item != Items.AIR && potion.isPresent()) {
                ItemStack stack = PotionContents.createItemStack(item, potion.get());
                String name = stack.getHoverName().getString();
                if (!name.isEmpty()) {
                    return name;
                }
            }
        } catch (Exception ignored) {
        }
        return entry.itemId() + " " + entry.potionId();
    }
}
