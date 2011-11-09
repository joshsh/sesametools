/**
 *
 */
package net.fortytwo.sesametools.rdfjson;

import org.json.JSONObject;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.rdfxml.RDFXMLParser;
import org.openrdf.rio.turtle.TurtleParser;
import org.openrdf.rio.turtle.TurtleWriter;

import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;

import static net.fortytwo.sesametools.rdfjson.RDFJSONTestConstants.BASE_URI;

/**
 * Test utils for comparative parsing using
 * the RDF/JSON parser and the Sesame Turtle parser
 *
 * @author Peter Ansell p_ansell@yahoo.com
 */
public final class RDFJSONTestUtils {
    public static final JSONObject parseAndWrite(final String fileName) throws Exception {
        RDFJSONParser p = new RDFJSONParser();
        Writer stringWriter = new StringWriter();

        RDFWriter w = new RDFJSONWriter(stringWriter);
        p.setRDFHandler(w);
        InputStream in = RDFJSONTestUtils.class.getResourceAsStream(fileName);
        try {
            p.parse(in, BASE_URI);
            return new JSONObject(stringWriter.toString());
        } finally {
            in.close();
        }
    }

    public static final JSONObject parseTurtleAndWriteJson(final String fileName) throws Exception {
        RDFParser p = new TurtleParser();
        Writer stringWriter = new StringWriter();

        RDFWriter w = new RDFJSONWriter(stringWriter);
        p.setRDFHandler(w);
        InputStream in = RDFJSONTestUtils.class.getResourceAsStream(fileName);
        try {
            p.parse(in, BASE_URI);
            return new JSONObject(stringWriter.toString());
        } finally {
            in.close();
        }
    }

    public static final String parseTurtleAndWriteTurtle(final String fileName) throws Exception {
        RDFParser p = new TurtleParser();
        Writer stringWriter = new StringWriter();

        RDFWriter w = new TurtleWriter(stringWriter);
        p.setRDFHandler(w);
        InputStream in = RDFJSONTestUtils.class.getResourceAsStream(fileName);
        try {
            p.parse(in, BASE_URI);
            return stringWriter.toString();
        } finally {
            in.close();
        }
    }

    public static final String parseJsonAndWriteTurtle(final String fileName) throws Exception {
        RDFParser p = new RDFJSONParser();
        Writer stringWriter = new StringWriter();

        RDFWriter w = new TurtleWriter(stringWriter);
        p.setRDFHandler(w);
        InputStream in = RDFJSONTestUtils.class.getResourceAsStream(fileName);
        try {
            p.parse(in, BASE_URI);
            return stringWriter.toString();
        } finally {
            in.close();
        }
    }

    public static JSONObject parseRdfXmlAndWriteJson(final String fileName) throws Exception {
        RDFParser p = new RDFXMLParser();
        Writer stringWriter = new StringWriter();

        RDFWriter w = new RDFJSONWriter(stringWriter);
        p.setRDFHandler(w);
        InputStream in = RDFJSONTestUtils.class.getResourceAsStream(fileName);
        try {
            p.parse(in, BASE_URI);
            return new JSONObject(stringWriter.toString());
        } finally {
            in.close();
        }
    }
}
