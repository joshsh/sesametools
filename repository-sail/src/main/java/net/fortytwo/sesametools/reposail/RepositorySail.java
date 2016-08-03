package net.fortytwo.sesametools.reposail;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.helpers.AbstractSail;

/**
 * A <code>Sail</code> which wraps a <code>Repository</code>
 *
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class RepositorySail extends AbstractSail {

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
