package net.fortytwo.sesametools.readonly;

import org.openrdf.model.ValueFactory;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.StackableSail;

import java.io.File;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class ReadOnlySail implements StackableSail {
    private Sail baseSail;

    public ReadOnlySail(final Sail baseSail) {
        this.baseSail = baseSail;
    }

    public void setDataDir(final File dir) {
        baseSail.setDataDir(dir);
    }

    public File getDataDir() {
        return baseSail.getDataDir();
    }

    public void initialize() throws SailException {
        // Do nothing.
    }

    public void shutDown() throws SailException {
        // Do nothing.
    }

    public boolean isWritable() throws SailException {
        return false;
    }

    public SailConnection getConnection() throws SailException {
        return new ReadOnlySailConnection(baseSail);
    }

    public ValueFactory getValueFactory() {
        return baseSail.getValueFactory();
    }

    public void setBaseSail(final Sail sail) {
        this.baseSail = sail;
    }

    public Sail getBaseSail() {
        return baseSail;
    }
}
