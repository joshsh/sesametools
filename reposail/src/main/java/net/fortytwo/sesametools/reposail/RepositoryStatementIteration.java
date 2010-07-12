
package net.fortytwo.sesametools.reposail;

import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.SailException;

import info.aduna.iteration.CloseableIteration;

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
