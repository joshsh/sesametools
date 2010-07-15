package net.fortytwo.sesametools.replay;

/**
 * User: josh
 * Date: Jul 12, 2010
 * Time: 10:32:06 AM
 */
public interface Source<T, E extends Exception> {
    void writeTo(Handler<T, E> handler) throws E;
}
