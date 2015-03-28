
package net.fortytwo.sesametools.replay;

import info.aduna.iteration.CloseableIteration;
import org.openrdf.IsolationLevel;
import org.openrdf.model.ValueFactory;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.StackableSail;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Sail which reads a log of previously recorded Sail operations and executes them.
 *
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class PlaybackSail implements StackableSail {
    private final Sail baseSail;
    private final Source<SailConnectionCall, SailException> querySource;

    private final Map<String, SailConnection> idToConnectionMap;
    private final Map<String, List<CloseableIteration>> idToIterationsMap;

    public PlaybackSail(final Sail baseSail, Source<SailConnectionCall, SailException> querySource) {
        this.baseSail = baseSail;
        this.querySource = querySource;
        this.idToConnectionMap = new HashMap<String, SailConnection>();
        this.idToIterationsMap = new HashMap<String, List<CloseableIteration>>();
    }

    public PlaybackSail(final Sail baseSail, final InputStream is) {
        this.baseSail = baseSail;
        this.querySource = new InputStreamSource(is);
        this.idToConnectionMap = new HashMap<String, SailConnection>();
        this.idToIterationsMap = new HashMap<String, List<CloseableIteration>>();
    }

    @Override
    public void setDataDir(final File file) {
        baseSail.setDataDir(file);
    }

    @Override
    public File getDataDir() {
        return baseSail.getDataDir();
    }

    @Override
    public void initialize() throws SailException {
//        baseSail.initialize();

        Handler<SailConnectionCall, SailException> handler = new Handler<SailConnectionCall, SailException>() {
            private long line = 0;

            public void handle(final SailConnectionCall call) throws SailException {
                line++;

                try {
                    String id = call.getId();
                    SailConnectionCall.Type type = call.getType();

                    if (id.contains("-")) {
                        CloseableIteration iter = getIteration(id);
                        call.execute(iter);
                    } else {
                        SailConnection sc = getConnection(id);
                        Object result = call.execute(sc);
                        if (SailConnectionCall.Type.CLOSE_CONNECTION == type) {
                            idToConnectionMap.remove(id);
                            idToIterationsMap.remove(id);
                        } else if (createsIteration(call)) {
                            addIteration(id, (CloseableIteration) result);
                        }
                    }
                } catch (Throwable e) {
                    System.err.println("Error on line " + line);
                    throw (e instanceof SailException)
                            ? (SailException) e
                            : new SailException(e);
                }
            }
        };

        querySource.writeTo(handler);
    }

    private SailConnection getConnection(final String id) throws SailException {
        SailConnection sc = idToConnectionMap.get(id);
        if (null == sc) {
            sc = getConnection();
            idToConnectionMap.put(id, sc);
        }

        return sc;
    }
    
    private CloseableIteration getIteration(final String id) {
        int i = id.indexOf("-");
        String conId = id.substring(0, i);
        int iterIndex = new Integer(id.substring(i + 1)).intValue() - 1;
        return idToIterationsMap.get(conId).get(iterIndex);
    }

    private boolean createsIteration(final SailConnectionCall call) {
        switch (call.getType()) {
            case GET_CONTEXT_IDS:
            case GET_NAMESPACES:
            case GET_STATEMENTS:
                return true;
            default:
                return false;
        }
    }

    private void addIteration(final String id, final CloseableIteration iter) {
        List<CloseableIteration> iters = idToIterationsMap.get(id);

        if (null == iters) {
            iters = new ArrayList<CloseableIteration>();
            idToIterationsMap.put(id, iters);
        }

        iters.add(iter);
    }

    @Override
    public void shutDown() throws SailException {
        // Close any remaining open SailConnections
        for (SailConnection sc : idToConnectionMap.values()) {
            sc.close();
        }

//        baseSail.shutDown();
    }

    @Override
    public boolean isWritable() throws SailException {
        return baseSail.isWritable();
    }

    @Override
    public SailConnection getConnection() throws SailException {
        return baseSail.getConnection();
    }

    @Override
    public ValueFactory getValueFactory() {
        return baseSail.getValueFactory();
    }

    @Override
    public List<IsolationLevel> getSupportedIsolationLevels() {
        return baseSail.getSupportedIsolationLevels();
    }

    @Override
    public IsolationLevel getDefaultIsolationLevel() {
        return baseSail.getDefaultIsolationLevel();
    }

    @Override
    public void setBaseSail(final Sail sail) {
        // Do nothing -- base Sail is final
    }

    @Override
    public Sail getBaseSail() {
        return baseSail;
    }

    private class InputStreamSource implements Source<SailConnectionCall, SailException> {
        private BufferedReader reader;

        public InputStreamSource(final InputStream is) {
            reader = new BufferedReader(new InputStreamReader(is));
        }

        public void writeTo(final Handler<SailConnectionCall, SailException> handler) throws SailException {
            try {
                try {
                    String line;

                    while (null != (line = reader.readLine())) {
                        line = line.trim();
                        if (0 != line.length()) {
                            handler.handle(SailConnectionCall.construct(line));
                        }
                    }
                } finally {
                    reader.close();
                }
            } catch (IOException e) {
                throw new SailException(e);
            }
        }
    }
}
