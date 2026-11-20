package net.lghast.elemenix.common.system.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class MemoriesRegisteredTrigger extends SimpleCriterionTrigger<MemoriesRegisteredTrigger.Instance> {
    public static class Instance implements SimpleCriterionTrigger.SimpleInstance {
        private final Optional<ContextAwarePredicate> playerPredicate;
        private final int count;

        public Instance(Optional<ContextAwarePredicate> playerPredicate, int count) {
            this.playerPredicate = playerPredicate;
            this.count = count;
        }

        @Override
        public @NotNull Optional<ContextAwarePredicate> player() {
            return playerPredicate;
        }

        public boolean matches(int registered) {
            return registered >= this.count;
        }

        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(inst -> inst.playerPredicate),
                        Codec.INT.optionalFieldOf("count", 64).forGetter(inst -> inst.count)
                ).apply(instance, Instance::new)
        );
    }

    @Override
    public @NotNull Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(ServerPlayer player, int registered) {
        this.trigger(player, instance -> instance.matches(registered));
    }

    public static Criterion<Instance> registered(int count) {
        return new Criterion<>(
                TRIGGER.get(),
                new Instance(Optional.empty(), count)
        );
    }

    public static final DeferredRegister<CriterionTrigger<?>> TRIGGER_TYPES =
            DeferredRegister.create(Registries.TRIGGER_TYPE, "elemenix");

    public static final DeferredHolder<CriterionTrigger<?>, MemoriesRegisteredTrigger> TRIGGER =
            TRIGGER_TYPES.register("memories_registered", MemoriesRegisteredTrigger::new);
}
