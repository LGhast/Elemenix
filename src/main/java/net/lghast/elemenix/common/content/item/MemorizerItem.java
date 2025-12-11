package net.lghast.elemenix.common.content.item;

import net.lghast.elemenix.common.system.advancement.WaxOffTrigger;
import net.lghast.elemenix.common.system.advancement.WaxOnTrigger;
import net.lghast.elemenix.common.system.datacomponent.MemoryData;
import net.lghast.elemenix.common.system.datacomponent.Style;
import net.lghast.elemenix.common.system.datacomponent.Waxed;
import net.lghast.elemenix.conifig.ClientConfig;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.register.system.ModDataComponents;
import net.lghast.elemenix.utils.ModUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ParametersAreNonnullByDefault
public class MemorizerItem extends Item{
    private static final Map<Item, Integer> STYLE_MAP = new HashMap<>();

    static {
        STYLE_MAP.put(Items.COPPER_INGOT, 0);
        STYLE_MAP.put(Items.WHITE_DYE, 1);
        STYLE_MAP.put(Items.RED_DYE, 2);
        STYLE_MAP.put(Items.ORANGE_DYE, 3);
        STYLE_MAP.put(Items.YELLOW_DYE, 4);
        STYLE_MAP.put(Items.GREEN_DYE, 5);
        STYLE_MAP.put(Items.CYAN_DYE, 6);
        STYLE_MAP.put(Items.BLUE_DYE, 7);
        STYLE_MAP.put(Items.PURPLE_DYE, 8);
        STYLE_MAP.put(Items.BLACK_DYE, 9);
        STYLE_MAP.put(Items.GRAY_DYE, 10);
        STYLE_MAP.put(Items.LIME_DYE, 11);
        STYLE_MAP.put(Items.PINK_DYE, 12);
        STYLE_MAP.put(Items.MAGENTA_DYE, 13);
        STYLE_MAP.put(Items.LIGHT_GRAY_DYE, 14);
        STYLE_MAP.put(Items.LIGHT_BLUE_DYE, 15);
        STYLE_MAP.put(Items.BROWN_DYE, 16);
        STYLE_MAP.put(Items.PRISMARINE_SHARD, 17);
        STYLE_MAP.put(Items.BREEZE_ROD, 18);
        STYLE_MAP.put(Items.POPPED_CHORUS_FRUIT, 19);
        STYLE_MAP.put(Items.GOLD_INGOT, 20);
        STYLE_MAP.put(Items.DIAMOND, 21);
    }

    public MemorizerItem(Properties properties) {
        super(properties.stacksTo(1)
                .component(ModDataComponents.MEMORY_DATA, new MemoryData(new ArrayList<>()))
                .component(ModDataComponents.WAXED, new Waxed(false))
                .component(ModDataComponents.STYLE, new Style(0))
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

    public static Style getOrCreateStyle(ItemStack stack) {
        if(!(stack.getItem() instanceof MemorizerItem)){
            return new Style(0);
        }
        Style style = stack.get(ModDataComponents.STYLE);
        if (style == null) {
            style = new Style(0);
        }
        return style;
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

    public static boolean isChangeable(ItemStack stack){
        if(!stack.is(ModItems.ELEMENIC_MEMORIZER)){
            return false;
        }
        return !isReadonly(stack);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack memorizerStack = player.getItemInHand(usedHand);
        if(usedHand != InteractionHand.MAIN_HAND){
            return super.use(level, player, usedHand);
        }
        if(!(level instanceof ServerLevel serverLevel)){
            return InteractionResultHolder.success(memorizerStack);
        }

        ItemStack offHandItem = player.getOffhandItem();
        if(offHandItem.isEmpty()){
            return InteractionResultHolder.pass(memorizerStack);
        }

        if(STYLE_MAP.containsKey(offHandItem.getItem())){
            changeStyle(serverLevel, player, memorizerStack, offHandItem);
            return InteractionResultHolder.success(memorizerStack);
        }

        if(isReadonly(memorizerStack)){
           waxOff(serverLevel, player, memorizerStack,offHandItem);
        }else{
            waxOn(serverLevel, player, memorizerStack, offHandItem);
        }
        return InteractionResultHolder.success(memorizerStack);
    }

    private void changeStyle(ServerLevel serverLevel, Player player, ItemStack memorizerStack, ItemStack offHandItem){
        int styleIndex = STYLE_MAP.get(offHandItem.getItem());

        if(styleIndex == getOrCreateStyle(memorizerStack).style()){
            return;
        }

        if(!player.isCreative()) {
            offHandItem.shrink(1);
        }
        memorizerStack.set(ModDataComponents.STYLE, new Style(styleIndex));

        serverLevel.playSound(null , player.getX(), player.getY(), player.getZ(),
                SoundEvents.DYE_USE, SoundSource.PLAYERS);
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

            if (player instanceof ServerPlayer serverPlayer) {
                WaxOnTrigger.TRIGGER.get().trigger(serverPlayer);
            }
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

            if (player instanceof ServerPlayer serverPlayer) {
                WaxOffTrigger.TRIGGER.get().trigger(serverPlayer);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag tooltipFlag) {
        if(isReadonly(stack) && ClientConfig.SHOW_READONLY_TOOLTIPS.get()){
            components.add(Component.translatable("tooltip.elemenix.readonly").withStyle(ChatFormatting.DARK_GRAY));
        }
        Style style = getOrCreateStyle(stack);
        if(style.style() != 0 && ClientConfig.SHOW_STYLE_TOOLTIPS.get()){
            String styleInfo = String.format(Component.translatable("tooltip.elemenix.style").getString(), Component.translatable(style.getStyleKey()).getString());
            components.add(Component.literal(styleInfo).withStyle(ChatFormatting.DARK_GRAY));
        }
        if(ClientConfig.SHOW_MEMORY_TOOLTIPS.get()) {
            String memories = String.format(Component.translatable("tooltip.elemenix.memories").getString(), getOrCreateMemories(stack).resolvedItems().size());
            components.add(Component.literal(memories).withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, context, components, tooltipFlag);
    }
}
