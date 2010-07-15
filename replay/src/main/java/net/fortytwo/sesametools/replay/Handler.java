package net.fortytwo.sesametools.replay;

/**
 * User: josh
 * Date: Jul 12, 2010
 * Time: 10:32:46 AM
 */
public interface Handler<T, E extends Exception> {
    void handle(T t) throws E;
}
