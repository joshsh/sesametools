
package net.fortytwo.sesametools.replay;

import org.openrdf.model.ValueFactory;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.StackableSail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class RecorderSail implements StackableSail {
    private final Sail baseSail;
    private final ReplayConfiguration config;
    private final Handler<SailConnectionCall, SailException> queryHandler;

    public RecorderSail(final Sail baseSail, final OutputStream out) {
        this.baseSail = baseSail;
        config = new ReplayConfiguration();
        final PrintStream ps = (out instanceof PrintStream)
                ? (PrintStream) out
                : new PrintStream(out);

        this.queryHandler = new Handler<SailConnectionCall, SailException>() {
            public void handle(final SailConnectionCall sailQuery) throws SailException {
                ps.println(sailQuery.toString());
            }
        };
    }

    public void setDataDir(final File file) {
        baseSail.setDataDir(file);
    }

    public File getDataDir() {
        return baseSail.getDataDir();
    }

    public void initialize() throws SailException {
        baseSail.initialize();
    }

    public void shutDown() throws SailException {
        baseSail.shutDown();
    }

    public boolean isWritable() throws SailException {
        return baseSail.isWritable();
    }

    public SailConnection getConnection() throws SailException {
        return new RecorderSailConnection(baseSail, config, queryHandler);
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

    // FIXME: temporary
    public static void main(final String[] args) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in;
        Sail baseSail, itmSail, sail;

        // Record and play back
/*
        baseSail = new MemoryStore(), itmSail;
        sail = new RecorderSail(baseSail, new PrintStream(out));
        sail.initialize();
        Repository repo = new SailRepository(sail);
        RepositoryConnection rc = repo.getConnection();
        rc.add(FormatConverter.class.getResourceAsStream("cens-addon.trig"), "", RDFFormat.TRIG);
//        rc.add(FormatConverter.class.getResourceAsStream("krsPrefixes.ttl"), "", RDFFormat.TURTLE);
        rc.close();
        System.out.println(out.toString());

        sail.shutDown();
        baseSail.shutDown();

        in = new ByteArrayInputStream(out.toByteArray());
        baseSail = new MemoryStore();
        baseSail.initialize();
        itmSail = new RecorderSail(baseSail, System.out);
        sail = new PlaybackSail(itmSail, in);
        sail.initialize();
        in.close();
*/
        // Play back from reef-recorder.log

        /*
        in = new FileInputStream("/tmp/reef-recorder.log");
        baseSail = new MemoryStore();
        baseSail.initialize();
        itmSail = new RecorderSail(baseSail, new FileOutputStream("/tmp/reef-recorder2.log"));
        sail = new PlaybackSail(itmSail, in);
        sail.initialize();
        in.close();

        ////////////////////////////////

        baseSail.shutDown();
        sail.shutDown();
        */
    }
}
