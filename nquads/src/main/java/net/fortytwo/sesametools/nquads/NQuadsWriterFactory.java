package net.fortytwo.sesametools.nquads;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;

import java.io.OutputStream;
import java.io.Writer;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class NQuadsWriterFactory implements RDFWriterFactory {
    public RDFFormat getRDFFormat() {
        return NQuadsFormat.NQUADS;
    }

    public RDFWriter getWriter(OutputStream out) {
        return new NQuadsWriter(out);
    }

    public RDFWriter getWriter(Writer writer) {
        return new NQuadsWriter(writer);
    }
}
