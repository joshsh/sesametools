package net.fortytwo.sesametools.readonly;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.helpers.AbstractSail;
import org.eclipse.rdf4j.sail.helpers.AbstractSailConnection;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class ReadOnlySailConnection extends AbstractSailConnection {
    private final SailConnection baseSailConnection;

    public ReadOnlySailConnection(final AbstractSail sail,
                                  final Sail baseSail) throws SailException {
        super(sail);
        baseSailConnection = baseSail.getConnection();
    }

    protected void closeInternal() throws SailException {
        baseSailConnection.close();
    }

    protected CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluateInternal(
            final TupleExpr tupleExpr, final Dataset dataset, final BindingSet bindings, final boolean includeInferred)
            throws SailException {

        return baseSailConnection.evaluate(tupleExpr, dataset, bindings, includeInferred);
    }

    protected CloseableIteration<? extends Resource, SailException> getContextIDsInternal() throws SailException {
        return baseSailConnection.getContextIDs();
    }

    protected CloseableIteration<? extends Statement, SailException> getStatementsInternal(
            final Resource subject, final IRI predicate, final Value object, final boolean includeInferred,
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
                                        final IRI predicate,
                                        final Value object,
                                        final Resource... contexts) throws SailException {
        // Do nothing.
    }

    protected void removeStatementsInternal(final Resource subject,
                                            final IRI predicate,
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

    @Override
    public boolean pendingRemovals() {
        return false;
    }
}
