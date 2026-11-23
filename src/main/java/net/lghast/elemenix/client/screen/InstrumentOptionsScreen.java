package net.lghast.elemenix.client.screen;

import net.lghast.elemenix.client.misc.Palette;
import net.lghast.elemenix.client.render.ItemIconRenderer;
import net.lghast.elemenix.common.util.ModUtils;
import net.lghast.elemenix.network.memorizerbox.CollectMemorizersPayload;
import net.lghast.elemenix.network.open.OpenAnalysisTerminalPayload;
import net.lghast.elemenix.network.open.OpenAnalyzerPayload;
import net.lghast.elemenix.network.open.OpenMemorizerBoxPayload;
import net.lghast.elemenix.network.open.OpenMemoryBurnerPayload;
import net.lghast.elemenix.register.content.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class InstrumentOptionsScreen extends Screen {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("elemenix", "textures/gui/instrument_options.png");

    private static final int GUI_WIDTH = 110;
    private static final int GUI_HEIGHT = 50;
    private static final int TITLE_Y = 6;
    private static final int SLOT_Y = 22;
    private static final int SLOT_SIZE = 16;
    private static final int[] SLOT_X = {11, 35, 59, 83};

    private static final String[] SLOT_IDS = {"analyzer", "memorizer_box", "analysis_terminal", "memory_burner"};
    private static final Item[] SLOT_ITEMS = {
            ModItems.ELEMENIC_ANALYZER.asItem(),
            ModItems.MEMORIZER_BOX.asItem(),
            ModItems.ANALYSIS_TERMINAL.asItem(),
            ModItems.MEMORY_BURNER.asItem()
    };

    private final List<ItemStack> carried;

    public InstrumentOptionsScreen() {
        super(Component.translatable("gui.elemenix.instrument_options"));
        this.carried = getCarriedInstruments();
    }

    public static List<ItemStack> getCarriedInstruments() {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return List.of(ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY);
        }
        return List.of(
                ModUtils.findItemInCuriosSlot(player, SLOT_IDS[0], SLOT_ITEMS[0]),
                ModUtils.findItemInCuriosSlot(player, SLOT_IDS[1], SLOT_ITEMS[1]),
                ModUtils.findItemInCuriosSlot(player, SLOT_IDS[2], SLOT_ITEMS[2]),
                ModUtils.findItemInCuriosSlot(player, SLOT_IDS[3], SLOT_ITEMS[3])
        );
    }

    public static int getCarriedCount() {
        int count = 0;
        for (ItemStack stack : getCarriedInstruments()) {
            if (!stack.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new InstrumentOptionsScreen());
    }

    public static void openForCurrentCarried() {
        List<ItemStack> stacks = getCarriedInstruments();
        int count = 0;
        int carriedIndex = -1;
        for (int i = 0; i < stacks.size(); i++) {
            if (!stacks.get(i).isEmpty()) {
                count++;
                carriedIndex = i;
            }
        }
        if (count == 1) {
            openCarriedInstrument(carriedIndex);
        } else if (count > 1) {
            open();
        }
    }

    public static boolean openFromScreen(Screen screen) {
        if (getCarriedCount() > 1) {
            screen.onClose();
            open();
            return true;
        }
        return false;
    }

    private static void openCarriedInstrument(int index) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            return;
        }
        switch (index) {
            case 0 -> connection.send(new OpenAnalyzerPayload());
            case 1 -> connection.send(new OpenMemorizerBoxPayload());
            case 2 -> connection.send(new OpenAnalysisTerminalPayload());
            case 3 -> connection.send(new OpenMemoryBurnerPayload());
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        int x = (this.width - GUI_WIDTH) / 2;
        int y = (this.height - GUI_HEIGHT) / 2;

        graphics.blit(TEXTURE, x, y, 0, 0, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);
        graphics.drawString(this.font, this.title, x + (GUI_WIDTH - this.font.width(this.title)) / 2, y + TITLE_Y, Palette.BLACK, false);

        for (int i = 0; i < 4; i++) {
            int slotX = x + SLOT_X[i];
            int slotY = y + SLOT_Y;
            ItemStack stack = carried.get(i);
            if (stack.isEmpty()) {
                graphics.blit(slotTexture(i), slotX, slotY, 0, 0, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE);
            } else {
                ItemIconRenderer.render(graphics, stack, slotX, slotY);
            }
            if (isHovering(i, mouseX, mouseY)) {
                graphics.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, Palette.HIGHLIGHT);
            }
        }

        int hovered = getHoveredSlot(mouseX, mouseY);
        if (hovered >= 0) {
            renderSlotTooltip(graphics, hovered, mouseX, mouseY);
        }
    }

    private static ResourceLocation slotTexture(int index) {
        return ResourceLocation.fromNamespaceAndPath("elemenix", "textures/slot/" + SLOT_IDS[index] + ".png");
    }

    private int getHoveredSlot(int mouseX, int mouseY) {
        for (int i = 0; i < 4; i++) {
            if (isHovering(i, mouseX, mouseY)) {
                return i;
            }
        }
        return -1;
    }

    private boolean isHovering(int index, int mouseX, int mouseY) {
        int x = (this.width - GUI_WIDTH) / 2;
        int y = (this.height - GUI_HEIGHT) / 2;
        int slotX = x + SLOT_X[index];
        int slotY = y + SLOT_Y;
        return mouseX >= slotX && mouseX < slotX + SLOT_SIZE && mouseY >= slotY && mouseY < slotY + SLOT_SIZE;
    }

    private void renderSlotTooltip(GuiGraphics graphics, int index, int mouseX, int mouseY) {
        ItemStack stack = carried.get(index);
        Component name = stack.isEmpty()
                ? new ItemStack(SLOT_ITEMS[index]).getHoverName().copy().withColor(Palette.WHITE)
                : stack.getHoverName().copy().withColor(Palette.WHITE);
        if (stack.isEmpty()) {
            graphics.renderComponentTooltip(this.font,
                    List.of(name, Component.translatable("tooltip.elemenix.not_carried").withColor(Palette.GRAY_DARK)),
                    mouseX, mouseY);
        } else if (index == 2 && (carried.get(0).isEmpty() || carried.get(1).isEmpty())) {
            graphics.renderComponentTooltip(this.font,
                    List.of(name, Component.translatable("tooltip.elemenix.requires_analyzer_or_box").withColor(Palette.GRAY_DARK)),
                    mouseX, mouseY);
        } else if (index == 1) {
            graphics.renderComponentTooltip(this.font,
                    List.of(name, Component.translatable("tooltip.elemenix.memorizer_box.collect_all").withColor(Palette.GRAY_DARK)),
                    mouseX, mouseY);
        } else {
            graphics.renderComponentTooltip(this.font, List.of(name), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int hovered = getHoveredSlot((int) mouseX, (int) mouseY);
        if (hovered >= 0 && !carried.get(hovered).isEmpty()) {
            if (button == 0) {
                openCarriedInstrument(hovered);
                return true;
            }
            if (button == 1 && hovered == 1) {
                ClientPacketListener connection = Minecraft.getInstance().getConnection();
                if (connection != null) {
                    connection.send(new CollectMemorizersPayload());
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
