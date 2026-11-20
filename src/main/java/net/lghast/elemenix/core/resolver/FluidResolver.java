package net.lghast.elemenix.core.resolver;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.lghast.elemenix.core.Constituents;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lookup for the elemenic constituents of fluids.
 * <p>Explicit id and tag mappings are combined with values derived from technical recipes.
 * <p>Every value is expressed per {@link #FLUID_UNIT} (250 mB), and flowing fluids are canonicalized to their source.
 */
@ParametersAreNonnullByDefault
public class FluidResolver {
    public static final int FLUID_UNIT = 250;

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Map<Fluid, Constituents> DIRECT_MAP = new ConcurrentHashMap<>();
    private static final Map<Fluid, Constituents> DERIVED_MAP = new ConcurrentHashMap<>();
    private static final Map<TagKey<Fluid>, Constituents> TAG_MAP = new HashMap<>();

    /**
     * Drops all explicit, derived and tag mappings.
     */
    public static void clearAll() {
        DIRECT_MAP.clear();
        DERIVED_MAP.clear();
        TAG_MAP.clear();
    }

    /**
     * Registers an explicit mapping for one fluid id.
     */
    public static void putDirect(String fluidId, Constituents constituents) {
        Fluid fluid = getFluid(fluidId);
        if (fluid != null) {
            DIRECT_MAP.put(canonicalize(fluid), constituents);
        } else {
            LOGGER.info("Unknown fluid ID: {}", fluidId);
        }
    }

    /**
     * Registers an explicit mapping for one fluid tag.
     */
    public static void putDirectTag(String tagId, Constituents constituents) {
        try {
            ResourceLocation tagLocation = ResourceLocation.parse(tagId);
            TagKey<Fluid> tagKey = TagKey.create(BuiltInRegistries.FLUID.key(), tagLocation);
            TAG_MAP.put(tagKey, constituents);
        } catch (Exception e) {
            LOGGER.info("Unknown fluid tag ID: {}", tagId);
        }
    }

    /**
     * Stores a value derived from a technical recipe; explicit mappings take priority.
     */
    public static void putDerived(Fluid fluid, Constituents constituents) {
        DERIVED_MAP.put(canonicalize(fluid), constituents.copy());
    }



    /**
     * True when the fluid has an explicit, derived or tag mapping.
     */
    public static boolean hasConstituents(Fluid fluid) {
        return getMappedConstituents(fluid) != null;
    }

    /**
     * Constituents mapped to the fluid, or null when it has none;
     * <p>Among matching tags the one with the largest total wins.
     */
    @Nullable
    public static Constituents getMappedConstituents(@Nullable Fluid fluid) {
        if (fluid == null) return null;

        fluid = canonicalize(fluid);
        Constituents direct = DIRECT_MAP.get(fluid);
        if (direct != null) return direct.copy();

        Constituents derived = DERIVED_MAP.get(fluid);
        if (derived != null) return derived.copy();

        List<Map.Entry<TagKey<Fluid>, Constituents>> matchingTags = new ArrayList<>();
        for (Map.Entry<TagKey<Fluid>, Constituents> entry : TAG_MAP.entrySet()) {
            if (matchesTag(fluid, entry.getKey())) {
                matchingTags.add(entry);
            }
        }

        if (!matchingTags.isEmpty()) {
            return getBestTagConstituents(matchingTags).copy();
        }
        return null;
    }

    /**
     * Same as {@link #getMappedConstituents}, but returns empty constituents when the fluid is unmapped.
     */
    public static Constituents getConstituents(@Nullable Fluid fluid) {
        Constituents mapped = getMappedConstituents(fluid);
        return mapped != null ? mapped : new Constituents();
    }

    /**
     * Resolves a fluid id; null when malformed or unknown.
     */
    private static Fluid getFluid(String id) {
        try {
            return BuiltInRegistries.FLUID.get(ResourceLocation.parse(id));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Maps flowing fluid states to their source fluid.
     */
    private static Fluid canonicalize(Fluid fluid) {
        if (fluid instanceof FlowingFluid flowing && flowing.getSource() != fluid) {
            return flowing.getSource();
        }
        return fluid;
    }

    private static boolean matchesTag(Fluid fluid, TagKey<Fluid> tagKey) {
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
        Optional<Holder.Reference<Fluid>> holder =
                BuiltInRegistries.FLUID.getHolder(ResourceKey.create(BuiltInRegistries.FLUID.key(), id));
        return holder.isPresent() && holder.get().is(tagKey);
    }

    /**
     * Returns the matching tag mapping with the largest total.
     */
    private static Constituents getBestTagConstituents(List<Map.Entry<TagKey<Fluid>, Constituents>> matchingTags) {
        Map.Entry<TagKey<Fluid>, Constituents> bestMatch = matchingTags.getFirst();
        if (matchingTags.size() > 1) {
            for (int i = 1; i < matchingTags.size(); i++) {
                Map.Entry<TagKey<Fluid>, Constituents> current = matchingTags.get(i);
                if (current.getValue().getSum() > bestMatch.getValue().getSum()) {
                    bestMatch = current;
                }
            }
        }
        return bestMatch.getValue();
    }
}
