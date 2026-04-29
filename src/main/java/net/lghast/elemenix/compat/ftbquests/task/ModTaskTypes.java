package net.lghast.elemenix.compat.ftbquests.task;

import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import dev.ftb.mods.ftblibrary.icon.Icon;
import net.minecraft.resources.ResourceLocation;


public class ModTaskTypes {
    public static TaskType DECONSTRUCT_ITEM;
    public static TaskType RECONSTRUCT_ITEM;
    public static TaskType MEMORIZE_ITEM;

    public static void init() {
        DECONSTRUCT_ITEM = TaskTypes.register(
                ResourceLocation.fromNamespaceAndPath("elemenix", "deconstruct_item"),
                DeconstructItemTask::new,
                () -> Icon.getIcon("item:elemenix:elemenic_analyzer")
        );
        RECONSTRUCT_ITEM = TaskTypes.register(
                ResourceLocation.fromNamespaceAndPath("elemenix", "reconstruct_item"),
                ReconstructItemTask::new,
                () -> Icon.getIcon("item:elemenix:elemenic_analyzer")
        );
        MEMORIZE_ITEM = TaskTypes.register(
                ResourceLocation.fromNamespaceAndPath("elemenix", "memorize_item"),
                MemorizeItemTask::new,
                () -> Icon.getIcon("item:elemenix:elemenic_memorizer")
        );
    }
}
