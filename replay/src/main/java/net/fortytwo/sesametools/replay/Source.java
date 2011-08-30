package net.fortytwo.sesametools.replay;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public interface Source<T, E extends Exception> {
    void writeTo(Handler<T, E> handler) throws E;
}
