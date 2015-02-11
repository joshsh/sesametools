package net.fortytwo.sesametools;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.util.iterators.FilterIterator;

import java.util.Iterator;

/**
 * A <code>FilterIterator</code> which matches statements against a given
 * subject, predicate, object, and optional context(s).
 */
public class PatternIterator extends FilterIterator<Statement> {

    private Resource subj;

    private URI pred;

    private Value obj;

    private Resource[] contexts;

    public PatternIterator(Iterator<? extends Statement> iter, Resource subj, URI pred, Value obj,
                           Resource... contexts) {
        super(iter);
        this.subj = subj;
        this.pred = pred;
        this.obj = obj;
        this.contexts = contexts;
    }

    @Override
    protected boolean accept(Statement st) {
        if (subj != null && !subj.equals(st.getSubject())) {
            return false;
        }
        if (pred != null && !pred.equals(st.getPredicate())) {
            return false;
        }
        if (obj != null && !obj.equals(st.getObject())) {
            return false;
        }

        if (contexts.length == 0) {
            // Any context matches
            return true;
        } else {
            // Accept if one of the contexts from the pattern matches
            Resource stContext = st.getContext();

            for (Resource context : contexts) {
                if (context == null && stContext == null) {
                    return true;
                }
                if (context != null && context.equals(stContext)) {
                    return true;
                }
            }

            return false;
        }
    }
}