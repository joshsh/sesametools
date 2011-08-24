package net.fortytwo.sesametools.decompose;

import net.fortytwo.sesametools.SailConnectionTripleSource;
import info.aduna.iteration.CloseableIteration;
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
import org.openrdf.query.algebra.UpdateExpr;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

/**
 * Author: josh
 * Date: Jun 2, 2008
 * Time: 12:24:22 PM
 */
public class DecomposeTupleQueriesSailConnection implements SailConnection {
    private final SailConnection baseSailConnection;
    private final ValueFactory valueFactory;

    public DecomposeTupleQueriesSailConnection(final Sail baseSail) throws SailException {
        baseSailConnection = baseSail.getConnection();
        valueFactory = baseSail.getValueFactory();
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

    /*
     * Decompose tuple queries into getStatements calls.
     */
    public CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluate(
            final TupleExpr tupleExpr, final Dataset dataset, final BindingSet bindingSet, boolean includeInferred)
            throws SailException {
        try {
            TripleSource tripleSource = new SailConnectionTripleSource(this,
                    valueFactory, includeInferred);
            EvaluationStrategyImpl strategy = new EvaluationStrategyImpl(
                    tripleSource, dataset);
            return strategy.evaluate(tupleExpr, bindingSet);
        }
        catch (QueryEvaluationException e) {
            throw new SailException(e);
        }
    }

    @Override
	public void executeUpdate(UpdateExpr arg0, Dataset arg1, BindingSet arg2,
			boolean arg3) throws SailException {
    	baseSailConnection.executeUpdate(arg0, arg1, arg2, arg3);
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
