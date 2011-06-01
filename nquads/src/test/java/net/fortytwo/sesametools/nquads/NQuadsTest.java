package net.fortytwo.sesametools.nquads;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * User: josh
 * Date: 6/1/11
 * Time: 11:34 AM
 */
public class NQuadsTest extends TestCase {
    private static final String TEST_DATA =
            "<http://xmlns.com/foaf/0.1/Person> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> <urn:uuid:60d3626a-b65b-4841-bf07-4fc24ebf368b> .\n" +
            "<http://xmlns.com/foaf/0.1/Person> <http://www.w3.org/2000/01/rdf-schema#label> \"Person\" <urn:uuid:60d3626a-b65b-4841-bf07-4fc24ebf368b> .\n" +
            "<http://xmlns.com/foaf/0.1/Person> <http://www.w3.org/2000/01/rdf-schema#comment> \"A person.\" <urn:uuid:60d3626a-b65b-4841-bf07-4fc24ebf368b> .\n" +
            "<http://xmlns.com/foaf/0.1/Person> <http://www.w3.org/2003/06/sw-vocab-status/ns#term_status> \"stable\" <urn:uuid:60d3626a-b65b-4841-bf07-4fc24ebf368b> .\n" +
            "<http://xmlns.com/foaf/0.1/Person> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> <urn:uuid:60d3626a-b65b-4841-bf07-4fc24ebf368b> .\n" +
            "<http://xmlns.com/foaf/0.1/Person> <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://xmlns.com/foaf/0.1/Agent> <urn:uuid:60d3626a-b65b-4841-bf07-4fc24ebf368b> .\n" +
            "<http://xmlns.com/foaf/0.1/Person> <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://www.w3.org/2000/10/swap/pim/contact#Person> <urn:uuid:60d3626a-b65b-4841-bf07-4fc24ebf368b> .\n" +
            "<http://xmlns.com/foaf/0.1/Person> <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://www.w3.org/2003/01/geo/wgs84_pos#SpatialThing> <urn:uuid:60d3626a-b65b-4841-bf07-4fc24ebf368b> .\n" +
            "<http://xmlns.com/foaf/0.1/Person> <http://www.w3.org/2000/01/rdf-schema#isDefinedBy> <http://xmlns.com/foaf/0.1/> <urn:uuid:60d3626a-b65b-4841-bf07-4fc24ebf368b> .\n" +
            "<http://xmlns.com/foaf/0.1/Person> <http://www.w3.org/2002/07/owl#disjointWith> <http://xmlns.com/foaf/0.1/Organization> <urn:uuid:60d3626a-b65b-4841-bf07-4fc24ebf368b> .\n" +
            "<http://xmlns.com/foaf/0.1/Person> <http://www.w3.org/2002/07/owl#disjointWith> <http://xmlns.com/foaf/0.1/Project> <urn:uuid:60d3626a-b65b-4841-bf07-4fc24ebf368b> .\n" +
            "<http://xmlns.com/foaf/0.1/Organization> <http://www.w3.org/2002/07/owl#disjointWith> <http://xmlns.com/foaf/0.1/Person> <urn:uuid:60d3626a-b65b-4841-bf07-4fc24ebf368b> .\n" +
            "<http://xmlns.com/foaf/0.1/Project> <http://www.w3.org/2002/07/owl#disjointWith> <http://xmlns.com/foaf/0.1/Person> <urn:uuid:60d3626a-b65b-4841-bf07-4fc24ebf368b> .\n" +
            "<http://xmlns.com/foaf/0.1/geekcode> <http://www.w3.org/2000/01/rdf-schema#domain> <http://xmlns.com/foaf/0.1/Person> <urn:uuid:60d3626a-b65b-4841-bf07-4fc24ebf368b> .\n" +
            "<http://xmlns.com/foaf/0.1/firstName> <http://www.w3.org/2000/01/rdf-schema#domain> <http://xmlns.com/foaf/0.1/Person> <urn:uuid:60d3626a-b65b-4841-bf07-4fc24ebf368b> .\n" +
            "<http://xmlns.com/foaf/0.1/lastName> <http://www.w3.org/2000/01/rdf-schema#domain> <http://xmlns.com/foaf/0.1/Person> <urn:uuid:60d3626a-b65b-4841-bf07-4fc24ebf368b> .\n" +
            "<http://xmlns.com/foaf/0.1/surname> <http://www.w3.org/2000/01/rdf-schema#domain> <http://xmlns.com/foaf/0.1/Person> <urn:uuid:60d3626a-b65b-4841-bf07-4fc24ebf368b> .\n" +
            "<http://xmlns.com/foaf/0.1/family_name> <http://www.w3.org/2000/01/rdf-schema#domain> <http://xmlns.com/foaf/0.1/Person> <urn:uuid:60d3626a-b65b-4841-bf07-4fc24ebf368b> .\n" +
            "<http://xmlns.com/foaf/0.1/familyName> <http://www.w3.org/2000/01/rdf-schema#domain> <http://xmlns.com/foaf/0.1/Person> <urn:uuid:60d3626a-b65b-4841-bf07-4fc24ebf368b> .\n" +
            "<http://xmlns.com/foaf/0.1/plan> <http://www.w3.org/2000/01/rdf-schema#domain> <http://xmlns.com/foaf/0.1/Person> <urn:uuid:60d3626a-b65b-4841-bf07-4fc24ebf368b> .\n" +
            "<http://xmlns.com/foaf/0.1/img> <http://www.w3.org/2000/01/rdf-schema#domain> <http://xmlns.com/foaf/0.1/Person> <urn:uuid:60d3626a-b65b-4841-bf07-4fc24ebf368b> .\n" +
            "<http://xmlns.com/foaf/0.1/myersBriggs> <http://www.w3.org/2000/01/rdf-schema#domain> <http://xmlns.com/foaf/0.1/Person> <urn:uuid:60d3626a-b65b-4841-bf07-4fc24ebf368b> .\n" +
            "<http://xmlns.com/foaf/0.1/workplaceHomepage> <http://www.w3.org/2000/01/rdf-schema#domain> <http://xmlns.com/foaf/0.1/Person> <urn:uuid:60d3626a-b65b-4841-bf07-4fc24ebf368b> .\n" +
            "<http://xmlns.com/foaf/0.1/workInfoHomepage> <http://www.w3.org/2000/01/rdf-schema#domain> <http://xmlns.com/foaf/0.1/Person> <urn:uuid:60d3626a-b65b-4841-bf07-4fc24ebf368b> .\n" +
            "<http://xmlns.com/foaf/0.1/schoolHomepage> <http://www.w3.org/2000/01/rdf-schema#domain> <http://xmlns.com/foaf/0.1/Person> <urn:uuid:60d3626a-b65b-4841-bf07-4fc24ebf368b> .\n" +
            "<http://xmlns.com/foaf/0.1/knows> <http://www.w3.org/2000/01/rdf-schema#domain> <http://xmlns.com/foaf/0.1/Person> <urn:uuid:60d3626a-b65b-4841-bf07-4fc24ebf368b> .\n" +
            "<http://xmlns.com/foaf/0.1/knows> <http://www.w3.org/2000/01/rdf-schema#range> <http://xmlns.com/foaf/0.1/Person> <urn:uuid:60d3626a-b65b-4841-bf07-4fc24ebf368b> .\n" +
            "<http://xmlns.com/foaf/0.1/publications> <http://www.w3.org/2000/01/rdf-schema#domain> <http://xmlns.com/foaf/0.1/Person> <urn:uuid:60d3626a-b65b-4841-bf07-4fc24ebf368b> .\n" +
            "<http://xmlns.com/foaf/0.1/currentProject> <http://www.w3.org/2000/01/rdf-schema#domain> <http://xmlns.com/foaf/0.1/Person> <urn:uuid:60d3626a-b65b-4841-bf07-4fc24ebf368b> .\n" +
            "<http://xmlns.com/foaf/0.1/pastProject> <http://www.w3.org/2000/01/rdf-schema#domain> <http://xmlns.com/foaf/0.1/Person> <urn:uuid:60d3626a-b65b-4841-bf07-4fc24ebf368b> .\n" +
            "<urn:uuid:60d3626a-b65b-4841-bf07-4fc24ebf368b> <http://fortytwo.net/2008/01/webclosure#memo> \"status=Success; timestamp=2011-06-01T11:28:03\" <urn:uuid:ec73a5e3-bfbf-4542-b833-ca2f0f06bbb8> .";

    public void testAll() throws Exception {
        NQuadsParser p = new NQuadsParser();

        NQuadsWriter w = new NQuadsWriter(System.out);
        p.setRDFHandler(w);

        InputStream in = new ByteArrayInputStream(TEST_DATA.getBytes());
        try {
            p.parse(in, "");
        } finally {
            in.close();
        }
    }
}
