package net.lghast.elemenix.common.content.item;

import net.lghast.elemenix.client.misc.ClientInfuserDataCache;
import net.lghast.elemenix.common.content.block.InfuserBlock;
import net.lghast.elemenix.common.content.blockentity.InfuserBlockEntity;
import net.lghast.elemenix.common.system.datacomponent.ElemenicStorage;
import net.lghast.elemenix.common.system.datacomponent.RemoteStorageBinding;
import net.lghast.elemenix.conifig.ClientConfig;
import net.lghast.elemenix.network.infuser.RequestInfuserUpdatePayload;
import net.lghast.elemenix.register.system.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class RemoteStorageItem extends Item {
    public RemoteStorageItem(Properties properties) {
        super(properties.stacksTo(1)
                .component(ModDataComponents.REMOTE_STORAGE_BINDING.get(), RemoteStorageBinding.EMPTY));
    }

    public static boolean isBound(ItemStack stack) {
        if (stack.getItem() instanceof RemoteStorageItem) {
            RemoteStorageBinding binding = stack.get(ModDataComponents.REMOTE_STORAGE_BINDING.get());
            return binding != null && binding.isBound();
        }
        return false;
    }

    public static ElemenicStorage getRemoteStorage(ItemStack remoteStack, ServerLevel level) {
        RemoteStorageBinding binding = remoteStack.get(ModDataComponents.REMOTE_STORAGE_BINDING.get());

        if (binding == null || !binding.isBound()) {
            return new ElemenicStorage(new long[6]);
        }

        Optional<GlobalPos> globalPosOptional = binding.boundPos();
        if(globalPosOptional.isEmpty()) {
            return new ElemenicStorage(new long[6]);
        }
        GlobalPos globalPos = globalPosOptional.get();


        if (level.dimension() != globalPos.dimension()) {
            return new ElemenicStorage(new long[6]);
        }

        BlockEntity blockEntity = level.getBlockEntity(globalPos.pos());
        if (!(blockEntity instanceof InfuserBlockEntity infuser)) {
            return new ElemenicStorage(new long[6]);
        }

        ItemStack storageStack = infuser.getItem(0);
        if (!(storageStack.getItem() instanceof StorageItem)) {
            return new ElemenicStorage(new long[6]);
        }

        return StorageItem.getOrCreateData(storageStack);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (player != null && player.isShiftKeyDown() && level.getBlockState(pos).getBlock() instanceof InfuserBlock) {
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }

            GlobalPos globalPos = GlobalPos.of(level.dimension(), pos);
            stack.set(ModDataComponents.REMOTE_STORAGE_BINDING.get(), RemoteStorageBinding.of(globalPos));

            player.displayClientMessage(
                    Component.translatable("message.elemenix.remote_storage.bound", pos.getX(), pos.getY(), pos.getZ())
                            .withStyle(ChatFormatting.GREEN),
                    true
            );

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        RemoteStorageBinding binding = stack.get(ModDataComponents.REMOTE_STORAGE_BINDING.get());

        if (binding != null && binding.isBound() && binding.boundPos().isPresent()) {
            GlobalPos globalPos = binding.boundPos().get();
            if(ClientConfig.SHOW_REMOTE_COORDINATES.get()) {
                ResourceLocation dimensionId = globalPos.dimension().location();

                String translationKey = "dimension." + dimensionId.getNamespace() + "." + dimensionId.getPath();
                Component dimensionName = Component.translatable(translationKey);

                if (dimensionName.getString().equals(translationKey)) {
                    dimensionName = Component.literal(dimensionId.getPath().replace("_", " "));
                }

                tooltip.add(Component.translatable("tooltip.elemenix.remote_storage.bound_to",
                                globalPos.pos().getX(), globalPos.pos().getY(), globalPos.pos().getZ(),
                                dimensionName)
                        .withStyle(ChatFormatting.GRAY));
            }

            if(!ClientConfig.SHOW_REMOTE_CONNECTION_STATUS.get()) return;

            Level level = context.level();
            if (level != null && level.dimension() == globalPos.dimension()) {
                Optional<long[]> cachedData = ClientInfuserDataCache.getCachedData(globalPos);
                if (level.isClientSide) {
                    PacketDistributor.sendToServer(new RequestInfuserUpdatePayload(globalPos));
                }
                if (cachedData.isPresent()) {
                    ElemenicStorage storage = new ElemenicStorage(cachedData.get());
                    if (!storage.isEmpty()) {
                        tooltip.add(Component.translatable("tooltip.elemenix.remote_storage.bound_valid").withStyle(ChatFormatting.GREEN));

                        tooltip.add(storage.toComponentFormer());
                        tooltip.add(storage.toComponentLatter());
                        return;
                    }
                }
                tooltip.add(Component.translatable("tooltip.elemenix.remote_storage.invalid").withStyle(ChatFormatting.RED));
            } else {
                tooltip.add(Component.translatable("tooltip.elemenix.remote_storage.dimension_mismatch")
                        .withStyle(ChatFormatting.RED));
            }
            return;
        } else {
            tooltip.add(Component.translatable("tooltip.elemenix.remote_storage.not_bound").withStyle(ChatFormatting.RED));
        }
        if(ClientConfig.SHOW_REMOTE_BINDING_PROMPT.get()) {
            tooltip.add(Component.translatable("tooltip.elemenix.remote_storage.usage").withStyle(ChatFormatting.GRAY));
        }
    }
}
