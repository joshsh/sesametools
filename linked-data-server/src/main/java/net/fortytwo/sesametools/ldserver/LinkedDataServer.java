package net.fortytwo.sesametools.ldserver;

import net.fortytwo.sesametools.mappingsail.MappingSail;
import net.fortytwo.sesametools.mappingsail.MappingSchema;
import net.fortytwo.sesametools.mappingsail.RewriteRule;
import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.sail.Sail;
import org.restlet.Application;

/**
 * A RESTful web service which publishes the contents of a Sail data store as Linked Data.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class LinkedDataServer extends Application {

    private final Sail sail;
    private final IRI datasetURI;

    private static LinkedDataServer SINGLETON = null;

    /**
     * @param baseSail        the data store published by this server
     * @param internalBaseURI the base URI of resources within the data store
     * @param externalBaseURI the base URI of resources as they are to be seen in the Linked Data
     */
    public LinkedDataServer(final Sail baseSail,
                            final String internalBaseURI,
                            final String externalBaseURI) {
        this(baseSail, internalBaseURI, externalBaseURI, null);
    }

    /**
     * @param baseSail        the data store published by this server
     * @param internalBaseURI the base URI of resources within the data store
     * @param externalBaseURI the base URI of resources as they are to be seen in the Linked Data
     * @param dataset         the URI of the data set to be published.
     *                        This allows resource descriptions to be associated
     *                        with metadata about the data set which contains them.
     */
    public LinkedDataServer(final Sail baseSail,
                            final String internalBaseURI,
                            final String externalBaseURI,
                            final String dataset) {
        if (null != SINGLETON) {
            throw new IllegalStateException("only one LinkedDataServer may be instantiated per JVM");
        }

        SINGLETON = this;

        final ValueFactory vf = baseSail.getValueFactory();

        if (!internalBaseURI.equals(externalBaseURI)) {
            RewriteRule outboundRewriter = new RewriteRule() {
                public IRI rewrite(final IRI original) {
                    //System.out.println("outbound: " + original);

                    if (null == original) {
                        return null;
                    } else {
                        String s = original.stringValue();
                        //System.out.println("\t--> " + (s.startsWith(internalBaseURI)
                        //        ? vf.createURI(s.replace(internalBaseURI, externalBaseURI))
                        //        : original));
                        return s.startsWith(internalBaseURI)
                                ? vf.createIRI(s.replace(internalBaseURI, externalBaseURI))
                                : original;
                    }
                }
            };

            RewriteRule inboundRewriter = new RewriteRule() {
                public IRI rewrite(final IRI original) {
                    //System.out.println("inbound: " + original);
                    if (null == original) {
                        return null;
                    } else {
                        String s = original.stringValue();
                        //System.out.println("\t--> " + (s.startsWith(externalBaseURI)
                        //        ? vf.createURI(s.replace(externalBaseURI, internalBaseURI))
                        //        : original));
                        return s.startsWith(externalBaseURI)
                                ? vf.createIRI(s.replace(externalBaseURI, internalBaseURI))
                                : original;
                    }
                }
            };

            MappingSchema schema = new MappingSchema();
            schema.setRewriter(MappingSchema.Direction.INBOUND, inboundRewriter);
            schema.setRewriter(MappingSchema.Direction.OUTBOUND, outboundRewriter);
            this.sail = new MappingSail(baseSail, schema);

            datasetURI = null == dataset
                    ? null
                    : outboundRewriter.rewrite(vf.createIRI(dataset));
        } else {
            this.sail = baseSail;
            datasetURI = null == dataset
                    ? null
                    : vf.createIRI(dataset);
        }
    }

    /**
     * @return the data store published by this server
     */
    public Sail getSail() {
        return sail;
    }

    /**
     * @return the internal URI for the data set published by this server
     */
    public IRI getDatasetURI() {
        return datasetURI;
    }

    /**
     * @return the single Linked Data server
     */
    public static LinkedDataServer getInstance() {
        return SINGLETON;
    }
}
