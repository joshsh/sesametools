package net.fortytwo.sesametools.nquads;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.StatementCollector;

import java.io.Reader;
import java.io.StringReader;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class NQuadsTest {
    private static final String
            DOC1 = "<http://xmlns.com/foaf/0.1/Person> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Class> <urn:uuid:60d3626a-b65b-4841-bf07-4fc24ebf368b> .\n" +
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
            "<urn:uuid:60d3626a-b65b-4841-bf07-4fc24ebf368b> <http://fortytwo.net/2008/01/webclosure#memo> \"status=Success; timestamp=2011-06-01T11:28:03\" <urn:uuid:ec73a5e3-bfbf-4542-b833-ca2f0f06bbb8> .",

    DOC2 = "<http://tinkerpop.com#1> <http://tinkerpop.com#knows> <http://tinkerpop.com#2> <http://tinkerpop.com#7> .\n" +
            "<http://tinkerpop.com#1> <http://tinkerpop.com#knows> <http://tinkerpop.com#4> <http://tinkerpop.com#8> .\n" +
            "<http://tinkerpop.com#1> <http://tinkerpop.com#created> <http://tinkerpop.com#3> <http://tinkerpop.com#9> .\n" +
            "<http://tinkerpop.com#4> <http://tinkerpop.com#created> <http://tinkerpop.com#3> <http://tinkerpop.com#11> .\n" +
            "<http://tinkerpop.com#4> <http://tinkerpop.com#created> <http://tinkerpop.com#5> <http://tinkerpop.com#10> .\n" +
            "<http://tinkerpop.com#6> <http://tinkerpop.com#created> <http://tinkerpop.com#3> <http://tinkerpop.com#12> .\n" +
            "<http://tinkerpop.com#1> <http://tinkerpop.com#name> \"marko\"^^<http://www.w3.org/2001/XMLSchema#string> <http://tinkerpop.com#graph> .\n" +
            "<http://tinkerpop.com#1> <http://tinkerpop.com#age> \"29\"^^<http://www.w3.org/2001/XMLSchema#int> <http://tinkerpop.com#graph> .\n" +
            "<http://tinkerpop.com#2> <http://tinkerpop.com#name> \"vadas\"^^<http://www.w3.org/2001/XMLSchema#string> <http://tinkerpop.com#graph> .\n" +
            "<http://tinkerpop.com#2> <http://tinkerpop.com#age> \"27\"^^<http://www.w3.org/2001/XMLSchema#int> <http://tinkerpop.com#graph> .\n" +
            "<http://tinkerpop.com#3> <http://tinkerpop.com#name> \"lop\"^^<http://www.w3.org/2001/XMLSchema#string> <http://tinkerpop.com#graph> .\n" +
            "<http://tinkerpop.com#3> <http://tinkerpop.com#lang> \"java\"^^<http://www.w3.org/2001/XMLSchema#string> <http://tinkerpop.com#graph> .\n" +
            "<http://tinkerpop.com#4> <http://tinkerpop.com#name> \"josh\"^^<http://www.w3.org/2001/XMLSchema#string> <http://tinkerpop.com#graph> .\n" +
            "<http://tinkerpop.com#4> <http://tinkerpop.com#age> \"32\"^^<http://www.w3.org/2001/XMLSchema#int> <http://tinkerpop.com#graph> .\n" +
            "<http://tinkerpop.com#5> <http://tinkerpop.com#name> \"ripple\"^^<http://www.w3.org/2001/XMLSchema#string> <http://tinkerpop.com#graph> .\n" +
            "<http://tinkerpop.com#5> <http://tinkerpop.com#lang> \"java\"^^<http://www.w3.org/2001/XMLSchema#string> <http://tinkerpop.com#graph> .\n" +
            "<http://tinkerpop.com#6> <http://tinkerpop.com#name> \"peter\"^^<http://www.w3.org/2001/XMLSchema#string> <http://tinkerpop.com#graph> .\n" +
            "<http://tinkerpop.com#6> <http://tinkerpop.com#age> \"35\"^^<http://www.w3.org/2001/XMLSchema#int> <http://tinkerpop.com#graph> .\n" +
            "<http://tinkerpop.com#7> <http://tinkerpop.com#weight> \"0.5\"^^<http://www.w3.org/2001/XMLSchema#float> <http://tinkerpop.com#graph> .\n" +
            "<http://tinkerpop.com#8> <http://tinkerpop.com#weight> \"1.0\"^^<http://www.w3.org/2001/XMLSchema#float> <http://tinkerpop.com#graph> .\n" +
            "<http://tinkerpop.com#9> <http://tinkerpop.com#weight> \"0.4\"^^<http://www.w3.org/2001/XMLSchema#float> <http://tinkerpop.com#graph> .\n" +
            "<http://tinkerpop.com#10> <http://tinkerpop.com#weight> \"1.0\"^^<http://www.w3.org/2001/XMLSchema#float> <http://tinkerpop.com#graph> .\n" +
            "<http://tinkerpop.com#11> <http://tinkerpop.com#weight> \"0.4\"^^<http://www.w3.org/2001/XMLSchema#float> <http://tinkerpop.com#graph> .\n" +
            "<http://tinkerpop.com#12> <http://tinkerpop.com#weight> \"0.2\"^^<http://www.w3.org/2001/XMLSchema#float> <http://tinkerpop.com#graph> .";

    // "Manual" test
    @Test
    public void testAll() throws Exception {
        NQuadsParser p = new NQuadsParser();

        //RDFWriter w = new NQuadsWriter(System.out);
        RDFHandler w = new StatementCollector();
        p.setRDFHandler(w);

        Reader in = new StringReader(DOC1);
        try {
            p.parse(in, "");
        } finally {
            in.close();
        }
    }

    // "Manual" test
    @Test
    public void testToOtherFormat() throws Exception {
        NQuadsParser p = new NQuadsParser();

        //RDFWriter w = Rio.createWriter(RDFFormat.TRIG, System.out);
        RDFHandler w = new StatementCollector();
        p.setRDFHandler(w);

        Reader in = new StringReader(DOC2);
        try {
            p.parse(in, "");
        } finally {
            in.close();
        }
    }

    @Test
    public void testInvalid() throws Exception {
        NQuadsParser p = new NQuadsParser();

        StatementCollector h = new StatementCollector();

        p.setRDFHandler(h);

        // this line is invalid
        Reader testInput = new StringReader("<http://www.wrong.com> <http://wrong.com/1.1/tt> \"x\"^^<http://xxx.net/int> . <http://path.to.graph>");

        try {
            p.parse(testInput, "");
            Assert.fail("Did not receive expected parse exception");
        } catch (RDFParseException rdfpe) {
            Assert.assertEquals(1, rdfpe.getLineNumber());
        }

        // verify that no statements were given to the RDFHandler
        Assert.assertEquals(0, h.getStatements().size());
    }


}
