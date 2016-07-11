
package net.fortytwo.sesametools;

import org.openrdf.IsolationLevel;
import org.openrdf.model.ValueFactory;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.StackableSail;

import java.io.File;
import java.util.List;

/**
 * A StackableSail which allows multiple Sails to be stacked upon the same base
 * Sail (avoiding re-initialization of the base Sail as the individual stacked
 * Sails are initialized)
 * 
 * @author Joshua Shinavier (http://fortytwo.net)
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

    @Override
    public List<IsolationLevel> getSupportedIsolationLevels() {
        return baseSail.getSupportedIsolationLevels();
    }

    @Override
    public IsolationLevel getDefaultIsolationLevel() {
        return baseSail.getDefaultIsolationLevel();
    }
}
