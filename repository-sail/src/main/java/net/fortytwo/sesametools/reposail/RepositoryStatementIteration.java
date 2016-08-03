
package net.fortytwo.sesametools.reposail;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.sail.SailException;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class RepositoryStatementIteration implements CloseableIteration<Statement, SailException> {
    private CloseableIteration<? extends Statement, RepositoryException> innerIter;

    public RepositoryStatementIteration(CloseableIteration<? extends Statement, RepositoryException> innerIter) {
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

    public Statement next() throws SailException {
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
