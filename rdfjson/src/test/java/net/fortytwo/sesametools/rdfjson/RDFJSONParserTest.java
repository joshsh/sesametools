package net.fortytwo.sesametools.rdfjson;

import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * User: josh
 * Date: Dec 21, 2010
 * Time: 3:54:39 PM
 */
public class RDFJSONParserTest extends RDFJSONTestBase {

    public void testAll() throws Exception {
        Graph g;

        g = parseToGraph("example0.json");
        assertEquals(12, g.size());

        //for (Statement st : g) {
        //    System.out.println(st);
        //}

        g = parseToGraph("example1.json");

        assertExpected(g,
                vf.createStatement(ARTHUR, RDF.TYPE, FOAF.PERSON),
                vf.createStatement(ARTHUR, RDF.TYPE, vf.createURI(OWL.NAMESPACE + "Thing")),
                vf.createStatement(ARTHUR, FOAF.NAME, vf.createLiteral("Arthur Dent", "en")),
                vf.createStatement(ARTHUR, FOAF.KNOWS, P1),
                vf.createStatement(P1, FOAF.NAME, vf.createLiteral("Ford Prefect", XMLSchema.STRING)));
    }

    protected Graph parseToGraph(final String fileName) throws Exception {
        RDFJSONParser p = new RDFJSONParser();
        RDFCollector c = new RDFCollector();
        p.setRDFHandler(c);

        InputStream in = RDFJSONParser.class.getResourceAsStream(fileName);
        try {
            p.parse(in, BASE_URI);
        } finally {
            in.close();
        }

        return c.getGraph();
    }

    protected void assertExpected(final Graph graph,
                                  final Statement... expectedStatements) throws Exception {
        Set<Statement> expected = new HashSet<Statement>();
        expected.addAll(Arrays.asList(expectedStatements));
        for (Statement t : expected) {
            if (!graph.contains(t)) {
                fail("expected statement not found: " + t);
            }
        }
        for (Statement t : graph) {
            if (!expected.contains(t)) {
                fail("unexpected statement found: " + t);
            }
        }
    }
}
