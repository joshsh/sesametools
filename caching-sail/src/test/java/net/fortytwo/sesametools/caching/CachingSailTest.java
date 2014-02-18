package net.fortytwo.sesametools.caching;

import info.aduna.iteration.CloseableIteration;
import net.fortytwo.sesametools.replay.Handler;
import net.fortytwo.sesametools.replay.RecorderSail;
import net.fortytwo.sesametools.replay.SailConnectionCall;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class CachingSailTest {

    private static final Logger LOG = LoggerFactory.getLogger(CachingSailTest.class);

    private static final String NS = "http://example.org/ns/";
    private static final long CAPACITY = 10000000l;

    private Sail baseSail;
    private SailCounter counter;
    private SailConnection sc;
    private CachingSail cachingSail;
    private RecorderSail recorderSail;

    @Before
    public void setUp() throws Exception {
        counter = new SailCounter();
        baseSail = new MemoryStore();
        recorderSail = new RecorderSail(baseSail, counter);
        cachingSail = new CachingSail(recorderSail, true, false, false, CAPACITY);
        cachingSail.initialize();

        Repository repo = new SailRepository(baseSail);
        RepositoryConnection rc = repo.getConnection();
        InputStream is = CachingSailTest.class.getResourceAsStream("cachingSailTest.trig");
        rc.add(is, "", RDFFormat.TRIG);
        rc.close();

        sc = cachingSail.getConnection();
    }

    @After
    public void tearDown() {
        try {
            sc.close();
        } catch (SailException e) {
            LOG.error("Error closing connection", e);
        }

        sc = null;

        try {
            cachingSail.shutDown();
        } catch (SailException e) {
            LOG.error("Error shutting down repository", e);
        }

        cachingSail = null;
        baseSail = null;

        counter = null;
    }

    @Test
    public void testSubjectCaching() throws Exception {
        int count;

        // Subject "one" is not yet cached.
        counter.reset();
        count = countStatements(sc.getStatements(uri("one"), uri("two"), null, false));
        assertEquals(1, count);
        assertEquals(1, counter.getGets());
        // Subject "one" is now cached, so the base Sail should not be queried.
        counter.reset();
        count = countStatements(sc.getStatements(uri("one"), uri("two"), null, false));
        assertEquals(1, count);
        assertEquals(0, counter.getGets());
        // Different query, same caching behavior.
        counter.reset();
        count = countStatements(sc.getStatements(uri("one"), null, null, false));
        assertEquals(2, count);
        assertEquals(0, counter.getGets());
        // A query with a wildcard subject must be relayed to the base Sail.
        counter.reset();
        count = countStatements(sc.getStatements(null, uri("two"), null, false));
        assertEquals(1, count);
        assertEquals(1, counter.getGets());

    }

    @Test
    public void testWrite() throws Exception {
        int count;

        URI resA = uri("http://example.org/ns/resA");

        sc.begin();
        sc.addStatement(resA, resA, resA);
        sc.commit();
        count = countStatements(sc.getStatements(resA, null, null, false));
        assertEquals(1, count);
        sc.begin();
        sc.removeStatements(null, resA, null);
        sc.commit();
        count = countStatements(sc.getStatements(null, null, resA, false));
        assertEquals(0, count);

        sc.begin();
        sc.addStatement(resA, resA, resA, resA);
        sc.commit();
        count = countStatements(sc.getStatements(null, null, null, false, resA));
        assertEquals(1, count);
        sc.begin();
        sc.removeStatements(null, null, null, resA);
        sc.commit();
        count = countStatements(sc.getStatements(null, null, resA, false));
        assertEquals(0, count);
    }

    private URI uri(final String localName) {
        return baseSail.getValueFactory().createURI(NS + localName);
    }

    private int countStatements(final CloseableIteration<? extends Statement, SailException> iter) throws SailException {
        int count = 0;
        while (iter.hasNext()) {
            count++;
            iter.next();
        }
        return count;
    }

    private class SailCounter implements Handler<SailConnectionCall, SailException> {
        private int gets = 0;
        
        public void handle(final SailConnectionCall call) throws SailException {
            if (call.getType() == SailConnectionCall.Type.GET_STATEMENTS) {
                gets++;
            }
        }
        
        public void reset() {
            gets = 0;
        }
        
        public int getGets() {
            return gets;
        }
    }
}
