package net.fortytwo.sesametools.replay;

import info.aduna.iteration.CloseableIteration;
import net.fortytwo.sesametools.replay.calls.HasNextCall;
import net.fortytwo.sesametools.replay.calls.NextCall;
import net.fortytwo.sesametools.replay.calls.RemoveCall;
import net.fortytwo.sesametools.replay.calls.CloseIterationCall;

/**
 * Author: josh
 * Date: May 12, 2008
 * Time: 10:45:32 AM
 */
public class RecorderIteration<T, E extends Exception> implements CloseableIteration<T, E> {
    private final String id;
    private final CloseableIteration<T, E> baseIteration;
    private final Sink<SailConnectionCall, E> querySink;

    public RecorderIteration(final CloseableIteration<T, E> baseIteration,
                             final String id,
                             final Sink<SailConnectionCall, E> querySink) {
        this.baseIteration = baseIteration;
        this.id = id;
        this.querySink = querySink;
    }

    public void close() throws E {
        querySink.put(new CloseIterationCall(id));
        baseIteration.close();
    }

    public boolean hasNext() throws E {
        querySink.put(new HasNextCall(id));
        return baseIteration.hasNext();
    }

    public T next() throws E {
        querySink.put(new NextCall(id));
        return baseIteration.next();
    }

    public void remove() throws E {
        querySink.put(new RemoveCall(id));
        baseIteration.remove();
    }
}
