package net.lghast.elemenix.client.misc;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.client.screen.*;
import net.lghast.elemenix.conifig.ClientConfig;
import net.lghast.elemenix.network.*;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.register.system.ModMenus;
import net.lghast.elemenix.register.system.ModTags;
import net.lghast.elemenix.utils.Constituents;
import net.lghast.elemenix.utils.ElemenixInfo;
import net.lghast.elemenix.utils.ModUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;
import java.util.Objects;

@OnlyIn(Dist.CLIENT)
@SuppressWarnings("unused")
@EventBusSubscriber(modid = Elemenics.MOD_ID, value = Dist.CLIENT)
public class ClientEventHandler {
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        if(ClientConfig.SHOW_CONSTITUENT_TOOLTIPS_WHEN_SHIFT.get() && !Screen.hasShiftDown()) {
            return;
        }

        List<Component> tooltip = event.getToolTip();
        int insertIndex = findInsertIndex(tooltip);

        if(ClientConfig.SHOW_CONSTITUENT_TOOLTIPS.get() && !stack.is(ModItems.ELEMENIC_ANALYZER)) {
            Constituents constituents = ElemenixInfo.getConstituents(stack);

            if(constituents == null) return;
            if(constituents.isUnanalysable() && !ClientConfig.SHOW_UNANALYSABLE_TOOLTIPS.get()) return;

            tooltip.add(insertIndex, Component.translatable("tooltip.elemenix.constituents").withStyle(ChatFormatting.GRAY));
            insertIndex++;

            if(Screen.hasAltDown()){
                tooltip.add(insertIndex, constituents.toComponentFormerWhole());

                if (!constituents.isUnanalysable()) {
                    insertIndex++;
                    tooltip.add(insertIndex, constituents.toComponentLatterWhole());
                }
            }else {
                tooltip.add(insertIndex, constituents.toComponentFormer());

                if (!constituents.isUnanalysable()) {
                    insertIndex++;
                    tooltip.add(insertIndex, constituents.toComponentLatter());
                }
            }
        }

        if(stack.is(ModTags.UNDECONSTRUCTABLE)){
            insertIndex++;
            tooltip.add(insertIndex,Component.translatable("tooltip.elemenix.undeconstructable").withStyle(ChatFormatting.DARK_GRAY));
        }
        if(stack.is(ModTags.UNRECONSTRUCTABLE)){
            insertIndex++;
            tooltip.add(insertIndex,Component.translatable("tooltip.elemenix.unreconstructable").withStyle(ChatFormatting.DARK_GRAY));
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
    public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        Elemenics.started = false;
    }
}
