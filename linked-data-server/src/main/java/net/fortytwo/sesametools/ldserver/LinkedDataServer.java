package net.fortytwo.sesametools.ldserver;

import net.fortytwo.sesametools.mappingsail.MappingSail;
import net.fortytwo.sesametools.mappingsail.MappingSchema;
import net.fortytwo.sesametools.mappingsail.RewriteRule;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.sail.Sail;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.data.Protocol;
import org.restlet.routing.VirtualHost;

/**
 * A RESTful web service which publishes the contents of a Sail data store as Linked Data.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class LinkedDataServer {

    private final Sail sail;
    private final Component component;
    private final URI datasetURI;
    //private final Router router;
    private final Context context;
    private final VirtualHost host;

    private static LinkedDataServer singleton = null;

    /**
     * @param baseSail        the data store published by this server
     * @param internalBaseURI the base URI of resources within the data store
     * @param externalBaseURI the base URI of resources as they are to be seen in the Linked Data
     * @param serverPort      the TCP port through which to make the data accessible
     */
    public LinkedDataServer(final Sail baseSail,
                            final String internalBaseURI,
                            final String externalBaseURI,
                            final int serverPort) {
        this(baseSail, internalBaseURI, externalBaseURI, serverPort, null);
    }

    /**
     * @param baseSail        the data store published by this server
     * @param internalBaseURI the base URI of resources within the data store
     * @param externalBaseURI the base URI of resources as they are to be seen in the Linked Data
     * @param serverPort      the TCP port through which to make the data accessible
     * @param dataset         the URI of the data set to be published.
     *                        This allows resource descriptions to be associated with metadata about the data set which contains them.
     */
    public LinkedDataServer(final Sail baseSail,
                            final String internalBaseURI,
                            final String externalBaseURI,
                            final int serverPort,
                            final String dataset) {
        if (null != singleton) {
            throw new IllegalStateException("only one LinkedDataServer may be instantiated in the same JVM");
        }

        singleton = this;

        final ValueFactory vf = baseSail.getValueFactory();

        if (!internalBaseURI.equals(externalBaseURI)) {
            RewriteRule outboundRewriter = new RewriteRule() {
                public URI rewrite(final URI original) {
                    //System.out.println("outbound: " + original);

                    if (null == original) {
                        return null;
                    } else {
                        String s = original.stringValue();
                        //System.out.println("\t--> " + (s.startsWith(internalBaseURI)
                        //        ? vf.createURI(s.replace(internalBaseURI, externalBaseURI))
                        //        : original));
                        return s.startsWith(internalBaseURI)
                                ? vf.createURI(s.replace(internalBaseURI, externalBaseURI))
                                : original;
                    }
                }
            };

            RewriteRule inboundRewriter = new RewriteRule() {
                public URI rewrite(final URI original) {
                    //System.out.println("inbound: " + original);
                    if (null == original) {
                        return null;
                    } else {
                        String s = original.stringValue();
                        //System.out.println("\t--> " + (s.startsWith(externalBaseURI)
                        //        ? vf.createURI(s.replace(externalBaseURI, internalBaseURI))
                        //        : original));
                        return s.startsWith(externalBaseURI)
                                ? vf.createURI(s.replace(externalBaseURI, internalBaseURI))
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
                    : outboundRewriter.rewrite(vf.createURI(dataset));
        } else {
            this.sail = baseSail;
            datasetURI = null == dataset
                    ? null
                    : vf.createURI(dataset);
        }

        // Create a new Restlet component and add a HTTP server connector to it
        component = new Component();
        component.getServers().add(Protocol.HTTP, serverPort);
        component.getClients().add(Protocol.FILE);

        //component.getContext().getAttributes().put(SERVER_ATTR, this);

        host = component.getDefaultHost();

        //router = new Router(component.getContext());
        context = component.getContext();
    }

    /*
    public Router getRouter() {
        return router;
    }*/

    public Context getContext() {
        return context;
    }

    /*
    public Component getComponent() {
        return component;
    }
    */

    public VirtualHost getHost() {
        return host;
    }

    public void start() throws Exception {
        component.start();
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
    public URI getDatasetURI() {
        return datasetURI;
    }

    /**
     * @return the single Linked Data server
     */
    public static LinkedDataServer getInstance() {
        return singleton;
    }
}
