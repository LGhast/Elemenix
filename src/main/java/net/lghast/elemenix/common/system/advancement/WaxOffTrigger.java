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

import java.util.Optional;

public class WaxOffTrigger extends SimpleCriterionTrigger<WaxOffTrigger.Instance> {

    public static class Instance implements SimpleCriterionTrigger.SimpleInstance {
        private final Optional<ContextAwarePredicate> playerPredicate;

        public Instance(Optional<ContextAwarePredicate> playerPredicate) {
            this.playerPredicate = playerPredicate;
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return playerPredicate;
        }

        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(inst -> inst.playerPredicate)
                ).apply(instance, Instance::new)
        );
    }

    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, instance -> true);
    }

    public static Criterion<Instance> waxOff() {
        return new Criterion<>(
                TRIGGER.get(),
                new Instance(Optional.empty())
        );
    }

    public static final DeferredRegister<CriterionTrigger<?>> TRIGGER_TYPES =
            DeferredRegister.create(Registries.TRIGGER_TYPE, "elemenix");

    public static final DeferredHolder<CriterionTrigger<?>, WaxOffTrigger> TRIGGER =
            TRIGGER_TYPES.register("wax_off", WaxOffTrigger::new);
}
