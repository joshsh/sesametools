package net.fortytwo.sesametools.nquads;

import org.openrdf.rio.RDFParserFactory;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class NQuadsParserFactory implements RDFParserFactory {
    public RDFFormat getRDFFormat() {
        return RDFFormat.NQUADS;
    }

    public RDFParser getParser() {
        return new NQuadsParser();
    }
}
