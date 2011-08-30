package net.fortytwo.sesametools.rdfjson;

import net.fortytwo.sesametools.StatementComparator;
import org.junit.After;
import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.helpers.StatementCollector;

import java.io.InputStream;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import static net.fortytwo.sesametools.rdfjson.RDFJSONTestConstants.ARTHUR;
import static net.fortytwo.sesametools.rdfjson.RDFJSONTestConstants.BASE_URI;
import static net.fortytwo.sesametools.rdfjson.RDFJSONTestConstants.FOAF;
import static net.fortytwo.sesametools.rdfjson.RDFJSONTestConstants.GRAPH1;
import static net.fortytwo.sesametools.rdfjson.RDFJSONTestConstants.P1;
import static net.fortytwo.sesametools.rdfjson.RDFJSONTestConstants.vf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class RDFJSONParserTest {

	private Collection<Statement> g;
	
	@After
	public void cleanup()
	{
		// make sure we nullify the graph after each test is complete
		g = null;
	}
	
	@Test
    public void testSizeWithoutNullGraphs() throws Exception {
        g = parseToGraph("example0.json");

//        for (Statement st : g) {
//            System.out.println(st);
//        }

        assertEquals(12, g.size());

	}
	
	@Test
    public void testSizeWithNullGraphs() throws Exception {
        g = parseToGraph("example2.json");
        
//        for (Statement st : g) {
//            System.out.println(st);
//        }

        assertEquals(6, g.size());
	}
	
	@Test
    public void testExpectedStatements() throws Exception
    {
        g = parseToGraph("example1.json");
        
//        System.out.println("example1.json.size()="+g.size());

        assertEquals(6, g.size());
        
        assertExpected(g,
                vf.createStatement(ARTHUR, RDF.TYPE, FOAF.PERSON),
                vf.createStatement(ARTHUR, RDF.TYPE, vf.createURI(OWL.NAMESPACE + "Thing"), (Resource)null),
                vf.createStatement(ARTHUR, RDF.TYPE, vf.createURI(OWL.NAMESPACE + "Thing"), GRAPH1),
                vf.createStatement(ARTHUR, FOAF.NAME, vf.createLiteral("Arthur Dent", "en")),
                vf.createStatement(ARTHUR, FOAF.KNOWS, P1),
                vf.createStatement(P1, FOAF.NAME, vf.createLiteral("Ford Prefect", XMLSchema.STRING)));
    }

    protected Collection<Statement> parseToGraph(final String fileName) throws Exception {
        RDFJSONParser p = new RDFJSONParser();
        StatementCollector c = new StatementCollector();
        p.setRDFHandler(c);

        InputStream in = RDFJSONParser.class.getResourceAsStream(fileName);
        try {
            p.parse(in, BASE_URI);
        } finally {
            in.close();
        }

        return c.getStatements();
    }

    protected void assertExpected(final Collection<Statement> graph,
                                  final Statement... expectedStatements) throws Exception {
        Set<Statement> expected = new TreeSet<Statement>(new StatementComparator());
        for (Statement st : expectedStatements) {
            expected.add(st);
        }
        Set<Statement> actual = new TreeSet<Statement>(new StatementComparator());
        for (Statement st : graph) {
            actual.add(st);
        }
        for (Statement t : expected) {
            if (!actual.contains(t)) {
                fail("expected statement not found: " + t);
            }
        }
        for (Statement t : actual) {
            if (!expected.contains(t)) {
                fail("unexpected statement found: " + t);
            }
        }
    }
}
