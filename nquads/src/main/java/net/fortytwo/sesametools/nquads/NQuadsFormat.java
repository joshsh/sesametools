package net.fortytwo.sesametools.nquads;

import org.openrdf.rio.RDFFormat;

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class NQuadsFormat extends RDFFormat {
    public static NQuadsFormat NQUADS = new NQuadsFormat();

    private NQuadsFormat() {
        // RDFFormat(String name, Collection<String> mimeTypes, Charset charset, Collection<String> fileExtension, boolean supportsNamespaces, boolean supportsContexts)
        // Standard mime type and file extension are first in respective lists http://sw.deri.org/2008/07/n-quads/ 
        // Others added to match Any23 http://developers.any23.org/xref/org/deri/any23/extractor/rdf/NQuadsExtractor.html
        super("nquads", Arrays.asList("text/x-nquads", "text/nquads", "text/n-quads", "text/rdf+nq"), Charset.forName("US-ASCII"), Arrays.asList("nq", "nquad", "nquads"), false, true);
    }
}
