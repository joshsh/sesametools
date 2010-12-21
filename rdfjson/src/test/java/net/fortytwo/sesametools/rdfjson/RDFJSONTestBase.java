package net.fortytwo.sesametools.rdfjson;

import junit.framework.TestCase;
import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

/**
 * User: josh
 * Date: Dec 21, 2010
 * Time: 5:36:35 PM
 */
public abstract class RDFJSONTestBase extends TestCase {
    protected static final ValueFactory vf = new ValueFactoryImpl();

    protected static final String BASE_URI = "http://example.org/base#";
    protected static final URI
            ABOUT = vf.createURI("http://example.org/about"),
            ARTHUR = vf.createURI("http://example.org/Arthur");
    protected static final BNode
            PERSON = vf.createBNode("person"),
            P1 = vf.createBNode("p1");

    protected interface FOAF {
        static final String NAMESPACE = "http://xmlns.com/foaf/0.1/";
        static final URI
                KNOWS = vf.createURI(NAMESPACE + "knows"),
                NAME = vf.createURI(NAMESPACE + "name"),
                PERSON = vf.createURI(NAMESPACE + "Person");
    }

    protected class RDFCollector implements RDFHandler {
        private Graph graph = new GraphImpl();

        public void startRDF() throws RDFHandlerException {
            graph.clear();
        }

        public void endRDF() throws RDFHandlerException {
            // Do nothing.
        }

        public void handleNamespace(String s, String s1) throws RDFHandlerException {
            // Do nothing.
        }

        public void handleStatement(Statement statement) throws RDFHandlerException {
            //System.out.println("received: " + statement);
            //System.out.println("\t" + statement.getSubject().getClass());
            graph.add(statement);
        }

        public void handleComment(String s) throws RDFHandlerException {
            // Do nothing.
        }

        public Graph getGraph() {
            return graph;
        }
    }
}
