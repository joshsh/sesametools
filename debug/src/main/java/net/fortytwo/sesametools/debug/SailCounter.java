package net.fortytwo.sesametools.debug;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class SailCounter {
    public enum Method {
        GetStatements
    }

    private int countGetStatements;

    public int getMethodCount(final Method method) {
        switch (method) {
            case GetStatements:
                return countGetStatements;
            default:
                return 0;
        }
    }

    public void resetMethodCount() {
        countGetStatements = 0;
    }

    void incremenentMethodCount(final Method method) {
        switch (method) {
            case GetStatements:
                countGetStatements++;
                break;
            default:
        }
    }
}
