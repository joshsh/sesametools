
package net.fortytwo.sesametools.replay.calls;

import net.fortytwo.sesametools.replay.SailConnectionCall;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import java.util.StringTokenizer;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class GetNamespaceCall extends SailConnectionCall<SailConnection, String> {
    private final String prefix;

    public GetNamespaceCall(final String id,
                            final String prefix) {
        super(id, Type.GET_NAMESPACE);
        this.prefix = prefix;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(id).append(DELIM).append(type)
                .append(DELIM).append(toString(prefix));

        return sb.toString();
    }

    public GetNamespaceCall(final String id,
                            final Type type,
                            final StringTokenizer tok) {
        super(id, type);
        this.prefix = parseString(tok.nextToken());
    }

    public String execute(final SailConnection sc) throws SailException {
        return sc.getNamespace(prefix);
    }
}
