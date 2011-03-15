package net.fortytwo.sesametools.ldserver.query;

import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.restlet.data.MediaType;
import org.restlet.resource.OutputRepresentation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * User: josh
 * Date: Apr 18, 2010
 * Time: 2:51:46 PM
 */
public class SparqlQueryRepresentation extends OutputRepresentation {

    //private final String query;
    //private final Sail sail;
    //private final int limit;

    private final ByteArrayOutputStream data;

    public SparqlQueryRepresentation(final String query,
                                     final Sail sail,
                                     final int limit,
                                     final MediaType mediaType) throws QueryException, SailException {
        super(mediaType);

        //this.query = query;
        //this.sail = sail;
        //this.limit = limit;

        data = new ByteArrayOutputStream();
        SailConnection sc = sail.getConnection();
        try {
            //System.out.println("media type: " + this.getMediaType());
            SparqlTools.SparqlResultFormat format
                    = SparqlTools.SparqlResultFormat.lookup(this.getMediaType());
            SparqlTools.executeQuery(query, sc, data, limit, format);
        } finally {
            sc.close();
        }

    }

    public void write(final OutputStream out) throws IOException {
        data.writeTo(out);

        /*
        //try {
        try {
            SailConnection sc = sail.getConnection();
            try {
                try {
                    //System.out.println("media type: " + this.getMediaType());
                    SparqlTools.SparqlResultFormat format
                            = SparqlTools.SparqlResultFormat.lookup(this.getMediaType());
                    SparqlTools.executeQuery(query, sc, out, limit, format);
                } catch (QueryException e) {
                    e.printStackTrace();
                    throw new IOException(e);
                }
            } finally {
                sc.close();
            }
        } catch (SailException e) {
            e.printStackTrace();
            throw new IOException(e);
        }
        //} catch (Throwable t) {
        //    t.printStackTrace();
        //} */
    }
}
