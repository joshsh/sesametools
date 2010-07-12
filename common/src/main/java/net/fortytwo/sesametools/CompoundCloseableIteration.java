
package net.fortytwo.sesametools;

import info.aduna.iteration.CloseableIteration;

import java.util.Collection;
import java.util.Iterator;

/**
 * A CloseableIteration which wraps an ordered collection of other
 * CloseableIterations.
 *
 * @author josh
 * @param <T>
 * @param <E>
 */
public class CompoundCloseableIteration<T, E extends Exception> implements CloseableIteration<T, E> {
    private Iterator<CloseableIteration<T, E>> iterations;
    private CloseableIteration<T, E> currentIteration;

    public CompoundCloseableIteration(final Collection<CloseableIteration<T, E>> childIterations) {
        iterations = childIterations.iterator();
        if (iterations.hasNext()) {
            currentIteration = iterations.next();
        } else {
            currentIteration = null;
        }
    }

    public void close() throws E {
        if (null != currentIteration) {
            currentIteration.close();
            currentIteration = null;
        }

        while (iterations.hasNext()) {
            iterations.next().close();
        }
    }

    public boolean hasNext() throws E {
        if (null == currentIteration) {
            return false;
        } else if (currentIteration.hasNext()) {
            return true;
        } else {
            currentIteration.close();
            if (iterations.hasNext()) {
                currentIteration = iterations.next();

                // Recurse until a non-empty iteration is found.
                return hasNext();
            } else {
                currentIteration = null;
                return false;
            }
        }
    }

    public T next() throws E {
        return currentIteration.next();
    }

    public void remove() throws E {
        currentIteration.remove();
    }

}
