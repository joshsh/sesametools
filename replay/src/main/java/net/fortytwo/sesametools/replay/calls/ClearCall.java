
package net.fortytwo.sesametools.replay.calls;

import net.fortytwo.sesametools.replay.SailConnectionCall;
import org.openrdf.model.Resource;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import java.util.StringTokenizer;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class ClearCall extends SailConnectionCall<SailConnection, Object> {

    private final Resource[] contexts;

    public ClearCall(final String id,
                     final Resource... contexts) {
        super(id, Type.CLEAR);
        this.contexts = contexts;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(id).append(DELIM).append(type)
                .append(DELIM).append(toString(contexts));

        return sb.toString();
    }

    public ClearCall(final String id,
                     final Type type,
                     final StringTokenizer tok) {
        super(id, type);
        this.contexts = parseContexts(tok.nextToken());
    }

    public Object execute(final SailConnection sc) throws SailException {
        sc.clear(contexts);
        return null;
    }
}
