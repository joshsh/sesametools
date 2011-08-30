package net.fortytwo.sesametools.debug;

import org.openrdf.model.ValueFactory;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.StackableSail;

import java.io.File;

/**
 * A StackableSail which simply relays operations to the base Sail. Overload one
 * or more methods for testing and debugging.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class DebugSail implements StackableSail {
    private final SailCounter counter;
    private Sail baseSail;

    public DebugSail(final Sail baseSail, final SailCounter counter) {
        this.baseSail = baseSail;
        this.counter = counter;
    }

    public SailConnection getConnection() throws SailException {
        return new DebugSailConnection(baseSail, counter);
    }

    public File getDataDir() {
        return baseSail.getDataDir();
    }

    public ValueFactory getValueFactory() {
        return baseSail.getValueFactory();
    }

    public void initialize() throws SailException {
        baseSail.initialize();
    }

    public boolean isWritable() throws SailException {
        return baseSail.isWritable();
    }

    public void setDataDir(final File dir) {
        baseSail.setDataDir(dir);
    }

    public void shutDown() throws SailException {
        baseSail.shutDown();
    }

    public Sail getBaseSail() {
        return baseSail;
    }

    public void setBaseSail(final Sail sail) {
        baseSail = sail;
    }
}
