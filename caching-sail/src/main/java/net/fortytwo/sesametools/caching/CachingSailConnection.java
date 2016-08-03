package net.fortytwo.sesametools.caching;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import net.fortytwo.sesametools.SailConnectionTripleSource;
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
import org.eclipse.rdf4j.query.algebra.evaluation.EvaluationStrategy;
import org.eclipse.rdf4j.query.algebra.evaluation.TripleSource;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.SimpleEvaluationStrategy;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.helpers.AbstractSail;
import org.eclipse.rdf4j.sail.helpers.AbstractSailConnection;

import java.util.Set;

// TODO: define rollback behavior

// TODO: investigate inference

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class CachingSailConnection extends AbstractSailConnection {
    private boolean cacheSubject, cachePredicate, cacheObject;

    private ValueFactory valueFactory;

    private SailConnection baseSailConnection;
    private SailConnection cacheConnection;

    private Set<Resource> cachedSubjects;
    private Set<IRI> cachedPredicates;
    private Set<Value> cachedObjects;

    private boolean uncommittedChanges = false;

    public CachingSailConnection(final AbstractSail sail,
                                 final Sail baseSail,
                                 final Sail cache,
                                 final boolean cacheSubject,
                                 final boolean cachePredicate,
                                 final boolean cacheObject,
                                 final Set<Resource> cachedSubjects,
                                 final Set<IRI> cachedPredicates,
                                 final Set<Value> cachedObjects) throws SailException {
        super(sail);
        this.cacheSubject = cacheSubject;
        this.cachePredicate = cachePredicate;
        this.cacheObject = cacheObject;
        baseSailConnection = baseSail.getConnection();
        cacheConnection = cache.getConnection();

        this.cachedSubjects = cachedSubjects;
        this.cachedPredicates = cachedPredicates;
        this.cachedObjects = cachedObjects;

        this.valueFactory = baseSail.getValueFactory();
    }

    // Note: adding statements does not change the configuration of cached
    // values.
    protected void addStatementInternal(final Resource subj,
                                     final IRI pred,
                                     final Value obj,
                                     final Resource... contexts) throws SailException {
        cacheConnection.addStatement(subj, pred, obj, contexts);
        baseSailConnection.addStatement(subj, pred, obj, contexts);
        uncommittedChanges = true;
    }

    // Note: clearing statements does not change the configuration of cached
    // values.
    protected void clearInternal(final Resource... contexts) throws SailException {
        cacheConnection.clear(contexts);
        baseSailConnection.clear(contexts);
        uncommittedChanges = true;
    }

    protected void clearNamespacesInternal() throws SailException {
        baseSailConnection.clearNamespaces();
    }

    protected void closeInternal() throws SailException {
        baseSailConnection.close();
        cacheConnection.close();
    }

    protected void commitInternal() throws SailException {
        if (uncommittedChanges) {
            cacheConnection.commit();
            baseSailConnection.commit();
            uncommittedChanges = false;
        }
    }

    protected CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluateInternal(
            final TupleExpr tupleExpr,
            final Dataset dataSet,
            final BindingSet bindingSet,
            final boolean includeInferred) throws SailException {
        try {
            TripleSource tripleSource = new SailConnectionTripleSource(this, valueFactory, includeInferred);
            EvaluationStrategy strategy = new SimpleEvaluationStrategy(tripleSource, dataSet, null);

            return strategy.evaluate(tupleExpr, bindingSet);
        } catch (QueryEvaluationException e) {
            throw new SailException(e);
        }
    }

    /*
    @Override
    public void executeUpdate(final UpdateExpr updateExpr,
                              final Dataset dataset,
                              final BindingSet bindingSet,
                              final boolean b) throws SailException {
        cacheConnection.executeUpdate(updateExpr, dataset, bindingSet, b);
        baseSailConnection.executeUpdate(updateExpr, dataset, bindingSet, b);
        uncommittedChanges = true;
    }*/

    protected CloseableIteration<? extends Resource, SailException> getContextIDsInternal()
            throws SailException {
        return baseSailConnection.getContextIDs();
    }

    protected String getNamespaceInternal(final String prefix) throws SailException {
        return baseSailConnection.getNamespace(prefix);
    }

    protected CloseableIteration<? extends Namespace, SailException> getNamespacesInternal()
            throws SailException {
        return baseSailConnection.getNamespaces();
    }

    @Override
    protected CloseableIteration<? extends Statement, SailException> getStatementsInternal(
            final Resource subj,
            final IRI pred,
            final Value obj,
            final boolean includeInferred,
            final Resource... context) throws SailException {

        if (null != subj && cacheSubject) {
            if (!cachedSubjects.contains(subj)) {
                cacheStatements(subj, null, null);
                cachedSubjects.add(subj);
            }

            return cacheConnection.getStatements(subj, pred, obj, includeInferred, context);
        } else if (null != obj && cacheObject) {
            if (!cachedObjects.contains(obj)) {
                cacheStatements(null, null, obj);
                cachedObjects.add(obj);
            }

            return cacheConnection.getStatements(subj, pred, obj, includeInferred, context);
        } else if (null != pred && cachePredicate) {
            if (!cachedPredicates.contains(pred)) {
                cacheStatements(null, pred, null);
                cachedPredicates.add(pred);
            }

            return cacheConnection.getStatements(subj, pred, obj, includeInferred, context);
        } else {
            return baseSailConnection.getStatements(subj, pred, obj, includeInferred, context);
        }
    }

    protected void removeNamespaceInternal(final String prefix) throws SailException {
        baseSailConnection.removeNamespace(prefix);
    }

    // Note: removing statements does not change the configuration of cached
    // values.
    protected void removeStatementsInternal(final Resource subj, final IRI pred, final Value obj,
                                         final Resource... contexts) throws SailException {
        cacheConnection.removeStatements(subj, pred, obj, contexts);
        baseSailConnection.removeStatements(subj, pred, obj, contexts);
        uncommittedChanges = true;
    }

    // No rollback ability for now.
    protected void rollbackInternal() throws SailException {
        /*
          cacheConnection.rollback();
          baseSailConnection.rollback();
          uncommittedChanges = false;*/
    }

    protected void setNamespaceInternal(final String prefix, final String name) throws SailException {
        baseSailConnection.setNamespace(prefix, name);
    }

    protected long sizeInternal(final Resource... contexts) throws SailException {
        return baseSailConnection.size(contexts);
    }

    protected void startTransactionInternal() throws SailException {
        baseSailConnection.begin();
        cacheConnection.begin();
    }

    private void cacheStatements(final Resource subj, final IRI pred, final Value obj) throws SailException {

        cacheConnection.begin();

        CloseableIteration<? extends Statement, SailException> iter
                = baseSailConnection.getStatements(subj, pred, obj, false);

        while (iter.hasNext()) {
            Statement st = iter.next();
            cacheConnection.addStatement(st.getSubject(), st.getPredicate(), st.getObject(), st.getContext());
        }

        iter.close();

        cacheConnection.commit();
    }

    public SailConnection getBaseConnection() {
        return baseSailConnection;
    }
}
