package net.fortytwo.sesametools.reposail;

import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailChangedListener;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import java.io.File;
import java.util.logging.Logger;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class RepositorySail implements Sail {
    private static final Logger LOGGER = Logger.getLogger(RepositorySail.class.getName());

    private Repository repository;
    private final boolean autoCommit;
    private boolean inferenceDisabled = false;

    public RepositorySail(final Repository repo) {
        this(repo, false);
    }

    public RepositorySail(final Repository repo,
                          final boolean autoCommit) {
        this.repository = repo;
        this.autoCommit = autoCommit;
    }

    public void disableInference() {
        inferenceDisabled = true;
    }

    public void addSailChangedListener(SailChangedListener listener) {
        // TODO Auto-generated method stub
    }

    public SailConnection getConnection() throws SailException {
        RepositoryConnection rc;

        try {
            rc = repository.getConnection();

            try {
                rc.setAutoCommit(autoCommit);
            } catch (UnsupportedOperationException e) {
                LOGGER.warning("could not set autoCommit flag");
            }
        } catch (RepositoryException e) {
            throw new SailException(e);
        }

        return new RepositorySailConnection(rc, inferenceDisabled);
    }

    public File getDataDir() {
        return repository.getDataDir();
    }

    public ValueFactory getValueFactory() {
        return repository.getValueFactory();
    }

    public void initialize() throws SailException {
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

    public void removeSailChangedListener(SailChangedListener listener) {
        // TODO Auto-generated method stub
    }

    public void setDataDir(File file) {
        repository.setDataDir(file);
    }

    public void shutDown() throws SailException {
        try {
            repository.shutDown();
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }
}
