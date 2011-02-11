package net.fortytwo.sesametools.nquads;

import org.openrdf.rio.RDFParserFactory;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;

/**
 * Date: Oct 4, 2010
 * Time: 2:52:31 PM
 *
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class NQuadsParserFactory implements RDFParserFactory {
    public RDFFormat getRDFFormat() {
        return NQuadsFormat.NQUADS;
    }

    public RDFParser getParser() {
        return new NQuadsParser();
    }
}
