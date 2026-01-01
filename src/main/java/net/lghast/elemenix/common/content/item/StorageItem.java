package net.lghast.elemenix.common.content.item;

import net.lghast.elemenix.common.system.datacomponent.ElemenicStorage;
import net.lghast.elemenix.conifig.ClientConfig;
import net.lghast.elemenix.register.system.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class StorageItem extends Item {
    public StorageItem(Properties properties) {
        super(properties.stacksTo(1).component(ModDataComponents.ELEMENIC_STORAGE, new ElemenicStorage(new long[6])));
    }

    public static ElemenicStorage getOrCreateData(ItemStack stack) {
        ElemenicStorage data = stack.get(ModDataComponents.ELEMENIC_STORAGE.get());
        if (data == null) {
            data = new ElemenicStorage(new long[6]);
        }
        return data;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag tooltipFlag) {
        if(ClientConfig.SHOW_STORAGE_TOOLTIPS.get()) {
            ElemenicStorage storage = getOrCreateData(stack);
            components.add(Component.translatable("tooltip.elemenix.analyzer_storage").withStyle(ChatFormatting.GRAY));
            components.add(storage.toComponentFormer());
            components.add(storage.toComponentLatter());
        }
        super.appendHoverText(stack, context, components, tooltipFlag);
    }
}
