package net.fortytwo.sesametools.rdfjson;

import java.io.OutputStream;
import java.io.Writer;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;

/**
 * Writer Factory for the RDFJSONWriter.
 *
 * @author fkleedorfer
 */
public class RDFJSONWriterFactory implements RDFWriterFactory {

    public RDFFormat getRDFFormat() {
        return RDFJSONWriter.RDFJSON_FORMAT;
    }

    public RDFWriter getWriter(OutputStream out) {
        return new RDFJSONWriter(out);
    }

    public RDFWriter getWriter(Writer writer) {
        return new RDFJSONWriter(writer);
    }

}
