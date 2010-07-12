
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
