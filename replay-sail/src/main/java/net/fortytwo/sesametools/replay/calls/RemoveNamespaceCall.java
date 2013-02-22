
package net.fortytwo.sesametools.replay.calls;

import net.fortytwo.sesametools.replay.SailConnectionCall;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import java.util.StringTokenizer;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class RemoveNamespaceCall extends SailConnectionCall<SailConnection, Object> {
    private final String prefix;

    public RemoveNamespaceCall(final String id,
                               final String prefix) {
        super(id, Type.REMOVE_NAMESPACE);
        this.prefix = prefix;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(id).append(DELIM).append(type)
                .append(DELIM).append(toString(prefix));

        return sb.toString();
    }

    public RemoveNamespaceCall(final String id,
                               final Type type,
                               final StringTokenizer tok) {
        super(id, type);
        this.prefix = parseString(tok.nextToken());
    }

    public Object execute(final SailConnection sc) throws SailException {
        sc.removeNamespace(prefix);
        return null;
    }
}
