package net.fortytwo.sesametools.rdftransaction;

import org.openrdf.http.protocol.transaction.TransactionWriter;
import org.openrdf.http.protocol.transaction.operations.AddStatementOperation;
import org.openrdf.http.protocol.transaction.operations.ClearOperation;
import org.openrdf.http.protocol.transaction.operations.RemoveNamespaceOperation;
import org.openrdf.http.protocol.transaction.operations.RemoveStatementsOperation;
import org.openrdf.http.protocol.transaction.operations.SetNamespaceOperation;
import org.openrdf.http.protocol.transaction.operations.TransactionOperation;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.UpdateExpr;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailConnectionWrapper;

import java.util.LinkedList;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * User: josh
 * Date: Aug 10, 2010
 * Time: 12:13:44 PM
 */
public class RDFTransactionSailConnection extends SailConnectionWrapper {
    private final List<TransactionOperation> operations;
    private final List<TransactionOperation> buffer;
    private final TransactionWriter writer;
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
        this.operations = new LinkedList<TransactionOperation>();
        this.buffer = new LinkedList<TransactionOperation>();
        this.writer = new TransactionWriter();
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
        try {
            if (0 < buffer.size()) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                writer.serialize(buffer, bos);
                sail.uploadTransactionEntity(bos.toByteArray());
                bos.close();
            }
        } catch (IOException e) {
            throw new SailException(e);
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
    public void addStatement(Resource subject, URI predicate, Value object, Resource... contexts) throws SailException {
        this.getWrappedConnection().addStatement(subject, predicate, object, contexts);

        operations.add(new AddStatementOperation(subject, predicate, object, contexts));
    }

    @Override
    public void removeStatements(Resource subject, URI predicate, Value object, Resource... contexts) throws SailException {
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

        // FIXME: this should be restored.  It is temporarily disabled due to an AllegroGraph bug.
        //operations.add(new ClearNamespacesOperation());
    }

    @Override
    public void close() throws SailException {
        commitAll();
        super.close();
    }

    @Override
    public void executeUpdate(final UpdateExpr updateExpr,
                              final Dataset dataset,
                              final BindingSet bindingSet,
                              final boolean b) throws SailException {
        throw new UnsupportedOperationException("SPARQL Update is not yet supported");
    }
}
