
package net.fortytwo.sesametools.replay.calls;

import net.fortytwo.sesametools.replay.SailConnectionCall;
import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import java.util.StringTokenizer;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class AddStatementCall extends SailConnectionCall<SailConnection, Object> {

    private final Resource subject;
    private final IRI predicate;
    private final Value object;
    private final Resource[] contexts;

    public AddStatementCall(final String id,
                            final Resource subj,
                            final IRI pred,
                            final Value obj,
                            final Resource... contexts) {
        super(id, Type.ADD_STATEMENT);
        this.subject = subj;
        this.predicate = pred;
        this.object = obj;
        this.contexts = contexts;
//System.out.println("call: " + this);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(id).append(DELIM).append(type)
                .append(DELIM).append(toString(subject))
                .append(DELIM).append(toString(predicate))
                .append(DELIM).append(toString(object))
                .append(DELIM).append(toString(contexts));

        return sb.toString();
    }

    public AddStatementCall(final String id,
                            final Type type,
                            final StringTokenizer tok) {
        super(id, type);
        this.subject = parseResource(tok.nextToken());
        this.predicate = parseIRI(tok.nextToken());
        this.object = parseValue(tok.nextToken());
        this.contexts = parseContexts(tok.nextToken());
    }

    public Object execute(final SailConnection sc) throws SailException {
        sc.addStatement(subject, predicate, object, contexts);
        return null;
    }
}
