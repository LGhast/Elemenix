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

public class ValveTrigger extends SimpleCriterionTrigger<ValveTrigger.Instance> {

    public static class Instance implements SimpleCriterionTrigger.SimpleInstance {
        private final Optional<ContextAwarePredicate> playerPredicate;
        private final Optional<Integer> openness; 

        public Instance(Optional<ContextAwarePredicate> playerPredicate, Optional<Integer> openness) {
            this.playerPredicate = playerPredicate;
            this.openness = openness;
        }

        @Override
        public @NotNull Optional<ContextAwarePredicate> player() {
            return playerPredicate;
        }
        
        public Optional<Integer> getOpenness() {
            return openness;
        }

        
        public boolean matches(int actualOpenness) {
            return openness.map(reqOpenness -> actualOpenness == reqOpenness).orElse(true);
        }

        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(inst -> inst.playerPredicate),
                        Codec.INT.optionalFieldOf("style").forGetter(Instance::getOpenness)
                ).apply(instance, Instance::new)
        );
    }

    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }
    
    public void trigger(ServerPlayer player, int openness) {
        this.trigger(player, instance -> instance.matches(openness));
    }

    public static Criterion<Instance> valveOpen(int openness) {
        return valveOpen(Optional.of(openness));
    }

    private static Criterion<Instance> valveOpen(Optional<Integer> openness) {
        return new Criterion<>(
                TRIGGER.get(),
                new Instance(Optional.empty(), openness)
        );
    }

    public static final DeferredRegister<CriterionTrigger<?>> TRIGGER_TYPES =
            DeferredRegister.create(Registries.TRIGGER_TYPE, "elemenix");

    public static final DeferredHolder<CriterionTrigger<?>, ValveTrigger> TRIGGER =
            TRIGGER_TYPES.register("valve_open", ValveTrigger::new);
}
