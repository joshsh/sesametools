
package net.fortytwo.sesametools.reposail;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.sail.SailException;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
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
