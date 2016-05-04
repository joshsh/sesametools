package net.fortytwo.sesametools.deduplication;

import info.aduna.iteration.CloseableIteration;
import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
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
                             final IRI predicate,
                             final Value object,
                             final Resource... contexts) throws SailException {
        if (0 == contexts.length) {
            boolean includeInferred = false;
            try (CloseableIteration<? extends Statement, SailException> iter
                         = this.getWrappedConnection().getStatements(subject, predicate, object, includeInferred)) {
                if (iter.hasNext()) {
                    return;
                }
            }
        }

        super.addStatement(subject, predicate, object, contexts);
    }
}
