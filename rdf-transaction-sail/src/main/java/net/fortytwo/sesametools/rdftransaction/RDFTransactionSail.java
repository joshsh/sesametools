package net.fortytwo.sesametools.rdftransaction;

import org.openrdf.http.protocol.transaction.TransactionWriter;
import org.openrdf.http.protocol.transaction.operations.TransactionOperation;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * A <code>Sail</code> which uploads committed transactions in the application/x-rdftransaction format
 *
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public abstract class RDFTransactionSail extends SailWrapper {
    private final int commitsPerTransaction;
    private final TransactionWriter writer = new TransactionWriter();

    public RDFTransactionSail(final Sail baseSail,
                              final int commitsPerTransaction) {
        super(baseSail);
        this.commitsPerTransaction = commitsPerTransaction;
    }

    public RDFTransactionSail(final Sail baseSail) {
        this(baseSail, 1);
    }

    @Override
    public SailConnection getConnection() throws SailException {
        SailConnection b = this.getBaseSail().getConnection();

        return new RDFTransactionSailConnection(b, this, commitsPerTransaction);
    }

    protected byte[] createTransactionEntity(final List<TransactionOperation> operations) throws SailException {
        try {
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                writer.serialize(operations, bos);
                return bos.toByteArray();
            }
        } catch (IOException e) {
            throw new SailException(e);
        }
    }

    public abstract void handleTransaction(final List<TransactionOperation> operations) throws SailException;
}
