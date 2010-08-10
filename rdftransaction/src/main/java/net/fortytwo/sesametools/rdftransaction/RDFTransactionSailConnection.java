package net.fortytwo.sesametools.rdftransaction;

import org.openrdf.http.protocol.transaction.TransactionWriter;
import org.openrdf.http.protocol.transaction.operations.AddStatementOperation;
import org.openrdf.http.protocol.transaction.operations.ClearNamespacesOperation;
import org.openrdf.http.protocol.transaction.operations.ClearOperation;
import org.openrdf.http.protocol.transaction.operations.RemoveNamespaceOperation;
import org.openrdf.http.protocol.transaction.operations.RemoveStatementsOperation;
import org.openrdf.http.protocol.transaction.operations.SetNamespaceOperation;
import org.openrdf.http.protocol.transaction.operations.TransactionOperation;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailConnectionWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * User: josh
 * Date: Aug 10, 2010
 * Time: 12:13:44 PM
 */
public class RDFTransactionSailConnection extends SailConnectionWrapper {
    private final List<TransactionOperation> operations;
    private final TransactionWriter writer;
    private final RDFTransactionSail sail;

    public RDFTransactionSailConnection(final SailConnection c,
                                        final RDFTransactionSail sail) {
        super(c);
        this.sail = sail;
        this.operations = new LinkedList<TransactionOperation>();
        this.writer = new TransactionWriter();
    }

    @Override
    public void commit() throws SailException {
        this.getWrappedConnection().commit();

        try {
            if (0 < operations.size()) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                writer.serialize(operations, bos);
                sail.uploadTransactionEntity(bos.toByteArray());
                bos.close();
            }
        } catch (IOException e) {
            throw new SailException(e);
        }
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

        operations.add(new ClearNamespacesOperation());
    }
}
