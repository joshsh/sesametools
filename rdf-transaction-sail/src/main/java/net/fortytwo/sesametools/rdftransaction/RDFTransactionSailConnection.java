package net.fortytwo.sesametools.rdftransaction;

import org.openrdf.http.protocol.transaction.operations.AddStatementOperation;
import org.openrdf.http.protocol.transaction.operations.ClearNamespacesOperation;
import org.openrdf.http.protocol.transaction.operations.ClearOperation;
import org.openrdf.http.protocol.transaction.operations.RemoveNamespaceOperation;
import org.openrdf.http.protocol.transaction.operations.RemoveStatementsOperation;
import org.openrdf.http.protocol.transaction.operations.SetNamespaceOperation;
import org.openrdf.http.protocol.transaction.operations.TransactionOperation;
import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailConnectionWrapper;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class RDFTransactionSailConnection extends SailConnectionWrapper {
    private final List<TransactionOperation> operations;
    private final List<TransactionOperation> buffer;
    private final RDFTransactionSail sail;
    private final int commitsPerUpload;
    private int commits = 0;

    /**
     * @param c                the wrapped connection
     * @param sail             the owner Sail
     * @param commitsPerUpload if transactions should be grouped together, rather than uploaded
     *                         individually.  Note: when the SailConnection is closed, any leftover
     *                         transactions (committed but not uploaded) will be uploaded.
     */
    public RDFTransactionSailConnection(final SailConnection c,
                                        final RDFTransactionSail sail,
                                        final int commitsPerUpload) {
        super(c);
        this.sail = sail;
        this.operations = new LinkedList<>();
        this.buffer = new LinkedList<>();
        this.commitsPerUpload = commitsPerUpload;
    }

    @Override
    public void commit() throws SailException {

        this.getWrappedConnection().commit();

        buffer.addAll(operations);
        operations.clear();

        commits++;
        if (commits == commitsPerUpload) {
            commitAll();
        }
    }

    private void commitAll() throws SailException {
        if (0 < buffer.size()) {
            sail.handleTransaction(buffer);
        }

        buffer.clear();
        commits = 0;
    }

    @Override
    public void rollback() throws SailException {
        this.getWrappedConnection().rollback();

        operations.clear();
    }

    @Override
    public void addStatement(Resource subject, IRI predicate, Value object, Resource... contexts)
            throws SailException {

        this.getWrappedConnection().addStatement(subject, predicate, object, contexts);

        operations.add(new AddStatementOperation(subject, predicate, object, contexts));
    }

    @Override
    public void removeStatements(Resource subject, IRI predicate, Value object, Resource... contexts)
            throws SailException {

        this.getWrappedConnection().removeStatements(subject, predicate, object, contexts);

        operations.add(new RemoveStatementsOperation(subject, predicate, object, contexts));
    }

    @Override
    public void clear(Resource... contexts) throws SailException {
        this.getWrappedConnection().clear(contexts);

        operations.add(new ClearOperation(contexts));
    }

    @Override
    public void setNamespace(String prefix, String uri) throws SailException {
        this.getWrappedConnection().setNamespace(prefix, uri);

        operations.add(new SetNamespaceOperation(prefix, uri));
    }

    @Override
    public void removeNamespace(String prefix) throws SailException {
        this.getWrappedConnection().removeNamespace(prefix);

        operations.add(new RemoveNamespaceOperation(prefix));
    }

    @Override
    public void clearNamespaces() throws SailException {
        this.getWrappedConnection().clearNamespaces();

        operations.add(new ClearNamespacesOperation());
    }

    @Override
    public void close() throws SailException {
        commitAll();
        super.close();
    }
}
