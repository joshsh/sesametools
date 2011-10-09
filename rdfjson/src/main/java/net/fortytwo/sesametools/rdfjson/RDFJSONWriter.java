package net.fortytwo.sesametools.rdfjson;

import net.fortytwo.sesametools.StatementComparator;

import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import se.kmr.scam.rest.util.RDFJSON;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Set;
import java.util.TreeSet;

/**
 * RDFWriter implementation for the proposed RDF/JSON format (see http://n2.talis.com/wiki/RDF_JSON_Specification)
 *
 * @author Joshua Shinavier (http://fortytwo.net).  Builds on code by Hannes Ebner
 */
public class RDFJSONWriter implements RDFWriter {

    private final Writer writer;
    private Set<Statement> graph;

    public RDFJSONWriter(final OutputStream out) {
        this(new OutputStreamWriter(out));
    }

    public RDFJSONWriter(final Writer writer) {
        this.writer = writer;
    }

    public RDFFormat getRDFFormat() {
        return RDFJSONFormat.RDFJSON;
    }

    public void startRDF() throws RDFHandlerException {
        graph = new TreeSet<Statement>(new StatementComparator());
    }

    public void endRDF() throws RDFHandlerException {
        RDFJSON.graphToRdfJsonPreordered(graph, writer);
        try {
            writer.flush();
        } catch (IOException e) {
            throw new RDFHandlerException(e);
        }
    }

    public void handleNamespace(final String prefix,
                                final String uri) throws RDFHandlerException {
        // Namespace prefixes are not used in RDF/JSON.
    }

    public void handleStatement(final Statement statement) throws RDFHandlerException {
        //System.out.println("got it: " + statement);
        graph.add(statement);
    }

    public void handleComment(final String comment) throws RDFHandlerException {
        // Comments are ignored.
    }
}
