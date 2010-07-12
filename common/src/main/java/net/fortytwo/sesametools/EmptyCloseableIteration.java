
package net.fortytwo.sesametools;

import info.aduna.iteration.CloseableIteration;

public class EmptyCloseableIteration<T, E extends Exception> implements CloseableIteration<T, E> {

    public void close() throws E {
    }

    public boolean hasNext() throws E {
        return false;
    }

    public T next() throws E {
        return null;
    }

    public void remove() throws E {
    }
}
