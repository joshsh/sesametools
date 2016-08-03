package net.fortytwo.sesametools.ldserver;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.SailException;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Information and non-information resources are distinguished by the suffix of the resource's URI:
 * information resource URIs end in .rdf or .trig,
 * while non-information resources have no such suffix
 * (and LinkedDataServer will not make statements about such URIs).
 * A request for an information resource is fulfilled with the resource itself.  No content negotiation occurs.
 * A request for a non-information resource is fulfilled with a 303-redirect
 * to an information resource of the appropriate media type.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class WebResource extends ServerResource {
    private static final Logger logger = Logger.getLogger(WebResource.class.getName());

    enum WebResourceCategory {
        INFORMATION_RESOURCE, NON_INFORMATION_RESOURCE
    }

    protected String selfURI;

    protected String hostIdentifier;
    protected String baseRef;
    protected String subjectResourceURI;
    protected String typeSpecificId;
    protected WebResourceCategory webResourceCategory;
    protected Sail sail;
    protected IRI datasetURI;
    protected Optional<RDFFormat> format;

    public WebResource() {
        super();

        getVariants().addAll(RDFMediaTypes.getRDFVariants());
    }

    public void preprocessingHook() {
        // Do nothing unless overridden
    }

    public void postProcessingHook() {
        // Do nothing unless overridden
    }

    @Get
    public Representation get(final Variant variant) {
        selfURI = this.getRequest().getResourceRef().toString();

        int i = selfURI.lastIndexOf(".");
        if (i > 0) {
            format = RDFFormat.matchFileName(selfURI, null);
        }

        if (!format.isPresent()) {
            webResourceCategory = WebResourceCategory.NON_INFORMATION_RESOURCE;
            getVariants().addAll(RDFMediaTypes.getRDFVariants());
        } else {
            webResourceCategory = WebResourceCategory.INFORMATION_RESOURCE;
            getVariants().add(RDFMediaTypes.findVariant(format.get()));

            hostIdentifier = this.getRequest().getResourceRef().getHostIdentifier();
            baseRef = this.getRequest().getResourceRef().getBaseRef().toString();
            subjectResourceURI = selfURI.substring(0, i);
            typeSpecificId = subjectResourceURI.substring(baseRef.length());
            datasetURI = LinkedDataServer.getInstance().getDatasetURI();
            sail = LinkedDataServer.getInstance().getSail();
        }

        MediaType type = variant.getMediaType();

        switch (webResourceCategory) {
            case INFORMATION_RESOURCE:
                return representInformationResource();
            case NON_INFORMATION_RESOURCE:
                return representNonInformationResource(type);
            default:
                throw new IllegalStateException("no such resource type: " + webResourceCategory);
        }
    }

    private Representation representInformationResource() {
        try {
            preprocessingHook();
            IRI subject = sail.getValueFactory().createIRI(subjectResourceURI);
            Representation result = getRDFRepresentation(subject, format.get());
            postProcessingHook();
            return result;
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    private Representation representNonInformationResource(final MediaType type) {
        RDFFormat format = RDFMediaTypes.findRdfFormat(type);
        if (null == format) {
            throw new IllegalStateException("no RDF format for media type " + type);
        }
        String suffix = format.getDefaultFileExtension();
        if (null == suffix) {
            throw new IllegalStateException("no suffix for RDF format " + type);
        }

        getResponse().redirectSeeOther(selfURI + "." + suffix);

        return null;
    }

    private void addIncidentStatements(final org.eclipse.rdf4j.model.Resource vertex,
                                       final Collection<Statement> statements,
                                       final SailConnection c) throws SailException {
        // Select outbound statements
        CloseableIteration<? extends Statement, SailException> stIter
                = c.getStatements(vertex, null, null, false);
        try {
            while (stIter.hasNext()) {
                Statement s = stIter.next();
                if (null == s.getContext()) {
                    statements.add(s);
                }
            }
        } finally {
            stIter.close();
        }

        // Select inbound statements
        stIter = c.getStatements(null, null, vertex, false);
        try {
            while (stIter.hasNext()) {
                Statement s = stIter.next();
                if (null == s.getContext()) {
                    statements.add(s);
                }
            }
        } finally {
            stIter.close();
        }
    }

    // Note: a SPARQL query might be more efficient in some applications
    private void addSeeAlsoStatements(final org.eclipse.rdf4j.model.Resource subject,
                                      final Collection<Statement> statements,
                                      final SailConnection c,
                                      final ValueFactory vf) throws SailException {
        Set<IRI> contexts = new HashSet<>();
        CloseableIteration<? extends Statement, SailException> iter
                = c.getStatements(subject, null, null, false);
        try {
            while (iter.hasNext()) {
                Statement st = iter.next();
                org.eclipse.rdf4j.model.Resource context = st.getContext();

                if (null != context && context instanceof IRI && context.toString().startsWith(hostIdentifier)) {
                    contexts.add((IRI) context);
                }
            }
        } finally {
            iter.close();
        }

        iter = c.getStatements(null, null, subject, false);
        try {
            while (iter.hasNext()) {
                Statement st = iter.next();
                org.eclipse.rdf4j.model.Resource context = st.getContext();

                if (null != context && context instanceof IRI && context.toString().startsWith(hostIdentifier)) {
                    contexts.add((IRI) context);
                }
            }
        } finally {
            iter.close();
        }

        for (IRI r : contexts) {
            statements.add(vf.createStatement(subject, RDFS.SEEALSO, r));
        }
    }

    private void addDocumentMetadata(final Collection<Statement> statements,
                                     final ValueFactory vf) throws SailException {
        // Metadata about the document itself
        IRI docURI = vf.createIRI(selfURI);
        statements.add(vf.createStatement(docURI, RDF.TYPE, vf.createIRI("http://xmlns.com/foaf/0.1/Document")));
        statements.add(vf.createStatement(docURI, RDFS.LABEL,
                vf.createLiteral("" + format.get().getName() + " description of resource '"
                        + typeSpecificId + "'")));

        // Note: we go to the trouble of special-casing the dataset URI, so that
        // it is properly rewritten, along with all other TwitLogic resource
        // URIs (which are rewritten through the Sail).
        if (null != datasetURI) {
            statements.add(vf.createStatement(docURI, RDFS.SEEALSO, datasetURI));
        }
    }

    private String resourceDescriptor() {
        return "resource";
    }

    private Representation getRDFRepresentation(final IRI subject,
                                                final RDFFormat format) {
        try {
            Collection<Namespace> namespaces = new LinkedList<>();
            Collection<Statement> statements = new LinkedList<>();

            SailConnection c = sail.getConnection();
            try {
                // Add statements incident on the resource itself.
                addIncidentStatements(subject, statements, c);

                // Add virtual statements about named graphs.
                addSeeAlsoStatements(subject, statements, c, sail.getValueFactory());

                // Add virtual statements about the document.
                addDocumentMetadata(statements, sail.getValueFactory());

                // Select namespaces, for human-friendliness
                try (CloseableIteration<? extends Namespace, SailException> nsIter = c.getNamespaces()) {
                    while (nsIter.hasNext()) {
                        namespaces.add(nsIter.next());
                    }
                }
            } finally {
                c.close();
            }

            return new RDFRepresentation(statements, namespaces, format);

        } catch (Throwable t) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            t.printStackTrace(new PrintStream(bos));

            logger.log(Level.WARNING,
                    "failed to create RDF representation (stack trace follows)\n" + bos.toString(), t);
            return null;
        }
    }
}
