package net.fortytwo.sesametools.nquads;

import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.ntriples.NTriplesUtil;
import org.openrdf.model.Statement;

import java.io.OutputStream;
import java.io.Writer;
import java.io.IOException;

/**
 * RDFWriter implementation for the N-Quads RDF format.
 *
 * @author Joshua Shinavier (http://fortytwo.net).  Builds on code by Aduna.
 */
public class NQuadsWriter extends ModifiedNTriplesWriter {
    public NQuadsWriter(OutputStream outputStream) {
        super(outputStream);
    }

    public NQuadsWriter(Writer writer) {
        super(writer);
    }

    @Override
    public void handleStatement(Statement st)
            throws RDFHandlerException {
        if (!writingStarted) {
            throw new RuntimeException("Document writing has not yet been started");
        }

        try {
            // SUBJECT
            NTriplesUtil.append(st.getSubject(), writer);
            writer.write(" ");

            // PREDICATE
            NTriplesUtil.append(st.getPredicate(), writer);
            writer.write(" ");

            // OBJECT
            NTriplesUtil.append(st.getObject(), writer);

            if (null != st.getContext()) {
                writer.write(" ");
                NTriplesUtil.append(st.getContext(), writer);
            }

            writer.write(" .\n");
        } catch (IOException e) {
            throw new RDFHandlerException(e);
        }
    }
}
