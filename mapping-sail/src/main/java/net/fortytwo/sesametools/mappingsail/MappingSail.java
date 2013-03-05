package net.fortytwo.sesametools.mappingsail;

import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailWrapper;

/**
 * A Sail which maps between the internal URI space of a lower-level data store, and an externally visible URI space
 * (for example, published Linked Data).
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class MappingSail extends SailWrapper {
    private final MappingSchema schema;

    /**
     * @param baseSail the internal data store
     * @param schema a set of rules for URI rewriting
     */
    public MappingSail(final Sail baseSail,
                       final MappingSchema schema) {
        this.setBaseSail(baseSail);
        this.schema = schema;
    }

    @Override
    public SailConnection getConnection() throws SailException {
        return new MappingSailConnection(this.getBaseSail().getConnection(), schema, this.getValueFactory());
    }

    @Override
    public boolean isWritable() throws SailException {
        // TODO: handle rewriting for write operations
        return false;
    }
}
