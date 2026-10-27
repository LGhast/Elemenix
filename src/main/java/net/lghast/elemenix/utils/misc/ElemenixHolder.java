package net.lghast.elemenix.utils.misc;

import net.lghast.elemenix.utils.elemenix.Elemenix;

/**
 * Uniform view of a block entity's elemenix storage for the transfer network.
 * Sources allow {@link #extract};
 * targets allow {@link #receive}.
 */
public interface ElemenixHolder {
    long getStored(Elemenix type);

    boolean canExtract(Elemenix type);

    boolean canReceive(Elemenix type);

    void extract(Elemenix type, long amount);

    void receive(Elemenix type, long amount);
}
