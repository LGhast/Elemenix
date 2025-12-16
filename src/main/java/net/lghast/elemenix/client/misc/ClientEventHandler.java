package net.lghast.elemenix.client.misc;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.client.screen.*;
import net.lghast.elemenix.conifig.ClientConfig;
import net.lghast.elemenix.network.*;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.register.system.ModMenus;
import net.lghast.elemenix.utils.Constituents;
import net.lghast.elemenix.utils.ElemenixInfo;
import net.lghast.elemenix.utils.ModUtils;
import net.lghast.elemenix.utils.RecipeHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.List;
import java.util.Objects;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = Elemenics.MOD_ID)
public class ClientEventHandler {
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        if(ClientConfig.SHOW_CONSTITUENT_TOOLTIPS.get() && !stack.is(ModItems.ELEMENIC_ANALYZER)) {
            Constituents constituents = ElemenixInfo.getConstituents(stack);

            if(constituents == null) return;
            if(constituents.isUnanalysable() && !ClientConfig.SHOW_UNANALYSABLE_TOOLTIPS.get()) return;

            List<Component> tooltip = event.getToolTip();
            int insertIndex = findInsertIndex(tooltip);

            tooltip.add(insertIndex, Component.translatable("tooltip.elemenix.constituents").withStyle(ChatFormatting.GRAY));
            insertIndex++;

            tooltip.add(insertIndex, constituents.toComponentFormer());

            if (!constituents.isUnanalysable()) {
                insertIndex++;
                tooltip.add(insertIndex, constituents.toComponentLatter());
            }
        }
    }

    private static int findInsertIndex(List<Component> tooltip) {
        for (int i = 0; i < tooltip.size(); i++) {
            Component line = tooltip.get(i);
            if(line == null) continue;

            String content = line.getString();

            if (content.trim().isEmpty() || content.contains(Component.translatable("tooltip.elemenix.constituents").getString())) {
                continue;
            }

            if (content.contains(Component.translatable("item.durability").getString())) {
                return i;
            }
        }

        return Math.max(1, tooltip.size());
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.ELEMENIX_ANALYZER_MENU.get(), AnalyzerScreen::new);
        event.register(ModMenus.MEMORIZER_BOX_MENU.get(), MemorizerBoxScreen::new);
        event.register(ModMenus.BURNER_MENU.get(), BurnerScreen::new);
        event.register(ModMenus.TRANSFORMER_MENU.get(), TransformerScreen::new);
        event.register(ModMenus.INFUSER_MENU.get(), InfuserScreen::new);
        event.register(ModMenus.ENRICHER_MENU.get(), EnricherScreen::new);
        event.register(ModMenus.EJECTOR_MENU.get(), EjectorScreen::new);
    }

    @SubscribeEvent
    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1.0");

        registrar.playToServer(DeconstructionPayload.TYPE, DeconstructionPayload.STREAM_CODEC, DeconstructionPayload::handle)
                .playToServer(ReconstructionPayload.TYPE, ReconstructionPayload.STREAM_CODEC, ReconstructionPayload::handle);

        registrar.playToServer(OpenMemorizerBoxPayload.TYPE, OpenMemorizerBoxPayload.STREAM_CODEC, OpenMemorizerBoxPayload::handle);

        registrar.playToServer(BurnerMovePayload.TYPE, BurnerMovePayload.STREAM_CODEC, BurnerMovePayload::handle)
                .playToServer(BurnerDeletePayload.TYPE, BurnerDeletePayload.STREAM_CODEC, BurnerDeletePayload::handle)
                .playToServer(BurnerSwapPayload.TYPE, BurnerSwapPayload.STREAM_CODEC, BurnerSwapPayload::handle)
                .playToServer(BurnerActionPayload.TYPE, BurnerActionPayload.STREAM_CODEC, BurnerActionPayload::handle)
                .playToServer(BurnerInsertBeforePayload.TYPE, BurnerInsertBeforePayload.STREAM_CODEC, BurnerInsertBeforePayload::handle);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.level == null) return;

        while (ModKeyBindings.OPEN_MEMORIZER_BOX.consumeClick()) {
            if (!ModUtils.findItemInPlayerInventory(player, ModItems.MEMORIZER_BOX.asItem()).isEmpty()) {
                Objects.requireNonNull(minecraft.getConnection()).send(new OpenMemorizerBoxPayload());
            }
        }
    }

    @SubscribeEvent
    public static void onRecipesUpdated(RecipesUpdatedEvent event) {
        ElemenixInfo.clearCaches();
    }
}
