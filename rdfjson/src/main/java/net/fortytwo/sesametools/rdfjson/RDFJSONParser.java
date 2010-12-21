package net.fortytwo.sesametools.rdfjson;

import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.rio.ParseErrorListener;
import org.openrdf.rio.ParseLocationListener;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import se.kmr.scam.rest.util.RDFJSON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * RDFParser implementation for the proposed RDF/JSON format (see http://n2.talis.com/wiki/RDF_JSON_Specification)
 * <p/>
 * Date: Dec 21, 2010
 * Time: 2:55:39 PM
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class RDFJSONParser implements RDFParser {

    private ValueFactory valueFactory;
    private RDFHandler rdfHandler;
    private ParseErrorListener parseErrorListener;
    private ParseLocationListener parseLocationListener;
    private boolean verifyData;
    private boolean preserveBNodeIDs;
    private boolean stopAtFirstError;
    private DatatypeHandling datatypeHandling;

    public RDFFormat getRDFFormat() {
        return RDFJSONWriter.RDFJSON_FORMAT;
    }

    public void setValueFactory(final ValueFactory valueFactory) {
        this.valueFactory = valueFactory;
    }

    public void setRDFHandler(final RDFHandler handler) {
        rdfHandler = handler;
    }

    public void setParseErrorListener(final ParseErrorListener listener) {
        parseErrorListener = listener;
    }

    public void setParseLocationListener(final ParseLocationListener listener) {
        this.parseLocationListener = listener;
    }

    public void setVerifyData(final boolean verifyData) {
        this.verifyData = verifyData;
    }

    public void setPreserveBNodeIDs(final boolean preserveBNodeIDs) {
        this.preserveBNodeIDs = preserveBNodeIDs;
    }

    public void setStopAtFirstError(final boolean stopAtFirstError) {
        this.stopAtFirstError = stopAtFirstError;
    }

    public void setDatatypeHandling(final DatatypeHandling datatypeHandling) {
        this.datatypeHandling = datatypeHandling;
    }

    public void parse(final InputStream in,
                      final String baseURI) throws IOException, RDFParseException, RDFHandlerException {
        Reader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        parse(reader, baseURI);
    }

    public void parse(final Reader reader,
                      final String baseURI) throws IOException, RDFParseException, RDFHandlerException {
        if (null == rdfHandler) {
            throw new IllegalStateException("RDF handler has not been set");
        }

        String s = toString(reader);
        Graph g = RDFJSON.rdfJsonToGraph(s);
        rdfHandler.startRDF();
        for (Statement statement : g) {
            rdfHandler.handleStatement(statement);
        }
        rdfHandler.endRDF();
    }

    private String toString(final Reader reader) throws IOException {
        Writer writer = new StringWriter();

        char[] buffer = new char[1024];
        int n;
        while ((n = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, n);
        }
        return writer.toString();
    }
}
