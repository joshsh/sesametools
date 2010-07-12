
package net.fortytwo.sesametools.replay.calls;

import net.fortytwo.sesametools.replay.SailConnectionCall;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import java.util.StringTokenizer;

/**
 * Author: josh
 * Date: Apr 30, 2008
 * Time: 5:00:13 PM
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
