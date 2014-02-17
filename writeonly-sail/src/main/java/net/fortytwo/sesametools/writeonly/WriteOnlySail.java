
package net.fortytwo.sesametools.writeonly;

import org.openrdf.model.ValueFactory;
import org.openrdf.rio.RDFHandler;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailBase;

/**
 * A <code>Sail</code> which can be written to, but not read from.
 * Read operations are simply ignored or return no results.
 *
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class WriteOnlySail extends SailBase {
    private RDFHandler handler;
    private ValueFactory valueFactory;

    public WriteOnlySail(final RDFHandler handler,
                         final ValueFactory valueFactory) {
        this.handler = handler;
        this.valueFactory = valueFactory;
    }

    protected SailConnection getConnectionInternal() throws SailException {
        return new WriteOnlySailConnection(this, handler, valueFactory);
    }

    public ValueFactory getValueFactory() {
        return valueFactory;
    }

    protected void initializeInternal() throws SailException {
        // Does nothing
    }

    public boolean isWritable() throws SailException {
        return true;
    }

    protected void shutDownInternal() throws SailException {
        // Does nothing
    }
}
