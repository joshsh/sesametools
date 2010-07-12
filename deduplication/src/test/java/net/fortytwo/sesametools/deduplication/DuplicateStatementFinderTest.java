package net.fortytwo.sesametools.deduplication;

import junit.framework.TestCase;
import net.fortytwo.sesametools.deduplication.DuplicateStatementFinder;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.memory.MemoryStore;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Oct 5, 2008
 * Time: 10:37:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class DuplicateStatementFinderTest extends TestCase {
    private static final String NS = "http://example.org/test/";

    private Sail sail;
    private ValueFactory valueFactory;

    public void setUp() throws Exception {
        sail = new MemoryStore();
        sail.initialize();
        valueFactory = sail.getValueFactory();
    }

    public void tearDown() throws Exception {
        sail.shutDown();
    }

    public void testSimple() throws Exception {
        SailConnection sc = sail.getConnection();

        URI ctx1 = valueFactory.createURI(NS + "ctx1");
        URI ctx2 = valueFactory.createURI(NS + "ctx2");

        // Not a duplicate.
        sc.addStatement(RDF.NIL, RDF.TYPE, RDF.LIST, ctx1);

        // Duplicate: two non-null contexts.
        sc.addStatement(RDF.TYPE, RDF.TYPE, RDF.PROPERTY, ctx1);
        sc.addStatement(RDF.TYPE, RDF.TYPE, RDF.PROPERTY, ctx2);

        // Duplicate: null and non-null context.
        sc.addStatement(RDF.PROPERTY, RDF.TYPE, RDFS.CLASS, ctx1);
        sc.addStatement(RDF.PROPERTY, RDF.TYPE, RDFS.CLASS);

        Set<Statement> dups = DuplicateStatementFinder.findDuplicateStatements(sc);
        assertEquals(2, dups.size());
        //...


        sc.close();
    }
}
