package net.fortytwo.sesametools.debug;

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
import org.openrdf.query.algebra.UpdateExpr;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

/**
 * A SailConnection which simply relays operations to another SailConnection.
 * Overload one or more methods for testing and debugging.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class DebugSailConnection implements SailConnection {
    private final SailConnection baseSailConnection;
    private final SailCounter counter;

    public DebugSailConnection(final Sail baseSail, final SailCounter counter) throws SailException {
        baseSailConnection = baseSail.getConnection();
        this.counter = counter;
    }

    public void addStatement(final Resource subj, final URI pred, final Value obj,
                             final Resource... contexts) throws SailException {
        baseSailConnection.addStatement(subj, pred, obj, contexts);
    }

    public void clear(final Resource... contexts) throws SailException {
        baseSailConnection.clear(contexts);
    }

    public void clearNamespaces() throws SailException {
        baseSailConnection.clearNamespaces();
    }

    public void close() throws SailException {
        baseSailConnection.close();
    }

    public void commit() throws SailException {
        baseSailConnection.commit();
    }

    public CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluate(
            final TupleExpr tupleExpr, final Dataset dataset, final BindingSet bindingSet, boolean includeInferred)
            throws SailException {
        return baseSailConnection.evaluate(tupleExpr, dataset, bindingSet, includeInferred);
    }

    public void executeUpdate(final UpdateExpr updateExpr,
                              final Dataset dataset,
                              final BindingSet bindingSet,
                              final boolean b) throws SailException {
        baseSailConnection.executeUpdate(updateExpr, dataset, bindingSet, b);
    }

    public CloseableIteration<? extends Resource, SailException> getContextIDs()
            throws SailException {
        return baseSailConnection.getContextIDs();
    }

    public String getNamespace(final String prefix) throws SailException {
        return baseSailConnection.getNamespace(prefix);
    }

    public CloseableIteration<? extends Namespace, SailException> getNamespaces()
            throws SailException {
        return baseSailConnection.getNamespaces();
    }

    public CloseableIteration<? extends Statement, SailException> getStatements(
            final Resource subj, final URI pred, final Value obj, final boolean includeInferred, final Resource... contexts)
            throws SailException {
//System.out.println("getting statements: " + subj + ", " + pred + ", " + obj + ", " + includeInferred + ", " + contexts);
        counter.incremenentMethodCount(SailCounter.Method.GetStatements);
        return baseSailConnection.getStatements(subj, pred, obj, includeInferred, contexts);
    }

    public boolean isOpen() throws SailException {
        return baseSailConnection.isOpen();
    }

    public void removeNamespace(final String prefix) throws SailException {
        baseSailConnection.removeNamespace(prefix);
    }

    public void removeStatements(final Resource subj, final URI pred, final Value obj,
                                 final Resource... contexts) throws SailException {
        baseSailConnection.removeStatements(subj, pred, obj, contexts);
    }

    public void rollback() throws SailException {
        baseSailConnection.rollback();
    }

    public void setNamespace(final String prefix, final String name) throws SailException {
        baseSailConnection.setNamespace(prefix, name);
    }

    public long size(final Resource... contexts) throws SailException {
        return baseSailConnection.size(contexts);
    }
}
