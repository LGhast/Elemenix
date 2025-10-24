package net.lghast.elemenix.register.system;

import net.lghast.elemenix.ElemenixAnalyzer;
import net.lghast.elemenix.common.content.blockentity.EnricherBlockEntity;
import net.lghast.elemenix.common.content.blockentity.InfuserBlockEntity;
import net.lghast.elemenix.common.content.blockentity.TransformerBlockEntity;
import net.lghast.elemenix.common.system.menu.AnalyzerMenu;
import net.lghast.elemenix.common.system.menu.EnricherMenu;
import net.lghast.elemenix.common.system.menu.InfuserMenu;
import net.lghast.elemenix.common.system.menu.TransformerMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, ElemenixAnalyzer.MOD_ID);

    public static final Supplier<MenuType<AnalyzerMenu>> ELEMENIX_ANALYZER_MENU =
            MENUS.register("analyzer_menu", () ->
                    IMenuTypeExtension.create((windowId, inv, data) ->
                            new AnalyzerMenu(windowId, inv, ItemStack.EMPTY)));

    public static final Supplier<MenuType<TransformerMenu>> TRANSFORMER_MENU =
            MENUS.register("transformer_menu", () ->
                    IMenuTypeExtension.create((windowId, inv, data) ->
                            new TransformerMenu(windowId, inv, (TransformerBlockEntity) inv.player.level().getBlockEntity(data.readBlockPos()))));

    public static final Supplier<MenuType<InfuserMenu>> INFUSER_MENU =
            MENUS.register("infuser_menu", () ->
                    IMenuTypeExtension.create((windowId, inv, data) ->
                            new InfuserMenu(windowId, inv, (InfuserBlockEntity) inv.player.level().getBlockEntity(data.readBlockPos()))));

    public static final Supplier<MenuType<EnricherMenu>> ENRICHER_MENU =
            MENUS.register("enricher_menu", () ->
                    IMenuTypeExtension.create((windowId, inv, data) ->
                            new EnricherMenu(windowId, inv, (EnricherBlockEntity) inv.player.level().getBlockEntity(data.readBlockPos()))));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
