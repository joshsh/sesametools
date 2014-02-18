package net.fortytwo.sesametools.readonly;

import info.aduna.iteration.CloseableIteration;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailBase;
import org.openrdf.sail.helpers.SailConnectionBase;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class ReadOnlySailConnection extends SailConnectionBase {
    private final SailConnection baseSailConnection;

    public ReadOnlySailConnection(final SailBase sail,
                                  final Sail baseSail) throws SailException {
        super(sail);
        baseSailConnection = baseSail.getConnection();
    }

    protected void closeInternal() throws SailException {
        baseSailConnection.close();
    }

    protected CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluateInternal(final TupleExpr tupleExpr,
                                                                                       final Dataset dataset,
                                                                                       final BindingSet bindings,
                                                                                       final boolean includeInferred) throws SailException {
        return baseSailConnection.evaluate(tupleExpr, dataset, bindings, includeInferred);
    }

    protected CloseableIteration<? extends Resource, SailException> getContextIDsInternal() throws SailException {
        return baseSailConnection.getContextIDs();
    }

    protected CloseableIteration<? extends Statement, SailException> getStatementsInternal(final Resource subject,
                                                                                final URI predicate,
                                                                                final Value object,
                                                                                final boolean includeInferred,
                                                                                final Resource... contexts) throws SailException {
        return baseSailConnection.getStatements(subject, predicate, object, includeInferred, contexts);
    }

    protected long sizeInternal(final Resource... contexts) throws SailException {
        return baseSailConnection.size(contexts);
    }

    protected void commitInternal() throws SailException {
        // Do nothing.
    }

    protected void rollbackInternal() throws SailException {
        // Do nothing.
    }

    protected void addStatementInternal(final Resource subject,
                             final URI predicate,
                             final Value object,
                             final Resource... contexts) throws SailException {
        // Do nothing.
    }

    protected void removeStatementsInternal(final Resource subject,
                                 final URI predicate,
                                 final Value object,
                                 final Resource... contexts) throws SailException {
        // Do nothing.
    }

    protected void clearInternal(final Resource... contexts) throws SailException {
        // Do nothing.
    }

    protected CloseableIteration<? extends Namespace, SailException> getNamespacesInternal() throws SailException {
        return baseSailConnection.getNamespaces();
    }

    protected String getNamespaceInternal(final String prefix) throws SailException {
        return baseSailConnection.getNamespace(prefix);
    }

    protected void setNamespaceInternal(final String prefix,
                             final String uri) throws SailException {
        // Do nothing.
    }

    protected void removeNamespaceInternal(final String prefix) throws SailException {
        // Do nothing.
    }

    protected void clearNamespacesInternal() throws SailException {
        // Do nothing.
    }

    protected void startTransactionInternal() throws SailException {
        baseSailConnection.begin();
    }
}
