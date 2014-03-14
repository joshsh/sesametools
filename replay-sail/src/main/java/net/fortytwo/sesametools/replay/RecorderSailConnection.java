package net.fortytwo.sesametools.replay;

import info.aduna.iteration.CloseableIteration;
import net.fortytwo.sesametools.replay.calls.AddStatementCall;
import net.fortytwo.sesametools.replay.calls.BeginCall;
import net.fortytwo.sesametools.replay.calls.ClearCall;
import net.fortytwo.sesametools.replay.calls.ClearNamespacesCall;
import net.fortytwo.sesametools.replay.calls.CloseConnectionCall;
import net.fortytwo.sesametools.replay.calls.CommitCall;
import net.fortytwo.sesametools.replay.calls.ConstructorCall;
import net.fortytwo.sesametools.replay.calls.EvaluateCall;
import net.fortytwo.sesametools.replay.calls.GetContextIDsCall;
import net.fortytwo.sesametools.replay.calls.GetNamespaceCall;
import net.fortytwo.sesametools.replay.calls.GetNamespacesCall;
import net.fortytwo.sesametools.replay.calls.GetStatementsCall;
import net.fortytwo.sesametools.replay.calls.RemoveNamespaceCall;
import net.fortytwo.sesametools.replay.calls.RemoveStatementsCall;
import net.fortytwo.sesametools.replay.calls.RollbackCall;
import net.fortytwo.sesametools.replay.calls.SetNamespaceCall;
import net.fortytwo.sesametools.replay.calls.SizeCall;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailBase;
import org.openrdf.sail.helpers.SailConnectionBase;

import java.util.Random;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class RecorderSailConnection extends SailConnectionBase {
    private final String id = "" + new Random().nextInt(0xFFFF);
    private final Handler<SailConnectionCall, SailException> queryHandler;
    private final SailConnection baseSailConnection;
    private final ReplayConfiguration config;
    private int iterationCount = 0;

    public RecorderSailConnection(final SailBase sail,
                                  final Sail baseSail,
                                  final ReplayConfiguration config,
                                  final Handler<SailConnectionCall, SailException> queryHandler) throws SailException {
        super(sail);
        this.queryHandler = queryHandler;
        this.config = config;
        if (config.logTransactions) {
            queryHandler.handle(new ConstructorCall(id));
        }
        this.baseSailConnection = baseSail.getConnection();
    }

    // Note: adding statements does not change the configuration of cached
    // values.
    protected void addStatementInternal(final Resource subj,
                             final URI pred,
                             final Value obj,
                             final Resource... contexts) throws SailException {
        if (config.logWriteOperations) {
            queryHandler.handle(new AddStatementCall(id, subj, pred, obj, contexts));
        }
        baseSailConnection.addStatement(subj, pred, obj, contexts);
    }

    // Note: clearing statements does not change the configuration of cached
    // values.
    protected void clearInternal(final Resource... contexts) throws SailException {
        if (config.logWriteOperations) {
            queryHandler.handle(new ClearCall(id, contexts));
        }
        baseSailConnection.clear(contexts);
    }

    protected void clearNamespacesInternal() throws SailException {
        if (config.logWriteOperations) {
            queryHandler.handle(new ClearNamespacesCall(id));
        }
        baseSailConnection.clearNamespaces();
    }

    protected void closeInternal() throws SailException {
        if (config.logTransactions) {
            queryHandler.handle(new CloseConnectionCall(id));
        }
        baseSailConnection.close();
    }

    protected void commitInternal() throws SailException {
        if (config.logTransactions) {
            queryHandler.handle(new CommitCall(id));
        }
        baseSailConnection.commit();
    }

    protected CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluateInternal(
            final TupleExpr tupleExpr, final Dataset dataSet, final BindingSet bindingSet, final boolean includeInferred)
            throws SailException {
        // Note: there is no recording iterator for evaluate() results
        if (config.logReadOperations) {
            queryHandler.handle(new EvaluateCall(id, includeInferred));
        }
        return baseSailConnection.evaluate(tupleExpr, dataSet, bindingSet, includeInferred);
    }

    protected CloseableIteration<? extends Resource, SailException> getContextIDsInternal()
            throws SailException {
        if (config.logReadOperations) {
            queryHandler.handle(new GetContextIDsCall(id));
            return new RecorderIteration<Resource, SailException>(
                    (CloseableIteration<Resource, SailException>) baseSailConnection.getContextIDs(),
                    nextIterationId(),
                    queryHandler);
        } else {
            return baseSailConnection.getContextIDs();
        }
    }

    private String nextIterationId() {
        iterationCount++;
        return id + "-" + iterationCount;
    }

    protected String getNamespaceInternal(final String prefix) throws SailException {
        if (config.logReadOperations) {
            queryHandler.handle(new GetNamespaceCall(id, prefix));
        }
        return baseSailConnection.getNamespace(prefix);
    }

    protected CloseableIteration<? extends Namespace, SailException> getNamespacesInternal()
            throws SailException {
        if (config.logReadOperations) {
            queryHandler.handle(new GetNamespacesCall(id));
            return new RecorderIteration<Namespace, SailException>(
                    (CloseableIteration<Namespace, SailException>) baseSailConnection.getNamespaces(),
                    nextIterationId(),
                    queryHandler);
        } else {
            return baseSailConnection.getNamespaces();
        }
    }

    protected CloseableIteration<? extends Statement, SailException> getStatementsInternal(
            final Resource subj, final URI pred, final Value obj, final boolean includeInferred, final Resource... contexts)
            throws SailException {
        if (config.logReadOperations) {
            queryHandler.handle(new GetStatementsCall(id, subj, pred, obj, includeInferred, contexts));
            return new RecorderIteration<Statement, SailException>(
                    (CloseableIteration<Statement, SailException>) baseSailConnection.getStatements(subj, pred, obj, includeInferred, contexts),
                    nextIterationId(),
                    queryHandler);
        } else {
            return baseSailConnection.getStatements(subj, pred, obj, includeInferred, contexts);
        }
    }

    protected void removeNamespaceInternal(final String prefix) throws SailException {
        if (config.logWriteOperations) {
            queryHandler.handle(new RemoveNamespaceCall(id, prefix));
        }
        baseSailConnection.removeNamespace(prefix);
    }

    protected void removeStatementsInternal(final Resource subj,
                                 final URI pred,
                                 final Value obj,
                                 final Resource... contexts) throws SailException {
        if (config.logWriteOperations) {
            queryHandler.handle(new RemoveStatementsCall(id, subj, pred, obj, contexts));
        }
        baseSailConnection.removeStatements(subj, pred, obj, contexts);
    }

    protected void rollbackInternal() throws SailException {
        if (config.logTransactions) {
            queryHandler.handle(new RollbackCall(id));
        }
        baseSailConnection.rollback();
    }

    protected void setNamespaceInternal(final String prefix, final String name) throws SailException {
        if (config.logWriteOperations) {
            queryHandler.handle(new SetNamespaceCall(id, prefix, name));
        }
        baseSailConnection.setNamespace(prefix, name);
    }

    protected long sizeInternal(final Resource... contexts) throws SailException {
        if (config.logReadOperations) {
            queryHandler.handle(new SizeCall(id, contexts));
        }
        return baseSailConnection.size(contexts);
    }

    protected void startTransactionInternal() throws SailException {
        if (config.logTransactions) {
            queryHandler.handle(new BeginCall(id));
        }
        baseSailConnection.begin();
    }
}
