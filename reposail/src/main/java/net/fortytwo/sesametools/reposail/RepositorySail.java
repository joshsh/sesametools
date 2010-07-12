
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

/**
 * Author: josh
 * Date: Feb 26, 2008
 * Time: 1:34:19 PM
 */
public class RepositorySail implements Sail {
    private Repository repository;

    public RepositorySail(final Repository repo) {
        this.repository = repo;
    }

    public void addSailChangedListener(SailChangedListener listener) {
        // TODO Auto-generated method stub
    }

    public SailConnection getConnection() throws SailException {
        RepositoryConnection rc;

        try {
            rc = repository.getConnection();
        } catch (RepositoryException e) {
            throw new SailException(e);
        }

        return new RepositorySailConnection(rc);
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
