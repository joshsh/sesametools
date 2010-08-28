package net.fortytwo.sesametools.rdftransaction;

import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailWrapper;

/**
 * A Sail which uploads committed transactions in the application/x-rdftransaction format
 * 
 * User: josh
 * Date: Aug 10, 2010
 * Time: 12:13:17 PM
 */
public abstract class RDFTransactionSail extends SailWrapper {
    private final int commitsPerUpload;

    public RDFTransactionSail(final Sail baseSail,
                              final int commitsPerUpload) {
        super(baseSail);
        this.commitsPerUpload = commitsPerUpload;
    }

    public RDFTransactionSail(final Sail baseSail) {
        this(baseSail, 1);
    }

    @Override
    public SailConnection getConnection() throws SailException {
        SailConnection b = this.getBaseSail().getConnection();

        return new RDFTransactionSailConnection(b, this, commitsPerUpload);
    }

    public abstract void uploadTransactionEntity(final byte[] entity) throws SailException;
}
