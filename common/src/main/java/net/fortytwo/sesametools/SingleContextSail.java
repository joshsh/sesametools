
package net.fortytwo.sesametools;

import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.StackableSail;

import java.io.File;

public class SingleContextSail implements StackableSail {
    private Sail baseSail;
    private Resource context;

    public SingleContextSail(final Sail baseSail, final Resource context) {
        this.baseSail = baseSail;
        this.context = context;
    }

    public Sail getBaseSail() {
        return baseSail;
    }

    public void setBaseSail(final Sail baseSail) {
        this.baseSail = baseSail;
    }

    public SailConnection getConnection() throws SailException {
        return new SingleContextSailConnection(baseSail, context);
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
}
