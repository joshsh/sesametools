/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.fortytwo.sesametools.rdfjson;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParserFactory;

/**
 *
 */
public class RDFJSONParserFactory implements RDFParserFactory {

    public RDFParser getParser() {
        return new RDFJSONParser();
    }

    public RDFFormat getRDFFormat() {
        return RDFJSONWriter.RDFJSON_FORMAT;
    }

}
