package net.fortytwo.sesametools.jsonld;

import org.openrdf.rio.RDFFormat;

import java.nio.charset.Charset;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class JSONLDFormat extends RDFFormat {
    public static JSONLDFormat JSONLD = new JSONLDFormat();

    public JSONLDFormat() {
        super("JSON-LD",
                "application/json",  // Per the spec as of 2011-09-28 (http://json-ld.org/spec/latest/)
                Charset.forName("UTF-8"),  // See section 3 of the JSON RFC: http://www.ietf.org/rfc/rfc4627.txt
                "json",
                false,  // TODO: namespaces are not supported.  Right?
                false);  // TODO: contexts are not supported.  Right?
    }
}
