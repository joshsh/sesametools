
package net.fortytwo.sesametools.replay.calls;

import net.fortytwo.sesametools.replay.SailConnectionCall;
import info.aduna.iteration.CloseableIteration;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import java.util.StringTokenizer;

/**
 * Author: josh
 * Date: Apr 30, 2008
 * Time: 5:00:13 PM
 */
public class GetContextIDsCall extends SailConnectionCall<SailConnection, CloseableIteration> {
    public GetContextIDsCall(final String id) {
        super(id, Type.GET_CONTEXT_IDS);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(id).append(DELIM).append(type);

        return sb.toString();
    }

    public GetContextIDsCall(final String id,
                             final Type type,
                             final StringTokenizer tok) {
        super(id, type);
    }

    public CloseableIteration execute(final SailConnection sc) throws SailException {
        return sc.getContextIDs();
    }
}
