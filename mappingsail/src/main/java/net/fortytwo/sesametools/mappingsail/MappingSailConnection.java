package net.fortytwo.sesametools.mappingsail;

import info.aduna.iteration.CloseableIteration;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailConnectionWrapper;

/**
 * Author: josh
 * Date: Aug 11, 2009
 * Time: 3:32:06 PM
 */
public class MappingSailConnection extends SailConnectionWrapper {
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
    public CloseableIteration<? extends Statement, SailException> getStatements(Resource subj,
                                                                                URI pred,
                                                                                Value obj,
                                                                                final boolean includeInferred,
                                                                                Resource... contexts) throws SailException {
        if (subj instanceof URI) {
            subj = rewriters.getRewriter(MappingSchema.PartOfSpeech.SUBJECT, MappingSchema.Action.TO_STORE).rewrite((URI) subj);
        }
        pred = rewriters.getRewriter(MappingSchema.PartOfSpeech.PREDICATE, MappingSchema.Action.TO_STORE).rewrite(pred);
        if (obj instanceof URI) {
            obj = rewriters.getRewriter(MappingSchema.PartOfSpeech.OBJECT, MappingSchema.Action.TO_STORE).rewrite((URI) obj);
        }
        for (int i = 0; i < contexts.length; i++) {
            if (contexts[i] instanceof URI) {
                contexts[i] = rewriters.getRewriter(MappingSchema.PartOfSpeech.GRAPH, MappingSchema.Action.TO_STORE).rewrite((URI) contexts[i]);
            }
        }

        return new RewritingStatementIteration(this.getWrappedConnection().getStatements(subj, pred, obj, includeInferred, contexts));
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
            URI predicate = st.getPredicate();
            Value object = st.getObject();
            Resource context = st.getContext();

            if (subject instanceof URI) {
                subject = rewriters.getRewriter(
                        MappingSchema.PartOfSpeech.SUBJECT, MappingSchema.Action.FROM_STORE)
                        .rewrite((URI) subject);
            }
            predicate = rewriters.getRewriter(
                    MappingSchema.PartOfSpeech.PREDICATE, MappingSchema.Action.FROM_STORE)
                    .rewrite(predicate);
            if (object instanceof URI) {
                object = rewriters.getRewriter(
                        MappingSchema.PartOfSpeech.OBJECT, MappingSchema.Action.FROM_STORE)
                        .rewrite((URI) object);
            }
            if (null != context && context instanceof URI) {
                context = rewriters.getRewriter(
                        MappingSchema.PartOfSpeech.GRAPH, MappingSchema.Action.FROM_STORE)
                        .rewrite((URI) context);
            }

            return valueFactory.createStatement(subject, predicate, object, context);
        }

        public void remove() throws SailException {
            baseIteration.remove();
        }
    }
}
