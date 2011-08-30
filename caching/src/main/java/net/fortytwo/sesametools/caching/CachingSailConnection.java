package net.fortytwo.sesametools.caching;

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

import java.util.Set;

// TODO: define rollback behavior

// TODO: investigate inference

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class CachingSailConnection implements SailConnection {
    private boolean cacheSubject, cachePredicate, cacheObject;

    private ValueFactory valueFactory;

    private SailConnection baseSailConnection;
    private SailConnection cacheConnection;

    private Set<Resource> cachedSubjects;
    private Set<URI> cachedPredicates;
    private Set<Value> cachedObjects;

    private boolean uncommittedChanges = false;

    public CachingSailConnection(final Sail baseSail,
                                 final Sail cache,
                                 final boolean cacheSubject,
                                 final boolean cachePredicate,
                                 final boolean cacheObject,
                                 final Set<Resource> cachedSubjects,
                                 final Set<URI> cachedPredicates,
                                 final Set<Value> cachedObjects) throws SailException {
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
    public void addStatement(final Resource subj, final URI pred, final Value obj,
                             final Resource... contexts) throws SailException {
        cacheConnection.addStatement(subj, pred, obj, contexts);
        baseSailConnection.addStatement(subj, pred, obj, contexts);
        uncommittedChanges = true;
    }

    // Note: clearing statements does not change the configuration of cached
    // values.
    public void clear(final Resource... contexts) throws SailException {
        cacheConnection.clear(contexts);
        baseSailConnection.clear(contexts);
        uncommittedChanges = true;
    }

    public void clearNamespaces() throws SailException {
        baseSailConnection.clearNamespaces();
    }

    public void close() throws SailException {
        baseSailConnection.close();
        cacheConnection.close();
    }

    public void commit() throws SailException {
        if (uncommittedChanges) {
            cacheConnection.commit();
            baseSailConnection.commit();
            uncommittedChanges = false;
        }
    }

    public CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluate(
            final TupleExpr tupleExpr, final Dataset dataSet, final BindingSet bindingSet, final boolean includeInferred)
            throws SailException {
        try {
            TripleSource tripleSource = new SailConnectionTripleSource(this, valueFactory, includeInferred);
            EvaluationStrategyImpl strategy = new EvaluationStrategyImpl(tripleSource, dataSet);

            return strategy.evaluate(tupleExpr, bindingSet);
        } catch (QueryEvaluationException e) {
            throw new SailException(e);
        }
    }

    @Override
    public void executeUpdate(final UpdateExpr updateExpr,
                              final Dataset dataset,
                              final BindingSet bindingSet,
                              final boolean b) throws SailException {
        cacheConnection.executeUpdate(updateExpr, dataset, bindingSet, b);
        baseSailConnection.executeUpdate(updateExpr, dataset, bindingSet, b);
        uncommittedChanges = true;
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
            final Resource subj, final URI pred, final Value obj, final boolean includeInferred, final Resource... context)
            throws SailException {
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

    public boolean isOpen() throws SailException {
        return baseSailConnection.isOpen();
    }

    public void removeNamespace(final String prefix) throws SailException {
        baseSailConnection.removeNamespace(prefix);
    }

    // Note: removing statements does not change the configuration of cached
    // values.
    public void removeStatements(final Resource subj, final URI pred, final Value obj,
                                 final Resource... contexts) throws SailException {
        cacheConnection.removeStatements(subj, pred, obj, contexts);
        baseSailConnection.removeStatements(subj, pred, obj, contexts);
    }

    // No rollback ability for now.
    public void rollback() throws SailException {
        /*
          cacheConnection.rollback();
          baseSailConnection.rollback();
          uncommittedChanges = false;*/
    }

    public void setNamespace(final String prefix, final String name) throws SailException {
        baseSailConnection.setNamespace(prefix, name);
    }

    public long size(final Resource... contexts) throws SailException {
        return baseSailConnection.size(contexts);
    }

    private void cacheStatements(final Resource subj, final URI pred, final Value obj) throws SailException {
        boolean includeInferred = false;

        CloseableIteration<? extends Statement, SailException> iter
                = baseSailConnection.getStatements(subj, pred, obj, includeInferred);

        while (iter.hasNext()) {
            Statement st = iter.next();
            cacheConnection.addStatement(st.getSubject(), st.getPredicate(), st.getObject(), st.getContext());
        }

        iter.close();

        cacheConnection.commit();
    }
}
