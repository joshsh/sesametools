
package net.fortytwo.sesametools.caching;

import info.aduna.iteration.CloseableIteration;
import junit.framework.TestCase;
import net.fortytwo.sesametools.debug.DebugSail;
import net.fortytwo.sesametools.debug.SailCounter;
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

import java.io.InputStream;

public class CachingSailTest extends TestCase {
    private static final String NS = "http://example.org/ns/";
    private static final long CAPACITY = 10000000l;

    private Sail baseSail;
    private Sail proxySail;
    private SailCounter counter = new SailCounter();

    public void testSubjectCaching() throws Exception {
        //Collection<Statement> statementsAdded = new LinkedList<Statement>();
        //Collection<Statement> statementsRemoved = new LinkedList<Statement>();

        createBaseSail();
        CachingSail cachingSail = new CachingSail(proxySail, true, false, false, CAPACITY);
        cachingSail.initialize();
        loadData();
        SailConnection sc = cachingSail.getConnection();

        int count;

        // Subject "one" is not yet cached.
        counter.resetMethodCount();
        count = countStatements(sc.getStatements(uri("one"), uri("two"), null, false));
        assertEquals(1, count);
        assertEquals(1, counter.getMethodCount(SailCounter.Method.GetStatements));
        // Subject "one" is now cached, so the base Sail should not be queried.
        counter.resetMethodCount();
        count = countStatements(sc.getStatements(uri("one"), uri("two"), null, false));
        assertEquals(1, count);
        assertEquals(0, counter.getMethodCount(SailCounter.Method.GetStatements));
        // Different query, same caching behavior.
        counter.resetMethodCount();
        count = countStatements(sc.getStatements(uri("one"), null, null, false));
        assertEquals(2, count);
        assertEquals(0, counter.getMethodCount(SailCounter.Method.GetStatements));
        // A query with a wildcard subject must be relayed to the base Sail.
        counter.resetMethodCount();
        count = countStatements(sc.getStatements(null, uri("two"), null, false));
        assertEquals(1, count);
        assertEquals(1, counter.getMethodCount(SailCounter.Method.GetStatements));

        sc.close();
        cachingSail.shutDown();
    }

    public void testWrite() throws Exception {
        createBaseSail();
        CachingSail cachingSail = new CachingSail(proxySail, true, false, false, CAPACITY);
        cachingSail.initialize();
        int count;

        URI resA = cachingSail.getValueFactory().createURI("http://example.org/ns/resA");

        SailConnection sc = cachingSail.getConnection();

        sc.addStatement(resA, resA, resA);
        sc.commit();
        count = countStatements(sc.getStatements(resA, null, null, false));
        assertEquals(1, count);
        sc.removeStatements(null, resA, null);
        sc.commit();
        count = countStatements(sc.getStatements(null, null, resA, false));
        assertEquals(0, count);

        sc.addStatement(resA, resA, resA, resA);
        sc.commit();
        count = countStatements(sc.getStatements(null, null, null, false, resA));
        assertEquals(1, count);
        sc.removeStatements(null, null, null, resA);
        sc.commit();
        count = countStatements(sc.getStatements(null, null, resA, false));
        assertEquals(0, count);
    }

    private void createBaseSail() throws Exception {
        baseSail = new MemoryStore();
        proxySail = new DebugSail(baseSail, counter);
    }

    private void loadData() throws Exception {
        Repository repo = new SailRepository(baseSail);
        RepositoryConnection rc = repo.getConnection();
        InputStream is = CachingSailTest.class.getResourceAsStream("cachingSailTest.trig");
        rc.add(is, "", RDFFormat.TRIG);
        rc.close();
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
}
