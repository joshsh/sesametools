
package net.fortytwo.sesametools;

import org.openrdf.model.*;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

/**
 * Author: josh
 * Date: Jan 28, 2008
 * Time: 3:09:30 PM
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
