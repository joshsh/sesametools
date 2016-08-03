
package net.fortytwo.sesametools;

import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;

/**
 * An <code>RDFHandler</code> which forces each received statement into a designated Named Graph context.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class SingleContextHandler implements RDFHandler {
    private RDFHandler baseHandler;
    private ValueFactory valueFactory;
    private Resource context;

    public SingleContextHandler(final RDFHandler base, final ValueFactory vf, final Resource singleContext) {
        this.baseHandler = base;
        this.valueFactory = vf;
        this.context = singleContext;
    }

    public void startRDF() throws RDFHandlerException {
        baseHandler.startRDF();
    }

    public void endRDF() throws RDFHandlerException {
        baseHandler.endRDF();
    }

    public void handleNamespace(final String prefix, final String uri) throws RDFHandlerException {
        baseHandler.handleNamespace(prefix, uri);
    }

    public void handleStatement(final Statement st) throws RDFHandlerException {
        Resource subj = st.getSubject();
        IRI pred = st.getPredicate();
        Value obj = st.getObject();

        Statement newSt = valueFactory.createStatement(subj, pred, obj, context);

        baseHandler.handleStatement(newSt);
    }

    public void handleComment(final String comment) throws RDFHandlerException {
        baseHandler.handleComment(comment);
    }
}
