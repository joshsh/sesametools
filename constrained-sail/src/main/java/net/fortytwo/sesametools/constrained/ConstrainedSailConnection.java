
package net.fortytwo.sesametools.constrained;

import info.aduna.iteration.CloseableIteration;
import net.fortytwo.sesametools.CompoundCloseableIteration;
import net.fortytwo.sesametools.EmptyCloseableIteration;
import net.fortytwo.sesametools.SailConnectionTripleSource;
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
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailConnectionWrapper;

import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class ConstrainedSailConnection extends SailConnectionWrapper {
    // For now, we won't allow inferred statements through when statements from
    // specific contexts are requested.  Inferred statements for queries using
    // a wildcard context are allowed because they have to be checked
    // after-the-fact anyway.
    private static final boolean INCLUDE_INFERRED_STATEMENTS = false;

    // For a wilcard removeStatements, query for all matching statements and
    // remove only the ones which are in writable named graphs.
    private static final boolean WILDCARD_REMOVE_FROM_ALL_CONTEXTS = true;

    private boolean namespacesAreReadable;
    private boolean namespacesAreWritable;
    private boolean hideNonWritableContexts;

    private ValueFactory valueFactory;

    // For now, it is assumed that no requestor has permission to see the total
    // number of statements in all contexts.
    private boolean allowWildcardSize = false;

    // For now, it is assumed that no requestor has permission to clear
    // statements from all contexts.
    private boolean allowWildcardClear = false;

    private Dataset readableSet;
    private Dataset writableSet;
    private Resource defaultWriteContext;

    /**
     * @param baseSailConnection      a subordinate SailConnection.  When this SailConnection is
     *                                closed, it closes the subordinate collection as well.
     * @param valueFactory            ValueFactory from wrapped sail.
     * @param readableSet             all contexts from which the requestor is
     *                                allowed to read (possibly including the null context)
     * @param writableSet             all contexts to or from which the
     *                                requestor is allowed to add or delete statements.  Note that while it's
     *                                possible to add statements to the null context, it is not possible to
     *                                remove statements from it
     * @param defaultWriteContext     the default context to or from which to add
     *                                or remove statements when no other context is given.  It must be a
     *                                writable namespace to be of use
     * @param namespacesAreReadable   whether the requestor can see namespace
     *                                definitions
     * @param namespacesAreWritable   whether the requestor can modify namespace
     *                                definitions
     * @param hideNonWritableContexts removes context information from non-writable graphs.
     * @throws SailException If there is an error communicating with the base SAIL
     */
    public ConstrainedSailConnection(final SailConnection baseSailConnection,
                                     final ValueFactory valueFactory,
                                     final Dataset readableSet,
                                     final Dataset writableSet,
                                     final Resource defaultWriteContext,
                                     final boolean namespacesAreReadable,
                                     final boolean namespacesAreWritable,
                                     final boolean hideNonWritableContexts) throws SailException {
        super(baseSailConnection);
        this.valueFactory = valueFactory;
        this.readableSet = readableSet;
        this.writableSet = writableSet;
        this.defaultWriteContext = defaultWriteContext;
        this.namespacesAreReadable = namespacesAreReadable;
        this.namespacesAreWritable = namespacesAreWritable;
        this.hideNonWritableContexts = hideNonWritableContexts;

        if (null != defaultWriteContext
                && (!writePermitted(defaultWriteContext) || !deletePermitted(defaultWriteContext))) {
            this.defaultWriteContext = null;
        }
    }

    /**
     * Adds a statement to each of the given contexts for which the requestor
     * has write access.  If no context is given, statements will be written to
     * the default write context, provided that it is writable.
     */
    @Override
    public void addStatement(final Resource subj,
                             final URI pred,
                             final Value obj,
                             final Resource... contexts) throws SailException {
        if (0 == contexts.length) {
            if (writePermitted(defaultWriteContext)) {
                if (null == defaultWriteContext) {
//System.out.println("wildcard write to null context");
                    super.addStatement(subj, pred, obj);
                } else {
//System.out.println("wildcard write to context: " + defaultWriteContext);
                    super.addStatement(subj, pred, obj, defaultWriteContext);
                }
            }
        } else {
            for (Resource context : contexts) {
                if (writePermitted(context)) {
                    super.addStatement(subj, pred, obj, context);
                }
            }
        }
    }

    /**
     * Clears the statements in all of the given contexts for which the
     * requestor has write access.  If no context is given, the default write
     * context will be cleared, provided this is writable and not null.
     */
    @Override
    public void clear(final Resource... contexts) throws SailException {
        if (0 == contexts.length) {
            if (null != defaultWriteContext) {
                if (deletePermitted(defaultWriteContext)) {
                    super.clear(defaultWriteContext);
                }
            } else if (allowWildcardClear) {
                super.clear();
            }
        } else {
            for (Resource context : contexts) {
                if (writePermitted(context)) {
                    super.clear(context);
                }
            }
        }
    }

    @Override
    public void clearNamespaces() throws SailException {
        if (namespacesAreWritable) {
            super.clearNamespaces();
        }
    }

    @Override
    public CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluate(
            final TupleExpr tupleExpr,
            final Dataset dataset,
            final BindingSet bindings,
            final boolean includeInferred) throws SailException {
        return evaluateByGraphNames(tupleExpr, dataset, bindings, includeInferred);
        //return evaluateByDecomposition(tupleExpr, dataset, bindings, includeInferred);
    }

    // TODO: more thorough testing involving both "FROM" and "FROM NAMED" clauses in SPARQL queries
    public CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluateByGraphNames(
            final TupleExpr tupleExpr,
            final Dataset dataset,
            final BindingSet bindings,
            final boolean includeInferred) throws SailException {
        Dataset d;

        if (null == dataset) {
            d = this.readableSet;
        } else {
            DatasetImpl di = new DatasetImpl();
            d = di;

            for (URI r : dataset.getDefaultGraphs()) {
                if (this.readPermitted(r)) {
                    di.addDefaultGraph(r);
                }
            }

            for (URI r : dataset.getNamedGraphs()) {
                if (this.readPermitted(r)) {
                    di.addNamedGraph(r);
                }
            }
        }

        return super.evaluate(tupleExpr, d, bindings, includeInferred);
    }

    private CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluateByDecomposition(
            final TupleExpr tupleExpr,
            final Dataset dataset,
            final BindingSet bindings,
            final boolean includeInferred) throws SailException {
        try {
            TripleSource tripleSource = new SailConnectionTripleSource(this, valueFactory, includeInferred);
            EvaluationStrategyImpl strategy = new EvaluationStrategyImpl(tripleSource, dataset);

            return strategy.evaluate(tupleExpr, bindings);
        } catch (QueryEvaluationException e) {
            throw new SailException(e);
        }
    }

    /**
     * @return an iterator containing only those context IDs to which the
     * requestor has read access (excluding the null context).
     */
    @Override
    public CloseableIteration<? extends Resource, SailException> getContextIDs()
            throws SailException {
        return new ReadableContextIteration(super.getContextIDs());
    }

    @Override
    public String getNamespace(String prefix) throws SailException {
        return (namespacesAreReadable)
                ? super.getNamespace(prefix)
                : null;
    }

    @Override
    public CloseableIteration<? extends Namespace, SailException> getNamespaces()
            throws SailException {
        return (namespacesAreReadable)
                ? super.getNamespaces()
                : new EmptyCloseableIteration<Namespace, SailException>();
    }


    @Override
    public CloseableIteration<? extends Statement, SailException> getStatements(
            final Resource subj,
            final URI pred,
            final Value obj,
            final boolean includeInferred,
            final Resource... contexts) throws SailException {
        // Get statements from a wildcard context --> filter after retrieving
        // statements.
        if (0 == contexts.length) {
            return new ReadableStatementIteration(
                    super.getStatements(subj, pred, obj, includeInferred));
        }

        // Get statements in specific contexts --> filter before retrieving
        // statements.  The statements retrieved are assumed to be in one of the
        // requested contexts.
        else {
            Collection<CloseableIteration<? extends Statement, SailException>>
                    iterations = new LinkedList<CloseableIteration<? extends Statement, SailException>>();

            for (Resource context : contexts) {
                if (readPermitted(context)) {
                    iterations.add(super.getStatements(
                            subj, pred, obj, INCLUDE_INFERRED_STATEMENTS, context));
                }
            }

            return new CompoundCloseableIteration(iterations);
        }
    }

    @Override
    public void removeNamespace(final String prefix) throws SailException {
        if (namespacesAreWritable) {
            super.removeNamespace(prefix);
        }
    }

    /**
     * @param contexts if supplied, matching statements will be removed from
     *                 given contexts to which the requestor has delete access.  If absent,
     *                 matching statements will be removed from the designated writeable context.
     */
    @Override
    public void removeStatements(final Resource subj, final URI pred, final Value obj,
                                 final Resource... contexts) throws SailException {
        if (0 == contexts.length) {
            if (WILDCARD_REMOVE_FROM_ALL_CONTEXTS) {
                Collection<Resource> toRemove = new LinkedList<Resource>();
                boolean includeInferred = false;
                CloseableIteration<? extends Statement, SailException> iter
                        = super.getStatements(subj, pred, obj, includeInferred);
                try {
                    while (iter.hasNext()) {
                        Resource context = iter.next().getContext();
                        if (null != context && writePermitted(context)) {
                            toRemove.add(context);
                        }
                    }
                } finally {
                    iter.close();
                }

                if (0 < toRemove.size()) {
                    Resource[] ctxArray = new Resource[toRemove.size()];
                    toRemove.toArray(ctxArray);
                    super.removeStatements(subj, pred, obj, ctxArray);
                }
            } else {
                if (null != defaultWriteContext) {
                    if (deletePermitted(defaultWriteContext)) {
                        super.removeStatements(subj, pred, obj, defaultWriteContext);
                    }
                }
            }

            // Note: there is no way to remove statements from *only* the null context
        } else {
            for (Resource context : contexts) {
                if (deletePermitted(context)) {
                    super.removeStatements(subj, pred, obj, context);
                }
            }
        }
    }

    @Override
    public void setNamespace(final String prefix, final String name) throws SailException {
        if (namespacesAreWritable) {
            super.setNamespace(prefix, name);
        }
    }

    /**
     * Returns the number of readable statements in the given contexts.
     */
    @Override
    public long size(final Resource... contexts) throws SailException {
        if (0 == contexts.length) {
            return (allowWildcardSize)
                    ? super.size()
                    : 0;
        } else {
            long count = 0;

            for (Resource context : contexts) {
                if (readPermitted(context)) {
                    count += super.size(context);
                }
            }

            return count;
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    public boolean readPermitted(final Resource context) throws SailException {
        return readableSet.getDefaultGraphs().contains(context);
    }

    public boolean writePermitted(final Resource context) throws SailException {
        return writableSet.getDefaultGraphs().contains(context);
    }

    public boolean deletePermitted(final Resource context) throws SailException {
        return writePermitted(context);
    }

    private class ReadableContextIteration implements CloseableIteration<Resource, SailException> {
        private CloseableIteration<? extends Resource, SailException> baseIteration;

        private Resource nextContext = null;
        private boolean finished = false;

        public ReadableContextIteration(final CloseableIteration<? extends Resource, SailException> baseIteration) {
            this.baseIteration = baseIteration;
        }

        public void close() throws SailException {
            baseIteration.close();
        }

        public boolean hasNext() throws SailException {
            if (finished) {
                return false;
            } else if (null != nextContext) {
                return true;
            } else {
                // Break out when a query-permitted context is found or the
                // end of the base iteration is reached.
                while (true) {
                    if (!baseIteration.hasNext()) {
                        finished = true;
                        return false;
                    } else {
                        Resource r = baseIteration.next();
                        if (readPermitted(r)) {
                            nextContext = r;
                            return true;
                        }
                    }
                }
            }
        }

        public Resource next() throws SailException {
            Resource context = nextContext;
            nextContext = null;
            return context;
        }

        /**
         * Has no effect.
         */
        public void remove() throws SailException {
        }
    }

    private class ReadableStatementIteration implements CloseableIteration<Statement, SailException> {
        private CloseableIteration<? extends Statement, SailException> baseIteration;

        private Statement nextStatement = null;
        private boolean finished = false;

        public ReadableStatementIteration(final CloseableIteration<? extends Statement, SailException> baseIteration) {
            this.baseIteration = baseIteration;
        }

        public void close() throws SailException {
            baseIteration.close();
        }

        public boolean hasNext() throws SailException {
            if (finished) {
                return false;
            } else if (null != nextStatement) {
                return true;
            } else {
                // Break out when a query-permitted statement is found or the
                // end of the base iteration is reached.
                while (true) {
                    if (!baseIteration.hasNext()) {
                        finished = true;
                        return false;
                    } else {
                        Statement r = baseIteration.next();
                        if (readPermitted(r.getContext())) {
                            nextStatement = r;
                            return true;
                        }
                    }
                }
            }
        }

        public Statement next() throws SailException {
            Statement st = nextStatement;
            nextStatement = null;

            if (hideNonWritableContexts) {
                Resource c = st.getContext();
                if (null != c && !writePermitted(c)) {
                    st = valueFactory.createStatement(st.getSubject(), st.getPredicate(), st.getObject());
                }
            }

            return st;
        }

        /**
         * Has no effect.
         */
        public void remove() throws SailException {
        }
    }
}
