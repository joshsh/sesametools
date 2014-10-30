
package net.fortytwo.sesametools.writeonly;

import info.aduna.iteration.CloseableIteration;
import net.fortytwo.sesametools.EmptyCloseableIteration;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailBase;
import org.openrdf.sail.helpers.SailConnectionBase;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class WriteOnlySailConnection extends SailConnectionBase {
    private RDFHandler handler;
    private ValueFactory valueFactory;

    public WriteOnlySailConnection(final SailBase sail,
                                   final RDFHandler handler,
                                   final ValueFactory valueFactory) {
        super(sail);
        this.handler = handler;
        this.valueFactory = valueFactory;
    }

    protected void addStatementInternal(
            final Resource subj, final URI pred, final Value obj, final Resource... contexts) throws SailException {

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
        return new EmptyCloseableIteration<BindingSet, QueryEvaluationException>();
    }

    protected CloseableIteration<? extends Resource, SailException> getContextIDsInternal()
            throws SailException {
        return new EmptyCloseableIteration<Resource, SailException>();
    }

    protected String getNamespaceInternal(final String prefix) throws SailException {
        return null;
    }

    protected CloseableIteration<? extends Namespace, SailException> getNamespacesInternal()
            throws SailException {
        return new EmptyCloseableIteration<Namespace, SailException>();
    }

    protected CloseableIteration<? extends Statement, SailException> getStatementsInternal(
            Resource arg0, URI arg1, Value arg2, boolean arg3, Resource... arg4)
            throws SailException {
        return new EmptyCloseableIteration<Statement, SailException>();
    }

    protected void removeNamespaceInternal(String arg0) throws SailException {
        // Does nothing.
    }

    protected void removeStatementsInternal(Resource arg0, URI arg1, Value arg2,
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
}
