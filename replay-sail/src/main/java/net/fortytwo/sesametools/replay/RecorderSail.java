
package net.fortytwo.sesametools.replay;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.StackableSail;
import org.eclipse.rdf4j.sail.helpers.AbstractSail;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * A Sail which creates an ongoing log of operations as they are executed.
 * The log can later be used to recreate the operations in order.
 *
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class RecorderSail extends AbstractSail implements StackableSail {
    private final Sail baseSail;
    private final ReplayConfiguration config;
    private final Handler<SailConnectionCall, SailException> queryHandler;

    public RecorderSail(final Sail baseSail,
                        final Handler<SailConnectionCall, SailException> queryHandler) {
        this.baseSail = baseSail;
        config = new ReplayConfiguration();

        this.queryHandler = queryHandler;
    }
    
    public RecorderSail(final Sail baseSail, final OutputStream out) {
        this(baseSail, createDefaultHandler(out));
    }
    
    private static Handler<SailConnectionCall, SailException> createDefaultHandler(final OutputStream out) {
        final PrintStream ps = (out instanceof PrintStream)
                ? (PrintStream) out
                : new PrintStream(out);

        return new Handler<SailConnectionCall, SailException>() {
            public void handle(final SailConnectionCall sailQuery) throws SailException {
                ps.println(sailQuery.toString());
            }
        };
    }

    @Override
    public void setDataDir(final File file) {
        baseSail.setDataDir(file);
    }

    @Override
    public File getDataDir() {
        return baseSail.getDataDir();
    }

    protected void initializeInternal() throws SailException {
        baseSail.initialize();
    }

    protected void shutDownInternal() throws SailException {
        baseSail.shutDown();
    }

    @Override
    public boolean isWritable() throws SailException {
        return baseSail.isWritable();
    }

    protected SailConnection getConnectionInternal() throws SailException {
        return new RecorderSailConnection(this, baseSail, config, queryHandler);
    }

    public ValueFactory getValueFactory() {
        return baseSail.getValueFactory();
    }
    
    public void setBaseSail(final Sail sail) {
        // Do nothing -- base Sail is final
    }

    public Sail getBaseSail() {
        return baseSail;
    }

    public ReplayConfiguration getConfiguration() {
        return config;
    }
}
