package net.lghast.elemenix.client.event;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.client.misc.Palette;
import net.lghast.elemenix.client.misc.ModKeyBindings;
import net.lghast.elemenix.client.screen.*;
import net.lghast.elemenix.config.ClientConfig;
import net.lghast.elemenix.core.resolver.BaseResolver;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.register.system.ModDataComponents;
import net.lghast.elemenix.register.system.ModMenus;
import net.lghast.elemenix.register.system.ModTags;
import net.lghast.elemenix.core.Constituents;
import net.lghast.elemenix.core.resolver.ItemStackResolver;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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

@OnlyIn(Dist.CLIENT)
@SuppressWarnings("unused")
@EventBusSubscriber(modid = Elemenics.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> tooltip = event.getToolTip();

        ResourceLocation imitforgedOrigin = stack.get(ModDataComponents.IMITFORGED_ORIGIN.get());
        if (imitforgedOrigin != null) {
            Item originItem = BuiltInRegistries.ITEM.get(imitforgedOrigin);
            tooltip.add(Math.min(2, tooltip.size()),
                    Component.translatable("tooltip.elemenix.imitforged_from").withColor(Palette.GRAY_LIGHT));
            tooltip.add(Math.min(3, tooltip.size()),
                    Component.literal(new ItemStack(originItem).getHoverName().getString()).withColor(Palette.GRAY_DARK));
        }

        if(ClientConfig.SHOW_CONSTITUENT_TOOLTIPS_WHEN_SHIFT.get() && !Screen.hasShiftDown()) {
            return;
        }

        int insertIndex = findInsertIndex(tooltip);
        if(ClientConfig.SHOW_CONSTITUENT_TOOLTIPS.get() && !stack.is(ModItems.ELEMENIC_ANALYZER)) {
            if (BaseResolver.isCalculating()) {
                tooltip.add(insertIndex, Component.translatable("tooltip.elemenix.calculating").withColor(Palette.GRAY_LIGHT));
                insertIndex++;
            } else {
                ItemStackResolver.StackResult result = ItemStackResolver.getResult(stack);
                Constituents constituents = result.constituents();
                if(constituents == null) return;

                if(constituents.isUnanalysable() && !ClientConfig.SHOW_UNANALYSABLE_TOOLTIPS.get()) return;
                tooltip.add(insertIndex, Component.translatable("tooltip.elemenix.constituents").withColor(Palette.GRAY_LIGHT));
                insertIndex++;

                if (result.state() == ItemStackResolver.StackState.UNANALYSABLE_CONTENT
                        || result.state() == ItemStackResolver.StackState.EXCEEDS_LIMIT
                        || result.state() == ItemStackResolver.StackState.NESTED_CONTAINER
                        || result.state() == ItemStackResolver.StackState.UNANALYSABLE_COLOR
                        || result.state() == ItemStackResolver.StackState.UNANALYSABLE_POTION) {
                    tooltip.add(insertIndex, stateComponent(result.state()));
                } else if(Screen.hasAltDown()){
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
        }

        if(stack.is(ModTags.UNDECONSTRUCTABLE)){
            insertIndex++;
            tooltip.add(insertIndex,Component.translatable("tooltip.elemenix.undeconstructable").withColor(Palette.GRAY_DARK));
        }

        if(stack.is(ModTags.UNRECONSTRUCTABLE)){
            insertIndex++;
            tooltip.add(insertIndex,Component.translatable("tooltip.elemenix.unreconstructable").withColor(Palette.GRAY_DARK));
        }
    }

    private static Component stateComponent(ItemStackResolver.StackState state) {
        String key = switch (state) {
            case UNANALYSABLE_CONTENT -> "tooltip.elemenix.contains_unanalysable";
            case EXCEEDS_LIMIT -> "tooltip.elemenix.exceeds_limit";
            case NESTED_CONTAINER -> "tooltip.elemenix.nested_container";
            case UNANALYSABLE_COLOR -> "tooltip.elemenix.unanalysable_color";
            case UNANALYSABLE_POTION -> "tooltip.elemenix.unanalysable_potion";
            default -> "tooltip.elemenix.unanalysable";
        };
        return Component.translatable(key).withColor(Palette.GRAY_DARK);
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
        event.register(ModMenus.BURNER_MENU.get(), MemoryBurnerScreen::new);
        event.register(ModMenus.ANALYSIS_TERMINAL_MENU.get(), AnalysisTerminalScreen::new);
        event.register(ModMenus.TRANSFORMER_MENU.get(), TransformerScreen::new);
        event.register(ModMenus.INFUSER_MENU.get(), InfuserScreen::new);
        event.register(ModMenus.ENRICHER_MENU.get(), EnricherScreen::new);
        event.register(ModMenus.EJECTOR_MENU.get(), EjectorScreen::new);
        event.register(ModMenus.SHUNT_MENU.get(), ShuntScreen::new);
        event.register(ModMenus.DECONSTRUCTOR_MENU.get(), DeconstructorScreen::new);
        event.register(ModMenus.TANK_MENU.get(), TankScreen::new);
        event.register(ModMenus.OUTLET_MENU.get(), OutletScreen::new);
        event.register(ModMenus.ELEMENIC_COMPARATOR_MENU.get(), ElemenicComparatorScreen::new);
        event.register(ModMenus.PRINTER_MENU.get(), PrinterScreen::new);
        event.register(ModMenus.REGISTRAR_MENU.get(), RegistrarScreen::new);
        event.register(ModMenus.MEMORIZER_REPLICATOR_MENU.get(), ReplicatorScreen::new);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.level == null) return;

        while (ModKeyBindings.OPEN_INSTRUMENTS.consumeClick()) {
            if (minecraft.screen == null) {
                InstrumentOptionsScreen.openForCurrentCarried();
            }
        }
    }

    @SubscribeEvent
    public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        Elemenics.started = false;
    }
}
