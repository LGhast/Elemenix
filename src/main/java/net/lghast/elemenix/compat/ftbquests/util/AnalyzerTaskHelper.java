package net.lghast.elemenix.compat.ftbquests.util;

import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import net.lghast.elemenix.compat.ftbquests.task.DeconstructItemTask;
import net.lghast.elemenix.compat.ftbquests.task.MemorizeItemTask;
import net.lghast.elemenix.compat.ftbquests.task.ReconstructItemTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import dev.ftb.mods.ftbquests.quest.BaseQuestFile;

public class AnalyzerTaskHelper {
    public static void handleDeconstructProgress(ServerPlayer player, ItemStack stack, int count) {
        TeamData team = TeamData.get(player);
        BaseQuestFile questFile = team.getFile();

        questFile.forAllQuests(quest -> {
            if (!team.isCompleted(quest)) {
                for (Task task : quest.getTasks()) {
                    if (task instanceof DeconstructItemTask dcTask && dcTask.matches(stack)) {
                        dcTask.addProgress(team, count);
                    }
                }
            }
        });
    }

    public static void handleReconstructProgress(ServerPlayer player, ItemStack stack, int count) {
        TeamData team = TeamData.get(player);
        BaseQuestFile questFile = team.getFile();

        questFile.forAllQuests(quest -> {
            if (!team.isCompleted(quest)) {
                for (Task task : quest.getTasks()) {
                    if (task instanceof ReconstructItemTask rcTask && rcTask.matches(stack)) {
                        rcTask.addProgress(team, count);
                    }
                }
            }
        });
    }

    public static void handleMemorizeProgress(ServerPlayer player, ItemStack stack) {
        TeamData team = TeamData.get(player);
        BaseQuestFile questFile = team.getFile();

        questFile.forAllQuests(quest -> {
            if (!team.isCompleted(quest)) {
                for (Task task : quest.getTasks()) {
                    if (task instanceof MemorizeItemTask memorizeTask && memorizeTask.matches(stack)) {
                        memorizeTask.addProgress(team, 1);
                    }
                }
            }
        });
    }
}
