package net.lghast.elemenix.utils.misc;

import net.lghast.elemenix.common.system.attachment.RegistrarCollection;
import net.lghast.elemenix.config.CommonConfig;
import net.lghast.elemenix.network.registrar.RegistrationStageToastPayload;
import net.lghast.elemenix.register.system.ModAttachments;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RegistrationStageNotifier {
    private static final Map<UUID, Integer> ANNOUNCED_STAGES = new HashMap<>();

    private RegistrationStageNotifier() {}

    public static void check(ServerPlayer player) {
        RegistrarCollection collection = player.getData(ModAttachments.REGISTRAR_COLLECTION);
        int total = collection.items().size() + collection.potions().size();
        if (total <= 0 || total % 100 != 0) return;

        Integer last = ANNOUNCED_STAGES.get(player.getUUID());
        if (last != null && last == total) return;
        ANNOUNCED_STAGES.put(player.getUUID(), total);

        if (CommonConfig.BROADCAST_REGISTRATION_STAGE.get()) {
            MinecraftServer server = player.getServer();
            if(server != null) {
                server.getPlayerList().broadcastSystemMessage(
                        Component.translatable("chat.elemenix.registration_stage", player.getDisplayName(), total), false);
            }
        }
        PacketDistributor.sendToPlayer(player, new RegistrationStageToastPayload(total));
    }
}
