package net.fortytwo.sesametools.rdftransaction;

import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailWrapper;

/**
 * User: josh
 * Date: Aug 10, 2010
 * Time: 12:13:17 PM
 */
public abstract class RDFTransactionSail extends SailWrapper {
    public RDFTransactionSail(final Sail baseSail) {
        super(baseSail);
    }

    @Override
    public SailConnection getConnection() throws SailException {
        SailConnection b = this.getBaseSail().getConnection();

        return new RDFTransactionSailConnection(b, this);
    }

    public abstract void uploadTransactionEntity(final byte[] entity) throws SailException;
}
