
package net.fortytwo.sesametools.writeonly;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import net.fortytwo.sesametools.EmptyCloseableIteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.helpers.AbstractSail;
import org.eclipse.rdf4j.sail.helpers.AbstractSailConnection;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class WriteOnlySailConnection extends AbstractSailConnection {
    private RDFHandler handler;
    private ValueFactory valueFactory;

    public WriteOnlySailConnection(final AbstractSail sail,
                                   final RDFHandler handler,
                                   final ValueFactory valueFactory) {
        super(sail);
        this.handler = handler;
        this.valueFactory = valueFactory;
    }

    protected void addStatementInternal(
            final Resource subj, final IRI pred, final Value obj, final Resource... contexts) throws SailException {

        if (null == contexts || 0 == contexts.length) {
            Statement st = valueFactory.createStatement(subj, pred, obj);
            try {
                handler.handleStatement(st);
            } catch (RDFHandlerException e) {
                throw new SailException(e);
            }
        } else {
            for (Resource ctx : contexts) {
                Statement st = valueFactory.createStatement(subj, pred, obj, ctx);
                try {
                    handler.handleStatement(st);
                } catch (RDFHandlerException e) {
                    throw new SailException(e);
                }
            }
        }
    }

    protected void clearInternal(Resource... arg0) throws SailException {
        // Does nothing.
    }

    protected void clearNamespacesInternal() throws SailException {
        // Does nothing.
    }

    protected void closeInternal() throws SailException {
    }

    protected void commitInternal() throws SailException {
    }

    protected CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluateInternal(
            TupleExpr arg0, Dataset arg1, BindingSet arg2, boolean arg3)
            throws SailException {
        return new EmptyCloseableIteration<>();
    }

    protected CloseableIteration<? extends Resource, SailException> getContextIDsInternal()
            throws SailException {
        return new EmptyCloseableIteration<>();
    }

    protected String getNamespaceInternal(final String prefix) throws SailException {
        return null;
    }

    protected CloseableIteration<? extends Namespace, SailException> getNamespacesInternal()
            throws SailException {
        return new EmptyCloseableIteration<>();
    }

    protected CloseableIteration<? extends Statement, SailException> getStatementsInternal(
            Resource arg0, IRI arg1, Value arg2, boolean arg3, Resource... arg4)
            throws SailException {
        return new EmptyCloseableIteration<>();
    }

    protected void removeNamespaceInternal(String arg0) throws SailException {
        // Does nothing.
    }

    protected void removeStatementsInternal(Resource arg0, IRI arg1, Value arg2,
                                 Resource... arg3) throws SailException {
        // Does nothing.
    }

    protected void rollbackInternal() throws SailException {
    }

    protected void setNamespaceInternal(String arg0, String arg1) throws SailException {
        // Does nothing.
    }

    protected long sizeInternal(final Resource... contexts) throws SailException {
        return 0;
    }

    protected void startTransactionInternal() throws SailException {
        // Does nothing.
    }

    @Override
    public boolean pendingRemovals() {
        return false;
    }
}
