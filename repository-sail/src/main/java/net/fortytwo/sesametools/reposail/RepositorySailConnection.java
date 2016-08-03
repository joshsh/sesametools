package net.fortytwo.sesametools.reposail;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import net.fortytwo.sesametools.SailConnectionTripleSource;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.evaluation.EvaluationStrategy;
import org.eclipse.rdf4j.query.algebra.evaluation.TripleSource;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.SimpleEvaluationStrategy;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.helpers.AbstractSail;
import org.eclipse.rdf4j.sail.helpers.AbstractSailConnection;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class RepositorySailConnection extends AbstractSailConnection {
    private RepositoryConnection repoConnection;
    private final boolean inferenceDisabled;
    private final ValueFactory valueFactory;

    public RepositorySailConnection(final AbstractSail sail,
                                    final RepositoryConnection repoConnection,
                                    final boolean inferenceDisabled,
                                    final ValueFactory valueFactory) {
        super(sail);
        this.repoConnection = repoConnection;
        this.inferenceDisabled = inferenceDisabled;
        this.valueFactory = valueFactory;
    }

    protected void addStatementInternal(Resource subj, IRI pred, Value obj,
                             Resource... contexts) throws SailException {
        try {
            repoConnection.add(subj, pred, obj, contexts);
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }

    protected void clearInternal(Resource... contexts) throws SailException {
        try {
            repoConnection.clear(contexts);
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }

    protected void clearNamespacesInternal() throws SailException {
        try {
            repoConnection.clearNamespaces();
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }

    protected void closeInternal() throws SailException {
        try {
            repoConnection.close();
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }

    protected void commitInternal() throws SailException {
        try {
            repoConnection.commit();
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }

    protected CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluateInternal(
            TupleExpr query, Dataset dataset, BindingSet bindings, boolean includeInferred)
            throws SailException {
        try {
            TripleSource tripleSource = new SailConnectionTripleSource(this, valueFactory, includeInferred);
            EvaluationStrategy strategy = new SimpleEvaluationStrategy(tripleSource, dataset, null);
            return strategy.evaluate(query, bindings);
        } catch (QueryEvaluationException e) {
            throw new SailException(e);
        }
    }

    protected CloseableIteration<? extends Resource, SailException> getContextIDsInternal()
            throws SailException {
        try {
            return new RepositoryResourceIteration(repoConnection.getContextIDs());
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }

    protected String getNamespaceInternal(String prefix) throws SailException {
        try {
            return repoConnection.getNamespace(prefix);
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }

    protected CloseableIteration<? extends Namespace, SailException> getNamespacesInternal()
            throws SailException {
        try {
            return new RepositoryNamespaceIteration(
                    repoConnection.getNamespaces());
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }

    protected CloseableIteration<? extends Statement, SailException> getStatementsInternal(
            Resource subj, IRI pred, Value obj, boolean includeInferred, Resource... contexts)
            throws SailException {
        try {
            return new RepositoryStatementIteration(
                    repoConnection.getStatements(subj, pred, obj, includeInferred && !inferenceDisabled, contexts));
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }

    protected void removeNamespaceInternal(String prefix) throws SailException {
        try {
            repoConnection.removeNamespace(prefix);
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }

    protected void removeStatementsInternal(Resource subj, IRI pred, Value obj,
                                 Resource... contexts) throws SailException {
        try {
            repoConnection.remove(subj, pred, obj, contexts);
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }

    protected void rollbackInternal() throws SailException {
        try {
            repoConnection.rollback();
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }

    protected void setNamespaceInternal(String prefix, String name) throws SailException {
        try {
            repoConnection.setNamespace(prefix, name);
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }

    protected long sizeInternal(Resource... contexts) throws SailException {
        try {
            return repoConnection.size();
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }

    protected void startTransactionInternal() throws SailException {
        try {
            repoConnection.begin();
        } catch (RepositoryException e) {
            throw new SailException(e);
        }
    }
}
