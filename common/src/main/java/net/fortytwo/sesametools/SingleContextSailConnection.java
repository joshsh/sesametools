package net.fortytwo.sesametools;

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
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;


/**
 * A SailConnection which treats the wildcard context as a single, specific context,
 * and disallows read and write access to all other contexts, including the default context.
 * Namespaces may be set and retrieved without restriction.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
class SingleContextSailConnection implements SailConnection {
    private SailConnection baseSailConnection;
    private Resource singleContext;

    public SingleContextSailConnection(final Sail baseSail, final Resource context) throws SailException {
        baseSailConnection = baseSail.getConnection();
        singleContext = context;
    }

    public void addStatement(final Resource subj, final URI pred, final Value obj,
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

    public void clear(final Resource... contexts) throws SailException {
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
            final TupleExpr tupleExpr, final Dataset dataSet, final BindingSet bindingSet, final boolean includeInferred)
            throws SailException {
        // ignore the given dataset and restrict everything to the single context we have been setup with
        DatasetImpl singleContextDataset = new DatasetImpl();
        if (singleContext instanceof URI) {
            singleContextDataset.setDefaultInsertGraph((URI) singleContext);
            singleContextDataset.addDefaultGraph((URI) singleContext);
            singleContextDataset.addNamedGraph((URI) singleContext);
            singleContextDataset.addDefaultRemoveGraph((URI) singleContext);
        }

        return baseSailConnection.evaluate(tupleExpr, singleContextDataset, bindingSet, includeInferred);
    }

    /*
    public void executeUpdate(final UpdateExpr updateExpr, final Dataset dataSet, final BindingSet bindingSet,
                              final boolean includeInferred) throws SailException {
        // ignore the given dataset and restrict everything to the single context we have been setup with
        DatasetImpl singleContextDataset = new DatasetImpl();
        if (singleContext instanceof URI) {
            singleContextDataset.setDefaultInsertGraph((URI) singleContext);
            singleContextDataset.addDefaultGraph((URI) singleContext);
            singleContextDataset.addNamedGraph((URI) singleContext);
            singleContextDataset.addDefaultRemoveGraph((URI) singleContext);
        }

        baseSailConnection.executeUpdate(updateExpr, singleContextDataset, bindingSet, includeInferred);
    }*/

    public CloseableIteration<? extends Resource, SailException> getContextIDs()
            throws SailException {
        return new SingleContextIteration();
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
        if (0 == contexts.length) {
            return baseSailConnection.getStatements(subj, pred, obj, includeInferred, singleContext);
        } else {
            for (Resource context : contexts) {
                if (null != context && context.equals(singleContext)) {
                    return baseSailConnection.getStatements(subj, pred, obj, includeInferred, singleContext);
                }
            }

            return new EmptyCloseableIteration<Statement, SailException>();
        }
    }

    public boolean isOpen() throws SailException {
        return baseSailConnection.isOpen();
    }

    public void removeNamespace(final String prefix) throws SailException {
        baseSailConnection.removeNamespace(prefix);
    }

    public void removeStatements(final Resource subj, final URI pred, final Value obj,
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

    public void rollback() throws SailException {
        baseSailConnection.rollback();
    }

    public void setNamespace(final String prefix, final String name) throws SailException {
        baseSailConnection.setNamespace(prefix, name);
    }

    public long size(final Resource... contexts) throws SailException {
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

    ////////////////////////////////////////////////////////////////////////////

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
