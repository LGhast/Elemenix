package net.lghast.elemenix.register.content;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.common.content.item.AnalyzerItem;
import net.lghast.elemenix.common.content.item.FuelItem;
import net.lghast.elemenix.common.content.item.MemorizerItem;
import net.lghast.elemenix.common.content.item.ThrottleValveItem;
import net.lghast.elemenix.register.system.ModFoodProperties;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Elemenics.MOD_ID);

    public static final DeferredItem<Item> ELEMENIC_ANALYZER = ITEMS.register("elemenic_analyzer",
            ()-> new AnalyzerItem(new Item.Properties()));

    public static final DeferredItem<Item> ELEMENIC_MEMORIZER = ITEMS.register("elemenic_memorizer",
            ()-> new MemorizerItem(new Item.Properties()));

    public static final DeferredItem<Item> ORGANIX_ESSENCE = ITEMS.register("organix_essence",
            ()-> new Item(new Item.Properties()));

    public static final DeferredItem<Item> TERRIX_ESSENCE = ITEMS.register("terrix_essence",
            ()-> new Item(new Item.Properties()));

    public static final DeferredItem<Item> FLUMIX_ESSENCE = ITEMS.register("flumix_essence",
            ()-> new Item(new Item.Properties()));

    public static final DeferredItem<Item> METALLIX_ESSENCE = ITEMS.register("metallix_essence",
            ()-> new Item(new Item.Properties()));

    public static final DeferredItem<Item> ENERGIX_ESSENCE = ITEMS.register("energix_essence",
            ()-> new FuelItem(new Item.Properties(), 8100));

    public static final DeferredItem<Item> ARCANIX_ESSENCE = ITEMS.register("arcanix_essence",
            ()-> new Item(new Item.Properties()));

    public static final DeferredItem<Item> ELEMENIC_EQUILIBRIUM = ITEMS.register("elemenic_equilibrium",
            ()-> new Item(new Item.Properties()));

    public static final DeferredItem<Item> ORGANIX_ESSENPLEX = ITEMS.register("organix_essenplex",
            ()-> new Item(new Item.Properties()));

    public static final DeferredItem<Item> TERRIX_ESSENPLEX = ITEMS.register("terrix_essenplex",
            ()-> new Item(new Item.Properties()));

    public static final DeferredItem<Item> FLUMIX_ESSENPLEX = ITEMS.register("flumix_essenplex",
            ()-> new Item(new Item.Properties()));

    public static final DeferredItem<Item> METALLIX_ESSENPLEX = ITEMS.register("metallix_essenplex",
            ()-> new Item(new Item.Properties()));

    public static final DeferredItem<Item> ENERGIX_ESSENPLEX = ITEMS.register("energix_essenplex",
            ()-> new FuelItem(new Item.Properties(), 72900));

    public static final DeferredItem<Item> ARCANIX_ESSENPLEX = ITEMS.register("arcanix_essenplex",
            ()-> new Item(new Item.Properties()));

    public static final DeferredItem<Item> THROTTLE_VALVE = ITEMS.register("throttle_valve",
            ()-> new ThrottleValveItem(new Item.Properties()));

    public static final DeferredItem<Item> FLOW_STRAIGHTENER = ITEMS.register("flow_straightener",
            ()-> new Item(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> FONDANT_CAKE = ITEMS.register("fondant_cake",
            ()-> new Item(new Item.Properties().food(ModFoodProperties.FONDANT_CAKE)));

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
