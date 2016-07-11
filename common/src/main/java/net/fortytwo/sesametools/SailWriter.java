package net.fortytwo.sesametools;

import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.model.Statement;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An <code>RDFHandler</code> which either adds or removes received <code>Statements</code> from a <code>Sail</code>
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class SailWriter implements RDFHandler {
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    public enum Action {
        ADD, REMOVE
    }

    private final Sail sail;
    private final Action action;
    private SailConnection sailConnection;

    public SailWriter(final Sail sail, final Action action) {
        this.sail = sail;
        this.action = action;
    }

    protected void finalize() throws Throwable {
        super.finalize();

        if (null != sailConnection) {
            sailConnection.close();
        }
        
        sailConnection = null;
    }

    public void startRDF() throws RDFHandlerException {
        try {
            sailConnection = sail.getConnection();
        } catch (SailException e) {
            throw new RDFHandlerException(e);
        }
    }

    public void endRDF() throws RDFHandlerException {
        try {
            sailConnection.commit();
        } catch (SailException e) {
            throw new RDFHandlerException(e);
        } finally {
            if(null != sailConnection) {
                try {
                    sailConnection.close();
                } catch(SailException e) {
                    log.error("Found SailException while trying to close Sail connection in SailWriter", e);
                }
            }
            sailConnection = null;
        }
    }

    public void handleNamespace(String s, String s1) throws RDFHandlerException {
        try {
            switch (action) {
                case ADD:
                    sailConnection.setNamespace(s, s1);
                    break;
                case REMOVE:
                    String name = sailConnection.getNamespace(s);
                    if (null != name && name.equals(s1)) {
                        sailConnection.removeNamespace(s);
                    }
                    break;
            }
        } catch (SailException e) {
            throw new RDFHandlerException(e);
        }
    }

    public void handleStatement(Statement statement) throws RDFHandlerException {
        try {
            switch (action) {
                case ADD:
                    sailConnection.addStatement(
                            statement.getSubject(),
                            statement.getPredicate(),
                            statement.getObject(),
                            statement.getContext());
                    break;
                case REMOVE:
                    sailConnection.removeStatements(
                            statement.getSubject(),
                            statement.getPredicate(),
                            statement.getObject(),
                            statement.getContext());
                    break;
            }
        } catch (SailException e) {
            throw new RDFHandlerException(e);
        }
    }

    public void handleComment(String s) throws RDFHandlerException {
        // Do nothing.
    }
}
