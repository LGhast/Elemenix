package net.lghast.elemenix.common.content.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class NullvoidItem extends Item {
    public NullvoidItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag tooltipFlag) {
        components.add(Component.translatable("tooltip.elemenix.technical").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, context, components, tooltipFlag);
    }
}
