package net.fortytwo.sesametools.reposail;

import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * A <code>Sail</code> which wraps a <code>Repository</code>
 *
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class RepositorySail extends SailBase {

    private Repository repository;
    private boolean inferenceDisabled = false;

    public RepositorySail(final Repository repo) {
        this.repository = repo;
    }

    public Repository getRepository() {
        return repository;
    }
    
    public void disableInference() {
        inferenceDisabled = true;
    }

    protected SailConnection getConnectionInternal() throws SailException {
        RepositoryConnection rc;

        try {
            rc = repository.getConnection();
        } catch (RepositoryException e) {
            throw new SailException(e);
        }

        return new RepositorySailConnection(this, rc, inferenceDisabled, this.getValueFactory());
    }

    public ValueFactory getValueFactory() {
        return repository.getValueFactory();
    }

    protected void initializeInternal() throws SailException {
        try {
            repository.initialize();
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }

    public boolean isWritable() throws SailException {
        try {
            return repository.isWritable();
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }

    protected void shutDownInternal() throws SailException {
        try {
            repository.shutDown();
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }
}
