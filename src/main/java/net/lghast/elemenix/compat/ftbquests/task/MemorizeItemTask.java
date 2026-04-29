package net.lghast.elemenix.compat.ftbquests.task;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import net.lghast.elemenix.register.content.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;


public class MemorizeItemTask extends Task {
    private ItemStack targetStack = ItemStack.EMPTY;

    public MemorizeItemTask(long id, Quest quest) {
        super(id, quest);
    }

    @Override
    public TaskType getType() {
        return ModTaskTypes.MEMORIZE_ITEM;
    }

    @Override
    public long getMaxProgress() {
        return 1L;
    }

    public boolean matches(ItemStack stack) {
        if (targetStack.isEmpty() || stack.isEmpty()) {
            return false;
        }
        return stack.is(targetStack.getItem());
    }

    public void addProgress(TeamData teamData, long amount) {
        long current = teamData.getProgress(this);
        if (current < 1L && amount > 0L) {
            teamData.setProgress(this, 1L);
            teamData.markDirty();
        }
    }

    @Override
    public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
        super.writeData(nbt, provider);
        if (!targetStack.isEmpty()) {
            nbt.put("target_stack", targetStack.save(provider));
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
    }

    @Override
    public void writeNetData(RegistryFriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, targetStack);
    }

    @Override
    public void readNetData(RegistryFriendlyByteBuf buffer) {
        super.readNetData(buffer);
        targetStack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
    }

    @Override
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);
        config.addItemStack("item", targetStack, v -> targetStack = v, ItemStack.EMPTY, false, false)
                .setNameKey("ftbquests.task.ftbquests.item");
    }

    @Override
    public Icon getAltIcon() {
        if (targetStack.isEmpty()) {
            return ItemIcon.getItemIcon(ModItems.ELEMENIC_MEMORIZER.asItem());
        }
        return ItemIcon.getItemIcon(targetStack);
    }

    @Override
    public String formatMaxProgress() {
        return "1";
    }

    @Override
    public boolean consumesResources() {
        return false;
    }
}
