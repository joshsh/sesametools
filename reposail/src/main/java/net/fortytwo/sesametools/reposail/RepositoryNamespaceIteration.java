
package net.fortytwo.sesametools.reposail;

import info.aduna.iteration.CloseableIteration;
import org.openrdf.model.Namespace;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.SailException;

public class RepositoryNamespaceIteration implements CloseableIteration<Namespace, SailException> {
    private CloseableIteration<? extends Namespace, RepositoryException> innerIter;

    public RepositoryNamespaceIteration(CloseableIteration<? extends Namespace, RepositoryException> innerIter) {
        this.innerIter = innerIter;
    }

    public void close() throws SailException {
        try {
            innerIter.close();
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }

    public boolean hasNext() throws SailException {
        try {
            return innerIter.hasNext();
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }

    public Namespace next() throws SailException {
        try {
            return innerIter.next();
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }

    public void remove() throws SailException {
        try {
            innerIter.remove();
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }

}
