package net.fortytwo.sesametools.ldserver.query;

import org.openrdf.sail.SailException;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import java.util.Map;

/*
wget "http://localhost:8182/sparql?query=SELECT%20%3Fs%20%3Fp%20%3Fo%20%20WHERE%20%7B%3Fs%20%3Fp%20%3Fo%7D%20limit%2010"

wget --header "Accept: application/sparql-results+json" "http://localhost:8182/sparql?query=SELECT%20%3Fs%20%3Fp%20%3Fo%20%20WHERE%20%7B%3Fs%20%3Fp%20%3Fo%7D%20limit%2010"

wget --header "Accept: application/sparql-results+xml" "http://localhost:8182/sparql?query=SELECT%20%3Fs%20%3Fp%20%3Fo%20%20WHERE%20%7B%3Fs%20%3Fp%20%3Fo%7D%20limit%2010"

wget "http://localhost:8182/sparql?query=SELECT%20%3Fs%20%3Fp%20%3Fo%20%20WHERE%20%7B%3Fs%20%3Fp%20%3Fo%7D%20limit%2010&output=json"

wget "http://localhost:8182/sparql?query=SELECT%20%3Fs%20%3Fp%20%3Fo%20%20WHERE%20%7B%3Fs%20%3Fp%20%3Fo%7D%20limit%2010&output=xml"
 */

/**
 * A RESTful resource serving as a SPARQL endpoint.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class SparqlResource extends QueryResource {

    @Override
    public void handle(final Request request,
                       final Response response) {
        MediaType mt;
        String query = null;
        Map<String, String> arguments;

        try {
            arguments = getArguments(request);

            if (request.getMethod() == Method.POST) {
                String type = request.getEntity().getMediaType().toString();
                String ent = request.getEntity().getText();

                if (type.equals("application/x-www-form-urlencoded")) {
                    arguments = parseParams(ent);
                } else if (type.equals("application/sparql-query")) {
                    query = ent;
                } else {
                    throw new IllegalArgumentException("POST entity has unsupported media type for SPARQL");
                }
            }

            //for (Map.Entry<String, String> e : arguments.entrySet()) {
            //    System.out.println("\t" + e.getKey() + ": " + e.getValue());
            //}

            if (null == query) {
                query = arguments.get("query");
            }

            if (null == query) {
                throw new IllegalArgumentException("no query argument specified");
            }

            String output = arguments.get("output");

            // If an "output" argument is provided, use it.
            if (null != output) {
                if (output.equals("json")) {
                    mt = SparqlTools.SparqlResultFormat.JSON.getMediaType();
                } else if (output.equals("xml")) {
                    mt = SparqlTools.SparqlResultFormat.XML.getMediaType();
                } else {
                    throw new IllegalArgumentException("bad value for 'output' parameter: " + output);
                }
            } else {
                mt = SparqlTools.SparqlResultFormat.getVariants().get(0).getMediaType();
            }
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            throw new ResourceException(t);
        }

        try {
            response.setEntity(new SparqlQueryRepresentation(query, sail, readLimit(arguments), mt));
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