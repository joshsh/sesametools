package net.fortytwo.sesametools.jsonld;

import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;

import static net.fortytwo.sesametools.jsonld.JSONLDTestConstants.BASE_URI;
import static net.fortytwo.sesametools.jsonld.JSONLDTestConstants.vf;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class JSONLDWriterTest {
    @Test
    public void testSimple() throws Exception {
        URI arthur = vf.createURI(BASE_URI + "arthur");
        URI ford = vf.createURI(BASE_URI + "ford");

        RDFWriter w = new JSONLDWriter(System.out);
        w.startRDF();

        add(w, arthur, JSONLDTestConstants.FOAF.KNOWS, ford);
        add(w, arthur, JSONLDTestConstants.FOAF.NAME, vf.createLiteral("Arthur Dent"));

        w.endRDF();
        System.out.flush();
    }

    private void add(final RDFWriter w,
                     final Resource subject,
                     final URI predicate,
                     final Value object) throws RDFHandlerException {
        w.handleStatement(vf.createStatement(subject, predicate, object));
    }
}
