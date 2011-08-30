package net.fortytwo.sesametools.decompose;

import org.openrdf.model.ValueFactory;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.StackableSail;

import java.io.File;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class DecomposeTupleQueriesSail implements StackableSail {
    private Sail baseSail;

    public DecomposeTupleQueriesSail(final Sail baseSail) {
        this.baseSail = baseSail;
    }

    public SailConnection getConnection() throws SailException {
        return new DecomposeTupleQueriesSailConnection(baseSail);
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
