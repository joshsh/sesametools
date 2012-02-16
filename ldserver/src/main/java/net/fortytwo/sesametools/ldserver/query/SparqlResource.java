package net.fortytwo.sesametools.ldserver.query;

import org.openrdf.sail.SailException;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

import java.util.Map;

/*
wget "http://localhost:8182/sparql?query=SELECT%20%3Fs%20%3Fp%20%3Fo%20%20WHERE%20%7B%3Fs%20%3Fp%20%3Fo%7D%20limit%2010"

wget --header "Accept: application/sparql-results+json" "http://localhost:8182/sparql?query=SELECT%20%3Fs%20%3Fp%20%3Fo%20%20WHERE%20%7B%3Fs%20%3Fp%20%3Fo%7D%20limit%2010"

wget --header "Accept: application/sparql-results+xml" "http://localhost:8182/sparql?query=SELECT%20%3Fs%20%3Fp%20%3Fo%20%20WHERE%20%7B%3Fs%20%3Fp%20%3Fo%7D%20limit%2010"

wget "http://localhost:8182/sparql?query=SELECT%20%3Fs%20%3Fp%20%3Fo%20%20WHERE%20%7B%3Fs%20%3Fp%20%3Fo%7D%20limit%2010&output=json"

wget "http://localhost:8182/sparql?query=SELECT%20%3Fs%20%3Fp%20%3Fo%20%20WHERE%20%7B%3Fs%20%3Fp%20%3Fo%7D%20limit%2010&output=xml"
 */

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class SparqlResource extends QueryResource {
    private String query;

    public SparqlResource(final Context context,
                          final Request request,
                          final Response response) throws Exception {
        super(context, request, response);

        try {
            if (request.getMethod() == Method.POST) {
                System.out.println("it's a POST");
                String type = request.getEntity().getMediaType().toString();
                String entity = request.getEntity().getText();
                System.out.println("\t" + entity);

                if (type.equals("application/x-www-form-urlencoded")) {
                    arguments = parseParams(entity);
                } else if (type.equals("application/sparql-query")) {
                    query = entity;
                } else {
                    throw new IllegalArgumentException("POST entity has unsupported media type for SPARQL");
                }
            }

            for (Map.Entry<String, String> e : arguments.entrySet()) {
                System.out.println("\t" + e.getKey() + ": " + e.getValue());
            }

            if (null == query) {
                query = arguments.get("query");
            }

            if (null == query) {
                throw new IllegalArgumentException("no query argument specified");
            }
            //System.out.println("query = " + query);

            String output = arguments.get("output");

            // If an "output" argument is provided, use it.
            if (null != output) {
                if (output.equals("json")) {
                    getVariants().add(new Variant(SparqlTools.SparqlResultFormat.JSON.getMediaType()));
                } else if (output.equals("xml")) {
                    getVariants().add(new Variant(SparqlTools.SparqlResultFormat.XML.getMediaType()));
                } else {
                    throw new IllegalArgumentException("bad value for 'output' parameter: " + output);
                }
            } else {
                getVariants().addAll(SparqlTools.SparqlResultFormat.getVariants());
            }
            System.out.println("A");
            System.out.flush();
        } catch (Throwable t) {
            // TODO: use logging instead
            t.printStackTrace(System.err);
            System.err.flush();
            throw new Exception(t);
        }
    }

    @Override
    public Representation represent(final Variant variant) throws ResourceException {
        try {
            System.out.println("a");
            System.out.flush();
            return new SparqlQueryRepresentation(query, sail, readLimit(), variant.getMediaType());
        } catch (QueryException e) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
        } catch (SailException e) {
            // TODO: use logging instead
            e.printStackTrace(System.err);
            System.err.flush();
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
        } catch (Throwable e) {
            // TODO: use logging instead
            e.printStackTrace(System.err);
            System.err.flush();
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
        }
    }
}