package net.fortytwo.sesametools.deduplication;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.helpers.SailConnectionWrapper;

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
