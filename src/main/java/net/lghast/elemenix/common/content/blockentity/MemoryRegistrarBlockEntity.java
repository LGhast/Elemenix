package net.lghast.elemenix.common.content.blockentity;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.common.content.block.MemoryRegistrarBlock;
import net.lghast.elemenix.common.content.item.MemorizerItem;
import net.lghast.elemenix.common.content.item.WitchcraftMemorizerItem;
import net.lghast.elemenix.common.system.advancement.MemoriesRegisteredTrigger;
import net.lghast.elemenix.common.system.misc.RegistrationStageNotifier;
import net.lghast.elemenix.common.system.attachment.RegistrarCollection;
import net.lghast.elemenix.common.system.datacomponent.PotionMemories;
import net.lghast.elemenix.config.CommonConfig;
import net.lghast.elemenix.register.content.ModBlockEntities;
import net.lghast.elemenix.register.content.ModItems;
import net.lghast.elemenix.register.system.ModAttachments;
import net.lghast.elemenix.register.system.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
public class MemoryRegistrarBlockEntity extends BlockEntity {
    private static final String TAG_MEMORIZER = "Memorizer";
    private static final String TAG_OWNER = "Owner";
    private static final String TAG_REGISTER_TIMER = "RegisterTimer";
    private static final String TAG_CURSOR = "Cursor";
    private static final String TAG_CURRENT = "Current";
    private static final String TAG_REGISTERED_COUNT = "RegisteredCount";

    public enum PlacementResult {
        EMPTY_OR_INVALID,
        ALL_REGISTERED,
        OK
    }

    private static final List<ResourceLocation> ANALYZER_BUILT_IN_MEMORIES = List.of(
            id("elemenic_analyzer"),
            id("organix_essence"), id("terrix_essence"), id("flumix_essence"),
            id("metallix_essence"), id("energix_essence"), id("arcanix_essence"),
            id("organix_essenplex"), id("terrix_essenplex"), id("flumix_essenplex"),
            id("metallix_essenplex"), id("energix_essenplex"), id("arcanix_essenplex")
    );

    private static final List<ResourceLocation> TERMINAL_BUILT_IN_MEMORIES = List.of(
            id("analysis_terminal"),
            id("elemenic_equilibrium"), id("elemenic_equiliplex"), id("elemenic_chaos")
    );

    private ItemStack memorizer = ItemStack.EMPTY;
    @Nullable
    private UUID owner = null;
    private int registerTimer = 0;
    private int cursor = 0;
    private ItemStack currentRegisteringStack = ItemStack.EMPTY;
    private int registeredCount = 0;

    public MemoryRegistrarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MEMORY_REGISTRAR.get(), pos, state);
    }

    public boolean hasMemorizer() {
        return !memorizer.isEmpty();
    }

    public ItemStack getMemorizer() {
        return memorizer;
    }

    public void setMemorizer(ItemStack memorizer) {
        this.memorizer = memorizer;
        this.registeredCount = 0;
    }

    @Nullable
    public UUID getOwner() {
        return owner;
    }

    public void setOwner(@Nullable UUID owner) {
        this.owner = owner;
    }

    public ItemStack getCurrentRegisteringStack() {
        return currentRegisteringStack;
    }

    public int getRegisteredCount() {
        return registeredCount;
    }
    /**
     * Determines whether the given memorizer can be placed on this registrar.
     */
    public static PlacementResult checkPlacement(ItemStack memorizer, ServerPlayer player) {
        RegistrarCollection collection = player.getData(ModAttachments.REGISTRAR_COLLECTION);

        if (memorizer.getItem() instanceof WitchcraftMemorizerItem) {
            List<PotionMemories.Entry> entries = WitchcraftMemorizerItem.getOrCreateMemories(memorizer).entries();
            int valid = 0;
            int registerable = 0;
            for (PotionMemories.Entry entry : entries) {
                if (isInvalidPotion(entry)) continue;
                valid++;
                if (!collection.containsPotion(entry)) registerable++;
            }
            if (valid == 0) return PlacementResult.EMPTY_OR_INVALID;
            if (registerable == 0) return PlacementResult.ALL_REGISTERED;
            return PlacementResult.OK;
        }

        if (memorizer.getItem() instanceof MemorizerItem) {
            return checkItemPlacement(collection, MemorizerItem.getOrCreateMemories(memorizer).memories());
        }

        if (isBuiltInMemorySource(memorizer)) {
            return checkItemPlacement(collection, getBuiltInMemories(memorizer));
        }

        return PlacementResult.EMPTY_OR_INVALID;
    }

    private static PlacementResult checkItemPlacement(RegistrarCollection collection, List<ResourceLocation> ids) {
        int valid = 0;
        int registerable = 0;
        for (ResourceLocation id : ids) {
            if (isInvalidItem(id)) continue;
            valid++;
            if (!collection.containsItem(id)) registerable++;
        }
        if (valid == 0) return PlacementResult.EMPTY_OR_INVALID;
        if (registerable == 0) return PlacementResult.ALL_REGISTERED;
        return PlacementResult.OK;
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;
        if (!Elemenics.started) return;

        boolean working = false;

        if (hasMemorizer() && level instanceof ServerLevel serverLevel) {
            if (owner == null) {
                dropMemorizer(serverLevel);
            } else if (serverLevel.getEntity(owner) instanceof ServerPlayer ownerPlayer) {
                working = processRegistration(ownerPlayer, serverLevel);
            } else {
                dropMemorizerDueToOffline(serverLevel);
            }
        }

        boolean stateWorking = state.getValue(MemoryRegistrarBlock.WORKING);
        if (stateWorking != working) {
            level.setBlock(pos, state.setValue(MemoryRegistrarBlock.WORKING, working), Block.UPDATE_ALL);
        }
    }

    private boolean processRegistration(ServerPlayer ownerPlayer, ServerLevel serverLevel) {
        if (memorizer.getItem() instanceof WitchcraftMemorizerItem) {
            return processPotionRegistration(ownerPlayer, serverLevel);
        }
        if (memorizer.getItem() instanceof MemorizerItem || isBuiltInMemorySource(memorizer)) {
            return processItemRegistration(ownerPlayer, serverLevel);
        }
        dropMemorizer(serverLevel);
        return false;
    }

    private boolean processItemRegistration(ServerPlayer ownerPlayer, ServerLevel serverLevel) {
        List<ResourceLocation> itemIds;
        if (memorizer.getItem() instanceof MemorizerItem) {
            itemIds = MemorizerItem.getOrCreateMemories(memorizer).memories();
        } else {
            itemIds = getBuiltInMemories(memorizer);
        }
        RegistrarCollection collection = ownerPlayer.getData(ModAttachments.REGISTRAR_COLLECTION);

        while (cursor < itemIds.size()) {
            ResourceLocation id = itemIds.get(cursor);
            if (collection.containsItem(id) || isInvalidItem(id)) {
                cursor++;
            } else {
                break;
            }
        }

        if (cursor >= itemIds.size()) {
            finishRegistration(ownerPlayer, serverLevel);
            return false;
        }

        ResourceLocation id = itemIds.get(cursor);
        if (currentRegisteringStack.isEmpty()) {
            currentRegisteringStack = new ItemStack(BuiltInRegistries.ITEM.get(id));
            setChanged();
        }

        registerTimer++;
        if (registerTimer >= getRegisterInterval()) {
            registerTimer = 0;
            ownerPlayer.setData(ModAttachments.REGISTRAR_COLLECTION, collection.withItem(id));
            registeredCount++;
            cursor++;
            currentRegisteringStack = ItemStack.EMPTY;
            setChanged();
            playRegistrarSound(serverLevel, ModSounds.REGISTRAR.get());
            RegistrationStageNotifier.check(ownerPlayer);
            MemoriesRegisteredTrigger.TRIGGER.get().trigger(ownerPlayer,
                    collection.items().size() + collection.potions().size() + 1);
        }

        return true;
    }

    private boolean processPotionRegistration(ServerPlayer ownerPlayer, ServerLevel serverLevel) {
        List<PotionMemories.Entry> entries = WitchcraftMemorizerItem.getOrCreateMemories(memorizer).entries();
        RegistrarCollection collection = ownerPlayer.getData(ModAttachments.REGISTRAR_COLLECTION);

        while (cursor < entries.size()) {
            PotionMemories.Entry entry = entries.get(cursor);
            if (collection.containsPotion(entry) || isInvalidPotion(entry)) {
                cursor++;
            } else {
                break;
            }
        }

        if (cursor >= entries.size()) {
            finishRegistration(ownerPlayer, serverLevel);
            return false;
        }

        PotionMemories.Entry entry = entries.get(cursor);
        if (currentRegisteringStack.isEmpty()) {
            currentRegisteringStack = createPotionStack(entry);
            setChanged();
        }

        registerTimer++;
        if (registerTimer >= getRegisterInterval()) {
            registerTimer = 0;
            ownerPlayer.setData(ModAttachments.REGISTRAR_COLLECTION, collection.withPotion(entry));
            registeredCount++;
            cursor++;
            currentRegisteringStack = ItemStack.EMPTY;
            setChanged();
            playRegistrarSound(serverLevel, ModSounds.REGISTRAR.get());
            RegistrationStageNotifier.check(ownerPlayer);
            MemoriesRegisteredTrigger.TRIGGER.get().trigger(ownerPlayer,
                    collection.items().size() + collection.potions().size() + 1);
        }

        return true;
    }

    private void finishRegistration(ServerPlayer ownerPlayer, ServerLevel serverLevel) {
        if (registeredCount > 0) {
            ownerPlayer.displayClientMessage(
                    Component.translatable("message.elemenix.registrar.success", registeredCount), true);
        } else {
            ownerPlayer.displayClientMessage(
                    Component.translatable("message.elemenix.registrar.invalid"), true);
        }
        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
                12, 0.3, 0.3, 0.3, 0.05);
        playRegistrarSound(serverLevel, ModSounds.REGISTRAR_FINISH.get());
        dropMemorizer(serverLevel);
    }

    private void dropMemorizerDueToOffline(ServerLevel serverLevel) {
        serverLevel.sendParticles(ParticleTypes.SMOKE,
                worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
                8, 0.2, 0.2, 0.2, 0.02);
        playRegistrarSound(serverLevel, ModSounds.MEMORIZER_REMOVE.get());
        dropMemorizer(serverLevel);
    }

    private void playRegistrarSound(ServerLevel serverLevel, SoundEvent sound) {
        serverLevel.playSound(null, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
                sound, SoundSource.BLOCKS, 0.9F, 1.0F);
    }

    private void dropMemorizer(ServerLevel serverLevel) {
        if (hasMemorizer()) {
            Block.popResource(serverLevel, worldPosition, memorizer.copy());
        }
        setMemorizer(ItemStack.EMPTY);
        owner = null;
        registerTimer = 0;
        cursor = 0;
        currentRegisteringStack = ItemStack.EMPTY;
        setChanged();
    }

    private int getRegisterInterval() {
        return CommonConfig.REGISTRAR_REGISTER_INTERVAL.get();
    }

    public static boolean isBuiltInMemorySource(ItemStack stack) {
        return stack.getItem() == ModItems.ELEMENIC_ANALYZER.get()
                || stack.getItem() == ModItems.ANALYSIS_TERMINAL.get();
    }

    public static boolean isBuiltInMemoryItem(Item item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        return ANALYZER_BUILT_IN_MEMORIES.contains(id) || TERMINAL_BUILT_IN_MEMORIES.contains(id);
    }

    private static List<ResourceLocation> getBuiltInMemories(ItemStack stack) {
        if (stack.getItem() == ModItems.ELEMENIC_ANALYZER.get()) return ANALYZER_BUILT_IN_MEMORIES;
        if (stack.getItem() == ModItems.ANALYSIS_TERMINAL.get()) return TERMINAL_BUILT_IN_MEMORIES;
        return List.of();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Elemenics.MOD_ID, path);
    }

    private static boolean isInvalidItem(ResourceLocation id) {
        try {
            Item item = BuiltInRegistries.ITEM.get(id);
            return item == Items.AIR;
        } catch (Exception e) {
            return true;
        }
    }

    private static boolean isInvalidPotion(PotionMemories.Entry entry) {
        try {
            Item item = BuiltInRegistries.ITEM.get(entry.itemId());
            if (item == Items.AIR) return true;
            return BuiltInRegistries.POTION
                    .getHolder(ResourceKey.create(Registries.POTION, entry.potionId()))
                    .isEmpty();
        } catch (Exception e) {
            return true;
        }
    }

    private static ItemStack createPotionStack(PotionMemories.Entry entry) {
        try {
            Item item = BuiltInRegistries.ITEM.get(entry.itemId());
            Optional<Holder.Reference<Potion>> potion = BuiltInRegistries.POTION
                    .getHolder(ResourceKey.create(Registries.POTION, entry.potionId()));
            if (potion.isPresent()) {
                return PotionContents.createItemStack(item, potion.get());
            }
        } catch (Exception ignored) {
        }
        return ItemStack.EMPTY;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains(TAG_MEMORIZER)) {
            this.memorizer = ItemStack.parseOptional(provider, tag.getCompound(TAG_MEMORIZER));
        }
        if (tag.contains(TAG_OWNER)) {
            try {
                this.owner = UUID.fromString(tag.getString(TAG_OWNER));
            } catch (IllegalArgumentException ignored) {
                this.owner = null;
            }
        }
        registerTimer = tag.getInt(TAG_REGISTER_TIMER);
        cursor = tag.getInt(TAG_CURSOR);
        registeredCount = tag.getInt(TAG_REGISTERED_COUNT);
        if (tag.contains(TAG_CURRENT)) {
            this.currentRegisteringStack = ItemStack.parseOptional(provider, tag.getCompound(TAG_CURRENT));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        if (!memorizer.isEmpty()) {
            tag.put(TAG_MEMORIZER, memorizer.saveOptional(provider));
        }
        if (owner != null) {
            tag.putString(TAG_OWNER, owner.toString());
        }
        tag.putInt(TAG_REGISTER_TIMER, registerTimer);
        tag.putInt(TAG_CURSOR, cursor);
        tag.putInt(TAG_REGISTERED_COUNT, registeredCount);
        if (!currentRegisteringStack.isEmpty()) {
            tag.put(TAG_CURRENT, currentRegisteringStack.saveOptional(provider));
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.put(TAG_MEMORIZER, memorizer.saveOptional(registries));
        tag.put(TAG_CURRENT, currentRegisteringStack.saveOptional(registries));
        if (owner != null) {
            tag.putString(TAG_OWNER, owner.toString());
        }
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }
}
