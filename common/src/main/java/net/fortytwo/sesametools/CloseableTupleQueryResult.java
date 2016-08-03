package net.fortytwo.sesametools;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;

import java.util.List;

/**
 * Auto-closing tuple query result.
 *
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class CloseableTupleQueryResult implements TupleQueryResult {
    private RepositoryConnection connection;
    private TupleQueryResult result;

    public CloseableTupleQueryResult(RepositoryConnection connection, TupleQueryResult result) {
        this.connection = connection;
        this.result = result;
    }

    @Override
    public List<String> getBindingNames() throws QueryEvaluationException {
        return result.getBindingNames();
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
    public BindingSet next() throws QueryEvaluationException {
        return result.next();
    }

    @Override
    public void remove() throws QueryEvaluationException {
        result.remove();
    }
}
