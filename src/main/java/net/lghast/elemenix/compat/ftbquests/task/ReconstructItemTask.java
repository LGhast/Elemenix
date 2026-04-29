package net.lghast.elemenix.compat.ftbquests.task;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ReconstructItemTask extends Task {
    private ItemStack targetStack = ItemStack.EMPTY;
    private long requiredAmount = 1L;

    public ReconstructItemTask(long id, Quest quest) {
        super(id, quest);
    }

    @Override
    public TaskType getType() {
        return ModTaskTypes.RECONSTRUCT_ITEM;
    }

    @Override
    public long getMaxProgress() {
        return requiredAmount;
    }

    public boolean matches(ItemStack stack) {
        if (targetStack.isEmpty() || stack.isEmpty()) {
            return false;
        }
        return stack.is(targetStack.getItem());
    }

    public void addProgress(TeamData teamData, long amount) {
        long current = teamData.getProgress(this);
        long newProgress = Math.min(current + amount, requiredAmount);
        if (newProgress != current) {
            teamData.setProgress(this, newProgress);
            teamData.markDirty();
        }
    }

    @Override
    public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
        super.writeData(nbt, provider);
        if (!targetStack.isEmpty()) {
            nbt.put("target_stack", targetStack.save(provider));
        }
        if (requiredAmount > 1) {
            nbt.putLong("required_amount", requiredAmount);
        }
    }

    @Override
    public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
        super.readData(nbt, provider);

        if (nbt.contains("target_stack")) {
            targetStack = ItemStack.parse(provider, nbt.getCompound("target_stack")).orElse(ItemStack.EMPTY);
        } else if (nbt.contains("targetId")) {
            ResourceLocation oldId = ResourceLocation.tryParse(nbt.getString("targetId"));
            if (oldId != null) {
                var item = BuiltInRegistries.ITEM.get(oldId);
                if (item != Items.AIR) {
                    targetStack = new ItemStack(item);
                }
            }
        }

        if (nbt.contains("required_amount")) {
            requiredAmount = Math.max(nbt.getLong("required_amount"), 1L);
        } else if (nbt.contains("requiredAmount")) {
            requiredAmount = Math.max(nbt.getLong("requiredAmount"), 1L);
        } else {
            requiredAmount = 1L;
        }
    }

    @Override
    public void writeNetData(RegistryFriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, targetStack);
        buffer.writeVarLong(requiredAmount);
    }

    @Override
    public void readNetData(RegistryFriendlyByteBuf buffer) {
        super.readNetData(buffer);
        targetStack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
        requiredAmount = buffer.readVarLong();
    }

    @Override
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);
        config.addItemStack("item", targetStack, v -> targetStack = v, ItemStack.EMPTY, false, false)
                .setNameKey("ftbquests.task.ftbquests.item");
        config.addLong("count", requiredAmount, v -> requiredAmount = v, 1L, 1L, Long.MAX_VALUE)
                .setNameKey("ftbquests.task.elemenix.count");
    }

    @Override
    public Icon getAltIcon() {
        if (targetStack.isEmpty()) {
            return ItemIcon.getItemIcon(Items.BARRIER);
        }
        return ItemIcon.getItemIcon(targetStack);
    }

    @Override
    public String formatMaxProgress() {
        return Long.toString(requiredAmount);
    }

    @Override
    public boolean consumesResources() {
        return false;
    }
}
