package net.fortytwo.sesametools.nquads;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.RDFParserBase;
import org.openrdf.rio.ntriples.NTriplesUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

/**
 * An implementation of the RDFParser interface that reads RDF documents in
 * N-Triples format. The N-Triples format is defined in <a
 * href="http://www.w3.org/TR/rdf-testcases/#ntriples">this section</a> of the
 * RDF Test Cases document.
 * <p/>
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 * This code has been slightly modified (by Joshua Shinavier) so as to be used with NQuadsParser.
 */
public class ModifiedNTriplesParser extends RDFParserBase {
    /*-----------*
     * Variables *
     *-----------*/

    protected Reader reader;

    protected int lineNo;

    protected Resource subject;

    protected URI predicate;

    protected Value object;

    /*--------------*
     * Constructors *
     *--------------*/

    /**
     * Creates a new NTriplesParser that will use a {@link org.openrdf.model.impl.ValueFactoryImpl} to
     * create object for resources, bNodes and literals.
     */
    public ModifiedNTriplesParser() {
        super();
    }

    /**
     * Creates a new NTriplesParser that will use the supplied
     * <tt>ValueFactory</tt> to create RDF model objects.
     *
     * @param valueFactory A ValueFactory.
     */
    public ModifiedNTriplesParser(ValueFactory valueFactory) {
        super(valueFactory);
    }

    /*---------*
     * Methods *
     *---------*/

    // implements RDFParser.getRDFFormat()

    public RDFFormat getRDFFormat() {
        return RDFFormat.NTRIPLES;
    }

    /**
     * Implementation of the <tt>parse(InputStream, String)</tt> method defined
     * in the RDFParser interface.
     *
     * @param in      The InputStream from which to read the data, must not be
     *                <tt>null</tt>. The InputStream is supposed to contain 7-bit
     *                US-ASCII characters, as per the N-Triples specification.
     * @param baseURI The URI associated with the data in the InputStream, must not be
     *                <tt>null</tt>.
     * @throws java.io.IOException      If an I/O error occurred while data was read from the InputStream.
     * @throws org.openrdf.rio.RDFParseException
     *                                  If the parser has found an unrecoverable parse error.
     * @throws org.openrdf.rio.RDFHandlerException
     *                                  If the configured statement handler encountered an unrecoverable
     *                                  error.
     * @throws IllegalArgumentException If the supplied input stream or base URI is <tt>null</tt>.
     */
    public synchronized void parse(InputStream in, String baseURI)
            throws IOException, RDFParseException, RDFHandlerException {
        if (in == null) {
            throw new IllegalArgumentException("Input stream can not be 'null'");
        }
        // Note: baseURI will be checked in parse(Reader, String)

        try {
            parse(new InputStreamReader(in, "US-ASCII"), baseURI);
        } catch (UnsupportedEncodingException e) {
            // Every platform should support the US-ASCII encoding...
            throw new RuntimeException(e);
        }
    }

    /**
     * Implementation of the <tt>parse(Reader, String)</tt> method defined in the
     * RDFParser interface.
     *
     * @param reader  The Reader from which to read the data, must not be <tt>null</tt>.
     * @param baseURI The URI associated with the data in the Reader, must not be
     *                <tt>null</tt>.
     * @throws java.io.IOException      If an I/O error occurred while data was read from the InputStream.
     * @throws org.openrdf.rio.RDFParseException
     *                                  If the parser has found an unrecoverable parse error.
     * @throws org.openrdf.rio.RDFHandlerException
     *                                  If the configured statement handler encountered an unrecoverable
     *                                  error.
     * @throws IllegalArgumentException If the supplied reader or base URI is <tt>null</tt>.
     */
    public synchronized void parse(Reader reader, String baseURI)
            throws IOException, RDFParseException, RDFHandlerException {
        if (reader == null) {
            throw new IllegalArgumentException("Reader can not be 'null'");
        }
        if (baseURI == null) {
            throw new IllegalArgumentException("base URI can not be 'null'");
        }

        rdfHandler.startRDF();

        this.reader = reader;
        lineNo = 1;

        reportLocation(lineNo, 1);

        try {
            int c = reader.read();
            c = skipWhitespace(c);

            while (c != -1) {
                if (c == '#') {
                    // Comment, ignore
                    c = skipLine(c);
                } else if (c == '\r' || c == '\n') {
                    // Empty line, ignore
                    c = skipLine(c);
                } else {
                    c = parseTriple(c);
                }

                c = skipWhitespace(c);
            }
        } finally {
            clear();
        }

        rdfHandler.endRDF();
    }

    /**
     * Reads characters from reader until it finds a character that is not a
     * space or tab, and returns this last character. In case the end of the
     * character stream has been reached, -1 is returned.
     */
    protected int skipWhitespace(int c)
            throws IOException {
        while (c == ' ' || c == '\t') {
            c = reader.read();
        }

        return c;
    }

    protected int assertLineTerminates(int c) throws IOException, RDFParseException {
        c = reader.read();

        c = skipWhitespace(c);

        if (c != -1 && c != '\r' && c != '\n') {
            reportFatalError("content after '.' is not allowed");
        }

        return c;
    }

    /**
     * Reads characters from reader until the first EOL has been read. The first
     * character after the EOL is returned. In case the end of the character
     * stream has been reached, -1 is returned.
     */
    protected int skipLine(int c)
            throws IOException {
        while (c != -1 && c != '\r' && c != '\n') {
            c = reader.read();
        }

        // c is equal to -1, \r or \n. In case of a \r, we should
        // check whether it is followed by a \n.

        if (c == '\n') {
            c = reader.read();

            lineNo++;

            reportLocation(lineNo, 1);
        } else if (c == '\r') {
            c = reader.read();

            if (c == '\n') {
                c = reader.read();
            }

            lineNo++;

            reportLocation(lineNo, 1);
        }

        return c;
    }

    private int parseTriple(int c)
            throws IOException, RDFParseException, RDFHandlerException {
        c = parseSubject(c);

        c = skipWhitespace(c);

        c = parsePredicate(c);

        c = skipWhitespace(c);

        c = parseObject(c);

        c = skipWhitespace(c);

        if (c == -1) {
            throwEOFException();
        } else if (c != '.') {
            reportFatalError("Expected '.', found: " + (char) c);
        }

        c = skipLine(c);

        Statement st = createStatement(subject, predicate, object);
        rdfHandler.handleStatement(st);

        subject = null;
        predicate = null;
        object = null;

        return c;
    }

    protected int parseSubject(int c)
            throws IOException, RDFParseException {
        StringBuilder sb = new StringBuilder(100);

        // subject is either an uriref (<foo://bar>) or a nodeID (_:node1)
        if (c == '<') {
            // subject is an uriref
            c = parseUriRef(c, sb);
            subject = createURI(sb.toString());
        } else if (c == '_') {
            // subject is a bNode
            c = parseNodeID(c, sb);
            subject = createBNode(sb.toString());
        } else if (c == -1) {
            throwEOFException();
        } else {
            reportFatalError("Expected '<' or '_', found: " + (char) c);
        }

        return c;
    }

    protected int parsePredicate(int c)
            throws IOException, RDFParseException {
        StringBuilder sb = new StringBuilder(100);

        // predicate must be an uriref (<foo://bar>)
        if (c == '<') {
            // predicate is an uriref
            c = parseUriRef(c, sb);
            predicate = createURI(sb.toString());
        } else if (c == -1) {
            throwEOFException();
        } else {
            reportFatalError("Expected '<', found: " + (char) c);
        }

        return c;
    }

    protected int parseObject(int c)
            throws IOException, RDFParseException {
        StringBuilder sb = new StringBuilder(100);

        // object is either an uriref (<foo://bar>), a nodeID (_:node1) or a
        // literal ("foo"-en or "1"^^<xsd:integer>).
        if (c == '<') {
            // object is an uriref
            c = parseUriRef(c, sb);
            object = createURI(sb.toString());
        } else if (c == '_') {
            // object is a bNode
            c = parseNodeID(c, sb);
            object = createBNode(sb.toString());
        } else if (c == '"') {
            // object is a literal
            StringBuilder lang = new StringBuilder(8);
            StringBuilder datatype = new StringBuilder(40);
            c = parseLiteral(c, sb, lang, datatype);
            object = createLiteral(sb.toString(), lang.toString(), datatype.toString());
        } else if (c == -1) {
            throwEOFException();
        } else {
            reportFatalError("Expected '<', '_' or '\"', found: " + (char) c);
        }

        return c;
    }

    protected int parseUriRef(int c, StringBuilder uriRef)
            throws IOException, RDFParseException {
        assert c == '<' : "Supplied char should be a '<', is: " + c;

        // Read up to the next '>' character
        c = reader.read();
        while (c != '>') {
            if (c == -1) {
                throwEOFException();
            }
            uriRef.append((char) c);
            c = reader.read();
        }

        // c == '>', query next char
        c = reader.read();

        return c;
    }

    protected int parseNodeID(int c, StringBuilder name)
            throws IOException, RDFParseException {
        assert c == '_' : "Supplied char should be a '_', is: " + c;

        c = reader.read();
        if (c == -1) {
            throwEOFException();
        } else if (c != ':') {
            reportError("Expected ':', found: " + (char) c);
        }

        c = reader.read();
        if (c == -1) {
            throwEOFException();
        } else if (!NTriplesUtil.isLetter(c)) {
            reportError("Expected a letter, found: " + (char) c);
        }
        name.append((char) c);

        // Read all following letter and numbers, they are part of the name
        c = reader.read();
        while (c != -1 && NTriplesUtil.isLetterOrNumber(c)) {
            name.append((char) c);
            c = reader.read();
        }

        return c;
    }

    private int parseLiteral(int c, StringBuilder value, StringBuilder lang, StringBuilder datatype)
            throws IOException, RDFParseException {
        assert c == '"' : "Supplied char should be a '\"', is: " + c;

        // Read up to the next '"' character
        c = reader.read();
        while (c != '"') {
            if (c == -1) {
                throwEOFException();
            }
            value.append((char) c);

            if (c == '\\') {
                // This escapes the next character, which might be a double quote
                c = reader.read();
                if (c == -1) {
                    throwEOFException();
                }
                value.append((char) c);
            }

            c = reader.read();
        }

        // c == '"', query next char
        c = reader.read();

        if (c == '@') {
            // Read language
            c = reader.read();
            while (c != -1 && c != '.' && c != '^' && c != ' ' && c != '\t') {
                lang.append((char) c);
                c = reader.read();
            }
        } else if (c == '^') {
            // Read datatype
            c = reader.read();

            // c should be another '^'
            if (c == -1) {
                throwEOFException();
            } else if (c != '^') {
                reportError("Expected '^', found: " + (char) c);
            }

            c = reader.read();

            // c should be a '<'
            if (c == -1) {
                throwEOFException();
            } else if (c != '<') {
                reportError("Expected '<', found: " + (char) c);
            }

            c = parseUriRef(c, datatype);
        }

        return c;
    }

    @Override
    protected URI createURI(String uri)
            throws RDFParseException {
        try {
            uri = NTriplesUtil.unescapeString(uri);
        } catch (IllegalArgumentException e) {
            reportError(e.getMessage());
        }

        return super.createURI(uri);
    }

    protected Literal createLiteral(String label, String lang, String datatype)
            throws RDFParseException {
        try {
            label = NTriplesUtil.unescapeString(label);
        } catch (IllegalArgumentException e) {
            reportError(e.getMessage());
        }

        if (lang.length() == 0) {
            lang = null;
        }

        if (datatype.length() == 0) {
            datatype = null;
        }

        URI dtURI = null;
        if (datatype != null) {
            dtURI = createURI(datatype);
        }

        return super.createLiteral(label, lang, dtURI);
    }

    /**
     * Overrides {@link org.openrdf.rio.helpers.RDFParserBase#reportWarning(String)}, adding line number
     * information to the error.
     */
    @Override
    protected void reportWarning(String msg) {
        reportWarning(msg, lineNo, -1);
    }

    /**
     * Overrides {@link org.openrdf.rio.helpers.RDFParserBase#reportError(String)}, adding line number
     * information to the error.
     */
    @Override
    protected void reportError(String msg)
            throws RDFParseException {
        reportError(msg, lineNo, -1);
    }

    /**
     * Overrides {@link org.openrdf.rio.helpers.RDFParserBase#reportFatalError(String)}, adding line
     * number information to the error.
     */
    @Override
    protected void reportFatalError(String msg)
            throws RDFParseException {
        reportFatalError(msg, lineNo, -1);
    }

    /**
     * Overrides {@link org.openrdf.rio.helpers.RDFParserBase#reportFatalError(Exception)}, adding line
     * number information to the error.
     */
    @Override
    protected void reportFatalError(Exception e)
            throws RDFParseException {
        reportFatalError(e, lineNo, -1);
    }

    protected void throwEOFException()
            throws RDFParseException {
        throw new RDFParseException("Unexpected end of file");
    }


}