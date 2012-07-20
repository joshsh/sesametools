package net.fortytwo.sesametools;

import info.aduna.iteration.CloseableIteration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.memory.MemoryStore;

import static junit.framework.Assert.assertEquals;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class SingleContextSailTest {
    private static final URI
            SPECIAL_CONTEXT = new URIImpl("http://example.org/ns/special-context"),
            OTHER_CONTEXT = new URIImpl("http://example.org/ns/other-context"),
            THING1 = new URIImpl("http://example.org/ns/thing1"),
            THING2 = new URIImpl("http://example.org/ns/thing2"),
            RELATION1 = new URIImpl("http://example.org/ns/relation1");

    private Sail baseSail;
    private Sail sail;

    @Before
    public void setup() throws Exception {
        baseSail = new MemoryStore();
        sail = new SingleContextSail(baseSail, SPECIAL_CONTEXT);
        sail.initialize();
    }

    @After
    public void tearDown() throws Exception {
        sail.shutDown();
    }

    @Test
    public void testStatementsInSpecialContextAreVisible() throws Exception {
        SailConnection bc = baseSail.getConnection();
        bc.addStatement(THING1, RELATION1, THING2, SPECIAL_CONTEXT);
        bc.addStatement(THING2, RELATION1, THING2, SPECIAL_CONTEXT);
        bc.commit();
        bc.close();

        assertEquals(2, countStatements(null, null, null, SPECIAL_CONTEXT));
        assertEquals(2, countStatements(null, null, null));
        assertEquals(1, countStatements(THING1, null, null, SPECIAL_CONTEXT));
        assertEquals(1, countStatements(THING1, null, null));

        assertEquals(0, countStatements(null, null, null, OTHER_CONTEXT));
        assertEquals(0, countStatements(null, null, null, (Resource) null));
    }

    @Test
    public void testStatementsInOtherContextsAreInvisible() throws Exception {
        SailConnection bc = baseSail.getConnection();
        bc.addStatement(THING1, RELATION1, THING2, SPECIAL_CONTEXT);
        bc.addStatement(THING2, RELATION1, THING1, OTHER_CONTEXT);
        bc.addStatement(THING2, RELATION1, THING2, (Resource) null);
        bc.commit();
        bc.close();

        assertEquals(1, countStatements(null, null, null, SPECIAL_CONTEXT));
        assertEquals(1, countStatements(null, null, null));
        assertEquals(1, countStatements(THING1, null, null, SPECIAL_CONTEXT));
        assertEquals(1, countStatements(THING1, null, null));
        assertEquals(0, countStatements(THING2, null, null, SPECIAL_CONTEXT));
        assertEquals(0, countStatements(THING2, null, null));

        assertEquals(0, countStatements(null, null, null, OTHER_CONTEXT));
        assertEquals(0, countStatements(null, null, null, (Resource) null));
    }

    @Test
    public void canWriteIntoOnlySpecialContext() throws Exception {
        SailConnection c = sail.getConnection();
        c.addStatement(THING1, RELATION1, THING2, SPECIAL_CONTEXT);
        c.addStatement(THING2, RELATION1, THING1, OTHER_CONTEXT);
        c.addStatement(THING2, RELATION1, THING2, (Resource) null);
        c.commit();
        c.close();

        assertEquals(1, countStatements(null, null, null));
    }

    private long countStatements(final Resource subject,
                                 final URI predicate,
                                 final Value object,
                                 final Resource... contexts) throws Exception {
        long count = 0;

        SailConnection c = sail.getConnection();
        try {
            CloseableIteration<? extends Statement, SailException> iter
                    = c.getStatements(subject, predicate, object, false, contexts);
            try {
                while (iter.hasNext()) {
                    count++;
                    iter.next();
                }
            } finally {
                iter.close();
            }
        } finally {
            c.close();
        }

        return count;
    }
}
