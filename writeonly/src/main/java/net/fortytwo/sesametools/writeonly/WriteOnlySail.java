
package net.fortytwo.sesametools.writeonly;

import org.openrdf.model.ValueFactory;
import org.openrdf.rio.RDFHandler;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailChangedListener;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import java.io.File;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class WriteOnlySail implements Sail {
    private RDFHandler handler;
    private ValueFactory valueFactory;

    public WriteOnlySail(final RDFHandler handler, final ValueFactory valueFactory) {
        this.handler = handler;
        this.valueFactory = valueFactory;
    }

    public void addSailChangedListener(SailChangedListener arg0) {
        // TODO Auto-generated method stub
    }

    public SailConnection getConnection() throws SailException {
        return new WriteOnlySailConnection(handler, valueFactory);
    }

    public File getDataDir() {
        // TODO Auto-generated method stub
        return null;
    }

    public ValueFactory getValueFactory() {
        return valueFactory;
    }

    public void initialize() throws SailException {
        // TODO Auto-generated method stub
    }

    public boolean isWritable() throws SailException {
        return true;
    }

    public void removeSailChangedListener(SailChangedListener arg0) {
        // TODO Auto-generated method stub
    }

    public void setDataDir(File arg0) {
        // TODO Auto-generated method stub

    }

    public void shutDown() throws SailException {
        // TODO Auto-generated method stub
    }
}
