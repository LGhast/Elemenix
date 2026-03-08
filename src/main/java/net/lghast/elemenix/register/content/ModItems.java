package net.lghast.elemenix.register.content;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.common.content.item.*;
import net.lghast.elemenix.register.system.ModFoodProperties;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Elemenics.MOD_ID);

    public static final DeferredItem<Item> ELEMENIC_ANALYZER = ITEMS.register("elemenic_analyzer",
            ()-> new AnalyzerItem(new Item.Properties()));

    public static final DeferredItem<Item> ELEMENIC_STORAGE = ITEMS.register("elemenic_storage",
            ()-> new StorageItem(new Item.Properties()));

    public static final DeferredItem<Item> ELEMENIC_MEMORIZER = ITEMS.register("elemenic_memorizer",
            ()-> new MemorizerItem(new Item.Properties()));

    public static final DeferredItem<Item> REMOTE_ELEMENIC_STORAGE = ITEMS.register("remote_elemenic_storage",
            ()-> new RemoteStorageItem(new Item.Properties()));

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

    public static final DeferredItem<Item> ELEMENIC_EQUILIPLEX = ITEMS.register("elemenic_equiliplex",
            ()-> new Item(new Item.Properties()));

    public static final DeferredItem<Item> NULLVOID = ITEMS.register("nullvoid",
            ()-> new NullvoidItem(new Item.Properties()));

    public static final DeferredItem<Item> THROTTLE_VALVE = ITEMS.register("throttle_valve",
            ()-> new ThrottleValveItem(new Item.Properties()));

    public static final DeferredItem<Item> FLOW_STRAIGHTENER = ITEMS.register("flow_straightener",
            ()-> new Item(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> FONDANT_CAKE = ITEMS.register("fondant_cake",
            ()-> new Item(new Item.Properties().food(ModFoodProperties.FONDANT_CAKE)));

    public static final DeferredItem<Item> MEMORIZER_BOX = ITEMS.register("memorizer_box",
            ()-> new MemorizerBoxItem(new Item.Properties()));

    public static final DeferredItem<Item> MEMORY_BURNER = ITEMS.register("memory_burner",
            ()-> new BurnerItem(new Item.Properties()));

    public static final DeferredItem<Item> ELEMENIC_SCANNER = ITEMS.register("elemenic_scanner",
            ()-> new ScannerItem(new Item.Properties()));

    public static final DeferredItem<Item> SCANNING_STORAGE = ITEMS.register("scanning_storage",
            ()-> new ScanningStorageItem(new Item.Properties()));

    public static final DeferredItem<Item> ANALYZING_CHIP = ITEMS.register("analyzing_chip",
            ()-> new Item(new Item.Properties()));

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
