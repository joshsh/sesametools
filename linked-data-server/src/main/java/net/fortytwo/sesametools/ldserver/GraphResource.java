package net.fortytwo.sesametools.ldserver;

import info.aduna.iteration.CloseableIteration;
import org.openrdf.model.Namespace;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Graph resources are information resources which (in the present schema) do not use suffixes identifying the RDF
 * format (e.g. .rdf or .ttl).  Instead, they use content negotiation to serve an appropriate representation against
 * the URI of the graph, without redirection.
 * <p>
 * This conforms to the common expectation that RDF documents and corresponding named graphs have the same URI.
 * </p>
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class GraphResource extends ServerResource {
    private static final Logger LOGGER = Logger.getLogger(GraphResource.class.getName());

    protected String selfURI;

    protected Sail sail;

    public GraphResource() {
    	super();

        getVariants().addAll(RDFMediaTypes.getRDFVariants());

        sail = LinkedDataServer.getInstance().getSail();
    }

    @Override
    @Get
    public Representation get(final Variant entity) {
        MediaType type = entity.getMediaType();
        RDFFormat format = RDFMediaTypes.findRdfFormat(type);
        selfURI = this.getRequest().getResourceRef().toString();

        /*
        System.out.println("selfURI = " + selfURI);
        System.out.println("baseRef = " + request.getResourceRef().getBaseRef());
        System.out.println("host domain = " + request.getResourceRef().getHostDomain());
        System.out.println("host identifier = " + request.getResourceRef().getHostIdentifier());
        System.out.println("hierarchical part = " + request.getResourceRef().getHierarchicalPart());
        System.out.println("host ref = " + request.getHostRef().toString());
        //*/
        
        try {
            URI subject = sail.getValueFactory().createURI(selfURI);
            return getRDFRepresentation(subject, format);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    private void addStatementsInGraph(final org.openrdf.model.Resource graph,
                                      final Collection<Statement> statements,
                                      final SailConnection c) throws SailException {
        CloseableIteration<? extends Statement, SailException> stIter
                = c.getStatements(null, null, null, false, graph);
        try {
            while (stIter.hasNext()) {
                statements.add(stIter.next());
            }
        } finally {
            stIter.close();
        }
    }

    private Representation getRDFRepresentation(final URI graph,
                                                final RDFFormat format) {
        try {
            Collection<Namespace> namespaces = new LinkedList<Namespace>();
            Collection<Statement> statements = new LinkedList<Statement>();

            SailConnection c = sail.getConnection();
            try {
                // Note: do NOT add graph or document metadata, as this document is to contain only those statements
                // asserted in the graph in question.

                // Add statements in this graph, preserving the graph component of the statements.
                addStatementsInGraph(graph, statements, c);

                // Select namespaces, for human-friendliness
                CloseableIteration<? extends Namespace, SailException> ns
                        = c.getNamespaces();
                try {
                    while (ns.hasNext()) {
                        namespaces.add(ns.next());
                    }
                } finally {
                    ns.close();
                }
            } finally {
                c.close();
            }
            return new RDFRepresentation(statements, namespaces, format);

        } catch (Throwable t) {
            // TODO: put this in the logger message
            t.printStackTrace();

            LOGGER.log(Level.WARNING, "failed to create RDF representation", t);
            return null;
        }
    }
}