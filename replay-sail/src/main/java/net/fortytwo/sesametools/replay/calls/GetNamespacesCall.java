
package net.fortytwo.sesametools.replay.calls;

import net.fortytwo.sesametools.replay.SailConnectionCall;
import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.SailException;

import java.util.StringTokenizer;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class GetNamespacesCall extends SailConnectionCall<SailConnection, CloseableIteration> {

    public GetNamespacesCall(final String id) {
        super(id, Type.GET_NAMESPACES);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(id).append(DELIM).append(type);

        return sb.toString();
    }

    public GetNamespacesCall(final String id,
                             final Type type,
                             final StringTokenizer tok) {
        super(id, type);
    }

    public CloseableIteration execute(final SailConnection sc) throws SailException {
        return sc.getNamespaces();
    }
}
