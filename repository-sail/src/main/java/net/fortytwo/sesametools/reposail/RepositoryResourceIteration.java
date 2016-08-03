
package net.fortytwo.sesametools.reposail;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.sail.SailException;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class RepositoryResourceIteration implements CloseableIteration<Resource, SailException> {
    private CloseableIteration<? extends Resource, RepositoryException> innerIter;

    public RepositoryResourceIteration(CloseableIteration<? extends Resource, RepositoryException> innerIter) {
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

    public Resource next() throws SailException {
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
