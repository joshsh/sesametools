package net.fortytwo.sesametools.nquads;

import org.openrdf.rio.RDFFormat;

import java.nio.charset.Charset;

/**
 * Date: Aug 3, 2009
 * Time: 3:25:39 PM
 *
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class NQuadsFormat extends RDFFormat {
    public static NQuadsFormat NQUADS = new NQuadsFormat();

    private NQuadsFormat() {
        // RDFFormat(String name, String mimeType, Charset charset, String fileExtension, boolean supportsNamespaces, boolean supportsContexts)
        super("nquads", "text/x-nquads", Charset.forName("US-ASCII"), "nq", true, true);
    }
}
