package net.fortytwo.sesametools.ldserver;

import net.fortytwo.sesametools.SesameTools;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.restlet.representation.OutputRepresentation;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An RDF document as an HTTP entity.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class RDFRepresentation extends OutputRepresentation {
    private static final Logger logger
            = Logger.getLogger(RDFRepresentation.class.getName());

    private final Collection<Statement> statements;
    private final Collection<Namespace> namespaces;
    private final RDFFormat format;

    public RDFRepresentation(final Collection<Statement> statements,
                             final Collection<Namespace> namespaces,
                             final RDFFormat format) {
        super(RDFMediaTypes.findMediaType(format));

        this.statements = statements;
        this.namespaces = namespaces;
        this.format = format;
    }

    @Override
    public void write(final OutputStream os) throws IOException {
        try {
            RDFWriter writer = Rio.createWriter(format, os);
            writer.startRDF();
            try {
                for (Namespace ns : namespaces) {
                    writer.handleNamespace(ns.getPrefix(), ns.getName());
                }
                for (Statement st : statements) {
                    writer.handleStatement(st);
                }
                writer.handleComment("created by LinkedDataServer "
                        + SesameTools.getProperties().getProperty(SesameTools.VERSION_PROP)
                        + " using the Sesame 2 RDF framework");
            } finally {
                writer.endRDF();
            }
        } catch (Throwable t) {
            if (t instanceof IOException) {
                throw (IOException) t;
            } else {
                logger.log(Level.WARNING, "failed to write RDF representation", t);
                throw new IOException(t.getMessage());
            }
        }
    }
}

