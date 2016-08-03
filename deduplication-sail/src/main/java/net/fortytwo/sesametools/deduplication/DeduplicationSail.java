package net.fortytwo.sesametools.deduplication;

import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.helpers.SailWrapper;

/**
 * A <code>Sail</code> which avoids adding duplicate statements to a base <code>Sail</code>.
 * For use with <code>Sail</code> implementations in which duplicate statements are possible.
 *
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
