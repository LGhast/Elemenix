package net.lghast.elemenix.client.misc;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.client.render.OutlineRendererHelper;
import net.lghast.elemenix.config.ClientConfig;
import net.lghast.elemenix.common.content.block.ChannelBlock;
import net.lghast.elemenix.common.content.block.ElemenicComparatorBlock;
import net.lghast.elemenix.common.content.block.OutletBlock;
import net.lghast.elemenix.common.content.blockentity.AbstractPrinterBlockEntity;
import net.lghast.elemenix.common.content.blockentity.EjectorBlockEntity;
import net.lghast.elemenix.common.content.blockentity.ElemenicComparatorBlockEntity;
import net.lghast.elemenix.common.content.blockentity.OutletBlockEntity;
import net.lghast.elemenix.common.content.blockentity.ShuntBlockEntity;
import net.lghast.elemenix.common.content.item.AnalyticalWrenchItem;
import net.lghast.elemenix.common.content.item.ScannerItem;
import net.lghast.elemenix.common.content.item.ScanningStorageItem;
import net.lghast.elemenix.common.system.datacomponent.ElemenicStorage;
import net.lghast.elemenix.register.system.ModTags;
import net.lghast.elemenix.utils.elemenix.Elemenix;
import net.lghast.elemenix.utils.misc.ElemenixHolder;
import net.lghast.elemenix.utils.misc.ModUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = Elemenics.MOD_ID, value = Dist.CLIENT)
public class AnalyticalWrenchClientEvents {
    private static final Object HOVER_SLOT = new Object();
    private static final int TITLE_COLOR = 0xFFD5FFF6;
    private static final String[] LETTERS = {"O", "T", "F", "M", "E", "A"};
    private static final String[] RULE_SYMBOLS = {">", "<", "=", "≥", "≤"};
    private static final long[] UNIT_MULTIPLIERS = {1L, 10_000L, 100_000_000L, 1_000_000_000_000L};

    private static int getDefaultColor() {
        return 0xFF000000 | ClientConfig.ANALYTICAL_WRENCH_OUTLINE_COLOR.get();
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        OutlineRendererHelper.INSTANCE.tick();
        updateHoverOverlay();
    }

    private static boolean isWrenchActive(ItemStack main, ItemStack off) {
        if (main.getItem() instanceof AnalyticalWrenchItem) return true;
        return off.getItem() instanceof AnalyticalWrenchItem
                && !(main.getItem() instanceof ScannerItem)
                && !(main.getItem() instanceof ScanningStorageItem);
    }

    private static void updateHoverOverlay() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        ItemStack main = mc.player.getMainHandItem();
        ItemStack off = mc.player.getOffhandItem();
        if (!isWrenchActive(main, off)) return;

        HitResult hit = mc.hitResult;
        if (!(hit instanceof BlockHitResult blockHit) || blockHit.getType() != HitResult.Type.BLOCK) return;

        Level level = mc.level;
        BlockPos pos = blockHit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        BlockEntity be = level.getBlockEntity(pos);

        if (!isWrenchTarget(state)) return;

        for (BlockPos p : collectGroup(be, level, pos, state)) {
            BlockState s = level.getBlockState(p);
            int color = getBoxColor(level.getBlockEntity(p));
            Block block = s.getBlock();
            if (block instanceof ChannelBlock || block instanceof OutletBlock) {
                OutlineRendererHelper.INSTANCE.showShape(p, s.getShape(level, p).move(p.getX(), p.getY(), p.getZ()), color);
            } else {
                OutlineRendererHelper.INSTANCE.showBox(p, s.getShape(level, p).bounds().move(p.getX(), p.getY(), p.getZ()), color);
            }
        }

        List<Component> lines = buildLines(be);
        if (!lines.isEmpty()) {
            float height = (float) state.getShape(level, pos).bounds().maxY + 0.15f;
            OutlineRendererHelper.INSTANCE.showText(HOVER_SLOT, pos, height, lines);
        }
    }

    private static boolean isWrenchTarget(BlockState state) {
        Block block = state.getBlock();
        return state.is(ModTags.ELEMENIX_OUTPUTS)
                || state.is(ModTags.ELEMENIX_INPUTS)
                || state.is(ModTags.ELEMENIC_OUTLET_BLOCKS)
                || block instanceof ChannelBlock
                || block instanceof ElemenicComparatorBlock;
    }

    private static int getBoxColor(BlockEntity be) {
        if (be instanceof EjectorBlockEntity ejector && ejector.getFilterIndex() != EjectorBlockEntity.FILTER_ALL) {
            return Elemenix.values()[ejector.getFilterIndex()].getColor();
        }
        if (be instanceof ShuntBlockEntity shunt && shunt.getFilterIndex() != ShuntBlockEntity.FILTER_UNSPECIFIED) {
            return Elemenix.values()[shunt.getFilterIndex()].getColor();
        }
        return getDefaultColor();
    }

    private static List<BlockPos> collectGroup(BlockEntity be, Level level, BlockPos pos, BlockState state) {
        if (be instanceof ShuntBlockEntity shunt) {
            List<BlockPos> members = shunt.getGroupMembers();
            if (!members.isEmpty()) return members;
        }
        if (be instanceof ElemenicComparatorBlockEntity) {
            return collectComparatorChain(level, pos, state);
        }
        if (state.getBlock() instanceof ChannelBlock || state.getBlock() instanceof OutletBlock) {
            return collectChannelChain(level, pos, state);
        }
        return List.of(pos);
    }

    private static List<BlockPos> collectChannelChain(Level level, BlockPos pos, BlockState state) {
        List<BlockPos> chain = new ArrayList<>();
        chain.add(pos);

        boolean isOutlet = state.getBlock() instanceof OutletBlock;
        Direction downFacing = state.getBlock() instanceof ChannelBlock
                ? state.getValue(ChannelBlock.FACING)
                : state.getValue(OutletBlock.FACING);

        BlockPos next = pos.relative(downFacing);
        for (int i = 0; i < 64; i++) {
            BlockState s = level.getBlockState(next);
            if (!(s.getBlock() instanceof ChannelBlock)) break;
            if (s.getValue(ChannelBlock.INPUT) != downFacing.getOpposite()) break;
            chain.add(next);
            downFacing = s.getValue(ChannelBlock.FACING);
            next = next.relative(downFacing);
        }

        if (!isOutlet) {
            Direction upFacing = state.getValue(ChannelBlock.INPUT);
            BlockPos prev = pos.relative(upFacing);
            for (int i = 0; i < 64; i++) {
                BlockState s = level.getBlockState(prev);
                if (!(s.getBlock() instanceof ChannelBlock)) break;
                if (s.getValue(ChannelBlock.FACING) != upFacing.getOpposite()) break;
                chain.add(prev);
                upFacing = s.getValue(ChannelBlock.INPUT);
                prev = prev.relative(upFacing);
            }
        }

        return chain;
    }

    private static List<BlockPos> collectComparatorChain(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(ElemenicComparatorBlock.FACING);
        List<BlockPos> chain = new ArrayList<>();
        chain.add(pos);

        BlockPos cursor = pos.relative(facing.getOpposite());
        for (int i = 0; i < 16; i++) {
            BlockState s = level.getBlockState(cursor);
            if (s.getBlock() instanceof ElemenicComparatorBlock && s.getValue(ElemenicComparatorBlock.FACING) == facing) {
                chain.add(cursor);
                cursor = cursor.relative(facing.getOpposite());
            } else {
                break;
            }
        }

        cursor = pos.relative(facing);
        for (int i = 0; i < 16; i++) {
            BlockState s = level.getBlockState(cursor);
            if (s.getBlock() instanceof ElemenicComparatorBlock && s.getValue(ElemenicComparatorBlock.FACING) == facing) {
                chain.add(cursor);
                cursor = cursor.relative(facing);
            } else {
                break;
            }
        }

        return chain;
    }

    private static List<Component> buildLines(BlockEntity be) {
        List<Component> lines = new ArrayList<>();

        if (be instanceof OutletBlockEntity outlet) {
            lines.add(buildOutputLine(true, outlet.getAllowedOutputs()));
            lines.add(buildOutputLine(false, outlet.getCurrentOutputs()));
        }

        if (be instanceof AbstractPrinterBlockEntity printer) {
            String modeName = AbstractPrinterBlockEntity.getModeName(printer.getMode());
            lines.add(Component.translatable("tooltip.elemenix.printer_mode",
                    Component.literal(modeName).withColor(0xFFFFFFFF)).withColor(TITLE_COLOR));

            ItemStack printingStack = printer.getPrintingItemStack();
            if (!printingStack.isEmpty()) {
                String itemName = printingStack.getHoverName().getString();
                lines.add(Component.translatable("tooltip.elemenix.printer_printing",
                        Component.literal(itemName).withColor(0xFFFFFFFF)).withColor(TITLE_COLOR));
            }
        }

        if (be instanceof ElemenixHolder holder) {
            long[] storage = new long[6];
            for (Elemenix type : Elemenix.values()) {
                storage[type.getIndex()] = holder.getStored(type);
            }
            ElemenicStorage es = new ElemenicStorage(storage);
            lines.add(Component.translatable("tooltip.elemenix.analyzer_storage").withColor(TITLE_COLOR));
            lines.add(es.toComponentFormer());
            lines.add(es.toComponentLatter());
        }

        if (be instanceof ElemenicComparatorBlockEntity comparator) {
            Component criterionContent = Component.literal(buildCriterion(comparator)).withColor(0xFFFFFFFF);
            lines.add(Component.translatable("gui.elemenix.comparator_criterion", criterionContent).withColor(TITLE_COLOR));
            boolean passing = comparator.isOwnCriterionPassing();
            Component judgmentContent = Component.translatable(passing
                    ? "gui.elemenix.comparator_true"
                    : "gui.elemenix.comparator_false")
                    .withColor(passing ? 0xFF55FF55 : 0xFFFF5555);
            lines.add(Component.translatable("gui.elemenix.comparator_judgment", judgmentContent).withColor(TITLE_COLOR));
        }

        return lines;
    }

    private static Component buildOutputLine(boolean allowed, boolean[] flags) {
        MutableComponent line = Component.translatable(allowed
                ? "gui.elemenix.analytical_wrench.allowed_output"
                : "gui.elemenix.analytical_wrench.current_output").withColor(TITLE_COLOR);
        for (int i = 0; i < 6; i++) {
            int color = flags[i] ? Elemenix.values()[i].getColor() : getDefaultColor();
            line.append(Component.literal(LETTERS[i] + " ").withColor(color));
        }
        return line;
    }

    private static String buildCriterion(ElemenicComparatorBlockEntity comparator) {
        BlockState state = comparator.getBlockState();
        int target = state.getValue(ElemenicComparatorBlock.TARGET);
        int rule = state.getValue(ElemenicComparatorBlock.RULE);
        int threshold = comparator.getThreshold();
        int unit = comparator.getUnit();

        String targetPart;
        if (target == ElemenicComparatorBlockEntity.TARGET_SUM) {
            targetPart = Component.translatable("gui.elemenix.comparator_criterion_sum").getString();
        } else if (target == ElemenicComparatorBlockEntity.TARGET_ANY) {
            targetPart = Component.translatable("gui.elemenix.comparator_criterion_any").getString();
        } else {
            targetPart = Elemenix.values()[target].getName();
        }

        long value = (long) threshold * UNIT_MULTIPLIERS[unit];
        String valuePart = Screen.hasAltDown() ? String.valueOf(value) : ModUtils.formatNumber(value);

        return targetPart + " " + RULE_SYMBOLS[rule] + " " + valuePart;
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        OutlineRendererHelper.INSTANCE.render(
                event.getPoseStack(),
                bufferSource,
                event.getCamera());
        bufferSource.endBatch();
    }
}
