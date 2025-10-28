package net.lghast.elemenix.common.content.item;

import net.lghast.elemenix.common.system.advancement.ValveTrigger;
import net.lghast.elemenix.common.system.advancement.WaxOffTrigger;
import net.lghast.elemenix.common.system.datacomponent.ValveOpenness;
import net.lghast.elemenix.conifig.ClientConfig;
import net.lghast.elemenix.register.system.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class ThrottleValveItem extends Item{
    public ThrottleValveItem(Properties properties) {
        super(properties.stacksTo(1).component(ModDataComponents.VALVE_OPENNESS, new ValveOpenness(5)));
    }

    public static ValveOpenness getOrCreateOpenness(ItemStack stack) {
        if(!(stack.getItem() instanceof ThrottleValveItem)){
            return new ValveOpenness(5);
        }
        ValveOpenness openness = stack.get(ModDataComponents.VALVE_OPENNESS);
        if (openness == null) {
            openness = new ValveOpenness(5);
        }
        return openness;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack valveStack = player.getItemInHand(usedHand);
        if(usedHand != InteractionHand.MAIN_HAND){
            return super.use(level, player, usedHand);
        }
        if(!(level instanceof ServerLevel serverLevel)){
            return InteractionResultHolder.success(valveStack);
        }

        serverLevel.playSound(null , player.getX(), player.getY(), player.getZ(),
                SoundEvents.LEVER_CLICK, SoundSource.PLAYERS);

        ValveOpenness openness = getOrCreateOpenness(valveStack).decrement();
        valveStack.set(ModDataComponents.VALVE_OPENNESS, openness);

        if(player instanceof ServerPlayer serverPlayer) {
            ValveTrigger.TRIGGER.get().trigger(serverPlayer, openness.openness());
        }

        player.displayClientMessage(Component.translatable("tooltip.elemenix.valve_opening", openness.getOpennessPercentage()), true);
        return InteractionResultHolder.success(valveStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag tooltipFlag) {
        if(ClientConfig.SHOW_VALVE_OPENNESS_TOOLTIPS.get()){
            ValveOpenness openness = getOrCreateOpenness(stack);
            components.add(Component.translatable("tooltip.elemenix.valve_opening", openness.getOpennessPercentage())
                    .withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, context, components, tooltipFlag);
    }
}
