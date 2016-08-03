
package net.fortytwo.sesametools;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;

/**
 * An <code>RDFHandler</code> which wraps another handler and ignores calls
 * to <code>startRDF</code> and <code>endRDF</code>,
 * allowing the base handler to be used multiple times.
 * For example, there may be several distinct operations which push RDF statements
 * into a wrapped RDFWriter before the document is terminated.
 * To actually call the base handler's <code>startRDF</code> and <code>endRDF</code> methods,
 * use <code>reallyStartRDF</code> and <code>reallyEndRDF</code>, respectively.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class ReusableRDFHandler implements RDFHandler {
    private RDFHandler baseHandler;

    public ReusableRDFHandler(final RDFHandler base) {
        this.baseHandler = base;
    }

    public void startRDF() throws RDFHandlerException {
    }

    public void endRDF() throws RDFHandlerException {
    }

    public void reallyStartRDF() throws RDFHandlerException {
        baseHandler.startRDF();
    }

    public void reallyEndRDF() throws RDFHandlerException {
        baseHandler.endRDF();
    }

    public void handleNamespace(final String prefix, final String uri) throws RDFHandlerException {
        baseHandler.handleNamespace(prefix, uri);
    }

    public void handleStatement(final Statement st) throws RDFHandlerException {
        baseHandler.handleStatement(st);
    }

    public void handleComment(final String comment) throws RDFHandlerException {
        baseHandler.handleComment(comment);
    }
}
