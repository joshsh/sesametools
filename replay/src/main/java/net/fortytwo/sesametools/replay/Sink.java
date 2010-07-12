package net.fortytwo.sesametools.replay;

/**
 * User: josh
 * Date: Jul 12, 2010
 * Time: 10:32:46 AM
 */
public interface Sink<T, E extends Exception> {
    void put(T t) throws E;
}
