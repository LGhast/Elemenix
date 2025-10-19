package net.lghast.elemenix.common.content.item;

import net.lghast.elemenix.common.system.datacomponent.MemoryData;
import net.lghast.elemenix.common.system.datacomponent.Waxed;
import net.lghast.elemenix.conifig.ClientConfig;
import net.lghast.elemenix.register.system.ModDataComponents;
import net.lghast.elemenix.utils.ModUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.particle.GlowParticle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CopperBulbBlock;
import net.neoforged.neoforge.registries.datamaps.builtin.Waxable;

import java.util.ArrayList;
import java.util.List;

public class MemorizerItem extends Item{
    public MemorizerItem(Properties properties) {
        super(properties.stacksTo(1)
                .component(ModDataComponents.MEMORY_DATA, new MemoryData(new ArrayList<>()))
                .component(ModDataComponents.WAXED, new Waxed(false))
        );
    }

    public static MemoryData getOrCreateMemories(ItemStack stack) {
        if(!(stack.getItem() instanceof MemorizerItem)){
            return new MemoryData(new ArrayList<>());
        }
        MemoryData data = stack.get(ModDataComponents.MEMORY_DATA);
        if (data == null) {
            data = new MemoryData(new ArrayList<>());
        }
        return data;
    }

    public static boolean isReadonly(ItemStack stack){
        if (!(stack.getItem() instanceof MemorizerItem)) {
            return true;
        }
        Waxed waxedData = stack.get(ModDataComponents.WAXED);
        if(waxedData == null){
            waxedData = new Waxed(false);
        }
        return waxedData.waxed();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack memorizerStack = player.getItemInHand(usedHand);
        if(usedHand != InteractionHand.MAIN_HAND){
            return super.use(level, player, usedHand);
        }
        if(!(level instanceof ServerLevel serverLevel)){
            return InteractionResultHolder.success(memorizerStack);
        }
        ItemStack offHandItem = player.getOffhandItem();
        if(isReadonly(memorizerStack)){
           waxOff(serverLevel, player, memorizerStack,offHandItem);
        }else{
            waxOn(serverLevel, player, memorizerStack, offHandItem);
        }
        return InteractionResultHolder.success(memorizerStack);
    }

    private void waxOn(ServerLevel serverLevel, Player player, ItemStack memorizerStack, ItemStack offHandItem){
        if(offHandItem.is(Items.HONEYCOMB)){
            if(!player.isCreative()) {
                offHandItem.shrink(1);
            }
            memorizerStack.set(ModDataComponents.WAXED, new Waxed(true));
            ModUtils.spawnParticles(serverLevel, ParticleTypes.WAX_ON,
                    player.getX(), player.getY()+1.2, player.getZ(), 0.4, 0.3, 0.4, 8, 0.01);
            serverLevel.playSound(null , player.getX(), player.getY(), player.getZ(),
                    SoundEvents.HONEYCOMB_WAX_ON, SoundSource.PLAYERS);
        }
    }

    private void waxOff(ServerLevel serverLevel, Player player, ItemStack memorizerStack, ItemStack offHandItem){
        if(offHandItem.is(ItemTags.AXES)){
            offHandItem.hurtAndBreak(1, player, EquipmentSlot.OFFHAND);
            memorizerStack.set(ModDataComponents.WAXED, new Waxed(false));
            ModUtils.spawnParticles(serverLevel, ParticleTypes.WAX_OFF,
                    player.getX(), player.getY()+1, player.getZ(), 0.4, 0.3, 0.4, 8, 0.01);
            serverLevel.playSound(null , player.getX(), player.getY(), player.getZ(),
                    SoundEvents.AXE_WAX_OFF, SoundSource.PLAYERS);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag tooltipFlag) {
        if(isReadonly(stack) && ClientConfig.SHOW_READONLY_TOOLTIPS.get()){
            components.add(Component.translatable("tooltip.elemenix.readonly").withStyle(ChatFormatting.DARK_GRAY));
        }
        if(ClientConfig.SHOW_MEMORY_TOOLTIPS.get()) {
            String memories = String.format(Component.translatable("tooltip.elemenix.memories").getString(), getOrCreateMemories(stack).resolvedItems().size());
            components.add(Component.literal(memories).withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, context, components, tooltipFlag);
    }
}
