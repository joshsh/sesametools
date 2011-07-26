package net.fortytwo.sesametools.sesamize.nquads;

import org.openrdf.rio.RDFHandlerException;
import org.openrdf.model.Statement;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.Literal;

import java.io.OutputStream;
import java.io.Writer;
import java.io.IOException;

/**
 * User: josh
 * Date: Jul 9, 2010
 * Time: 7:18:32 PM
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

        Resource subj = st.getSubject();
        URI pred = st.getPredicate();
        Value obj = st.getObject();
        Resource ctx = st.getContext();

        try {
            // SUBJECT
            writeResource(subj);
            writer.write(" ");

            // PREDICATE
            writeURI(pred);
            writer.write(" ");

            // OBJECT
            if (obj instanceof Resource) {
                writeResource((Resource) obj);
            } else if (obj instanceof Literal) {
                writeLiteral((Literal) obj);
            }

            if (null != ctx) {
                writer.write(" ");
                writeResource(ctx);
            }

            writer.write(" .");
            writeNewLine();
        }
        catch (IOException e) {
            throw new RDFHandlerException(e);
        }
    }
}
