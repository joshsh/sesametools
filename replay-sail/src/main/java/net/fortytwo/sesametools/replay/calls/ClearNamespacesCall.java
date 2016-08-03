
package net.fortytwo.sesametools.replay.calls;

import net.fortytwo.sesametools.replay.SailConnectionCall;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.SailException;

import java.util.StringTokenizer;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class ClearNamespacesCall extends SailConnectionCall<SailConnection, Object> {

    public ClearNamespacesCall(final String id) {
        super(id, Type.CLEAR_NAMESPACES);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(id).append(DELIM).append(type);

        return sb.toString();
    }

    public ClearNamespacesCall(final String id,
                               final Type type,
                               final StringTokenizer tok) {
        super(id, type);
    }

    public Object execute(final SailConnection sc) throws SailException {
        sc.clearNamespaces();
        return null;
    }
}
