package net.lghast.elemenix.client.misc;

import net.lghast.elemenix.Elemenics;
import net.lghast.elemenix.client.cache.ClientScanningCache;
import net.lghast.elemenix.client.render.OutlineRendererHelper;
import net.lghast.elemenix.common.content.block.InfuserBlock;
import net.lghast.elemenix.common.content.item.AnalyticalWrenchItem;
import net.lghast.elemenix.common.content.item.RemoteStorageItem;
import net.lghast.elemenix.common.content.item.ScannerItem;
import net.lghast.elemenix.common.content.item.ScanningStorageItem;
import net.lghast.elemenix.common.system.datacomponent.RemoteStorageBinding;
import net.lghast.elemenix.config.ClientConfig;
import net.lghast.elemenix.network.scan.RequestContainerScanPayload;
import net.lghast.elemenix.register.system.ModDataComponents;
import net.lghast.elemenix.utils.elemenix.Constituents;
import net.lghast.elemenix.utils.elemenix.ElemenixInfo;
import net.lghast.elemenix.utils.misc.ScanHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = Elemenics.MOD_ID, value = Dist.CLIENT)
public class ScannerClientEvents {
    private static final Object SCANNER_SLOT = new Object();
    private static final Object STORAGE_SLOT = new Object();
    private static final Object REMOTE_SLOT = new Object();
    private static final int DEFAULT_COLOR = 0xFFA1FBE8;
    private static final int REMOTE_BOUND_COLOR = 0xFF88C35A;
    private static final int EMPTY_COLOR = 0xFFFF7B7B;
    private static final int TITLE_COLOR = 0xFFD5FFF6;
    private static final int WHITE = 0xFFFFFFFF;

    private static BlockPos cachedStoragePos;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        updateOverlay();
    }

    private static void updateOverlay() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        ItemStack main = mc.player.getMainHandItem();
        ItemStack off = mc.player.getOffhandItem();
        HitResult hit = mc.hitResult;
        if (!(hit instanceof BlockHitResult blockHit) || blockHit.getType() != HitResult.Type.BLOCK) return;

        Level level = mc.level;
        BlockPos pos = blockHit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        BlockEntity be = level.getBlockEntity(pos);

        Item mainItem = main.getItem();
        Item offItem = off.getItem();

        if (mainItem instanceof ScanningStorageItem) {
            handleScanningStorage(level, pos, state, be);
        } else if (mainItem instanceof ScannerItem) {
            handleScanner(level, pos, state);
        } else if (mainItem instanceof RemoteStorageItem) {
            handleRemoteStorage(level, pos, state, main);
        } else if (offItem instanceof ScannerItem && !(mainItem instanceof AnalyticalWrenchItem)) {
            handleScanner(level, pos, state);
        } else if (offItem instanceof RemoteStorageItem && !(mainItem instanceof AnalyticalWrenchItem)) {
            handleRemoteStorage(level, pos, state, off);
        }
    }

    private static void handleScanner(Level level, BlockPos pos, BlockState state) {
        VoxelShape shape = state.getShape(level, pos);
        if (shape.isEmpty()) return;

        OutlineRendererHelper.INSTANCE.showBox(SCANNER_SLOT,
                shape.bounds().move(pos.getX(), pos.getY(), pos.getZ()), DEFAULT_COLOR);

        List<Component> lines = new ArrayList<>();
        ItemStack itemStack = new ItemStack(state.getBlock().asItem());
        boolean hasItem = itemStack.getItem() != Items.AIR;
        if (hasItem) {
            lines.add(Component.translatable("tooltip.elemenix.scanner.block_name")
                    .withColor(TITLE_COLOR)
                    .append(Component.literal(itemStack.getHoverName().getString()).withColor(WHITE)));
        }

        Constituents constituents = hasItem ? ElemenixInfo.getConstituents(itemStack, level) : null;
        if (!hasItem || constituents == null || constituents.isUnanalysable()) {
            lines.add(Component.translatable("tooltip.elemenix.unanalysable").withColor(EMPTY_COLOR));
        } else {
            lines.add(Component.translatable("tooltip.elemenix.constituents").withColor(TITLE_COLOR));
            lines.add(constituents.toComponentFormer());
            lines.add(constituents.toComponentLatter());
        }

        float height = (float) shape.bounds().maxY + 0.15f;
        OutlineRendererHelper.INSTANCE.showText(SCANNER_SLOT, pos, height, lines);
    }

    private static void handleScanningStorage(Level level, BlockPos pos, BlockState state, BlockEntity be) {
        if (!(be instanceof BaseContainerBlockEntity)) return;
        if (ScanHelper.isStorageBlacklisted(state)) return;

        VoxelShape shape = state.getShape(level, pos);
        if (shape.isEmpty()) return;

        if (cachedStoragePos == null || !cachedStoragePos.equals(pos)) {
            cachedStoragePos = pos;
            Minecraft mc = Minecraft.getInstance();
            if (mc.getConnection() != null) {
                mc.getConnection().send(new RequestContainerScanPayload(pos));
            }
        }

        Boolean hasParseable = ClientScanningCache.getHasParseable(pos);
        int color = (hasParseable != null && !hasParseable) ? EMPTY_COLOR : DEFAULT_COLOR;
        OutlineRendererHelper.INSTANCE.showBox(STORAGE_SLOT,
                shape.bounds().move(pos.getX(), pos.getY(), pos.getZ()), color);
    }

    private static void handleRemoteStorage(Level level, BlockPos pos, BlockState state, ItemStack stack) {
        if (!(state.getBlock() instanceof InfuserBlock)) return;

        VoxelShape shape = state.getShape(level, pos);
        if (shape.isEmpty()) return;

        int color = 0xFF000000 | ClientConfig.ANALYTICAL_WRENCH_OUTLINE_COLOR.get();
        RemoteStorageBinding binding = stack.get(ModDataComponents.REMOTE_STORAGE_BINDING.get());
        if (binding != null && binding.isBound() && binding.boundPos().isPresent()) {
            GlobalPos boundPos = binding.boundPos().get();
            if (boundPos.dimension().equals(level.dimension()) && boundPos.pos().equals(pos)) {
                color = REMOTE_BOUND_COLOR;
            }
        }

        OutlineRendererHelper.INSTANCE.showBox(REMOTE_SLOT,
                shape.bounds().move(pos.getX(), pos.getY(), pos.getZ()), color);
    }
}
