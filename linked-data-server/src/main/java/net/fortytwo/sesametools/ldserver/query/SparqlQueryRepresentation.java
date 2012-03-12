package net.fortytwo.sesametools.ldserver.query;

import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A SPARQL query result as an HTTP entity.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class SparqlQueryRepresentation extends OutputRepresentation {

    private final ByteArrayOutputStream data;

    public SparqlQueryRepresentation(final String query,
                                     final Sail sail,
                                     final int limit,
                                     final MediaType mediaType) throws Exception {
        super(mediaType);

        try {
            data = new ByteArrayOutputStream();
            SailConnection sc = sail.getConnection();
            try {
                SparqlTools.SparqlResultFormat format
                        = SparqlTools.SparqlResultFormat.lookup(this.getMediaType());

                SparqlTools.executeQuery(query, sc, data, limit, format);
            } finally {
                sc.close();
            }
        } catch (Throwable e) {
            // TODO: use logging instead
            e.printStackTrace(System.err);
            throw new Exception(e);
        }
    }

    public void write(final OutputStream out) throws IOException {
        data.writeTo(out);
    }
}
