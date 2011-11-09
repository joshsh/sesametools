/**
 * 
 */
package net.fortytwo.sesametools.rdfjson;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
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

        Statement testStatement1 = vf.createStatement(testURI1, testURI2, testURI3);
        testStatements.add(testStatement1);
        Statement testStatement2 = vf.createStatement(testURI1, testURI2, testBNode1);
        testStatements.add(testStatement2);
        Statement testStatement3 = vf.createStatement(testURI1, testURI2, testBNode2);
        testStatements.add(testStatement3);

        Statement testStatement4 = vf.createStatement(testURI4, testURI2, testURI3);
        testStatements.add(testStatement4);
        Statement testStatement5 = vf.createStatement(testURI4, testURI2, testBNode2);
        testStatements.add(testStatement5);
        Statement testStatement6 = vf.createStatement(testURI4, testURI2, testBNode1);
        testStatements.add(testStatement6);
        
        Statement testStatement7 = vf.createStatement(testBNode1, testURI5, testBNode2);
        testStatements.add(testStatement7);
        Statement testStatement8 = vf.createStatement(testBNode1, testURI5, testURI1);
        testStatements.add(testStatement8);
        Statement testStatement9 = vf.createStatement(testBNode1, testURI5, testURI4);
        testStatements.add(testStatement9);
        
        log.info("testStatements="+testStatements);
        
        Assert.assertEquals(9, testStatements.size());
        
        // Verify that the statements are in an acceptable order (testStatement5 and testStatement6 can be legitimately swapped)
        
        Iterator<Statement> testStatementIterator = testStatements.iterator();
        
        Assert.assertTrue(testStatementIterator.hasNext());
        
        // testStatement7 should always be first by virtue of the fact that it has two blank nodes and no other statements have two blank nodes
        Assert.assertEquals(testStatement7, testStatementIterator.next());
        Assert.assertTrue(testStatementIterator.hasNext());

        // Then testStatement8
        Assert.assertEquals(testStatement8, testStatementIterator.next());
        Assert.assertTrue(testStatementIterator.hasNext());

        // Then testStatement9
        Assert.assertEquals(testStatement9, testStatementIterator.next());
        Assert.assertTrue(testStatementIterator.hasNext());

        Writer testWriter2 = RDFJSON.graphToRdfJsonPreordered(testStatements, testWriter);
        
        // The returned writer should be the same as the one that was sent in
        Assert.assertEquals(testWriter, testWriter2);
        
        testOutput = testWriter.toString();
        
        Assert.assertTrue(testOutput.length() > 0);
        
        Assert.assertTrue(testOutput.startsWith("{"));
        
        Assert.assertTrue(testOutput.endsWith("}"));
        
        log.info("testOutput="+testOutput);

        int firstBlankNode = testOutput.indexOf("\"_:");
        
        // Test that a bnode exists after the opening brace
        Assert.assertTrue(firstBlankNode > 0);
        
        // The first value after the first blank node should be a blank node identifier
        int firstValue = testOutput.indexOf("\"value\":\"_:", firstBlankNode);
        
        Assert.assertTrue("A suitable blank node value was not found", firstValue > 0);
        
        // This should be guaranteed by the indexOf contract, but doing a quick check anyway
        Assert.assertTrue(firstValue > firstBlankNode);
        
        // Do a quick check to see if the testOutput is valid JSON
        JSONObject testJSONObject = new JSONObject(testOutput);
        
        Assert.assertNotNull(testJSONObject);
        Assert.assertTrue(testJSONObject.length() > 0);
        Assert.assertTrue(testJSONObject.names().length() > 0);
        Assert.assertTrue(testJSONObject.keys().hasNext());
    }
    
}
