package net.lghast.elemenix.common.system.menu;

import net.lghast.elemenix.common.content.block.GeologicalSimulatorBlock;
import net.lghast.elemenix.common.content.block.GerminalAcceleratorBlock;
import net.lghast.elemenix.common.content.block.MetallurgicalActivatorBlock;
import net.lghast.elemenix.common.content.block.TransformerBlock;
import net.lghast.elemenix.common.content.blockentity.TransformerBlockEntity;
import net.lghast.elemenix.common.system.advancement.GerminateTrigger;
import net.lghast.elemenix.common.system.advancement.ImitforgeTrigger;
import net.lghast.elemenix.common.system.advancement.MineralizeTrigger;
import net.lghast.elemenix.common.system.recipe.GerminatingRecipe;
import net.lghast.elemenix.common.system.recipe.MineralizingRecipe;
import net.lghast.elemenix.register.system.ModMenus;
import net.lghast.elemenix.register.system.ModRecipes;
import net.lghast.elemenix.register.system.ModStats;
import net.lghast.elemenix.register.system.ModTags;
import net.lghast.elemenix.utils.Constituents;
import net.lghast.elemenix.utils.imitforge.Imitforging;
import net.lghast.elemenix.utils.elemenix.ElemenixInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TransformerMenu extends AbstractContainerMenu {
    public static final int TRANSFORM_IN_A = 0;
    public static final int TRANSFORM_IN_B = 1;
    public static final int TRANSFORM_OUT = 2;

    public static final int MINERAL_SLOT = 3;
    public static final int MEDIUM_SLOT = 4;
    public static final int SPECIAL_OUTPUT_SLOT = 5;

    public static final int GENE_TEMPLATE_SLOT = 3;
    public static final int GERMINATING_PRODUCT_SLOT_FIRST = 4;
    public static final int GERMINATING_PRODUCT_SLOT_COUNT = 6;

    public static final int IMITFORGING_TARGET_SLOT = 3;
    public static final int IMITFORGING_APPEARANCE_SLOT = 4;

    private static final int[] SLOT_X_TRANSFORMING = {42, 42, 42};
    private static final int[] SLOT_Y_TRANSFORMING = {21, 40, 63};

    private static final int SLOT_X_MINERAL = 26;
    private static final int SLOT_Y_MINERAL = 52;
    private static final int SLOT_X_MEDIUM = 134;
    private static final int SLOT_Y_MEDIUM = 52;
    private static final int SLOT_X_ORE = 81;
    private static final int SLOT_Y_ORE = 52;

    private static final int SLOT_X_TEMPLATE = 46;
    private static final int SLOT_Y_TEMPLATE = 30;
    private static final int SLOT_Y_PRODUCT = 60;

    private static final int SLOT_X_IMITFORGING_TARGET = 36;
    private static final int SLOT_Y_IMITFORGING_TARGET = 49;
    private static final int SLOT_X_IMITFORGING_APPEARANCE = 80;
    private static final int SLOT_Y_IMITFORGING_APPEARANCE = 49;
    private static final int SLOT_X_IMITFORGING_OUTPUT = 124;
    private static final int SLOT_Y_IMITFORGING_OUTPUT = 49;

    private final TransformerBlockEntity blockEntity;
    private final Player player;
    private final Container previewContainer = new SimpleContainer(1);
    private final ContainerData data;
    private final ResourceLocation guiTexture;
    private final int playerInvStart;

    public TransformerMenu(int containerId, Inventory playerInventory, TransformerBlockEntity blockEntity) {
        this(containerId, playerInventory, blockEntity,
                blockEntity.getTransformerBlock() != null ?
                        blockEntity.getTransformerBlock().getGuiTexture() :
                        getDefaultTexture());
    }

    public TransformerMenu(int containerId, Inventory playerInventory, TransformerBlockEntity blockEntity, ResourceLocation guiTexture) {
        super(ModMenus.TRANSFORMER_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.player = playerInventory.player;
        this.guiTexture = guiTexture;

        checkContainerSize(blockEntity, 10);

        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> blockEntity.getInputA();
                    case 1 -> blockEntity.getInputB();
                    case 2 -> blockEntity.getOutputC();
                    case 3 -> blockEntity.getMode();
                    case 4 -> blockEntity.getGerminatingProgress();
                    case 5 -> blockEntity.isTransformEnabled() ? 1 : 0;
                    case 6 -> blockEntity.isEnrichEnabled() ? 1 : 0;
                    default -> 0;
                };
            }
            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> blockEntity.setInputA(value);
                    case 1 -> blockEntity.setInputB(value);
                    case 2 -> blockEntity.setOutputC(value);
                    case 3 -> blockEntity.setMode(value);
                    case 4 -> blockEntity.setGerminatingProgress(value);
                    case 5 -> blockEntity.setTransformEnabled(value != 0);
                    case 6 -> blockEntity.setEnrichEnabled(value != 0);
                }
            }
            @Override
            public int getCount() {
                return 7;
            }
        };
        checkContainerDataCount(data, 7);

        checkContainerDataCount(data, 5);
        addSlots(playerInventory);
        addDataSlots(data);

        this.playerInvStart = this.slots.size() - 36;
        refreshResultSlot();
    }

    //region Common
    public TransformerBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public ResourceLocation getGuiTexture() {
        return guiTexture;
    }

    public long getInputA() {
        return blockEntity.getInputA();
    }

    public long getInputB() {
        return blockEntity.getInputB();
    }

    public long getOutputC() {
        return blockEntity.getOutputC();
    }

    public ContainerData getData() {
        return data;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity.stillValid(player);
    }

    private static ResourceLocation getDefaultTexture() {
        return ResourceLocation.fromNamespaceAndPath("elemenix", "textures/gui/transformer.png");
    }

    public int getPageCount() {
        TransformerBlock block = blockEntity.getTransformerBlock();
        return block != null ? block.getPageCount() : 1;
    }

    public int getMode() {
        int mode = blockEntity.getMode();
        int pageCount = getPageCount();
        return mode >= 0 && mode < pageCount ? mode : TransformerBlockEntity.MODE_TRANSFORMING;
    }

    public boolean isTransformEnabled() {
        return blockEntity.isTransformEnabled();
    }

    public boolean isEnrichEnabled() {
        return blockEntity.isEnrichEnabled();
    }

    public void toggleTransform() {
        blockEntity.toggleTransform();
    }

    public void toggleEnrich() {
        blockEntity.toggleEnrich();
    }

    public void switchPage(int page) {
        if (page < 0 || page >= getPageCount()) return;
        if (blockEntity.getMode() == page) return;
        blockEntity.setMode(page);
        if (page == TransformerBlockEntity.MODE_SPECIAL && isGerminatingPage()) {
            blockEntity.setTransformEnabled(false);
            blockEntity.setEnrichEnabled(false);
        }
        refreshResultSlot();
        broadcastChanges();
    }

    private void refreshResultSlot() {
        ItemStack preview = ItemStack.EMPTY;
        if (getMode() == TransformerBlockEntity.MODE_SPECIAL) {
            if (isMineralizingPage()) {
                MineralizingRecipe recipe = getCurrentMineralizingRecipe();
                if (recipe != null) {
                    preview = recipe.result().copy();
                }
            } else if (isImitforgingPage() && canImitforge()) {
                preview = getImitforgingResult();
            }
        }
        previewContainer.setItem(0, preview);
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (container == blockEntity && player != null && !player.level().isClientSide) {
            refreshResultSlot();
        }
    }

    @Override
    public void broadcastChanges() {
        if (player != null && !player.level().isClientSide) {
            refreshResultSlot();
        }
        super.broadcastChanges();
    }

    private void addSlots(Inventory playerInventory) {
        this.addSlot(new Slot(blockEntity, TRANSFORM_IN_A, SLOT_X_TRANSFORMING[0], SLOT_Y_TRANSFORMING[0]) {
            @Override
            public boolean isActive() {
                return getMode() == TransformerBlockEntity.MODE_TRANSFORMING;
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                TransformerBlock block = blockEntity.getTransformerBlock();
                if (block == null) return false;
                if (ElemenixInfo.isUndeconstructable(stack) || stack.is(ModTags.IGNORED_BY_DECONSTRUCTOR_INPUT)) return false;

                Constituents constituents = ElemenixInfo.getConstituents(stack);
                return constituents.isPure(block.getInputElemenixA());
            }

            @Override
            public void setChanged() {
                super.setChanged();
                TransformerMenu.this.slotsChanged(blockEntity);
            }
        });

        this.addSlot(new Slot(blockEntity, TRANSFORM_IN_B, SLOT_X_TRANSFORMING[1], SLOT_Y_TRANSFORMING[1]) {
            @Override
            public boolean isActive() {
                return getMode() == TransformerBlockEntity.MODE_TRANSFORMING;
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                TransformerBlock block = blockEntity.getTransformerBlock();
                if (block == null) return false;
                if (ElemenixInfo.isUndeconstructable(stack) || stack.is(ModTags.IGNORED_BY_DECONSTRUCTOR_INPUT)) return false;

                Constituents constituents = ElemenixInfo.getConstituents(stack);
                return constituents.isPure(block.getInputElemenixB());
            }

            @Override
            public void setChanged() {
                super.setChanged();
                TransformerMenu.this.slotsChanged(blockEntity);
            }
        });

        this.addSlot(new Slot(blockEntity, TRANSFORM_OUT, SLOT_X_TRANSFORMING[2], SLOT_Y_TRANSFORMING[2]) {
            @Override
            public boolean isActive() {
                return getMode() == TransformerBlockEntity.MODE_TRANSFORMING;
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        if (isGerminatingPage()) {
            addGerminalSlots();
        } else if (isMineralizingPage()) {
            addMineralizingSlots();
        } else if (isImitforgingPage()) {
            addImitforgingSlots();
        }

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 88 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 146));
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        if (getMode() == TransformerBlockEntity.MODE_SPECIAL) {
            if (isGerminatingPage()) {
                return quickMoveForGerminating(player, index);
            }
            if (isImitforgingPage()) {
                return quickMoveFroImitforging(player, index);
            }
            if (index == SPECIAL_OUTPUT_SLOT) {
                return quickMoveForMineralizing(player);
            }
        }

        ItemStack originalStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            originalStack = slotStack.copy();

            boolean moved = false;

            if (index < 3 || (getMode() == TransformerBlockEntity.MODE_SPECIAL && index <= SPECIAL_OUTPUT_SLOT)) {
                moved = this.moveItemStackTo(slotStack, playerInvStart, playerInvStart + 36, true);
            } else {
                if (getMode() == TransformerBlockEntity.MODE_SPECIAL) {
                    Slot mineralSlot = this.slots.get(MINERAL_SLOT);
                    if (mineralSlot.mayPlace(slotStack) && !mineralSlot.hasItem()) {
                        moved = this.moveItemStackTo(slotStack, MINERAL_SLOT, MINERAL_SLOT + 1, false);
                    }
                    if (!moved) {
                        Slot mediumSlot = this.slots.get(MEDIUM_SLOT);
                        if (mediumSlot.mayPlace(slotStack) && !mediumSlot.hasItem()) {
                            moved = this.moveItemStackTo(slotStack, MEDIUM_SLOT, MEDIUM_SLOT + 1, false);
                        }
                    }
                } else {
                    Slot slotA = this.slots.getFirst();
                    if (slotA.mayPlace(slotStack) && !slotA.hasItem()) {
                        moved = this.moveItemStackTo(slotStack, TRANSFORM_IN_A, TRANSFORM_IN_A + 1, false);
                    }
                    if (!moved) {
                        Slot slotB = this.slots.get(TRANSFORM_IN_B);
                        if (slotB.mayPlace(slotStack) && !slotB.hasItem()) {
                            moved = this.moveItemStackTo(slotStack, TRANSFORM_IN_B, TRANSFORM_IN_B + 1, false);
                        }
                    }
                }

                if (!moved) {
                    if (index < playerInvStart + 27) {
                        moved = this.moveItemStackTo(slotStack, playerInvStart + 27, playerInvStart + 36, false);
                    } else {
                        moved = this.moveItemStackTo(slotStack, playerInvStart, playerInvStart + 27, false);
                    }
                }
            }

            if (!moved) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == originalStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }

        return originalStack;
    }
    // endregion

    //region Mineralizing
    public boolean isMineralizingPage() {
        return blockEntity.getTransformerBlock() instanceof GeologicalSimulatorBlock;
    }

    public boolean isMineral(ItemStack stack) {
        return !stack.isEmpty() && matchesMineralizingIngredient(stack, true);
    }

    public boolean isMedium(ItemStack stack) {
        return !stack.isEmpty() && matchesMineralizingIngredient(stack, false);
    }

    @Nullable
    public MineralizingRecipe getCurrentMineralizingRecipe() {
        Level level = blockEntity.getLevel();
        if (level == null) return null;
        return MineralizingRecipe.findRecipe(level, blockEntity.getItem(MINERAL_SLOT), blockEntity.getItem(MEDIUM_SLOT));
    }

    private boolean matchesMineralizingIngredient(ItemStack stack, boolean checkMineral) {
        Level level = blockEntity.getLevel();
        if (level == null) return false;
        for (RecipeHolder<MineralizingRecipe> holder :
                level.getRecipeManager().getAllRecipesFor(ModRecipes.MINERALIZING.get())) {
            MineralizingRecipe recipe = holder.value();
            boolean matches = checkMineral
                    ? recipe.mineral().test(stack)
                    : recipe.medium().test(stack);
            if (matches) return true;
        }
        return false;
    }

    private void consumeMineralizingCraft(MineralizingRecipe recipe, Player player) {
        ItemStack mineral = blockEntity.getItem(MINERAL_SLOT);
        ItemStack medium = blockEntity.getItem(MEDIUM_SLOT);
        if (mineral.isEmpty() || medium.isEmpty()) return;

        Item mediumItem = medium.getItem();
        int flumixCost = recipe.getFlumixCost(mineral);
        mineral.shrink(1);
        medium.shrink(1);
        blockEntity.setItem(MINERAL_SLOT, mineral);
        blockEntity.setItem(MEDIUM_SLOT, medium);

        if (flumixCost > 0) {
            long flumix = blockEntity.getInputB();
            blockEntity.setInputB((int) Math.max(0, flumix - (long) flumixCost));
            blockEntity.setChanged();
        }

        Level level = blockEntity.getLevel();
        if (level != null) {
            Block block = Block.byItem(mediumItem);
            if (block != Blocks.AIR) {
                BlockState state = block.defaultBlockState();
                level.playSound(null, player.blockPosition(),
                        state.getSoundType(level, player.blockPosition(), player).getBreakSound(),
                        SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }
    }

    private void addMineralizingSlots() {
        this.addSlot(new Slot(blockEntity, MINERAL_SLOT, SLOT_X_MINERAL, SLOT_Y_MINERAL) {
            @Override
            public boolean isActive() {
                return getMode() == TransformerBlockEntity.MODE_SPECIAL;
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return TransformerMenu.this.isMineral(stack);
            }

            @Override
            public void setChanged() {
                super.setChanged();
                TransformerMenu.this.slotsChanged(blockEntity);
            }
        });

        this.addSlot(new Slot(blockEntity, MEDIUM_SLOT, SLOT_X_MEDIUM, SLOT_Y_MEDIUM) {
            @Override
            public boolean isActive() {
                return getMode() == TransformerBlockEntity.MODE_SPECIAL;
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return TransformerMenu.this.isMedium(stack);
            }

            @Override
            public void setChanged() {
                super.setChanged();
                TransformerMenu.this.slotsChanged(blockEntity);
            }
        });

        this.addSlot(new Slot(previewContainer, 0, SLOT_X_ORE, SLOT_Y_ORE) {
            @Override
            public boolean isActive() {
                return getMode() == TransformerBlockEntity.MODE_SPECIAL;
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                if (ElemenixInfo.isCalculating()) return false;

                MineralizingRecipe recipe = getCurrentMineralizingRecipe();
                if (recipe == null || !hasItem()) return false;

                return blockEntity.getInputB() >= recipe.getFlumixCost(blockEntity.getItem(MINERAL_SLOT));
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                MineralizingRecipe recipe = getCurrentMineralizingRecipe();
                if (recipe != null) {
                    consumeMineralizingCraft(recipe, player);
                }
                player.awardStat(ModStats.MINERALIZING_COUNT.get());
                if (player instanceof ServerPlayer serverPlayer) {
                    MineralizeTrigger.TRIGGER.get().trigger(serverPlayer);
                }
                refreshResultSlot();
                broadcastChanges();
                super.onTake(player, stack);
            }
        });
    }

    private ItemStack quickMoveForMineralizing(Player player) {
        MineralizingRecipe recipe = getCurrentMineralizingRecipe();
        Slot result = this.slots.get(SPECIAL_OUTPUT_SLOT);
        if (recipe == null || !result.hasItem() || !result.mayPickup(player)) return ItemStack.EMPTY;

        ItemStack crafted = result.getItem().copy();
        if (crafted.getCount() != 1) return ItemStack.EMPTY;

        ItemStack toMove = crafted.copy();
        if (!this.moveItemStackTo(toMove, playerInvStart, playerInvStart + 36, false)) return ItemStack.EMPTY;
        if (!toMove.isEmpty()) return ItemStack.EMPTY;

        result.set(ItemStack.EMPTY);
        consumeMineralizingCraft(recipe, player);
        player.awardStat(ModStats.MINERALIZING_COUNT.get());

        if (player instanceof ServerPlayer serverPlayer) {
            MineralizeTrigger.TRIGGER.get().trigger(serverPlayer);
        }

        refreshResultSlot();
        broadcastChanges();
        return crafted;
    }
    //endregion

    //region Germinating
    public boolean isGerminatingPage() {
        return blockEntity.getTransformerBlock() instanceof GerminalAcceleratorBlock;
    }

    public boolean isGerminatingTemplate(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Level level = blockEntity.getLevel();
        if (level == null) return false;
        for (RecipeHolder<GerminatingRecipe> holder :
                level.getRecipeManager().getAllRecipesFor(ModRecipes.GERMINATING.get())) {
            if (holder.value().template().test(stack)) return true;
        }
        return false;
    }

    public int getGerminatingProgress() {
        return blockEntity.getGerminatingProgress();
    }

    @Nullable
    public GerminatingRecipe getCurrentGerminatingRecipe() {
        Level level = blockEntity.getLevel();
        if (level == null) return null;
        return GerminatingRecipe.findRecipe(level, blockEntity.getItem(GENE_TEMPLATE_SLOT));
    }

    @Nullable
    public int[] getGerminatingCost() {
        if (ElemenixInfo.isCalculating()) return null;

        GerminatingRecipe recipe = getCurrentGerminatingRecipe();
        return recipe != null ? recipe.getCost() : null;
    }

    private void addGerminalSlots() {
        this.addSlot(new Slot(blockEntity, GENE_TEMPLATE_SLOT, SLOT_X_TEMPLATE, SLOT_Y_TEMPLATE) {
            @Override
            public boolean isActive() {
                return getMode() == TransformerBlockEntity.MODE_SPECIAL;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return TransformerMenu.this.isGerminatingTemplate(stack);
            }

            @Override
            public void setChanged() {
                super.setChanged();
                TransformerMenu.this.slotsChanged(blockEntity);
            }
        });

        for (int i = 0; i < GERMINATING_PRODUCT_SLOT_COUNT; i++) {
            this.addSlot(new Slot(blockEntity, GERMINATING_PRODUCT_SLOT_FIRST + i, 49 + i * 18, SLOT_Y_PRODUCT) {
                @Override
                public boolean isActive() {
                    return getMode() == TransformerBlockEntity.MODE_SPECIAL;
                }

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }

                @Override
                public void onTake(Player player, ItemStack stack) {
                    if (player instanceof ServerPlayer serverPlayer) {
                        GerminateTrigger.TRIGGER.get().trigger(serverPlayer);
                    }
                    super.onTake(player, stack);
                }
            });
        }
    }

    private ItemStack quickMoveForGerminating(Player player, int index) {
        ItemStack originalStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            originalStack = slotStack.copy();

            boolean moved;
            if (index >= GENE_TEMPLATE_SLOT && index < playerInvStart) {
                moved = this.moveItemStackTo(slotStack, playerInvStart, playerInvStart + 36, true);
                if (moved && slotStack.isEmpty() && index > GENE_TEMPLATE_SLOT && player instanceof ServerPlayer serverPlayer) {
                    GerminateTrigger.TRIGGER.get().trigger(serverPlayer);
                }
            } else {
                Slot template = this.slots.get(GENE_TEMPLATE_SLOT);
                if (template.mayPlace(slotStack) && !template.hasItem()) {
                    moved = this.moveItemStackTo(slotStack, GENE_TEMPLATE_SLOT, GENE_TEMPLATE_SLOT + 1, false);
                } else {
                    moved = index < playerInvStart + 27
                            ? this.moveItemStackTo(slotStack, playerInvStart + 27, playerInvStart + 36, false)
                            : this.moveItemStackTo(slotStack, playerInvStart, playerInvStart + 27, false);
                }
            }

            if (!moved) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == originalStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }

        return originalStack;
    }
    //endregion

    //region Imitforging
    public boolean isImitforgingPage() {
        return blockEntity.getTransformerBlock() instanceof MetallurgicalActivatorBlock;
    }

    public boolean isImitforgingTarget(ItemStack stack) {
        return Imitforging.isImitforgable(stack);
    }

    public boolean isImitforgingAppearance(ItemStack stack) {
        return !stack.isEmpty() && Imitforging.isAllowedAppearance(stack);
    }

    public boolean canImitforge() {
        ItemStack target = blockEntity.getItem(IMITFORGING_TARGET_SLOT);
        ItemStack appearance = blockEntity.getItem(IMITFORGING_APPEARANCE_SLOT);
        return Imitforging.canApply(target, appearance);
    }

    public boolean canAffordImitforging() {
        if (!canImitforge()) return false;
        if (isImitforgingRemoval()) return true;

        int cost = Imitforging.getEnergixCost(blockEntity.getItem(IMITFORGING_TARGET_SLOT));
        return cost >= 0 && blockEntity.getInputB() >= cost;
    }

    public int getImitforgingCost() {
        if (ElemenixInfo.isCalculating()) return 0;
        if (isImitforgingRemoval()) return 0;

        int cost = Imitforging.getEnergixCost(blockEntity.getItem(IMITFORGING_TARGET_SLOT));
        return Math.max(cost, 0);
    }

    public ItemStack getImitforgingResult() {
        ItemStack target = blockEntity.getItem(IMITFORGING_TARGET_SLOT);
        ItemStack appearance = blockEntity.getItem(IMITFORGING_APPEARANCE_SLOT);
        return Imitforging.forgeResult(target, appearance);
    }

    @Nullable
    public Component getImitforgingError() {
        if (ElemenixInfo.isCalculating()) return null;
        if (Imitforging.isDisabled()) {
            return Component.translatable("gui.elemenix.imitforging.error.disabled");
        }

        ItemStack target = blockEntity.getItem(IMITFORGING_TARGET_SLOT);
        ItemStack appearance = blockEntity.getItem(IMITFORGING_APPEARANCE_SLOT);
        if (target.isEmpty()) return null;

        boolean removal = isImitforgingRemoval();
        if (!removal) {
            if (appearance.isEmpty()) return null;
            if (target.getItem() == appearance.getItem()) {
                return Component.translatable("gui.elemenix.imitforging.error.same_item");
            }
        } else {
            return null;
        }

        int cost = Imitforging.getEnergixCost(target);
        if (cost < 0) return null;
        if (blockEntity.getInputB() < cost) {
            return Component.translatable("gui.elemenix.imitforging.error.not_enough_energix");
        }
        return null;
    }

    private boolean isImitforgingRemoval() {
        return Imitforging.isImitforged(blockEntity.getItem(IMITFORGING_TARGET_SLOT))
                && blockEntity.getItem(IMITFORGING_APPEARANCE_SLOT).isEmpty();
    }

    private void consumeImitforging(Player player) {
        ItemStack target = blockEntity.getItem(IMITFORGING_TARGET_SLOT);
        ItemStack appearance = blockEntity.getItem(IMITFORGING_APPEARANCE_SLOT);
        if (target.isEmpty()) return;

        boolean removal = appearance.isEmpty();
        target.shrink(1);
        blockEntity.setItem(IMITFORGING_TARGET_SLOT, target);

        if (!removal) {
            int cost = Imitforging.getEnergixCost(target);
            if (cost > 0) {
                blockEntity.setInputB((int) Math.max(0, (long) blockEntity.getInputB() - cost));
            }
        }
        blockEntity.setChanged();

        Level level = blockEntity.getLevel();
        if (level != null) {
            level.playSound(null, player.blockPosition(), SoundEvents.SMITHING_TABLE_USE,
                    SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    private void addImitforgingSlots() {
        this.addSlot(new Slot(blockEntity, IMITFORGING_TARGET_SLOT, SLOT_X_IMITFORGING_TARGET, SLOT_Y_IMITFORGING_TARGET) {
            @Override
            public boolean isActive() {
                return getMode() == TransformerBlockEntity.MODE_SPECIAL;
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return TransformerMenu.this.isImitforgingTarget(stack);
            }

            @Override
            public void setChanged() {
                super.setChanged();
                TransformerMenu.this.slotsChanged(blockEntity);
            }
        });

        this.addSlot(new Slot(blockEntity, IMITFORGING_APPEARANCE_SLOT, SLOT_X_IMITFORGING_APPEARANCE, SLOT_Y_IMITFORGING_APPEARANCE) {
            @Override
            public boolean isActive() {
                return getMode() == TransformerBlockEntity.MODE_SPECIAL;
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return TransformerMenu.this.isImitforgingAppearance(stack);
            }

            @Override
            public void setChanged() {
                super.setChanged();
                TransformerMenu.this.slotsChanged(blockEntity);
            }
        });

        this.addSlot(new Slot(previewContainer, 0, SLOT_X_IMITFORGING_OUTPUT, SLOT_Y_IMITFORGING_OUTPUT) {
            @Override
            public boolean isActive() {
                return getMode() == TransformerBlockEntity.MODE_SPECIAL;
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                if (ElemenixInfo.isCalculating()) return false;
                return hasItem() && TransformerMenu.this.canAffordImitforging();
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                boolean removal = TransformerMenu.this.isImitforgingRemoval();
                TransformerMenu.this.consumeImitforging(player);
                if (!removal) {
                    player.awardStat(ModStats.IMITFORGING_COUNT.get());
                    if (player instanceof ServerPlayer serverPlayer) {
                        ImitforgeTrigger.TRIGGER.get().trigger(serverPlayer);
                    }
                }
                refreshResultSlot();
                broadcastChanges();
                super.onTake(player, stack);
            }
        });
    }

    private ItemStack quickMoveFroImitforging(Player player, int index) {
        ItemStack originalStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return originalStack;
        }

        ItemStack slotStack = slot.getItem();
        originalStack = slotStack.copy();
        if (index == SPECIAL_OUTPUT_SLOT) {
            if (!slot.mayPickup(player)) return ItemStack.EMPTY;

            ItemStack crafted = slot.getItem().copy();
            if (crafted.getCount() != 1) return ItemStack.EMPTY;

            ItemStack toMove = crafted.copy();
            if (!this.moveItemStackTo(toMove, playerInvStart, playerInvStart + 36, false)) return ItemStack.EMPTY;
            if (!toMove.isEmpty()) return ItemStack.EMPTY;

            boolean removal = isImitforgingRemoval();
            slot.set(ItemStack.EMPTY);
            consumeImitforging(player);

            if (!removal) {
                player.awardStat(ModStats.IMITFORGING_COUNT.get());
                if (player instanceof ServerPlayer serverPlayer) {
                    ImitforgeTrigger.TRIGGER.get().trigger(serverPlayer);
                }
            }

            refreshResultSlot();
            broadcastChanges();
            return crafted;
        }

        boolean moved = false;
        if (index >= IMITFORGING_TARGET_SLOT && index <= SPECIAL_OUTPUT_SLOT) {
            moved = this.moveItemStackTo(slotStack, playerInvStart, playerInvStart + 36, true);
        } else {
            Slot targetSlot = this.slots.get(IMITFORGING_TARGET_SLOT);
            if (targetSlot.mayPlace(slotStack) && !targetSlot.hasItem()) {
                moved = this.moveItemStackTo(slotStack,
                        IMITFORGING_TARGET_SLOT,
                        IMITFORGING_TARGET_SLOT + 1,
                        false
                );
            }
            if (!moved) {
                Slot appearanceSlot = this.slots.get(IMITFORGING_APPEARANCE_SLOT);
                if (appearanceSlot.mayPlace(slotStack) && !appearanceSlot.hasItem()) {
                    moved = this.moveItemStackTo(slotStack,
                            IMITFORGING_APPEARANCE_SLOT,
                            IMITFORGING_APPEARANCE_SLOT + 1,
                            false
                    );
                }
            }
            if (!moved) {
                moved = index < playerInvStart + 27
                        ? this.moveItemStackTo(slotStack, playerInvStart + 27, playerInvStart + 36, false)
                        : this.moveItemStackTo(slotStack, playerInvStart, playerInvStart + 27, false);
            }
        }

        if (!moved) {
            return ItemStack.EMPTY;
        }
        if (slotStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (slotStack.getCount() == originalStack.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, slotStack);
        return originalStack;
    }
    //endregion
}