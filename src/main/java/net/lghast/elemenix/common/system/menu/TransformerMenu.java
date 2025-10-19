package net.lghast.elemenix.common.system.menu;

import net.lghast.elemenix.common.content.block.TransformerBlock;
import net.lghast.elemenix.common.content.blockentity.TransformerBlockEntity;
import net.lghast.elemenix.register.system.ModMenus;
import net.lghast.elemenix.utils.Constituents;
import net.lghast.elemenix.utils.ElemenixInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

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

    public TransformerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory,
                (TransformerBlockEntity) playerInventory.player.level().getBlockEntity(extraData.readBlockPos()),
                extraData.readResourceLocation());
    }

    public TransformerMenu(int containerId, Inventory playerInventory, TransformerBlockEntity blockEntity, ResourceLocation guiTexture) {
        super(ModMenus.TRANSFORMER_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.guiTexture = guiTexture;

        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> (int) Math.min(blockEntity.getInputA(), Integer.MAX_VALUE);
                    case 1 -> (int) Math.min(blockEntity.getInputB(), Integer.MAX_VALUE);
                    case 2 -> (int) Math.min(blockEntity.getOutputC(), Integer.MAX_VALUE);
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

                Constituents constituents = ElemenixInfo.getConstituents(stack);
                return constituents.isPure(block.getInputTypeA());
            }
        });

        this.addSlot(new Slot(blockEntity, 1, 42, 40) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                TransformerBlock block = blockEntity.getTransformerBlock();
                if (block == null) return false;

                Constituents constituents = ElemenixInfo.getConstituents(stack);
                return constituents.isPure(block.getInputTypeB());
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
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < 3) {
                if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(itemstack1, 0, 2, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
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