package net.lghast.elemenix.common.system.menu;

import net.lghast.elemenix.common.content.block.GerminalAcceleratorBlock;
import net.lghast.elemenix.common.content.block.TransformerBlock;
import net.lghast.elemenix.common.content.blockentity.TransformerBlockEntity;
import net.lghast.elemenix.common.system.recipe.GerminatingRecipe;
import net.lghast.elemenix.common.system.recipe.MineralizingRecipe;
import net.lghast.elemenix.register.system.ModMenus;
import net.lghast.elemenix.register.system.ModRecipes;
import net.lghast.elemenix.register.system.ModTags;
import net.lghast.elemenix.utils.Constituents;
import net.lghast.elemenix.utils.elemenix.ElemenixInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
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
    public static final int PREVIEW_SLOT = 5;
    public static final int TEMPLATE_SLOT = 3;
    public static final int PRODUCT_SLOT_FIRST = 4;
    public static final int PRODUCT_SLOT_COUNT = 6;

    private static final int[] SLOT_X_TRANSFORMING = {42, 42, 42};
    private static final int[] SLOT_Y_TRANSFORMING = {21, 40, 63};
    private static final int SLOT_X_MINERAL = 26;
    private static final int SLOT_Y_MINERAL = 52;
    private static final int SLOT_X_PREVIEW = 81;
    private static final int SLOT_Y_PREVIEW = 52;
    private static final int SLOT_X_MEDIUM = 134;
    private static final int SLOT_Y_MEDIUM = 52;
    private static final int SLOT_X_TEMPLATE = 46;
    private static final int SLOT_Y_TEMPLATE = 30;
    private static final int SLOT_Y_PRODUCT = 60;

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

    public void switchPage(int page) {
        if (page < 0 || page >= getPageCount()) return;
        if (blockEntity.getMode() == page) return;
        blockEntity.setMode(page);
        if (page == TransformerBlockEntity.MODE_MINERALIZING && isGerminalPage()) {
            blockEntity.setTransformEnabled(false);
            blockEntity.setEnrichEnabled(false);
        }
        refreshResultSlot();
        broadcastChanges();
    }

    public boolean isGerminalPage() {
        return blockEntity.getTransformerBlock() instanceof GerminalAcceleratorBlock;
    }

    @Nullable
    public MineralizingRecipe getCurrentRecipe() {
        Level level = blockEntity.getLevel();
        if (level == null) return null;
        return MineralizingRecipe.findRecipe(level, blockEntity.getItem(MINERAL_SLOT), blockEntity.getItem(MEDIUM_SLOT));
    }

    public boolean isMineral(ItemStack stack) {
        return !stack.isEmpty() && matchesMineralizingIngredient(stack, true);
    }

    public boolean isMedium(ItemStack stack) {
        return !stack.isEmpty() && matchesMineralizingIngredient(stack, false);
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

    private void refreshResultSlot() {
        ItemStack preview = ItemStack.EMPTY;
        if (getMode() == TransformerBlockEntity.MODE_MINERALIZING && !isGerminalPage()) {
            MineralizingRecipe recipe = getCurrentRecipe();
            if (recipe != null) {
                preview = recipe.result().copy();
            }
        }
        previewContainer.setItem(0, preview);
    }

    private void consumeCraft(MineralizingRecipe recipe) {
        ItemStack mineral = blockEntity.getItem(MINERAL_SLOT);
        ItemStack medium = blockEntity.getItem(MEDIUM_SLOT);
        if (mineral.isEmpty() || medium.isEmpty()) return;
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
    }

    @Nullable
    public GerminatingRecipe getCurrentGerminatingRecipe() {
        Level level = blockEntity.getLevel();
        if (level == null) return null;
        return GerminatingRecipe.findRecipe(level, blockEntity.getItem(TEMPLATE_SLOT));
    }

    @Nullable
    public int[] getGerminatingCost() {
        GerminatingRecipe recipe = getCurrentGerminatingRecipe();
        return recipe != null ? recipe.getCost() : null;
    }

    public int getGerminatingProgress() {
        return blockEntity.getGerminatingProgress();
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

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (container == blockEntity && player != null && !player.level().isClientSide) {
            refreshResultSlot();
        }
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

        if (isGerminalPage()) {
            addGerminalSlots();
        } else {
            addMineralizingSlots();
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

    private void addMineralizingSlots() {
        this.addSlot(new Slot(blockEntity, MINERAL_SLOT, SLOT_X_MINERAL, SLOT_Y_MINERAL) {
            @Override
            public boolean isActive() {
                return getMode() == TransformerBlockEntity.MODE_MINERALIZING;
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
                return getMode() == TransformerBlockEntity.MODE_MINERALIZING;
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

        this.addSlot(new Slot(previewContainer, 0, SLOT_X_PREVIEW, SLOT_Y_PREVIEW) {
            @Override
            public boolean isActive() {
                return getMode() == TransformerBlockEntity.MODE_MINERALIZING;
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                MineralizingRecipe recipe = getCurrentRecipe();
                if (recipe == null || !hasItem()) return false;
                return blockEntity.getInputB() >= recipe.getFlumixCost(blockEntity.getItem(MINERAL_SLOT));
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                MineralizingRecipe recipe = getCurrentRecipe();
                if (recipe != null) {
                    consumeCraft(recipe);
                }
                refreshResultSlot();
                broadcastChanges();
                super.onTake(player, stack);
            }
        });
    }

    private void addGerminalSlots() {
        this.addSlot(new Slot(blockEntity, TEMPLATE_SLOT, SLOT_X_TEMPLATE, SLOT_Y_TEMPLATE) {
            @Override
            public boolean isActive() {
                return getMode() == TransformerBlockEntity.MODE_MINERALIZING;
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

        for (int i = 0; i < PRODUCT_SLOT_COUNT; i++) {
            this.addSlot(new Slot(blockEntity, PRODUCT_SLOT_FIRST + i, 49 + i * 18, SLOT_Y_PRODUCT) {
                @Override
                public boolean isActive() {
                    return getMode() == TransformerBlockEntity.MODE_MINERALIZING;
                }

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        if (getMode() == TransformerBlockEntity.MODE_MINERALIZING) {
            if (isGerminalPage()) {
                return germinalQuickMove(player, index);
            }
            if (index == PREVIEW_SLOT) {
                return quickMovePreview(player);
            }
        }

        ItemStack originalStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            originalStack = slotStack.copy();

            boolean moved = false;

            if (index < 3 || (getMode() == TransformerBlockEntity.MODE_MINERALIZING && index <= PREVIEW_SLOT)) {
                moved = this.moveItemStackTo(slotStack, playerInvStart, playerInvStart + 36, true);
            } else {
                if (getMode() == TransformerBlockEntity.MODE_MINERALIZING) {
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

    private ItemStack germinalQuickMove(Player player, int index) {
        ItemStack originalStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            originalStack = slotStack.copy();

            boolean moved;
            if (index >= TEMPLATE_SLOT && index < playerInvStart) {
                moved = this.moveItemStackTo(slotStack, playerInvStart, playerInvStart + 36, true);
            } else {
                Slot template = this.slots.get(TEMPLATE_SLOT);
                if (template.mayPlace(slotStack) && !template.hasItem()) {
                    moved = this.moveItemStackTo(slotStack, TEMPLATE_SLOT, TEMPLATE_SLOT + 1, false);
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

    private ItemStack quickMovePreview(Player player) {
        MineralizingRecipe recipe = getCurrentRecipe();
        Slot result = this.slots.get(PREVIEW_SLOT);
        if (recipe == null || !result.hasItem() || !result.mayPickup(player)) return ItemStack.EMPTY;

        ItemStack crafted = result.getItem().copy();
        if (crafted.getCount() != 1) return ItemStack.EMPTY;

        ItemStack toMove = crafted.copy();
        if (!this.moveItemStackTo(toMove, playerInvStart, playerInvStart + 36, false)) return ItemStack.EMPTY;
        if (!toMove.isEmpty()) return ItemStack.EMPTY;

        result.set(ItemStack.EMPTY);
        consumeCraft(recipe);
        refreshResultSlot();
        broadcastChanges();
        return crafted;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity.stillValid(player);
    }

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
}