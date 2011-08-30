package net.fortytwo.sesametools.deduplication;

import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailWrapper;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class DeduplicationSail extends SailWrapper {
    public DeduplicationSail(final Sail baseSail) {
        super(baseSail);
    }

    @Override
    public SailConnection getConnection() throws SailException {
        return new DeduplicationSailConnection(this.getBaseSail().getConnection());
    }
}
