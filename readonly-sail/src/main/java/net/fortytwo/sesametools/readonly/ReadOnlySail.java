package net.fortytwo.sesametools.readonly;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.StackableSail;
import org.eclipse.rdf4j.sail.helpers.AbstractSail;

import java.io.File;

/**
 * A Sail implementation which protects a base Sail from write operations.
 * This Sail does not throw exceptions when write operations are attempted;
 * it simply ignores them so that they have no effect.
 *
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class ReadOnlySail extends AbstractSail implements StackableSail {
    private Sail baseSail;

    public ReadOnlySail(final Sail baseSail) {
        this.baseSail = baseSail;
    }

    @Override
    public void setDataDir(final File dir) {
        baseSail.setDataDir(dir);
    }

    @Override
    public File getDataDir() {
        return baseSail.getDataDir();
    }

    protected void initializeInternal() throws SailException {
        // Do nothing.
    }

    protected void shutDownInternal() throws SailException {
        // Do nothing.
    }

    @Override
    public boolean isWritable() throws SailException {
        return false;
    }

    protected SailConnection getConnectionInternal() throws SailException {
        return new ReadOnlySailConnection(this, baseSail);
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
