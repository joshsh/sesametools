package net.fortytwo.sesametools.deduplication;

import info.aduna.iteration.CloseableIteration;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.UpdateExpr;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailConnectionWrapper;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class DeduplicationSailConnection extends SailConnectionWrapper {
    public DeduplicationSailConnection(final SailConnection baseSailConnection) {
        super(baseSailConnection);
    }

    @Override
    public void addStatement(final Resource subject,
                             final URI predicate,
                             final Value object,
                             final Resource... contexts) throws SailException {
        if (0 == contexts.length) {
            boolean includeInferred = false;
            CloseableIteration<? extends Statement, SailException> iter
                    = this.getWrappedConnection().getStatements(subject, predicate, object, includeInferred);
            try {
                if (iter.hasNext()) {
                    return;
                }
            } finally {
                iter.close();
            }
        }

        super.addStatement(subject, predicate, object, contexts);
    }

    @Override
    public void executeUpdate(final UpdateExpr updateExpr,
                              final Dataset dataset,
                              final BindingSet bindingSet,
                              final boolean b) throws SailException {
        throw new UnsupportedOperationException("SPARQL Update is not yet supported");
    }
}
