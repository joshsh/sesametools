package net.fortytwo.sesametools.jsonld;

import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class JSONLDWriter implements RDFWriter {
    @Override
    public RDFFormat getRDFFormat() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void startRDF() throws RDFHandlerException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void endRDF() throws RDFHandlerException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void handleNamespace(String s, String s1) throws RDFHandlerException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void handleStatement(Statement statement) throws RDFHandlerException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void handleComment(String s) throws RDFHandlerException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
