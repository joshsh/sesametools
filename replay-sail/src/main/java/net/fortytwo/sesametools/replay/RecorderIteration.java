package net.fortytwo.sesametools.replay;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import net.fortytwo.sesametools.replay.calls.HasNextCall;
import net.fortytwo.sesametools.replay.calls.NextCall;
import net.fortytwo.sesametools.replay.calls.RemoveCall;
import net.fortytwo.sesametools.replay.calls.CloseIterationCall;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class RecorderIteration<T, E extends Exception> implements CloseableIteration<T, E> {
    private final String id;
    private final CloseableIteration<T, E> baseIteration;
    private final Handler<SailConnectionCall, E> queryHandler;

    public RecorderIteration(final CloseableIteration<T, E> baseIteration,
                             final String id,
                             final Handler<SailConnectionCall, E> queryHandler) {
        this.baseIteration = baseIteration;
        this.id = id;
        this.queryHandler = queryHandler;
    }

    public void close() throws E {
        queryHandler.handle(new CloseIterationCall(id));
        baseIteration.close();
    }

    public boolean hasNext() throws E {
        queryHandler.handle(new HasNextCall(id));
        return baseIteration.hasNext();
    }

    public T next() throws E {
        queryHandler.handle(new NextCall(id));
        return baseIteration.next();
    }

    public void remove() throws E {
        queryHandler.handle(new RemoveCall(id));
        baseIteration.remove();
    }
}
