package net.fortytwo.sesametools.deduplication;

import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailWrapper;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Sep 30, 2008
 * Time: 1:35:44 PM
 * To change this template use File | Settings | File Templates.
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
