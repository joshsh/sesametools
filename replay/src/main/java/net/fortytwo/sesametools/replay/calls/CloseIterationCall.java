
package net.fortytwo.sesametools.replay.calls;

import net.fortytwo.sesametools.replay.SailConnectionCall;
import info.aduna.iteration.CloseableIteration;
import org.openrdf.sail.SailException;

import java.util.StringTokenizer;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class CloseIterationCall extends SailConnectionCall<CloseableIteration<?, SailException>, Object> {
    public CloseIterationCall(final String id) {
        super(id, Type.CLOSE_ITERATION);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(id).append(DELIM).append(type);

        return sb.toString();
    }

    public CloseIterationCall(final String id,
                     final Type type,
                     final StringTokenizer tok) {
        super(id, type);
    }

    public Object execute(final CloseableIteration<?, SailException> t) throws SailException {
        t.close();
        return null;
    }
}
