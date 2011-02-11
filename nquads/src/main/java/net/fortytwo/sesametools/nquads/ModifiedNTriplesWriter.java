/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 * This code has been slightly modified (by Joshua Shinavier) so as to be used with NQuadsWriter.
 */
package net.fortytwo.sesametools.nquads;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.ntriples.NTriplesUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * An implementation of the RDFWriter interface that writes RDF documents in
 * N-Triples format. The N-Triples format is defined in <a
 * href="http://www.w3.org/TR/rdf-testcases/#ntriples">this section</a> of the
 * RDF Test Cases document.
 */
public class ModifiedNTriplesWriter implements RDFWriter {

    /*-----------*
      * Variables *
      *-----------*/

    protected Writer writer;

    protected boolean writingStarted;

    /*--------------*
      * Constructors *
      *--------------*/

    /**
     * Creates a new NTriplesWriter that will write to the supplied OutputStream.
     *
     * @param out The OutputStream to write the N-Triples document to.
     */
    public ModifiedNTriplesWriter(OutputStream out) {
        this(new OutputStreamWriter(out, Charset.forName("US-ASCII")));
    }

    /**
     * Creates a new NTriplesWriter that will write to the supplied Writer.
     *
     * @param writer The Writer to write the N-Triples document to.
     */
    public ModifiedNTriplesWriter(Writer writer) {
        this.writer = writer;
        writingStarted = false;
    }

    /*---------*
      * Methods *
      *---------*/

    public RDFFormat getRDFFormat() {
        return RDFFormat.NTRIPLES;
    }

    public void setBaseURI(String baseURI) {
        // ignore, N-Triples doesn't support this
    }

    public void startRDF()
            throws RDFHandlerException {
        if (writingStarted) {
            throw new RuntimeException("Document writing has already started");
        }

        writingStarted = true;
    }

    public void endRDF()
            throws RDFHandlerException {
        if (!writingStarted) {
            throw new RuntimeException("Document writing has not yet started");
        }

        try {
            writer.flush();
        }
        catch (IOException e) {
            throw new RDFHandlerException(e);
        }
        finally {
            writingStarted = false;
        }
    }

    public void handleNamespace(String prefix, String name) {
        // N-Triples does not support namespace prefixes.
    }

    public void handleStatement(Statement st)
            throws RDFHandlerException {
        if (!writingStarted) {
            throw new RuntimeException("Document writing has not yet been started");
        }

        Resource subj = st.getSubject();
        URI pred = st.getPredicate();
        Value obj = st.getObject();

        try {
            // SUBJECT
            writeResource(subj);
            writer.write(" ");

            // PREDICATE
            writeURI(pred);
            writer.write(" ");

            // OBJECT
            if (obj instanceof Resource) {
                writeResource((Resource) obj);
            } else if (obj instanceof Literal) {
                writeLiteral((Literal) obj);
            }

            writer.write(" .");
            writeNewLine();
        }
        catch (IOException e) {
            throw new RDFHandlerException(e);
        }
    }

    public void handleComment(String comment)
            throws RDFHandlerException {
        try {
            writer.write("# ");
            writer.write(comment);
            writeNewLine();
        }
        catch (IOException e) {
            throw new RDFHandlerException(e);
        }
    }

    protected void writeResource(Resource res)
            throws IOException {
        if (res instanceof BNode) {
            writeBNode((BNode) res);
        } else {
            writeURI((URI) res);
        }
    }

    protected void writeURI(URI uri)
            throws IOException {
        writer.write(NTriplesUtil.toNTriplesString(uri));
    }

    private void writeBNode(BNode bNode)
            throws IOException {
        writer.write(NTriplesUtil.toNTriplesString(bNode));
    }

    protected void writeLiteral(Literal lit)
            throws IOException {
        writer.write(NTriplesUtil.toNTriplesString(lit));
    }

    protected void writeNewLine()
            throws IOException {
        writer.write("\n");
	}
}
