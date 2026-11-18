package net.lghast.elemenix.client.screen.widget;

import net.lghast.elemenix.common.system.menu.MemorizerReplicatorMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class ReplicatorMemoryListWidget extends MemoryListWidget {
    private final List<String> keys = new ArrayList<>();

    public ReplicatorMemoryListWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    protected boolean isMenuWrong() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return true;
        return !(minecraft.player.containerMenu instanceof MemorizerReplicatorMenu);
    }

    @Override
    protected String getTipText() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.containerMenu instanceof MemorizerReplicatorMenu menu) {
            if (menu.getMasterStack().isEmpty()) {
                return Component.translatable("gui.elemenix.replicator.place_master").getString();
            }
        }
        return Component.translatable("gui.elemenix.no_resolved_items").getString();
    }

    @Override
    protected void updateItemList() {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return;
        if (!(player.containerMenu instanceof MemorizerReplicatorMenu menu)) return;

        List<String> newKeys = new ArrayList<>();
        List<ItemStack> newStacks = new ArrayList<>();

        List<String> memoryKeys = menu.getMemoryKeys();
        if (memoryKeys != null) {
            for (String key : memoryKeys) {
                newKeys.add(key);
                newStacks.add(menu.createEntryStack(key));
            }
        }

        boolean changed = !newKeys.equals(this.keys);
        if (changed) {
            currentPage = 0;
        }

        this.keys.clear();
        this.itemIds.clear();
        this.potionIds.clear();
        this.displayStacks.clear();
        this.keys.addAll(newKeys);
        this.displayStacks.addAll(newStacks);

        if (onPageChange != null) {
            onPageChange.run();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }
}
