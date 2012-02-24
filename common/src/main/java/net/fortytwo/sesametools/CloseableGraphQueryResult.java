package net.fortytwo.sesametools;

import org.openrdf.model.Statement;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import java.util.Map;

/**
 * Auto-closing tuple query result.
 *
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class CloseableGraphQueryResult implements GraphQueryResult {
    private RepositoryConnection connection;
    private GraphQueryResult result;

    public CloseableGraphQueryResult(RepositoryConnection connection, GraphQueryResult result) {
        this.connection = connection;
        this.result = result;
    }
    
    @Override
    public void close() throws QueryEvaluationException {
        try {
            result.close();
        } finally {
            try {
                connection.close();
            } catch (RepositoryException e) {
                throw new QueryEvaluationException("Exception closing connection.", e);
            }
        }
    }

    @Override
    public boolean hasNext() throws QueryEvaluationException {
        final boolean hasNext = result.hasNext();
        if (!hasNext) {
            close();
        }
        return hasNext;
    }

    @Override
    public void remove() throws QueryEvaluationException {
        result.remove();
    }

    @Override
    public Map<String, String> getNamespaces()
    {
        return result.getNamespaces();
    }

    @Override
    public Statement next() throws QueryEvaluationException
    {
        return result.next();
    }
}
