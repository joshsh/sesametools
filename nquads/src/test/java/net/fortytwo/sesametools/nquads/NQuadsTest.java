package net.fortytwo.sesametools.nquads;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import static junit.framework.Assert.assertEquals;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class NQuadsTest {

    // "Manual" test
    @Test
    public void testParserAndWriterSucceed() throws Exception {
        NQuadsParser p = new NQuadsParser();

        RDFHandler w = new NQuadsWriter(System.out);
        //RDFHandler w = new StatementCollector();
        p.setRDFHandler(w);

        Reader in = new InputStreamReader(NQuadsParser.class.getResourceAsStream("doc1.nq"));
        try {
            p.parse(in, "");
        } finally {
            in.close();
        }
    }

    // "Manual" test
    @Test
    public void testToOtherFormat() throws Exception {
        NQuadsParser p = new NQuadsParser();

        RDFHandler w = Rio.createWriter(RDFFormat.TRIG, System.out);
        //RDFHandler w = new StatementCollector();
        p.setRDFHandler(w);

        Reader in = new InputStreamReader(NQuadsParser.class.getResourceAsStream("doc2.nq"));
        try {
            p.parse(in, "");
        } finally {
            in.close();
        }
    }

    @Test
    public void testCountParsedStatements() throws Exception {
        NQuadsParser p = new NQuadsParser();

        StatementCollector w = new StatementCollector();
        p.setRDFHandler(w);

        Reader in = new InputStreamReader(NQuadsParser.class.getResourceAsStream("doc2.nq"));
        try {
            p.parse(in, "");
        } finally {
            in.close();
        }

        assertEquals(12, w.getStatements().size());
    }

    @Test
    public void testInvalid() throws Exception {
        NQuadsParser p = new NQuadsParser();

        StatementCollector h = new StatementCollector();

        p.setRDFHandler(h);

        // this line is invalid
        Reader testInput = new StringReader("<http://www.wrong.com> <http://wrong.com/1.1/tt> \"x\"^^<http://xxx.net/int> . <http://path.to.graph>");

        try {
            p.parse(testInput, "");
            Assert.fail("Did not receive expected parse exception");
        } catch (RDFParseException rdfpe) {
            Assert.assertEquals(1, rdfpe.getLineNumber());
        }

        // verify that no statements were given to the RDFHandler
        Assert.assertEquals(0, h.getStatements().size());
    }


}
