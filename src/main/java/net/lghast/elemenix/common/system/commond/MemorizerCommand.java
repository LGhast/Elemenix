package net.lghast.elemenix.common.system.commond;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.lghast.elemenix.common.content.item.MemorizerItem;
import net.lghast.elemenix.common.system.datacomponent.MemoryData;
import net.lghast.elemenix.common.system.datacomponent.Waxed;
import net.lghast.elemenix.register.system.ModDataComponents;
import net.lghast.elemenix.register.system.ModTags;
import net.lghast.elemenix.utils.ModUtils;
import net.lghast.elemenix.utils.elemenix.ElemenixInfo;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.*;
import java.util.stream.Collectors;

@EventBusSubscriber()
@SuppressWarnings("unused")
public class MemorizerCommand {
    private static final SuggestionProvider<CommandSourceStack> PLAYER_NAME_SUGGESTIONS = (context, builder) -> {
        List<String> names = context.getSource().getServer().getPlayerList().getPlayers().stream()
                .map(player -> player.getGameProfile().getName())
                .collect(Collectors.toList());
        return SharedSuggestionProvider.suggest(names, builder);
    };
    private static final SuggestionProvider<CommandSourceStack> ITEM_ID_SUGGESTIONS = (context, builder) -> {
        List<String> ids = BuiltInRegistries.ITEM.keySet().stream()
                .map(ResourceLocation::toString)
                .collect(Collectors.toList());
        return SharedSuggestionProvider.suggest(ids, builder);
    };
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("memorizer")
                .requires(source -> source.hasPermission(0))
                .then(Commands.literal("readonly")
                        .then(Commands.literal("on")
                                .executes(ctx -> readonlyOn(ctx, null))
                                .then(Commands.argument("player", StringArgumentType.string())
                                        .suggests(PLAYER_NAME_SUGGESTIONS)
                                        .executes(ctx -> readonlyOn(ctx, StringArgumentType.getString(ctx, "player")))))
                        .then(Commands.literal("off")
                                .executes(MemorizerCommand::readonlyOff)))
                .then(Commands.literal("add")
                        .then(Commands.argument("item", StringArgumentType.greedyString())
                                .suggests(ITEM_ID_SUGGESTIONS)
                                .executes(MemorizerCommand::addMemory)))
                .then(Commands.literal("remove")
                        .then(Commands.argument("item", StringArgumentType.greedyString())
                                .suggests(ITEM_ID_SUGGESTIONS)
                                .executes(MemorizerCommand::removeMemory)));
        dispatcher.register(command);
    }
    private static int readonlyOn(CommandContext<CommandSourceStack> context, String ownerArg) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.translatable("command.elemenix.memorizer.player_only"));
            return 0;
        }
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof MemorizerItem)) {
            context.getSource().sendFailure(Component.translatable("command.elemenix.memorizer.not_holding"));
            return 0;
        }
        if (ownerArg == null) {
            // 缺省：无设置者，永久只读（斧无法解除）
            stack.set(ModDataComponents.WAXED.get(), new Waxed(true, Optional.empty(), Optional.empty()));
            context.getSource().sendSuccess(
                    () -> Component.translatable("command.elemenix.memorizer.readonly.on.permanent"), false);
            return 1;
        }
        GameProfile owner = resolvePlayer(context.getSource().getServer(), ownerArg);
        if (owner == null) {
            context.getSource().sendFailure(
                    Component.translatable("command.elemenix.memorizer.readonly.on.player_not_found", ownerArg));
            return 0;
        }
        stack.set(ModDataComponents.WAXED.get(),
                new Waxed(true, Optional.of(owner.getId()), Optional.of(owner.getName())));
        context.getSource().sendSuccess(
                () -> Component.translatable("command.elemenix.memorizer.readonly.on.owner", owner.getName()), false);
        return 1;
    }
    private static GameProfile resolvePlayer(MinecraftServer server, String nameOrUuid) {
        ServerPlayer online = server.getPlayerList().getPlayerByName(nameOrUuid);
        if (online != null) {
            return online.getGameProfile();
        }
        try {
            UUID uuid = UUID.fromString(nameOrUuid);
            Optional<GameProfile> profile = Objects.requireNonNull(server.getProfileCache()).get(uuid);
            return profile.orElse(null);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    private static int readonlyOff(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.translatable("command.elemenix.memorizer.player_only"));
            return 0;
        }
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof MemorizerItem)) {
            context.getSource().sendFailure(Component.translatable("command.elemenix.memorizer.not_holding"));
            return 0;
        }
        stack.set(ModDataComponents.WAXED.get(), Waxed.unwaxed());
        context.getSource().sendSuccess(
                () -> Component.translatable("command.elemenix.memorizer.readonly.off"), false);
        return 1;
    }
    private static int addMemory(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.translatable("command.elemenix.memorizer.player_only"));
            return 0;
        }
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof MemorizerItem)) {
            context.getSource().sendFailure(Component.translatable("command.elemenix.memorizer.not_holding"));
            return 0;
        }
        String itemId = StringArgumentType.getString(context, "item");
        Item item = ModUtils.getItemFromString(itemId);
        if (item == null) {
            context.getSource().sendFailure(Component.translatable("command.elemenix.memorizer.invalid_item", itemId));
            return 0;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        MemoryData memoryData = MemorizerItem.getOrCreateMemories(stack);
        List<ResourceLocation> memories = memoryData.resolvedItems();
        if (memories.contains(id)) {
            context.getSource().sendFailure(Component.translatable("command.elemenix.memorizer.add.exists"));
            return 0;
        }
        ItemStack itemStack = new ItemStack(item);
        if (itemStack.is(ModTags.ANALYZER_UNRECORDABLE)) {
            context.getSource().sendFailure(Component.translatable("command.elemenix.memorizer.add.unmemorizable"));
            return 0;
        }
        if (ElemenixInfo.isUnanalysable(item)) {
            context.getSource().sendFailure(Component.translatable("command.elemenix.memorizer.add.unanalysable"));
            return 0;
        }
        if (memoryData.isFull()) {
            context.getSource().sendFailure(Component.translatable("command.elemenix.memorizer.add.full"));
            return 0;
        }
        // 无视只读，直接写入
        List<ResourceLocation> newMemories = new ArrayList<>(memories);
        newMemories.add(id);
        stack.set(ModDataComponents.MEMORY_DATA.get(), memoryData.withResolvedItems(newMemories));
        context.getSource().sendSuccess(
                () -> Component.translatable("command.elemenix.memorizer.add.success", id.toString()), false);
        return 1;
    }
    private static int removeMemory(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.translatable("command.elemenix.memorizer.player_only"));
            return 0;
        }
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof MemorizerItem)) {
            context.getSource().sendFailure(Component.translatable("command.elemenix.memorizer.not_holding"));
            return 0;
        }
        String itemId = StringArgumentType.getString(context, "item");
        Item item = ModUtils.getItemFromString(itemId);
        if (item == null) {
            context.getSource().sendFailure(Component.translatable("command.elemenix.memorizer.invalid_item", itemId));
            return 0;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        MemoryData memoryData = MemorizerItem.getOrCreateMemories(stack);
        List<ResourceLocation> memories = memoryData.resolvedItems();
        if (!memories.contains(id)) {
            context.getSource().sendFailure(Component.translatable("command.elemenix.memorizer.remove.missing"));
            return 0;
        }
        List<ResourceLocation> newMemories = new ArrayList<>(memories);
        newMemories.remove(id);
        stack.set(ModDataComponents.MEMORY_DATA.get(), memoryData.withResolvedItems(newMemories));
        context.getSource().sendSuccess(
                () -> Component.translatable("command.elemenix.memorizer.remove.success", id.toString()), false);
        return 1;
    }
}
