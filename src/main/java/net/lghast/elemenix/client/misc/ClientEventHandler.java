package net.lghast.elemenix.client.misc;

import net.lghast.elemenix.ElemenixAnalyzer;
import net.lghast.elemenix.client.screen.AnalyzerScreen;
import net.lghast.elemenix.client.screen.EnricherScreen;
import net.lghast.elemenix.client.screen.InfuserScreen;
import net.lghast.elemenix.client.screen.TransformerScreen;
import net.lghast.elemenix.conifig.ClientConfig;
import net.lghast.elemenix.network.DeconstructionPayload;
import net.lghast.elemenix.network.ReconstructionPayload;
import net.lghast.elemenix.register.content.ModBlocks;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.register.system.ModMenus;
import net.lghast.elemenix.utils.Constituents;
import net.lghast.elemenix.utils.ElemenixInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.List;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = ElemenixAnalyzer.MOD_ID)
public class ClientEventHandler {
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        if(ClientConfig.SHOW_CONSTITUENT_TOOLTIPS.get() && !stack.is(ModItems.ELEMENIC_ANALYZER)) {
            Constituents constituents = ElemenixInfo.getConstituents(stack);
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
        event.register(ModMenus.TRANSFORMER_MENU.get(), TransformerScreen::new);
        event.register(ModMenus.INFUSER_MENU.get(), InfuserScreen::new);
        event.register(ModMenus.ENRICHER_MENU.get(), EnricherScreen::new);
    }

    @SubscribeEvent
    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1.0");

        registrar.playToServer(
                DeconstructionPayload.TYPE,
                DeconstructionPayload.STREAM_CODEC,
                DeconstructionPayload::handle
        );

        registrar.playToServer(
                ReconstructionPayload.TYPE,
                ReconstructionPayload.STREAM_CODEC,
                ReconstructionPayload::handle
        );
    }
}
