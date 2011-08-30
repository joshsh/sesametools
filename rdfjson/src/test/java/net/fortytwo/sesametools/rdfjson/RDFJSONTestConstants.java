package net.fortytwo.sesametools.rdfjson;

import org.openrdf.model.BNode;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public final class RDFJSONTestConstants {
    protected static final ValueFactory vf = new ValueFactoryImpl();

    protected static final String BASE_URI = "http://example.org/base#";
    protected static final URI
            ABOUT = vf.createURI("http://example.org/about"),
            ARTHUR = vf.createURI("http://example.org/Arthur"),
            GRAPH1 = vf.createURI("http://example.org/graph1");
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
}
