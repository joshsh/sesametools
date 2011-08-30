package net.fortytwo.sesametools.replay;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public interface Handler<T, E extends Exception> {
    void handle(T t) throws E;
}
