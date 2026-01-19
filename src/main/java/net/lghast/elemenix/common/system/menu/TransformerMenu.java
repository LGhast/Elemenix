package net.lghast.elemenix.common.system.menu;

import net.lghast.elemenix.common.content.block.TransformerBlock;
import net.lghast.elemenix.common.content.blockentity.TransformerBlockEntity;
import net.lghast.elemenix.register.system.ModMenus;
import net.lghast.elemenix.utils.Constituents;
import net.lghast.elemenix.utils.ElemenixInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TransformerMenu extends AbstractContainerMenu {
    private final TransformerBlockEntity blockEntity;
    private final ContainerData data;
    private final ResourceLocation guiTexture;

    public TransformerMenu(int containerId, Inventory playerInventory, TransformerBlockEntity blockEntity) {
        this(containerId, playerInventory, blockEntity,
                blockEntity.getTransformerBlock() != null ?
                        blockEntity.getTransformerBlock().getGuiTexture() :
                        getDefaultTexture());
    }

    public TransformerMenu(int containerId, Inventory playerInventory, TransformerBlockEntity blockEntity, ResourceLocation guiTexture) {
        super(ModMenus.TRANSFORMER_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.guiTexture = guiTexture;

        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> blockEntity.getInputA();
                    case 1 -> blockEntity.getInputB();
                    case 2 -> blockEntity.getOutputC();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> blockEntity.setInputA(value);
                    case 1 -> blockEntity.setInputB(value);
                    case 2 -> blockEntity.setOutputC(value);
                }
            }

            @Override
            public int getCount() {
                return 3;
            }
        };

        addSlots(playerInventory);
        addDataSlots(data);
    }

    private static ResourceLocation getDefaultTexture() {
        return ResourceLocation.fromNamespaceAndPath("elemenix", "textures/gui/transformer.png");
    }

    private void addSlots(Inventory playerInventory) {
        this.addSlot(new Slot(blockEntity, 0, 42, 21) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                TransformerBlock block = blockEntity.getTransformerBlock();
                if (block == null) return false;
                if(ElemenixInfo.isUndeconstructable(stack)) return false;

                Constituents constituents = ElemenixInfo.getConstituents(stack);
                return constituents.isPure(block.getInputElemenixA());
            }
        });

        this.addSlot(new Slot(blockEntity, 1, 42, 40) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                TransformerBlock block = blockEntity.getTransformerBlock();
                if(block == null) return false;
                if(ElemenixInfo.isUndeconstructable(stack)) return false;

                Constituents constituents = ElemenixInfo.getConstituents(stack);
                return constituents.isPure(block.getInputElemenixB());
            }
        });

        this.addSlot(new Slot(blockEntity, 2, 42, 63) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 88 + i * 18));
            }
        }

        for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 146));
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        ItemStack originalStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            originalStack = slotStack.copy();

            boolean moved = false;

            if (index < 3) {
                moved = this.moveItemStackTo(slotStack, 3, 39, true);
            } else {
                ItemStack tempStack = slotStack.copy();
                Slot slotA = slots.get(0);
                if (slotA.mayPlace(tempStack) && !slotA.hasItem()) {
                    moved = this.moveItemStackTo(slotStack, 0, 1, false);
                }

                if (!moved) {
                    Slot slotB = slots.get(1);
                    if (slotB.mayPlace(tempStack) && !slotB.hasItem()) {
                        moved = this.moveItemStackTo(slotStack, 1, 2, false);
                    }
                }

                if (!moved) {
                    if (index < 3 + 27) {
                        moved = this.moveItemStackTo(slotStack, 30, 39, false);
                    } else {
                        moved = this.moveItemStackTo(slotStack, 3, 30, false);
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