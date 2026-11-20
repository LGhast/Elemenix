package net.lghast.elemenix.common.event;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.client.misc.ClientElemenixPrecompute;
import net.lghast.elemenix.common.content.item.MemorizerItem;
import net.lghast.elemenix.common.content.item.WitchcraftMemorizerItem;
import net.lghast.elemenix.common.system.advancement.StyleTrigger;
import net.lghast.elemenix.core.resolver.BaseResolver;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = Elemenics.MOD_ID)
public class CommonEvents {
    @SubscribeEvent
    public static void onRecipesUpdated(RecipesUpdatedEvent event) {
        BaseResolver.clearCaches();
        if (!Elemenics.started) return;

        Level level = Elemenics.getCurrentLevel();
        if (level == null) return;

        if (level.isClientSide()) {
            ClientElemenixPrecompute.start();
        } else {
            BaseResolver.initialize();
        }
    }

    @SubscribeEvent
    public static void onRegisterBrewingRecipes(RegisterBrewingRecipesEvent event) {
        event.getBuilder().addMix(Potions.THICK, Items.RABBIT_FOOT, Potions.LUCK);
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack crafted = event.getCrafting();
        if (!(crafted.getItem() instanceof MemorizerItem) && !(crafted.getItem() instanceof WitchcraftMemorizerItem)) return;
        if (MemorizerItem.getOrCreateStyle(crafted).style() == 0) return;
        StyleTrigger.TRIGGER.get().trigger(player);
    }
}
