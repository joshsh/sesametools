package net.fortytwo.sesametools.nquads;

import java.io.OutputStream;
import java.io.Writer;

import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;

/**
 * Date: Oct 4, 2010
 * Time: 2:52:31 PM
 *
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
