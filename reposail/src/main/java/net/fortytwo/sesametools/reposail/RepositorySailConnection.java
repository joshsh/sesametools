
package net.fortytwo.sesametools.reposail;

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
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailConnectionListener;
import org.openrdf.sail.SailException;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class RepositorySailConnection implements SailConnection {
    private RepositoryConnection repoConnection;
    private final boolean inferenceDisabled;

    public RepositorySailConnection(final RepositoryConnection repoConnection,
                                    final boolean inferenceDisabled) {
        this.repoConnection = repoConnection;
        this.inferenceDisabled = inferenceDisabled;
    }

    public void addConnectionListener(SailConnectionListener arg0) {
        // TODO Auto-generated method stub
    }

    public void addStatement(Resource subj, URI pred, Value obj,
                             Resource... contexts) throws SailException {
        try {
            repoConnection.add(subj, pred, obj, contexts);
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }

    public void clear(Resource... contexts) throws SailException {
        // TODO Auto-generated method stub
        try {
            repoConnection.clear(contexts);
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }

    public void clearNamespaces() throws SailException {
        try {
            repoConnection.clearNamespaces();
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }

    public void close() throws SailException {
        try {
            repoConnection.close();
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }

    public void commit() throws SailException {
        try {
            repoConnection.commit();
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }

    public CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluate(
            TupleExpr arg0, Dataset arg1, BindingSet arg2, boolean arg3)
            throws SailException {
        throw new UnsupportedOperationException();
    }

    public void executeUpdate(final UpdateExpr updateExpr,
                              final Dataset dataset,
                              final BindingSet bindingSet,
                              final boolean b) throws SailException {
    	throw new UnsupportedOperationException("Sail to Repository updates not implemented yet");
	}


    public CloseableIteration<? extends Resource, SailException> getContextIDs()
            throws SailException {
        try {
            return new RepositoryResourceIteration(repoConnection.getContextIDs());
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }

    public String getNamespace(String prefix) throws SailException {
        try {
            return repoConnection.getNamespace(prefix);
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }

    public CloseableIteration<? extends Namespace, SailException> getNamespaces()
            throws SailException {
        try {
            return new RepositoryNamespaceIteration(
                    repoConnection.getNamespaces());
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }

    public CloseableIteration<? extends Statement, SailException> getStatements(
            Resource subj, URI pred, Value obj, boolean includeInferred, Resource... contexts)
            throws SailException {
        try {
            return new RepositoryStatementIteration(
                    repoConnection.getStatements(subj, pred, obj, includeInferred && !inferenceDisabled, contexts));
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }

    public boolean isOpen() throws SailException {
        try {
            return repoConnection.isOpen();
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }

    public void removeConnectionListener(SailConnectionListener arg0) {
        // TODO Auto-generated method stub
    }

    public void removeNamespace(String prefix) throws SailException {
        try {
            repoConnection.removeNamespace(prefix);
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }

    public void removeStatements(Resource subj, URI pred, Value obj,
                                 Resource... contexts) throws SailException {
        try {
            repoConnection.remove(subj, pred, obj, contexts);
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }

    public void rollback() throws SailException {
        try {
            repoConnection.rollback();
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }

    public void setNamespace(String prefix, String name) throws SailException {
        try {
            repoConnection.setNamespace(prefix, name);
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }

    public long size(Resource... contexts) throws SailException {
        try {
            return repoConnection.size();
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }

}
