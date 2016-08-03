package net.fortytwo.sesametools.deduplication;

import junit.framework.TestCase;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.util.Set;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
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
        sc.begin();

        IRI ctx1 = valueFactory.createIRI(NS + "ctx1");
        IRI ctx2 = valueFactory.createIRI(NS + "ctx2");

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

        sc.rollback();
        sc.close();
    }
}
