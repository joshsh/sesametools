package net.fortytwo.sesametools.rdfjson;

import org.openrdf.rio.RDFFormat;

import java.nio.charset.Charset;

/**
 * User: josh
 * Date: 6/28/11
 * Time: 6:53 PM
 */
public class RDFJSONFormat extends RDFFormat {
    public static RDFJSONFormat RDFJSON = new RDFJSONFormat();

    private RDFJSONFormat() {
        super("RDF/JSON",
                "application/json",  // TODO: has a more specific MIME type been suggested for RDF/JSON?
                Charset.forName("UTF-8"),  // See section 3 of the JSON RFC: http://www.ietf.org/rfc/rfc4627.txt
                "json",
                false,  // namespaces are not supported
                true);  // contexts are supported
    }
}
