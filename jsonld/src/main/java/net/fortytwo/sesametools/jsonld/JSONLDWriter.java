package net.fortytwo.sesametools.jsonld;

import org.apache.stanbol.commons.jsonld.JsonLd;
import org.apache.stanbol.commons.jsonld.JsonLdResource;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

/**
 * A writer for the JSON-LD RDF format.
 * This implementation wraps the Stanbol JSON-LD library.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class JSONLDWriter implements RDFWriter {
    private final JsonLd json;

    private final Writer writer;

    public JSONLDWriter(final OutputStream out) {
        this(new OutputStreamWriter(out));
    }

    public JSONLDWriter(final Writer writer) {
        this.writer = writer;
        json = new JsonLd();
    }

    public RDFFormat getRDFFormat() {
        return JSONLDFormat.JSONLD;
    }

    public void startRDF() throws RDFHandlerException {
        // Do nothing.
    }

    public void endRDF() throws RDFHandlerException {
        try {
            writer.write(json.toString());
            writer.flush();
        } catch (IOException e) {
            throw new RDFHandlerException(e);
        }
    }

    public void handleNamespace(final String prefix,
                                final String uri) throws RDFHandlerException {
        json.getNamespacePrefixMap().put(prefix, uri);
    }

    public void handleStatement(final Statement s) throws RDFHandlerException {
        //System.out.println("handling: " + s);
        if (null != s.getContext()) {
            throw new IllegalStateException("this writer can't handle statements with graph context");
        }

        String ss = valueToString(s.getSubject());
        JsonLdResource sr = json.getResource(ss);
        if (null == sr) {
            sr = new JsonLdResource();
            sr.setSubject(ss);
            json.put(sr);
        }

        String ps = valueToString(s.getPredicate());
        String os = valueToString(s.getObject());

        Map<String, Object> props = sr.getPropertyMap();
        props.put(ps, os);
    }

    public void handleComment(final String c) throws RDFHandlerException {
        // Ignore comments.
    }

    private String valueToString(final Value v) {
        if (v instanceof URI) {
            return toString((URI) v);
        } else if (v instanceof Literal) {
            return toString((Literal) v);
        } else if (v instanceof BNode) {
            return toString((BNode) v);
        } else {
            throw new IllegalStateException("value is neither a URI, bnode, or literal: " + v);
        }
    }

    private String toString(final URI uri) {
        return uri.stringValue();
    }

    private String toString(final BNode bnode) {
        return "_:" + bnode.getID();
    }

    private String toString(final Literal lit) {
        if (null == lit.getDatatype() && null == lit.getLanguage()) {
            return lit.getLabel();
        } else {
            throw new IllegalStateException("this writer cannot (yet) handle data-typed literals, nor literals with language tags");
        }
    }
}
