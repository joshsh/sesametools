package net.fortytwo.sesametools.rdfjson;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParserFactory;

/**
 * Parser Factory for the RDFJSONParser.
 *
 * @author fkleedorfer
 */
public class RDFJSONParserFactory implements RDFParserFactory {

    public RDFParser getParser() {
        return new RDFJSONParser();
    }

    public RDFFormat getRDFFormat() {
        return RDFJSONWriter.RDFJSON_FORMAT;
    }

}
