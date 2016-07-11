package net.fortytwo.sesametools.ldserver.query;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import java.util.Map;

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

                switch (type) {
                    case "application/x-www-form-urlencoded":
                        arguments = parseParams(ent);
                        break;
                    case "application/sparql-query":
                        query = ent;
                        break;
                    default:
                        throw new IllegalArgumentException("POST entity has unsupported media type for SPARQL");
                }
            }

            if (null == query) {
                query = arguments.get("query");
            }

            if (null == query) {
                throw new IllegalArgumentException("no query argument specified");
            }

            String output = arguments.get("output");

            // If an "output" argument is provided, use it.
            if (null != output) {
                switch (output) {
                    case "json":
                        mt = SparqlTools.SparqlResultFormat.JSON.getMediaType();
                        break;
                    case "xml":
                        mt = SparqlTools.SparqlResultFormat.XML.getMediaType();
                        break;
                    default:
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
        } catch (Throwable e) {
            // TODO: use logging instead
            e.printStackTrace(System.err);
            System.err.flush();
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
        }
    }
}
