
package net.fortytwo.sesametools.replay.calls;

import net.fortytwo.sesametools.replay.SailConnectionCall;
import org.openrdf.model.Resource;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import java.util.StringTokenizer;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class SizeCall extends SailConnectionCall<SailConnection, Long> {

    private final Resource[] contexts;

    public SizeCall(final String id,
                    final Resource... contexts) {
        super(id, Type.SIZE);
        this.contexts = contexts;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(id).append(DELIM).append(type)
                .append(DELIM).append(toString(contexts));

        return sb.toString();
    }

    public SizeCall(final String id,
                    final Type type,
                    final StringTokenizer tok) {
        super(id, type);
        this.contexts = parseContexts(tok.nextToken());
    }

    public Long execute(final SailConnection sc) throws SailException {
        return sc.size(contexts);
    }
}
