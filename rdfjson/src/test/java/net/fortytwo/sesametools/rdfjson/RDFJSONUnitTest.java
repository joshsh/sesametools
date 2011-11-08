/**
 * 
 */
package net.fortytwo.sesametools.rdfjson;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import net.fortytwo.sesametools.StatementComparator;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.BNode;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kmr.scam.rest.util.RDFJSON;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 *
 */
public class RDFJSONUnitTest
{
    private static final Logger log = LoggerFactory.getLogger(RDFJSONUnitTest.class);

    private String testInputFile;
    private String testInput;
    private Writer testWriter;
    private String testOutput;
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        testWriter = new StringWriter();
    }
    
    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
        testInputFile = null;
        testWriter = null;
        testOutput = null;
    }
    
    /**
     * Test method for {@link se.kmr.scam.rest.util.RDFJSON#rdfJsonToGraph(java.lang.String)}.
     * @throws IOException 
     */
    @Test
    public void testRdfJsonToGraph() throws IOException
    {
        testInputFile = "example0.json";
        
        testInput = IOUtils.toString(this.getClass().getResourceAsStream(testInputFile), "utf-8");
        
        Assert.assertNotNull(testInput);
        
        Assert.assertTrue(testInput.length() > 0);
        
        Collection<Statement> rdfJsonToGraph = RDFJSON.rdfJsonToGraph(testInput);
        
        Assert.assertEquals(12, rdfJsonToGraph.size());
    }
    
    /**
     * Test method for {@link se.kmr.scam.rest.util.RDFJSON#graphToRdfJsonPreordered(java.util.Set)}.
     */
    @Test
    @Ignore
    public void testGraphToRdfJsonPreorderedSetOfStatement()
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for {@link se.kmr.scam.rest.util.RDFJSON#graphToRdfJsonPreordered(java.util.Set, java.io.Writer)}.
     * @throws JSONException 
     */
    @Test
    public void testGraphToRdfJsonPreorderedSetOfStatementWriter() throws JSONException
    {
        Set<Statement> testStatements = new TreeSet<Statement>(new StatementComparator());
        ValueFactoryImpl vf = ValueFactoryImpl.getInstance();
        
        BNode testBNode1 = vf.createBNode();
        
        BNode testBNode2 = vf.createBNode();
        
        URI testURI1 = vf.createURI("http://example.org/test/rdf/json/1");
        
        URI testURI2 = vf.createURI("http://my.test.org/rdf/type/2");

        URI testURI3 = vf.createURI("http://example.org/test/rdf/json/3");
        
        URI testURI4 = vf.createURI("http://example.org/test/rdf/json/4");

        URI testURI5 = vf.createURI("http://my.test.org/rdf/type/5");

        testStatements.add(vf.createStatement(testURI1, testURI2, testURI3));
        testStatements.add(vf.createStatement(testURI1, testURI2, testBNode1));
        testStatements.add(vf.createStatement(testURI1, testURI2, testBNode2));

        testStatements.add(vf.createStatement(testURI4, testURI2, testURI3));
        testStatements.add(vf.createStatement(testURI4, testURI2, testBNode2));
        testStatements.add(vf.createStatement(testURI4, testURI2, testBNode1));
        
        testStatements.add(vf.createStatement(testBNode1, testURI5, testBNode2));
        testStatements.add(vf.createStatement(testBNode1, testURI5, testURI1));
        testStatements.add(vf.createStatement(testBNode1, testURI5, testURI4));
        
        log.info("testStatements="+testStatements);
        
        Writer testWriter2 = RDFJSON.graphToRdfJsonPreordered(testStatements, testWriter);
        
        // The returned writer should be the same as the one that was sent in
        Assert.assertEquals(testWriter, testWriter2);
        
        testOutput = testWriter.toString();
        
        Assert.assertTrue(testOutput.length() > 0);
        
        Assert.assertTrue(testOutput.startsWith("{"));
        
        Assert.assertTrue(testOutput.endsWith("}"));
        
        log.info("testOutput="+testOutput);

        // Test that a bnode is the first subject after the opening brace
        Assert.assertTrue(testOutput.startsWith("\"_:", 1));
        
        // NOTE: We never test for the actual blank node id's as they are not stable by design
        
        int nextBrace = testOutput.indexOf("{", 1);
        
        // need at least one character for the blank node identifier after _:
        Assert.assertTrue(nextBrace > 4);
        
        String rdfTypeString = "{\"http://my.test.org/rdf/type/5\":[{";
        int rdfTypeStatement = testOutput.indexOf(rdfTypeString);
        
        // Test that the predicate for testURI5 is the next brace after the blank node by checking to see if they were at the same index
        Assert.assertEquals(rdfTypeStatement, nextBrace);
        
        // The first value should be a blank node identifier
        int firstValue = testOutput.indexOf("\"value\":\"_:");
        
        // check that the first value comes straight after the rdfTypeStatement
        Assert.assertEquals(firstValue, rdfTypeStatement+rdfTypeString.length());
        
        
        // Do a quick check to see if the testOutput is valid JSON
        JSONObject testJSONObject = new JSONObject(testOutput);
        
        Assert.assertNotNull(testJSONObject);
        Assert.assertTrue(testJSONObject.length() > 0);
        Assert.assertTrue(testJSONObject.names().length() > 0);
        Assert.assertTrue(testJSONObject.keys().hasNext());
    }
    
}
