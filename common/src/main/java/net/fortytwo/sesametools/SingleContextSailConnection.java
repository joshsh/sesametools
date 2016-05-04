package net.fortytwo.sesametools;

import info.aduna.iteration.CloseableIteration;
import org.openrdf.model.IRI;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.impl.SimpleDataset;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.AbstractSail;
import org.openrdf.sail.helpers.AbstractSailConnection;


/**
 * A SailConnection which treats the wildcard context as a single, specific context,
 * and disallows read and write access to all other contexts, including the default context.
 * Namespaces may be set and retrieved without restriction.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
class SingleContextSailConnection extends AbstractSailConnection {
    private SailConnection baseSailConnection;
    private Resource singleContext;

    public SingleContextSailConnection(final AbstractSail sail,
                                       final Sail baseSail,
                                       final Resource context) throws SailException {
        super(sail);
        baseSailConnection = baseSail.getConnection();
        singleContext = context;
    }

    protected void addStatementInternal(final Resource subj, final IRI pred, final Value obj,
                                        final Resource... contexts) throws SailException {
        if (0 == contexts.length) {
            baseSailConnection.addStatement(subj, pred, obj, singleContext);
        } else {
            for (Resource context : contexts) {
                if (null != context && context.equals(singleContext)) {
                    baseSailConnection.addStatement(subj, pred, obj, singleContext);
                    break;
                }
            }
        }
    }

    protected void clearInternal(final Resource... contexts) throws SailException {
        if (0 == contexts.length) {
            baseSailConnection.clear(singleContext);
        } else {
            for (Resource context : contexts) {
                if (null != context && context.equals(singleContext)) {
                    baseSailConnection.clear(singleContext);
                    break;
                }
            }
        }
    }

    protected void clearNamespacesInternal() throws SailException {
        baseSailConnection.clearNamespaces();
    }

    protected void startTransactionInternal() throws SailException {
        baseSailConnection.begin();
    }

    protected void closeInternal() throws SailException {
        baseSailConnection.close();
    }

    protected void commitInternal() throws SailException {
        baseSailConnection.commit();
    }

    protected CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluateInternal(
            final TupleExpr tupleExpr,
            final Dataset dataSet,
            final BindingSet bindingSet,
            final boolean includeInferred) throws SailException {

        // ignore the given dataset and restrict everything to the single context we have been setup with
        SimpleDataset singleContextDataset = new SimpleDataset();
        if (singleContext instanceof IRI) {
            singleContextDataset.setDefaultInsertGraph((IRI) singleContext);
            singleContextDataset.addDefaultGraph((IRI) singleContext);
            singleContextDataset.addNamedGraph((IRI) singleContext);
            singleContextDataset.addDefaultRemoveGraph((IRI) singleContext);
        }

        return baseSailConnection.evaluate(tupleExpr, singleContextDataset, bindingSet, includeInferred);
    }

    /*
    public void executeUpdate(final UpdateExpr updateExpr, final Dataset dataSet, final BindingSet bindingSet,
                              final boolean includeInferred) throws SailException {
        // ignore the given dataset and restrict everything to the single context we have been setup with
        DatasetImpl singleContextDataset = new DatasetImpl();
        if (singleContext instanceof URI) {
            singleContextDataset.setDefaultInsertGraph((IRI) singleContext);
            singleContextDataset.addDefaultGraph((IRI) singleContext);
            singleContextDataset.addNamedGraph((IRI) singleContext);
            singleContextDataset.addDefaultRemoveGraph((IRI) singleContext);
        }

        baseSailConnection.executeUpdate(updateExpr, singleContextDataset, bindingSet, includeInferred);
    }*/

    protected CloseableIteration<? extends Resource, SailException> getContextIDsInternal()
            throws SailException {
        return new SingleContextIteration();
    }

    protected String getNamespaceInternal(final String prefix) throws SailException {
        return baseSailConnection.getNamespace(prefix);
    }

    protected CloseableIteration<? extends Namespace, SailException> getNamespacesInternal()
            throws SailException {
        return baseSailConnection.getNamespaces();
    }

    protected CloseableIteration<? extends Statement, SailException> getStatementsInternal(
            final Resource subj,
            final IRI pred,
            final Value obj,
            final boolean includeInferred,
            final Resource... contexts) throws SailException {

        if (0 == contexts.length) {
            return baseSailConnection.getStatements(subj, pred, obj, includeInferred, singleContext);
        } else {
            for (Resource context : contexts) {
                if (null != context && context.equals(singleContext)) {
                    return baseSailConnection.getStatements(subj, pred, obj, includeInferred, singleContext);
                }
            }

            return new EmptyCloseableIteration<>();
        }
    }

    protected void removeNamespaceInternal(final String prefix) throws SailException {
        baseSailConnection.removeNamespace(prefix);
    }

    protected void removeStatementsInternal(final Resource subj, final IRI pred, final Value obj,
                                            final Resource... contexts) throws SailException {
        if (0 == contexts.length) {
            baseSailConnection.removeStatements(subj, pred, obj, singleContext);
        } else {
            for (Resource context : contexts) {
                if (null != context && context.equals(singleContext)) {
                    baseSailConnection.removeStatements(subj, pred, obj, singleContext);
                    break;
                }
            }
        }
    }

    protected void rollbackInternal() throws SailException {
        baseSailConnection.rollback();
    }

    protected void setNamespaceInternal(final String prefix, final String name) throws SailException {
        baseSailConnection.setNamespace(prefix, name);
    }

    protected long sizeInternal(final Resource... contexts) throws SailException {
        if (0 == contexts.length) {
            return baseSailConnection.size(singleContext);
        } else {
            for (Resource context : contexts) {
                if (null != context && context.equals(singleContext)) {
                    return baseSailConnection.size(singleContext);
                }
            }

            return 0;
        }
    }

    private class SingleContextIteration implements CloseableIteration<Resource, SailException> {
        private Resource nextContext;

        public SingleContextIteration() {
            nextContext = singleContext;
        }

        public void close() throws SailException {
        }

        public boolean hasNext() throws SailException {
            return (null != nextContext);
        }

        public Resource next() throws SailException {
            Resource r = nextContext;
            nextContext = null;
            return r;
        }

        public void remove() throws SailException {
        }
    }
}
