package net.fortytwo.sesametools;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import static junit.framework.Assert.assertEquals;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class SingleContextSailTest {
    private static final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private static final IRI
            SPECIAL_CONTEXT = valueFactory.createIRI("http://example.org/ns/special-context"),
            OTHER_CONTEXT = valueFactory.createIRI("http://example.org/ns/other-context"),
            THING1 = valueFactory.createIRI("http://example.org/ns/thing1"),
            THING2 = valueFactory.createIRI("http://example.org/ns/thing2"),
            RELATION1 = valueFactory.createIRI("http://example.org/ns/relation1");

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
        try {
            bc.begin();
            bc.addStatement(THING1, RELATION1, THING2, SPECIAL_CONTEXT);
            bc.addStatement(THING2, RELATION1, THING2, SPECIAL_CONTEXT);
            bc.commit();
        } finally {
            bc.close();
        }

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
        try {
            bc.begin();
            bc.addStatement(THING1, RELATION1, THING2, SPECIAL_CONTEXT);
            bc.addStatement(THING2, RELATION1, THING1, OTHER_CONTEXT);
            bc.addStatement(THING2, RELATION1, THING2, (Resource) null);
            bc.commit();
        } finally {
            bc.close();
        }

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
        try {
            c.begin();
            c.addStatement(THING1, RELATION1, THING2, SPECIAL_CONTEXT);
            c.addStatement(THING2, RELATION1, THING1, OTHER_CONTEXT);
            c.addStatement(THING2, RELATION1, THING2, (Resource) null);
            c.commit();
        } finally {
            c.close();
        }

        assertEquals(1, countStatements(null, null, null));
    }

    private long countStatements(final Resource subject,
                                 final IRI predicate,
                                 final Value object,
                                 final Resource... contexts) {
        long count = 0;

        SailConnection c = sail.getConnection();
        try {
            try (CloseableIteration<? extends Statement, SailException> iter
                         = c.getStatements(subject, predicate, object, false, contexts)) {
                while (iter.hasNext()) {
                    count++;
                    iter.next();
                }
            }
            c.rollback();
        } finally {
            c.close();
        }

        return count;
    }
}
