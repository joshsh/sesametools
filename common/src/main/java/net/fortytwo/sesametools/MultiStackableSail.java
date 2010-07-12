
package net.fortytwo.sesametools;

import org.openrdf.model.ValueFactory;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.StackableSail;

import java.io.File;

/**
 * A StackableSail which allows multiple Sails to be stacked upon the same base
 * Sail (avoiding re-initialization of the base Sail as the individual stacked
 * Sails are initialized)
 * <p/>
 * Author: josh
 * Date: Mar 28, 2008
 * Time: 3:51:50 PM
 */
public class MultiStackableSail implements StackableSail {
    private Sail baseSail;

    public MultiStackableSail(final Sail baseSail) {
        setBaseSail(baseSail);
    }

    public void setBaseSail(Sail sail) {
        this.baseSail = sail;
    }

    public Sail getBaseSail() {
        return baseSail;
    }

    public void setDataDir(File file) {
        baseSail.setDataDir(file);
    }

    public File getDataDir() {
        return baseSail.getDataDir();
    }

    public void initialize() throws SailException {
        // Do nothing -- assume that the base Sail is initialized elsewhere
    }

    public void shutDown() throws SailException {
        // Do nothing -- assume that the base Sail will be shut down elsewhere
    }

    public boolean isWritable() throws SailException {
        return baseSail.isWritable();
    }

    public SailConnection getConnection() throws SailException {
        return baseSail.getConnection();
    }

    public ValueFactory getValueFactory() {
        return baseSail.getValueFactory();
    }
}
