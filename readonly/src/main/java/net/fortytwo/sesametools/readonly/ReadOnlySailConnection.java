package net.fortytwo.sesametools.readonly;

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
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailConnectionListener;
import org.openrdf.sail.SailException;

import java.util.HashSet;
import java.util.Set;

/**
 * Author: josh
 * Date: Jul 23, 2008
 * Time: 12:23:48 PM
 */
public class ReadOnlySailConnection implements SailConnection {
    private final SailConnection baseSailConnection;
    private final Set<SailConnectionListener> listeners;
    private final ValueFactory valueFactory;

    public ReadOnlySailConnection(final Sail baseSail) throws SailException {
        baseSailConnection = baseSail.getConnection();
        valueFactory = baseSail.getValueFactory();
        listeners = new HashSet<SailConnectionListener>();
    }

    public boolean isOpen() throws SailException {
        return baseSailConnection.isOpen();
    }

    public void close() throws SailException {
        baseSailConnection.close();
    }

    public CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluate(final TupleExpr tupleExpr,
                                                                                       final Dataset dataset,
                                                                                       final BindingSet bindings,
                                                                                       final boolean includeInferred) throws SailException {
        return baseSailConnection.evaluate(tupleExpr, dataset, bindings, includeInferred);
    }

    public CloseableIteration<? extends Resource, SailException> getContextIDs() throws SailException {
        return baseSailConnection.getContextIDs();
    }

    public CloseableIteration<? extends Statement, SailException> getStatements(final Resource subject,
                                                                                final URI predicate,
                                                                                final Value object,
                                                                                final boolean includeInferred,
                                                                                final Resource... contexts) throws SailException {
        return baseSailConnection.getStatements(subject, predicate, object, includeInferred, contexts);
    }

    public long size(final Resource... contexts) throws SailException {
        return baseSailConnection.size(contexts);
    }

    public void commit() throws SailException {
        // Do nothing.
    }

    public void rollback() throws SailException {
        // Do nothing.
    }

    public void addStatement(final Resource subject,
                             final URI predicate,
                             final Value object,
                             final Resource... contexts) throws SailException {
        // Do nothing.

        // Note: this is only good for specific statements (no wildcards, no missing context).
        if (0 < listeners.size()) {
            for (Resource context : contexts) {
                Statement st = valueFactory.createStatement(subject, predicate, object, context);
                for (SailConnectionListener listener : listeners) {
                    listener.statementAdded(st);
                }
            }
        }
    }

    public void removeStatements(final Resource subject,
                                 final URI predicate,
                                 final Value object,
                                 final Resource... contexts) throws SailException {
        // Do nothing.

        // Note: this is only good for specific statements (no wildcards, no missing context).
        if (0 < listeners.size()) {
            for (Resource context : contexts) {
                Statement st = valueFactory.createStatement(subject, predicate, object, context);
                for (SailConnectionListener listener : listeners) {
                    listener.statementRemoved(st);
                }
            }
        }
    }

    public void clear(final Resource... contexts) throws SailException {
        // Do nothing.
    }

    public CloseableIteration<? extends Namespace, SailException> getNamespaces() throws SailException {
        return baseSailConnection.getNamespaces();
    }

    public String getNamespace(final String prefix) throws SailException {
        return baseSailConnection.getNamespace(prefix);
    }

    public void setNamespace(final String prefix,
                             final String uri) throws SailException {
        // Do nothing.
    }

    public void removeNamespace(final String prefix) throws SailException {
        // Do nothing.
    }

    public void clearNamespaces() throws SailException {
        // Do nothing.
    }

    public void addConnectionListener(final SailConnectionListener listener) {
        listeners.add(listener);
    }

    public void removeConnectionListener(final SailConnectionListener listener) {
        listeners.remove(listener);
    }
}
