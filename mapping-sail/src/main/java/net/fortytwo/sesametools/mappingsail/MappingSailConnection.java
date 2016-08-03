package net.fortytwo.sesametools.mappingsail;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.helpers.SailConnectionWrapper;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
class MappingSailConnection extends SailConnectionWrapper {
    private final MappingSchema rewriters;
    private final ValueFactory valueFactory;

    public MappingSailConnection(final SailConnection baseConnection,
                                 final MappingSchema rewriters,
                                 final ValueFactory valueFactory) {
        super(baseConnection);
        this.rewriters = rewriters;
        this.valueFactory = valueFactory;
    }

    @Override
    public CloseableIteration<? extends Statement, SailException> getStatements(
            Resource subj, IRI pred, Value obj, final boolean includeInferred, Resource... contexts)
            throws SailException {

        if (subj instanceof IRI) {
            subj = rewriters.getRewriter(
                    MappingSchema.PartOfSpeech.SUBJECT, MappingSchema.Direction.INBOUND).rewrite((IRI) subj);
        }
        pred = rewriters.getRewriter(
                MappingSchema.PartOfSpeech.PREDICATE, MappingSchema.Direction.INBOUND).rewrite(pred);
        if (obj instanceof IRI) {
            obj = rewriters.getRewriter(
                    MappingSchema.PartOfSpeech.OBJECT, MappingSchema.Direction.INBOUND).rewrite((IRI) obj);
        }
        for (int i = 0; i < contexts.length; i++) {
            if (contexts[i] instanceof IRI) {
                contexts[i] = rewriters.getRewriter(
                        MappingSchema.PartOfSpeech.CONTEXT, MappingSchema.Direction.INBOUND).rewrite((IRI) contexts[i]);
            }
        }

        return new RewritingStatementIteration(
                this.getWrappedConnection().getStatements(subj, pred, obj, includeInferred, contexts));
    }

    private class RewritingStatementIteration implements CloseableIteration<Statement, SailException> {
        private final CloseableIteration<? extends Statement, SailException> baseIteration;

        public RewritingStatementIteration(final CloseableIteration<? extends Statement, SailException> baseIteration) {
            this.baseIteration = baseIteration;
        }

        public void close() throws SailException {
            baseIteration.close();
        }

        public boolean hasNext() throws SailException {
            return baseIteration.hasNext();
        }

        public Statement next() throws SailException {
            Statement st = baseIteration.next();

            Resource subject = st.getSubject();
            IRI predicate = st.getPredicate();
            Value object = st.getObject();
            Resource context = st.getContext();

            if (subject instanceof IRI) {
                subject = rewriters.getRewriter(
                        MappingSchema.PartOfSpeech.SUBJECT, MappingSchema.Direction.OUTBOUND)
                        .rewrite((IRI) subject);
            }
            predicate = rewriters.getRewriter(
                    MappingSchema.PartOfSpeech.PREDICATE, MappingSchema.Direction.OUTBOUND)
                    .rewrite(predicate);
            if (object instanceof IRI) {
                object = rewriters.getRewriter(
                        MappingSchema.PartOfSpeech.OBJECT, MappingSchema.Direction.OUTBOUND)
                        .rewrite((IRI) object);
            }
            if (null != context && context instanceof IRI) {
                context = rewriters.getRewriter(
                        MappingSchema.PartOfSpeech.CONTEXT, MappingSchema.Direction.OUTBOUND)
                        .rewrite((IRI) context);
            }

            return valueFactory.createStatement(subject, predicate, object, context);
        }

        public void remove() throws SailException {
            baseIteration.remove();
        }
    }
}
