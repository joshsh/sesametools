package net.fortytwo.sesametools.jsonld;

import org.apache.stanbol.commons.jsonld.JsonLd;
import org.apache.stanbol.commons.jsonld.JsonLdParser;
import org.apache.stanbol.commons.jsonld.JsonLdResource;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.ParseErrorListener;
import org.openrdf.rio.ParseLocationListener;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class JSONLDParser implements RDFParser {
    private static final String DEFAULT_NAMESPACE = "http://example.org/default-namespace#";

    private ValueFactory valueFactory = new ValueFactoryImpl();
    private RDFHandler rdfHandler;
    private ParseErrorListener parseErrorListener;
    private ParseLocationListener parseLocationListener;
    private boolean verifyData;
    private boolean preserveBNodeIDs;
    private boolean stopAtFirstError;
    private DatatypeHandling datatypeHandling;

    public RDFFormat getRDFFormat() {
        return JSONLDFormat.JSONLD;
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
        JsonLd result;
        try {
            result = JsonLdParser.parse(s);
        } catch (Exception e) {
            throw new RDFParseException(e);
        }

        Collection<Statement> g = toStatements(result);

        if (null == g) {
            throw new RDFParseException("Could not parse JSON-LD graph");
        }

        rdfHandler.startRDF();
        for (Statement statement : g) {
            rdfHandler.handleStatement(statement);
        }
        rdfHandler.endRDF();
    }

    private Collection<Statement> toStatements(final JsonLd j) {
        Map<String, String> preds = new HashMap<String, String>();
        for (Map.Entry<String, String> e : j.getNamespacePrefixMap().entrySet()) {
            preds.put(e.getValue(), e.getKey());
        }

        Collection<Statement> results = new LinkedList<Statement>();

        for (String s : j.getResourceSubjects()) {
            //System.out.println("subject: " + s);
            Resource outSubject = toResource(s);

            JsonLdResource inSubject = j.getResource(s);

            //for (String type : inSubject.getTypes()) {
            //    System.out.println("type: " + type);
            //}

            for (Map.Entry<String, Object> e : inSubject.getPropertyMap().entrySet()) {
                String p = e.getKey();
                //System.out.println("\tpredicate: " + p);
                String p2 = preds.get(p);
                if (null == p2) {
                    p2 = p;
                }
                //System.out.println("\t\tpred: " + p2);
                URI outPredicate = toUri(p2);

                Object o = e.getValue();
                //System.out.println("\t\tobject: " + o + " (" + o.getClass() + ")");
                Value outObject = toValue(o);


                Statement st = valueFactory.createStatement(outSubject, outPredicate, outObject);
                results.add(st);
            }

            //for (Map.Entry<String, String> e : j.getNamespacePrefixMap().entrySet()) {
            //    System.out.println("key: " + e.getKey() + ", value: " + e.getValue());
            //}
        }

        return results;
    }

    private Value toValue(final Object o) {
        if (o instanceof String) {
            // FIXME: this is a crude hack.  How do we determine the type of the object, correctly?
            if (((String) o).startsWith("http://")) {
                return valueFactory.createURI((String) o);
            } else {
                return valueFactory.createLiteral((String) o);
            }
        } else {
            throw new IllegalStateException("objects is of unknown type: " + o);
        }
    }

    private Resource toResource(final String s) {
        if (s.startsWith("_:")) {
            return valueFactory.createBNode(s.substring(2));
        } else {
            return toUri(s);
        }
    }

    private URI toUri(final String s) {
        if (s.startsWith("http://")) {
            return valueFactory.createURI(s);
        } else {
            return valueFactory.createURI(DEFAULT_NAMESPACE + s);
        }
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
