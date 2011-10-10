package net.fortytwo.sesametools.jsonld;

import net.fortytwo.sesametools.StatementComparator;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.StatementCollector;

import java.io.InputStream;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import static net.fortytwo.sesametools.jsonld.JSONLDTestConstants.BASE_URI;
import static net.fortytwo.sesametools.jsonld.JSONLDTestConstants.vf;
import static net.fortytwo.sesametools.jsonld.JSONLDTestConstants.BNODE1;
import static org.junit.Assert.fail;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class JSONLDParserTest {

    private Collection<Statement> g;

        @Test
    public void testSingleNodeNoContext() throws Exception {
        g = parseToGraph("example1.json");

        assertExpected(g,
                vf.createStatement(BNODE1, JSONLDTestConstants.FOAF.NAME, vf.createLiteral("Manu Sporny")),
                vf.createStatement(BNODE1, JSONLDTestConstants.SIOC.AVATAR, vf.createURI("http://twitter.com/account/profile_image/manusporny")),
                vf.createStatement(BNODE1, JSONLDTestConstants.FOAF.HOMEPAGE, vf.createURI("http://manu.sporny.org/")));
    }

    @Test
    public void testSingleNodeWithContext() throws Exception {
        g = parseToGraph("example2.json");

        assertExpected(g,
                vf.createStatement(BNODE1, JSONLDTestConstants.FOAF.NAME, vf.createLiteral("Manu Sporny")),
                vf.createStatement(BNODE1, JSONLDTestConstants.SIOC.AVATAR, vf.createURI("http://twitter.com/account/profile_image/manusporny")),
                vf.createStatement(BNODE1, JSONLDTestConstants.FOAF.HOMEPAGE, vf.createURI("http://manu.sporny.org/")));
    }

    @Test
    public void testCoerceIRI() throws Exception {
        g = parseToGraph("example3.json");
    }
    
    @Ignore
    @Test
    public void testArrays() throws Exception {
        g = parseToGraph("example4.json");

    }
        
    protected Collection<Statement> parseToGraph(final String fileName) throws Exception {
        RDFParser p = new JSONLDParser();
        StatementCollector c = new StatementCollector();
        p.setRDFHandler(c);

        InputStream in = JSONLDParser.class.getResourceAsStream(fileName);
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
