
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
public class SetNamespaceCall extends SailConnectionCall<SailConnection, Object> {
    private final String prefix;
    private final String uri;

    public SetNamespaceCall(final String id,
                            final String prefix,
                            final String uri) {
        super(id, Type.SET_NAMESPACE);
        this.prefix = prefix;
        this.uri = uri;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(id).append(DELIM).append(type)
                .append(DELIM).append(toString(prefix))
                .append(DELIM).append(toString(uri));

        return sb.toString();
    }

    public SetNamespaceCall(final String id,
                            final Type type,
                            final StringTokenizer tok) {
        super(id, type);
        this.prefix = parseString(tok.nextToken());
        this.uri = parseString(tok.nextToken());
    }

    public Object execute(final SailConnection sc) throws SailException {
        sc.setNamespace(prefix, uri);
        return null;
    }
}
