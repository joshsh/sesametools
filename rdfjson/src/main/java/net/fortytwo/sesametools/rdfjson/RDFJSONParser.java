package net.fortytwo.sesametools.rdfjson;

import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.rio.ParseErrorListener;
import org.openrdf.rio.ParseLocationListener;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.RDFParserBase;
import se.kmr.scam.rest.util.RDFJSON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;

/**
 * RDFParser implementation for the proposed RDF/JSON format (see http://n2.talis.com/wiki/RDF_JSON_Specification)
 *
 * @author Joshua Shinavier (http://fortytwo.net).  Builds on code by Hannes Ebner
 */
@SuppressWarnings("unused")
public class RDFJSONParser extends RDFParserBase {

    private ValueFactory valueFactory;
    private RDFHandler rdfHandler;
    private ParseErrorListener parseErrorListener;
    private ParseLocationListener parseLocationListener;
    private ParserConfig config = new ParserConfig();

    public RDFFormat getRDFFormat() {
        return RDFJSONFormat.RDFJSON;
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

    public void setParserConfig(ParserConfig config) {
        this.config = config;
    }

    public ParserConfig getParserConfig() {
        return config;
    }

    public void setVerifyData(final boolean verifyData) {
        config = new ParserConfig(verifyData,
                config.stopAtFirstError(),
                config.isPreserveBNodeIDs(),
                config.datatypeHandling());
    }

    public void setPreserveBNodeIDs(final boolean preserveBNodeIDs) {
        config = new ParserConfig(config.verifyData(),
                config.stopAtFirstError(),
                preserveBNodeIDs,
                config.datatypeHandling());
    }

    public void setStopAtFirstError(final boolean stopAtFirstError) {
        config = new ParserConfig(config.verifyData(),
                stopAtFirstError,
                config.isPreserveBNodeIDs(),
                config.datatypeHandling());
    }

    public void setDatatypeHandling(final DatatypeHandling datatypeHandling) {
        config = new ParserConfig(config.verifyData(),
                config.stopAtFirstError(),
                config.isPreserveBNodeIDs(),
                datatypeHandling);
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
        Collection<Statement> g = RDFJSON.rdfJsonToGraph(s);
        
        if(g == null) {
        	throw new RDFParseException("Could not parse JSON RDF Graph");
        }
        
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
