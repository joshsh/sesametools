/**
 * 
 */
package net.fortytwo.sesametools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 *
 */
public class RdfListUtilTest 
{

	private Resource testSubjectUri1;
	private URI testPredicateUri1;
	private List<Value> testValuesEmpty;
	private Graph testGraph;
	private List<Value> testValuesSingleUri;
	private URI testObjectUri1;
	private List<Value> testValuesMultipleElements;
	private BNode testObjectBNode1;
	private Literal testObjectLiteral1;

	@Before
	public void setUp()
	{
		ValueFactory vf = ValueFactoryImpl.getInstance();
		
		testSubjectUri1 = vf.createURI("http://examples.net/testsubject/1");
		testPredicateUri1 = vf.createURI("http://more.example.org/testpredicate/1");
		
		testObjectUri1 = vf.createURI("http://example.org/testobject/1");
		testObjectBNode1 = vf.createBNode();
		testObjectLiteral1 = vf.createLiteral("testobjectliteral1");
		
		testValuesEmpty = Collections.emptyList();
		
		testValuesSingleUri = new ArrayList<Value>(1);
		testValuesSingleUri.add(testObjectUri1);
		
		testValuesMultipleElements = new ArrayList<Value>(3);
		testValuesMultipleElements.add(testObjectBNode1);
		testValuesMultipleElements.add(testObjectLiteral1);
		testValuesMultipleElements.add(testObjectUri1);
		
		testGraph = new GraphImpl();
	}

	@After
	public void tearDown()
	{
		testSubjectUri1 = null;
		testPredicateUri1 = null;
		testObjectUri1 = null;
		testObjectBNode1 = null;
		testObjectLiteral1 = null;

		testValuesEmpty = null;
		testValuesSingleUri = null;
		testValuesMultipleElements = null;
		
		testGraph = null;
	}
	
	/**
	 * Test method for {@link net.fortytwo.sesametools.RdfListUtil#addList(org.openrdf.model.Resource, org.openrdf.model.URI, java.util.List, org.openrdf.model.Graph, org.openrdf.model.Resource[])}.
	 */
	@Test
	public void testAddListEmptyNoContext() 
	{
		RdfListUtil.addList(testSubjectUri1, testPredicateUri1, testValuesEmpty, testGraph);
		
		Assert.assertEquals(0, testGraph.size());
	}
	
	
	/**
	 * Test method for {@link net.fortytwo.sesametools.RdfListUtil#addList(org.openrdf.model.Resource, org.openrdf.model.URI, java.util.List, org.openrdf.model.Graph, org.openrdf.model.Resource[])}.
	 */
	@Test
	public void testAddListSingleElementNoContext() 
	{
		RdfListUtil.addList(testSubjectUri1, testPredicateUri1, testValuesSingleUri, testGraph);
		
		Assert.assertEquals(3, testGraph.size());
		
		
		// Match the head
		Iterator<Statement> match = testGraph.match(this.testSubjectUri1, this.testPredicateUri1, null);
		
		Assert.assertTrue(match.hasNext());
		
		Statement matchedStatement = match.next();
		
		Assert.assertNotNull(matchedStatement);
		
		Assert.assertFalse(match.hasNext());
		
		Assert.assertTrue(matchedStatement.getObject() instanceof BNode);
		
		
		// match the first element
		Iterator<Statement> matchFirstOthers = testGraph.match((BNode)matchedStatement.getObject(), RDF.FIRST, null);
		
		Assert.assertTrue(matchFirstOthers.hasNext());
		
		Statement firstListMatchedStatement = matchFirstOthers.next();

		Assert.assertNotNull(firstListMatchedStatement);
		
		Assert.assertFalse(matchFirstOthers.hasNext());
		
		Assert.assertTrue(firstListMatchedStatement.getObject() instanceof URI);
		
		Assert.assertEquals(this.testObjectUri1, firstListMatchedStatement.getObject());
		

		// match the rest link, which should be rdf:nil for a single value list
		Iterator<Statement> matchRestNil = testGraph.match((BNode)matchedStatement.getObject(), RDF.REST, null);
		
		Assert.assertTrue(matchRestNil.hasNext());
		
		Statement restListMatchedStatement = matchRestNil.next();

		Assert.assertNotNull(restListMatchedStatement);
		
		Assert.assertFalse(matchRestNil.hasNext());
		
		Assert.assertTrue(restListMatchedStatement.getObject() instanceof URI);
		
		Assert.assertEquals(RDF.NIL, restListMatchedStatement.getObject());

	}

	/**
	 * Test method for {@link net.fortytwo.sesametools.RdfListUtil#addList(org.openrdf.model.Resource, org.openrdf.model.URI, java.util.List, org.openrdf.model.Graph, org.openrdf.model.Resource[])}.
	 */
	@Test
	public void testAddListMultipleElementsNoContext() 
	{
		RdfListUtil.addList(testSubjectUri1, testPredicateUri1, testValuesMultipleElements, testGraph);
		
		Assert.assertEquals(7, testGraph.size());
		
		// Match the head
		Iterator<Statement> headMatch = testGraph.match(this.testSubjectUri1, this.testPredicateUri1, null);
		
		Assert.assertTrue(headMatch.hasNext());
		
		Statement headMatchedStatement = headMatch.next();
		
		Assert.assertNotNull(headMatchedStatement);
		
		Assert.assertFalse(headMatch.hasNext());
		
		Assert.assertTrue(headMatchedStatement.getObject() instanceof BNode);
		
		
		// match the first element, which should be a bnode
		Iterator<Statement> matchFirst1 = testGraph.match((BNode)headMatchedStatement.getObject(), RDF.FIRST, null);
		
		Assert.assertTrue(matchFirst1.hasNext());
		
		Statement firstListMatchedStatement1 = matchFirst1.next();

		Assert.assertNotNull(firstListMatchedStatement1);
		
		Assert.assertFalse(matchFirst1.hasNext());
		
		Assert.assertTrue(firstListMatchedStatement1.getObject() instanceof BNode);
		// TODO: is this check consistent with BlankNode theory?
		Assert.assertEquals(this.testObjectBNode1, firstListMatchedStatement1.getObject());
		

		// match the rest link, which should be a BNode
		Iterator<Statement> matchRest1 = testGraph.match((BNode)headMatchedStatement.getObject(), RDF.REST, null);
		
		Assert.assertTrue(matchRest1.hasNext());
		
		Statement restListMatchedStatement1 = matchRest1.next();

		Assert.assertNotNull(restListMatchedStatement1);
		
		Assert.assertFalse(matchRest1.hasNext());
		
		Assert.assertTrue(restListMatchedStatement1.getObject() instanceof BNode);
		
		
		// match the next first node, which should be a literal
		Iterator<Statement> matchFirst2 = testGraph.match((BNode)restListMatchedStatement1.getObject(), RDF.FIRST, null);

		Assert.assertTrue(matchFirst2.hasNext());
		
		Statement firstListMatchedStatement2 = matchFirst2.next();

		Assert.assertNotNull(firstListMatchedStatement2);
		
		Assert.assertFalse(matchFirst2.hasNext());
		
		Assert.assertTrue(firstListMatchedStatement2.getObject() instanceof Literal);
		
		Assert.assertEquals(this.testObjectLiteral1, firstListMatchedStatement2.getObject());
		

		// match the rest link, which should be a BNode
		Iterator<Statement> matchRest2 = testGraph.match((BNode)restListMatchedStatement1.getObject(), RDF.REST, null);
		
		Assert.assertTrue(matchRest2.hasNext());
		
		Statement restListMatchedStatement2 = matchRest2.next();

		Assert.assertNotNull(restListMatchedStatement2);
		
		Assert.assertFalse(matchRest2.hasNext());
		
		Assert.assertTrue(restListMatchedStatement2.getObject() instanceof BNode);
		
		
		// match the next first node, which should be a URI
		Iterator<Statement> matchFirst3 = testGraph.match((BNode)restListMatchedStatement2.getObject(), RDF.FIRST, null);

		Assert.assertTrue(matchFirst3.hasNext());
		
		Statement firstListMatchedStatement3 = matchFirst3.next();

		Assert.assertNotNull(firstListMatchedStatement3);
		
		Assert.assertFalse(matchFirst3.hasNext());
		
		Assert.assertTrue(firstListMatchedStatement3.getObject() instanceof URI);
		
		Assert.assertEquals(this.testObjectUri1, firstListMatchedStatement3.getObject());
		

		// match the rest link, which should be the URI rdf:nil
		Iterator<Statement> matchRest3 = testGraph.match((BNode)restListMatchedStatement2.getObject(), RDF.REST, null);
		
		Assert.assertTrue(matchRest3.hasNext());
		
		Statement restListMatchedStatement3 = matchRest3.next();

		Assert.assertNotNull(restListMatchedStatement3);
		
		Assert.assertFalse(matchRest3.hasNext());
		
		Assert.assertTrue(restListMatchedStatement3.getObject() instanceof URI);
		
		Assert.assertEquals(RDF.NIL, restListMatchedStatement3.getObject());

	}
	
	/**
	 * Test method for {@link net.fortytwo.sesametools.RdfListUtil#getList(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Graph, org.openrdf.model.Resource)}.
	 */
	@Test
	public void testGetListMultipleElementsNullContext() 
	{
		RdfListUtil.addList(testSubjectUri1, testPredicateUri1, testValuesMultipleElements, testGraph);
		
		Assert.assertEquals(7, testGraph.size());
		
		List<Value> results = RdfListUtil.getList(testSubjectUri1, testPredicateUri1, testGraph, null);
		
		Assert.assertEquals(3, results.size());
		
		Assert.assertTrue(results.contains(testObjectBNode1));
		Assert.assertTrue(results.contains(testObjectLiteral1));
		Assert.assertTrue(results.contains(testObjectUri1));
		
	}
	
	/**
	 * Test method for {@link net.fortytwo.sesametools.RdfListUtil#getList(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Graph, org.openrdf.model.Resource)}.
	 */
	@Test
	public void testGetListAfterInvalidGraphOperation() 
	{
		RdfListUtil.addList(testSubjectUri1, testPredicateUri1, testValuesMultipleElements, testGraph);
		
		Assert.assertEquals(7, testGraph.size());
		
		// Modify the graph in an invalid way to test getList
		Iterator<Statement> matches = testGraph.match(null, RDF.REST, RDF.NIL);
		
		Assert.assertTrue(matches.hasNext());
		
		Statement matchedStatement = matches.next();
		
		Assert.assertFalse(matches.hasNext());
		
		Assert.assertTrue(testGraph.remove(matchedStatement));
		
		try
		{
			@SuppressWarnings("unused")
			List<Value> results = RdfListUtil.getList(testSubjectUri1, testPredicateUri1, testGraph, null);
			Assert.fail("Did not find expected exception");
		}
		catch(RuntimeException rex)
		{
			Assert.assertEquals("List structure was not complete", rex.getMessage());
		}
	}
	
}
