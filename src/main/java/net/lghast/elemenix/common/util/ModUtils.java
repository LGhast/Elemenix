package net.lghast.elemenix.common.util;

import net.lghast.elemenix.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Shared static helpers
 */
public class ModUtils {

    /**
     * Parses and looks up an item by id.
     *
     * @param itemId item id, possibly invalid
     * @return the item, or null when the id is unknown or malformed
     */
    public static Item getItemFromString(String itemId) {
        try {
            ResourceLocation resourceLocation = ResourceLocation.parse(itemId);
            return BuiltInRegistries.ITEM.get(resourceLocation);
        } catch (Exception e) {
            return null;
        }
    }

    public static void spawnParticles(ServerLevel serverLevel, ParticleOptions particle, double x, double y, double z,
                                      double dx, double dy, double dz, int count, double speed) {
        serverLevel.sendParticles(particle, x, y, z, count, dx, dy, dz, speed);
    }

    /**
     * Fits text into maxWidth by binary-searching the longest prefix and appending an ellipsis.
     *
     * @param font     font used for width measurement
     * @param text     original text
     * @param maxWidth available width in pixels
     */
    public static String truncateToWidth(Font font, String text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }

        String ellipsis = "...";
        int ellipsisWidth = font.width(ellipsis);
        int targetWidth = maxWidth - ellipsisWidth;

        if (targetWidth <= 0) {
            return ellipsis;
        }

        int low = 0;
        int high = text.length();
        int bestPos = 0;

        while (low <= high) {
            int mid = (low + high) / 2;
            String substring = text.substring(0, mid);
            int width = font.width(substring);

            if (width <= targetWidth) {
                bestPos = mid;
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        if (bestPos == 0) {
            bestPos = 1;
        }

        return text.substring(0, bestPos).trim() + ellipsis;
    }

    /**
     * Saturating addition: returns Long.MAX_VALUE instead of throwing on overflow.
     */
    public static long safeAdd(long a, long b) {
        try {
            return Math.addExact(a, b);
        } catch (ArithmeticException e) {
            return Long.MAX_VALUE;
        }
    }

    /**
     * Formats a value using the configured grouping:
     * four-digit units (W=ten-thousand, Y=hundred-million, Z=trillion, J=ten-quadrillion)
     * or three-digit units (K, M, B, T, Q).
     */
    public static String formatNumber(long value) {
        boolean useFourGroups;
        if (FMLEnvironment.dist.isClient()) {
            useFourGroups = ClientConfig.DIGIT_GROUPING_BY_FOURS.get();
        } else {
            useFourGroups = true;
        }
        return useFourGroups ? formatByFourGroups(value) : formatByThreeGroups(value);
    }

    public static String formatNumber(int value) {
        return formatNumber((long) value);
    }

    private static final String[] THREE_UNITS = {"", "K", "M", "G", "T", "P", "E"};
    private static final long[] THREE_VALUES = {
            1L,
            1_000L,                      // K - 千
            1_000_000L,                  // M - 百万
            1_000_000_000L,              // G - 十亿
            1_000_000_000_000L,          // T - 万亿
            1_000_000_000_000_000L,      // P - 千兆
            1_000_000_000_000_000_000L   // E - 百京
    };

    private static final String[] FOUR_UNITS = {"", "W", "Y", "Z", "J"};
    private static final long[] FOUR_VALUES = {
            1L,
            10_000L,                  // W - 万
            100_000_000L,             // Y - 亿
            1_000_000_000_000L,       // Z - 兆
            10_000_000_000_000_000L   // J - 京
    };

    public static String formatByThreeGroups(long value) {
        if (value <= 0) return "0";

        int unitIndex = 0;
        for (int i = THREE_VALUES.length - 1; i >= 0; i--) {
            if (value >= THREE_VALUES[i]) {
                unitIndex = i;
                break;
            }
        }

        return formatNumber(value, THREE_VALUES[unitIndex], THREE_UNITS[unitIndex], false);
    }

    public static String formatByFourGroups(long value) {
        if (value <= 0) return "0";

        int unitIndex = 0;
        for (int i = FOUR_VALUES.length - 1; i >= 0; i--) {
            if (value >= FOUR_VALUES[i]) {
                unitIndex = i;
                break;
            }
        }

        return formatNumber(value, FOUR_VALUES[unitIndex], FOUR_UNITS[unitIndex], true);
    }

    private static String formatNumber(long number, long unitValue, String unit, boolean isFourGroup) {
        if (unitValue == 1) {
            return String.valueOf(number);
        }

        double value = (double) number / unitValue;

        int integerPart = (int) value;
        boolean hasFourDigits = integerPart >= 1000;

        if (hasFourDigits && isFourGroup) {
            return integerPart + unit;
        }

        double rounded = Math.round(value * 10) / 10.0;

        if (rounded == (int) rounded) {
            return (int) rounded + ".0" + unit;
        } else {
            return rounded + unit;
        }
    }

    public static String formatNumber(long value, String extraFormat) {
        return String.format(extraFormat, formatNumber(value));
    }

    public static String formatNumber(int value, String extraFormat) {
        return String.format(extraFormat, formatNumber(value));
    }

    public static boolean hasServerMod(String modId) {
        return ModList.get().isLoaded(modId);
    }

    /**
     * Finds an item of the given type inside a Curios slot.
     *
     * @param slotId Curios slot identifier
     * @param item   item type to search for
     * @return the found stack, or ItemStack.EMPTY when absent or Curios is unavailable
     */
    public static ItemStack findItemInCuriosSlot(Player player, String slotId, Item item) {
        Optional<ICuriosItemHandler> optional = CuriosApi.getCuriosInventory(player);
        if (optional.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ICuriosItemHandler handler = optional.get();
        for (SlotResult result : handler.findCurios(slotId)) {
            ItemStack stack = result.stack();
            if (!stack.isEmpty() && stack.is(item)) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    /**
     * Attempts to equip the given stack into the first empty slot of the given Curios slot type.
     *
     * @param stack  the stack to equip, removed from its current location on success
     * @param slotId Curios slot identifier
     * @return true when the stack was equipped, false when the slot type is absent, full, or Curios is unavailable
     */
    public static boolean tryEquipToCurios(Player player, ItemStack stack, String slotId) {
        Optional<ICuriosItemHandler> optional = CuriosApi.getCuriosInventory(player);
        if (optional.isEmpty()) {
            return false;
        }

        ICuriosItemHandler handler = optional.get();
        Optional<ICurioStacksHandler> stacksHandler = handler.getStacksHandler(slotId);
        if (stacksHandler.isEmpty()) {
            return false;
        }

        IDynamicStackHandler stacks = stacksHandler.get().getStacks();
        for (int i = 0; i < stacks.getSlots(); i++) {
            if (stacks.getStackInSlot(i).isEmpty()) {
                ItemStack equipped = stack.copy();
                handler.setEquippedCurio(slotId, i, equipped);
                playEquipSound(player, equipped, slotId, i);
                stack.shrink(stack.getCount());
                return true;
            }
        }

        return false;
    }

    private static void playEquipSound(Player player, ItemStack stack, String slotId, int index) {
        if (player.level().isClientSide) {
            return;
        }
        CuriosApi.getCurio(stack).ifPresent(curio -> {
            ICurio.SoundInfo sound = curio.getEquipSound(new SlotContext(slotId, player, index, false, true));
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    sound.soundEvent(), player.getSoundSource(), sound.volume(), sound.pitch());
        });
    }

    /**
     * Builds a translation whose label (everything up to the first colon) is drawn in
     * {@code labelColor} while the value keeps its own styling. When the translation
     * has no colon the plain translated text is returned unchanged.
     *
     * @param key        translation key holding "Label：value" style text
     * @param labelColor ARGB color applied to the label part
     * @param args       format arguments for the value part
     */
    @OnlyIn(Dist.CLIENT)
    public static MutableComponent labeledText(String key, int labelColor, Object... args) {
        String format = Language.getInstance().getOrDefault(key);
        int separator = format.indexOf('：');
        if (separator < 0) {
            separator = format.indexOf(':');
        }
        if (separator < 0) {
            return Component.translatable(key, args);
        }

        String label = format.substring(0, separator + 1);
        String valueFormat = format.substring(separator + 1);
        MutableComponent result = Component.empty();
        result.append(Component.literal(label).withColor(labelColor));
        if (!valueFormat.isEmpty()) {
            if (args.length == 1 && args[0] instanceof Component component) {
                result.append(component);
            } else {
                result.append(Component.literal(String.format(valueFormat, args)));
            }
        }
        return result;
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public static Level getClientLevel() {
        try {
            return Minecraft.getInstance().level;
        } catch (Exception e) {
            return null;
        }
    }

    /** Returns whether the player is the mod developer (Dev or LGhast). */
    public static boolean isDeveloper(Player player) {
        String name = player.getGameProfile().getName();
        return "Dev".equalsIgnoreCase(name) || "LGhast".equalsIgnoreCase(name);
    }
}
