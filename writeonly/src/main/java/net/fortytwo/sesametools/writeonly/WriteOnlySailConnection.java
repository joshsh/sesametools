
package net.fortytwo.sesametools.writeonly;

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
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailConnectionListener;
import org.openrdf.sail.SailException;

import net.fortytwo.sesametools.EmptyCloseableIteration;

/**
 * Author: josh
 * Date: Feb 18, 2008
 * Time: 12:56:10 PM
 */
public class WriteOnlySailConnection implements SailConnection {
    private RDFHandler handler;
    private ValueFactory valueFactory;

    public WriteOnlySailConnection(final RDFHandler handler, final ValueFactory valueFactory) {
        this.handler = handler;
        this.valueFactory = valueFactory;
    }

    public void addConnectionListener(SailConnectionListener arg0) {
        // TODO Auto-generated method stub
    }

    public void addStatement(final Resource subj, final URI pred, final Value obj, final Resource... contexts) throws SailException {
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

    public void clear(Resource... arg0) throws SailException {
        // Does nothing.
    }

    public void clearNamespaces() throws SailException {
        // Does nothing.
    }

    public void close() throws SailException {
    }

    public void commit() throws SailException {
    }

    public CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluate(
            TupleExpr arg0, Dataset arg1, BindingSet arg2, boolean arg3)
            throws SailException {
        return new EmptyCloseableIteration<BindingSet, QueryEvaluationException>();
    }

    public CloseableIteration<? extends Resource, SailException> getContextIDs()
            throws SailException {
        return new EmptyCloseableIteration<Resource, SailException>();
    }

    public String getNamespace(final String prefix) throws SailException {
        return null;
    }

    public CloseableIteration<? extends Namespace, SailException> getNamespaces()
            throws SailException {
        return new EmptyCloseableIteration<Namespace, SailException>();
    }

    public CloseableIteration<? extends Statement, SailException> getStatements(
            Resource arg0, URI arg1, Value arg2, boolean arg3, Resource... arg4)
            throws SailException {
        return new EmptyCloseableIteration<Statement, SailException>();
    }

    public boolean isOpen() throws SailException {
        return true;
    }

    public void removeConnectionListener(SailConnectionListener arg0) {
        // TODO Auto-generated method stub
    }

    public void removeNamespace(String arg0) throws SailException {
        // Does nothing.
    }

    public void removeStatements(Resource arg0, URI arg1, Value arg2,
                                 Resource... arg3) throws SailException {
        // Does nothing.
    }

    public void rollback() throws SailException {
    }

    public void setNamespace(String arg0, String arg1) throws SailException {
        // Does nothing.
    }

    public long size(final Resource... contexts) throws SailException {
        return 0;
    }
}
