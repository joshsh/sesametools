package net.fortytwo.sesametools.replay;

import net.fortytwo.sesametools.replay.calls.AddStatementCall;
import net.fortytwo.sesametools.replay.calls.ClearCall;
import net.fortytwo.sesametools.replay.calls.ClearNamespacesCall;
import net.fortytwo.sesametools.replay.calls.CloseConnectionCall;
import net.fortytwo.sesametools.replay.calls.CommitCall;
import net.fortytwo.sesametools.replay.calls.ConstructorCall;
import net.fortytwo.sesametools.replay.calls.GetContextIDsCall;
import net.fortytwo.sesametools.replay.calls.GetNamespaceCall;
import net.fortytwo.sesametools.replay.calls.GetNamespacesCall;
import net.fortytwo.sesametools.replay.calls.GetStatementsCall;
import net.fortytwo.sesametools.replay.calls.RemoveNamespaceCall;
import net.fortytwo.sesametools.replay.calls.RemoveStatementsCall;
import net.fortytwo.sesametools.replay.calls.RollbackCall;
import net.fortytwo.sesametools.replay.calls.SetNamespaceCall;
import net.fortytwo.sesametools.replay.calls.SizeCall;
import info.aduna.iteration.CloseableIteration;
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

import java.util.Random;

public class RecorderSailConnection implements SailConnection {
    private final String id = "" + new Random().nextInt(0xFFFF);
    private final Handler<SailConnectionCall, SailException> queryHandler;
    private final SailConnection baseSailConnection;
    private final ReplayConfiguration config;
    private int iterationCount = 0;

    public RecorderSailConnection(final Sail baseSail,
                                  final ReplayConfiguration config,
                                  final Handler<SailConnectionCall, SailException> queryHandler) throws SailException {
        this.queryHandler = queryHandler;
        this.config = config;
        if (config.logTransactions) {
            queryHandler.handle(new ConstructorCall(id));
        }
        this.baseSailConnection = baseSail.getConnection();
    }

    // Note: adding statements does not change the configuration of cached
    // values.
    public void addStatement(final Resource subj,
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
    public void clear(final Resource... contexts) throws SailException {
        if (config.logWriteOperations) {
            queryHandler.handle(new ClearCall(id, contexts));
        }
        baseSailConnection.clear(contexts);
    }

    public void clearNamespaces() throws SailException {
        if (config.logWriteOperations) {
            queryHandler.handle(new ClearNamespacesCall(id));
        }
        baseSailConnection.clearNamespaces();
    }

    public void close() throws SailException {
        if (config.logTransactions) {
            queryHandler.handle(new CloseConnectionCall(id));
        }
        baseSailConnection.close();
    }

    public void commit() throws SailException {
        if (config.logTransactions) {
            queryHandler.handle(new CommitCall(id));
        }
        baseSailConnection.commit();
    }

    public CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluate(
            final TupleExpr tupleExpr, final Dataset dataSet, final BindingSet bindingSet, final boolean includeInferred)
            throws SailException {
        // Note: there is no EvaluateCall, nor is there a recording iterator for evaluate() results
        return baseSailConnection.evaluate(tupleExpr, dataSet, bindingSet, includeInferred);
    }

    public CloseableIteration<? extends Resource, SailException> getContextIDs()
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

    public String getNamespace(final String prefix) throws SailException {
        if (config.logReadOperations) {
            queryHandler.handle(new GetNamespaceCall(id, prefix));
        }
        return baseSailConnection.getNamespace(prefix);
    }

    public CloseableIteration<? extends Namespace, SailException> getNamespaces()
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

    public CloseableIteration<? extends Statement, SailException> getStatements(
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

    public boolean isOpen() throws SailException {
        return baseSailConnection.isOpen();
    }

    public void removeNamespace(final String prefix) throws SailException {
        if (config.logWriteOperations) {
            queryHandler.handle(new RemoveNamespaceCall(id, prefix));
        }
        baseSailConnection.removeNamespace(prefix);
    }

    public void removeStatements(final Resource subj,
                                 final URI pred,
                                 final Value obj,
                                 final Resource... contexts) throws SailException {
        if (config.logWriteOperations) {
            queryHandler.handle(new RemoveStatementsCall(id, subj, pred, obj, contexts));
        }
        baseSailConnection.removeStatements(subj, pred, obj, contexts);
    }

    public void rollback() throws SailException {
        if (config.logTransactions) {
            queryHandler.handle(new RollbackCall(id));
        }
        baseSailConnection.rollback();
    }

    public void setNamespace(final String prefix, final String name) throws SailException {
        if (config.logWriteOperations) {
            queryHandler.handle(new SetNamespaceCall(id, prefix, name));
        }
        baseSailConnection.setNamespace(prefix, name);
    }

    public long size(final Resource... contexts) throws SailException {
        if (config.logReadOperations) {
            queryHandler.handle(new SizeCall(id, contexts));
        }
        return baseSailConnection.size(contexts);
    }
}
