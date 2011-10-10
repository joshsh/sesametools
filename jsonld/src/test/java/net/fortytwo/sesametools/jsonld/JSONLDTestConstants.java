package net.fortytwo.sesametools.jsonld;

import org.openrdf.model.BNode;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public final class JSONLDTestConstants {
    protected static final ValueFactory vf = new ValueFactoryImpl();

    protected static final String BASE_URI = "http://example.org/base#";

    protected static final BNode BNODE1 = vf.createBNode("bnode1");

    protected interface FOAF {
        static final String NAMESPACE = "http://xmlns.com/foaf/0.1/";
        static final URI
                HOMEPAGE = vf.createURI(NAMESPACE + "homepage"),
                KNOWS = vf.createURI(NAMESPACE + "knows"),
                NAME = vf.createURI(NAMESPACE + "name"),
                PERSON = vf.createURI(NAMESPACE + "Person");
    }

    protected interface SIOC {
        static final String NAMESPACE = "http://rdfs.org/sioc/ns#";
        static final URI
                AVATAR = vf.createURI(NAMESPACE + "avatar");
    }
}
