/**
 * 
 */
package net.fortytwo.sesametools;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.datatypes.DBPediaDatatypeHandler;

/**
 * Tests StatementComparator to make sure it complies with its contract, 
 * and the equals contract for Statement
 * 
 * It also tests ValueComparator which is a direct dependency of StatementComparator
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class StatementComparatorTest
{
	private StatementComparator testComparator;
	/**
	 * Each test requires a new ValueFactory to ensure that the creation 
	 * of BNode identifiers are segregated between tests as far as possible.
	 * 
	 * However, sorting between unequal BNodes is not supported given the BNode 
	 * identifier definition, so BNodes are only used to verify consistent 
	 * sorting of the same BNode and sorting between BNodes and URIs/Literals
	 */
	private ValueFactory valueFactory;

	private BNode testSubjectBNode1;
	/**
	 * testSubjectUri1 needs to be constructed to sort before testSubjectUri2
	 */
	private URI testSubjectUri1;
	private URI testSubjectUri2;

	/**
	 * testPredicateUri1 needs to be constructed to sort before testPredicateUri2
	 */
	private URI testPredicateUri1;
	private URI testPredicateUri2;

	private BNode testObjectBNode1;
	/**
	 * testObjectUri1 needs to be constructed to sort before testObjectUri2
	 */
	private URI testObjectUri1;
	private URI testObjectUri2;
	/**
	 * testObjectLiteral1 needs to be constructed to sort before testObjectLiteral2
	 */
	private Literal testObjectLiteral1;
	private Literal testObjectLiteral2;
	
	private Literal testObjectLangLiteral1EN;
	private Literal testObjectLangLiteral1JA;
	private Literal testObjectLangLiteral2EN;
	
	private Literal testObjectTypedLiteral1String;
	private Literal testObjectTypedLiteral2String;
	private Literal testObjectTypedLiteral1Integer;
	private Literal testObjectTypedLiteral2Integer;
	private Literal testObjectTypedLiteral1IntegerString;
	private Literal testObjectTypedLiteral2IntegerString;

	private BNode testContextBNode1;
	/**
	 * testContextUri1 needs to be constructed to sort before testContextUri2
	 */
	private URI testContextUri1;
	private URI testContextUri2;

	private Statement statement1;
	private Statement statement2;
	private Statement statement3;
	private Statement statement4;
	private Statement statement5;
	private Statement statement6;
	private Statement statement7;
	private Statement statement8;
	private Statement statement9;
	private Statement statement10;
	private Statement statement11;
	
	/**
	 * Sets up a new StatementComparator before each test
	 * 
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		testComparator = new StatementComparator();
		valueFactory = new ValueFactoryImpl();
		
		testSubjectBNode1 = valueFactory.createBNode("SubjectBNode1");
		testSubjectUri1 = valueFactory.createURI("urn:test:statementcomparator:","subject1");
		testSubjectUri2 = valueFactory.createURI("urn:test:statementcomparator:","subject2");
		
		testPredicateUri1 = valueFactory.createURI("urn:test:statementcomparator:","predicate1");
		testPredicateUri2 = valueFactory.createURI("urn:test:statementcomparator:","predicate2");

		testObjectBNode1 = valueFactory.createBNode("ObjectBNode1");
		testObjectUri1 = valueFactory.createURI("urn:test:statementcomparator:","object1");
		testObjectUri2 = valueFactory.createURI("urn:test:statementcomparator:","object2");
		testObjectLiteral1 = valueFactory.createLiteral("test object literal 1");
		testObjectLiteral2 = valueFactory.createLiteral("test object literal 2");
		testObjectLangLiteral1EN = valueFactory.createLiteral("test object literal 1", "en");
		testObjectLangLiteral1JA = valueFactory.createLiteral("test object literal 1", "ja");
		testObjectLangLiteral2EN = valueFactory.createLiteral("test object literal 2", "en");
		testObjectTypedLiteral1String = valueFactory.createLiteral("test object literal 1", XMLSchema.STRING);
		testObjectTypedLiteral2String = valueFactory.createLiteral("test object literal 2", XMLSchema.STRING);
		testObjectTypedLiteral1IntegerString = valueFactory.createLiteral("1", XMLSchema.STRING);
		testObjectTypedLiteral2IntegerString = valueFactory.createLiteral("2", XMLSchema.STRING);
		testObjectTypedLiteral1Integer = valueFactory.createLiteral("1", XMLSchema.INTEGER);
		testObjectTypedLiteral2Integer = valueFactory.createLiteral("2", XMLSchema.INTEGER);
		
		testContextBNode1 = valueFactory.createBNode("ContextBNode1");
		testContextUri1 = valueFactory.createURI("urn:test:statementcomparator:","context1");
		testContextUri2 = valueFactory.createURI("urn:test:statementcomparator:","context2");
	}
	
	/**
	 * Cleans up after each test
	 * 
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception
	{
		testComparator = null;
		valueFactory = null;
		
		testSubjectUri1 = null;
		testSubjectUri2 = null;
		testSubjectBNode1 = null;
		
		testPredicateUri1 = null;
		testPredicateUri2 = null;
		
		testObjectUri1 = null;
		testObjectUri2 = null;
		testObjectLiteral1 = null;
		testObjectLiteral2 = null;
		testObjectBNode1 = null;
		
		testContextUri1 = null;
		testContextUri2 = null;
		testContextBNode1 = null;
		
		statement1 = null;
		statement2 = null;
		statement3 = null;
		statement4 = null;
		statement5 = null;
		statement6 = null;
		statement7 = null;
		statement8 = null;
		statement9 = null;
		statement10 = null;
		statement11 = null;
	}
	
	/**
	 * Tests whether the StatementComparator constants match 
	 * the general Comparable interface contract
	 */
	@Test
	public void testStatementComparatorConstants()
	{
		assertEquals(0, StatementComparator.EQUALS);
		assertTrue(StatementComparator.BEFORE < 0);
		assertTrue(StatementComparator.AFTER > 0);
	}

	/**
	 * Tests whether two equivalent statements (same subject/predicate/object) 
	 * with null contexts are sorted as EQUALS
	 */
	@Test
	public void testCompareEquivalentBothNullContexts()
	{
		statement1 = valueFactory.createStatement(testSubjectUri1, testPredicateUri1, testObjectUri1, null);
		statement2 = valueFactory.createStatement(testSubjectUri1, testPredicateUri1, testObjectUri1, null);
		
		assertEquals(0, testComparator.compare(statement1, statement2));
		assertEquals(0, testComparator.compare(statement2, statement1));
	}
	
	/**
	 * Tests whether two equivalent statements (same subject/predicate/object) 
	 * with typed null contexts are sorted as EQUALS
	 */
	@Test
	public void testCompareEquivalentBothNullContextsTyped1()
	{
		statement1 = valueFactory.createStatement(testSubjectUri1, testPredicateUri1, testObjectUri1, (Resource)null);
		statement2 = valueFactory.createStatement(testSubjectUri1, testPredicateUri1, testObjectUri1, (Resource)null);
		
		assertEquals(0, testComparator.compare(statement1, statement2));
		assertEquals(0, testComparator.compare(statement2, statement1));
	}
	
	/**
	 * Tests whether two equivalent statements (same subject/predicate/object) 
	 * with typed null contexts are sorted as EQUALS
	 */
	@Test
	public void testCompareEquivalentBothNullContextsTyped2()
	{
		statement1 = valueFactory.createStatement(testSubjectUri1, testPredicateUri1, testObjectUri1, (Resource)null);
		statement2 = valueFactory.createStatement(testSubjectUri1, testPredicateUri1, testObjectUri1, (BNode)null);
		
		assertEquals(0, testComparator.compare(statement1, statement2));
		assertEquals(0, testComparator.compare(statement2, statement1));
	}
	
	/**
	 * Tests whether two equivalent statements (same subject/predicate/object) 
	 * with typed null contexts are sorted as EQUALS
	 */
	@Test
	public void testCompareEquivalentBothNullContextsTyped3()
	{
		statement1 = valueFactory.createStatement(testSubjectUri1, testPredicateUri1, testObjectUri1, (URI)null);
		statement2 = valueFactory.createStatement(testSubjectUri1, testPredicateUri1, testObjectUri1, (BNode)null);
		
		assertEquals(0, testComparator.compare(statement1, statement2));
		assertEquals(0, testComparator.compare(statement2, statement1));
	}
	
	/**
	 * Tests whether two equivalent statements (same subject/predicate/object) 
	 * with typed null contexts are sorted as EQUALS
	 */
	@Test
	public void testCompareEquivalentBothNullContextsTyped4()
	{
		statement1 = valueFactory.createStatement(testSubjectUri1, testPredicateUri1, testObjectUri1, (URI)null);
		statement2 = valueFactory.createStatement(testSubjectUri1, testPredicateUri1, testObjectUri1, (Resource)null);
		
		assertEquals(0, testComparator.compare(statement1, statement2));
		assertEquals(0, testComparator.compare(statement2, statement1));
	}
	
	/**
	 * Tests whether two equivalent statements (same subject/predicate/object) 
	 * with no context defined is sorted as EQUALS
	 */
	@Test
	public void testCompareEquivalentNoContexts()
	{
		statement1 = valueFactory.createStatement(testSubjectUri1, testPredicateUri1, testObjectUri1);
		statement2 = valueFactory.createStatement(testSubjectUri1, testPredicateUri1, testObjectUri1);
		
		assertEquals(0, testComparator.compare(statement1, statement2));
		assertEquals(0, testComparator.compare(statement2, statement1));
	}

	/**
	 * Tests whether two equivalent statements (same subject/predicate/object) 
	 * with one null context and one with no context defined is sorted as EQUALS
	 */
	@Test
	public void testCompareEquivalentOneNullContext()
	{
		statement1 = valueFactory.createStatement(testSubjectUri1, testPredicateUri1, testObjectUri1, null);
		statement2 = valueFactory.createStatement(testSubjectUri1, testPredicateUri1, testObjectUri1);
		
		assertEquals(0, testComparator.compare(statement1, statement2));
		assertEquals(0, testComparator.compare(statement2, statement1));
	}

	/**
	 * Tests whether two equivalent statements (same subject/predicate/object) 
	 * with one null context and one with a URI defined is sorted as BEFORE
	 */
	@Test
	public void testCompareEquivalentOneNullOneURIContext()
	{
		statement1 = valueFactory.createStatement(testSubjectUri1, testPredicateUri1, testObjectUri1, null);
		statement2 = valueFactory.createStatement(testSubjectUri1, testPredicateUri1, testObjectUri1, testContextUri1);
		
		assertTrue(testComparator.compare(statement1, statement2) < 0);
		assertTrue(testComparator.compare(statement2, statement1) > 0);
	}

	/**
	 * Tests whether two equivalent statements (same subject/predicate/object) 
	 * with one null context and one with a URI defined is sorted as BEFORE
	 */
	@Test
	public void testCompareEquivalentOneNullOneBNodeContext()
	{
		statement1 = valueFactory.createStatement(testSubjectUri1, testPredicateUri1, testObjectUri1, null);
		statement2 = valueFactory.createStatement(testSubjectUri1, testPredicateUri1, testObjectUri1, testContextBNode1);
		
		assertTrue(testComparator.compare(statement1, statement2) < 0);
		assertTrue(testComparator.compare(statement2, statement1) > 0);
	}

	/**
	 * Tests whether two equivalent statements (same subject/predicate/object) 
	 * with a BNode context and one with a URI defined is sorted as BEFORE
	 */
	@Test
	public void testCompareEquivalentBNodeAndUriContext()
	{
		statement1 = valueFactory.createStatement(testSubjectUri1, testPredicateUri1, testObjectUri1, testContextBNode1);
		statement2 = valueFactory.createStatement(testSubjectUri1, testPredicateUri1, testObjectUri1, testContextUri1);
		
		assertTrue(testComparator.compare(statement1, statement2) < 0);
		assertTrue(testComparator.compare(statement2, statement1) > 0);
	}

	/**
	 * Tests whether two equivalent statements (same subject/predicate/object) 
	 * with two different URIs is sorted correctly
	 */
	@Test
	public void testCompareEquivalentTwoUrisContext()
	{
		statement1 = valueFactory.createStatement(testSubjectUri1, testPredicateUri1, testObjectUri1, testContextUri1);
		statement2 = valueFactory.createStatement(testSubjectUri1, testPredicateUri1, testObjectUri1, testContextUri2);
		
		assertTrue(testComparator.compare(statement1, statement2) < 0);
		assertTrue(testComparator.compare(statement2, statement1) > 0);
	}

	/**
	 * Tests whether two equivalent statements (same subject/predicate/object) 
	 * with the same context URI are sorted as EQUALS
	 */
	@Test
	public void testCompareEquivalentSameUriContext()
	{
		statement1 = valueFactory.createStatement(testSubjectUri1, testPredicateUri1, testObjectUri1, testContextUri1);
		statement2 = valueFactory.createStatement(testSubjectUri1, testPredicateUri1, testObjectUri1, testContextUri1);
		
		assertEquals(0, testComparator.compare(statement1, statement2));
		assertEquals(0, testComparator.compare(statement2, statement1));
	}

	/**
	 * Tests whether a Statement with a Blank Node subject is sorted BEFORE a
	 * similar statement with a URI subject
	 */
	@Test
	public void testCompareBNodeSubject()
	{
		statement1 = valueFactory.createStatement(testSubjectBNode1, testPredicateUri1, testObjectBNode1);
		statement2 = valueFactory.createStatement(testSubjectUri1, testPredicateUri1, testObjectBNode1);
		
		assertTrue(testComparator.compare(statement1, statement2) < 0);
		assertTrue(testComparator.compare(statement2, statement1) > 0);
	}

	/**
	 * Tests whether a statement with a BNode object is sorted before a 
	 * statement with a URI object
	 */
	@Test
	public void testCompareBNodeAndUriObjects()
	{
		statement1 = valueFactory.createStatement(testSubjectBNode1, testPredicateUri1, testObjectBNode1);
		statement2 = valueFactory.createStatement(testSubjectBNode1, testPredicateUri1, testObjectUri1);
		
		assertTrue(testComparator.compare(statement1, statement2) < 0);
		assertTrue(testComparator.compare(statement2, statement1) > 0);
	}

	/**
	 * Tests whether a statement with a BNode object is sorted before a 
	 * statement with a Literal object
	 */
	@Test
	public void testCompareBNodeAndLiteralObjects()
	{
		statement1 = valueFactory.createStatement(testSubjectBNode1, testPredicateUri1, testObjectBNode1);
		statement2 = valueFactory.createStatement(testSubjectBNode1, testPredicateUri1, testObjectLiteral1);
		
		assertTrue(testComparator.compare(statement1, statement2) < 0);
		assertTrue(testComparator.compare(statement2, statement1) > 0);
	}

	/**
	 * Tests consistency of sorting between equivalent statements with 
	 * different simple literal objects
	 */
	@Test
	public void testCompareLiteralObjectsSimple()
	{
		statement1 = valueFactory.createStatement(testSubjectBNode1, testPredicateUri1, testObjectLiteral1);
		statement2 = valueFactory.createStatement(testSubjectBNode1, testPredicateUri1, testObjectLiteral2);
		
		assertTrue(testComparator.compare(statement1, statement1) == 0);
		assertTrue(testComparator.compare(statement2, statement2) == 0);
		
		assertTrue(testComparator.compare(statement1, statement2) < 0);
		assertTrue(testComparator.compare(statement2, statement1) > 0);
	}

	/**
	 * Tests consistency of sorting between equivalent statements with 
	 * different language literal objects
	 */
	@Test
	public void testCompareLiteralObjectsLang()
	{
		statement1 = valueFactory.createStatement(testSubjectBNode1, testPredicateUri1, testObjectLiteral1);
		statement2 = valueFactory.createStatement(testSubjectBNode1, testPredicateUri1, testObjectLiteral2);
		
		statement3 = valueFactory.createStatement(testSubjectBNode1, testPredicateUri1, testObjectLangLiteral1EN);
		statement4 = valueFactory.createStatement(testSubjectBNode1, testPredicateUri1, testObjectLangLiteral1JA);
		statement5 = valueFactory.createStatement(testSubjectBNode1, testPredicateUri1, testObjectLangLiteral2EN);
		
		assertTrue(testComparator.compare(statement1, statement1) == 0);
		assertTrue(testComparator.compare(statement2, statement2) == 0);
		assertTrue(testComparator.compare(statement3, statement3) == 0);
		assertTrue(testComparator.compare(statement4, statement4) == 0);
		assertTrue(testComparator.compare(statement5, statement5) == 0);
		
		// Different literal values sort
		assertTrue(testComparator.compare(statement1, statement2) < 0);
		assertTrue(testComparator.compare(statement2, statement1) > 0);
		// Same literal values sort one missing lang tag
		assertTrue(testComparator.compare(statement1, statement3) < 0);
		assertTrue(testComparator.compare(statement3, statement1) > 0);
		// Same literal values sort one missing lang tag
		assertTrue(testComparator.compare(statement1, statement4) < 0);
		assertTrue(testComparator.compare(statement4, statement1) > 0);
		// Different literal values sort 
		assertTrue(testComparator.compare(statement1, statement5) < 0);
		assertTrue(testComparator.compare(statement5, statement1) > 0);
		
		// Different literal values sort one missing lang tag
		assertTrue(testComparator.compare(statement3, statement2) < 0);
		assertTrue(testComparator.compare(statement2, statement3) > 0);
		// Different literal values sort one missing lang tag
		assertTrue(testComparator.compare(statement4, statement2) < 0);
		assertTrue(testComparator.compare(statement2, statement4) > 0);
		// Same literal values sort
		assertTrue(testComparator.compare(statement2, statement5) < 0);
		assertTrue(testComparator.compare(statement5, statement2) > 0);

		// Same literal values sort by lang tag, en before ja
		assertTrue(testComparator.compare(statement3, statement4) < 0);
		assertTrue(testComparator.compare(statement4, statement3) > 0);
		// Different literal values sort by lang tag, en before ja
		assertTrue(testComparator.compare(statement4, statement5) < 0);
		assertTrue(testComparator.compare(statement5, statement4) > 0);
	}

	/**
	 * Tests consistency of sorting between equivalent statements with 
	 * different typed literal objects
	 */
	@Test
	public void testCompareLiteralObjectsTyped()
	{
		statement1 = valueFactory.createStatement(testSubjectBNode1, testPredicateUri1, testObjectLiteral1);
		statement2 = valueFactory.createStatement(testSubjectBNode1, testPredicateUri1, testObjectLiteral2);
		
		statement3 = valueFactory.createStatement(testSubjectBNode1, testPredicateUri1, testObjectTypedLiteral1String);
		statement4 = valueFactory.createStatement(testSubjectBNode1, testPredicateUri1, testObjectTypedLiteral2String);
		
		statement5 = valueFactory.createStatement(testSubjectBNode1, testPredicateUri1, testObjectTypedLiteral1Integer);
		statement6 = valueFactory.createStatement(testSubjectBNode1, testPredicateUri1, testObjectTypedLiteral2Integer);
		
		statement7 = valueFactory.createStatement(testSubjectBNode1, testPredicateUri1, testObjectTypedLiteral1IntegerString);
		statement8 = valueFactory.createStatement(testSubjectBNode1, testPredicateUri1, testObjectTypedLiteral2IntegerString);
		
		assertTrue(testComparator.compare(statement1, statement1) == 0);
		assertTrue(testComparator.compare(statement2, statement2) == 0);
		assertTrue(testComparator.compare(statement3, statement3) == 0);
		assertTrue(testComparator.compare(statement4, statement4) == 0);
		assertTrue(testComparator.compare(statement5, statement5) == 0);
		assertTrue(testComparator.compare(statement6, statement6) == 0);
		assertTrue(testComparator.compare(statement7, statement7) == 0);
		assertTrue(testComparator.compare(statement8, statement8) == 0);
		
		// Different literal values sort
		assertTrue(testComparator.compare(statement1, statement2) < 0);
		assertTrue(testComparator.compare(statement2, statement1) > 0);
		// Same literal values sort one missing type, other xsd:string
		// NOTE: This will start failing from Sesame-2.8/RDF-1.1 by switching to EQUALS in both cases
		assertTrue(testComparator.compare(statement1, statement3) < 0);
		assertTrue(testComparator.compare(statement3, statement1) > 0);
		// Different literal values sort
		assertTrue(testComparator.compare(statement1, statement4) < 0);
		assertTrue(testComparator.compare(statement4, statement1) > 0);
		// Different literal values sort
		assertTrue(testComparator.compare(statement5, statement1) < 0);
		assertTrue(testComparator.compare(statement1, statement5) > 0);
		// Different literal values sort
		assertTrue(testComparator.compare(statement6, statement1) < 0);
		assertTrue(testComparator.compare(statement1, statement6) > 0);
		// Different literal values sort
		assertTrue(testComparator.compare(statement7, statement1) < 0);
		assertTrue(testComparator.compare(statement1, statement7) > 0);
		// Different literal values sort
		assertTrue(testComparator.compare(statement8, statement1) < 0);
		assertTrue(testComparator.compare(statement1, statement8) > 0);
		
		// Different literal values sort
		assertTrue(testComparator.compare(statement3, statement2) < 0);
		assertTrue(testComparator.compare(statement2, statement3) > 0);
		// Same literal values sort one missing type, other xsd:string
		// NOTE: This will start failing from Sesame-2.8/RDF-1.1 by switching to EQUALS in both cases
		assertTrue(testComparator.compare(statement2, statement4) < 0);
		assertTrue(testComparator.compare(statement4, statement2) > 0);
		// Different literal values sort
		assertTrue(testComparator.compare(statement5, statement2) < 0);
		assertTrue(testComparator.compare(statement2, statement5) > 0);
		// Different literal values sort
		assertTrue(testComparator.compare(statement6, statement2) < 0);
		assertTrue(testComparator.compare(statement2, statement6) > 0);
		// Different literal values sort
		assertTrue(testComparator.compare(statement7, statement2) < 0);
		assertTrue(testComparator.compare(statement2, statement7) > 0);
		// Different literal values sort
		assertTrue(testComparator.compare(statement8, statement2) < 0);
		assertTrue(testComparator.compare(statement2, statement8) > 0);

		// Different literal values sort
		assertTrue(testComparator.compare(statement3, statement4) < 0);
		assertTrue(testComparator.compare(statement4, statement3) > 0);
		// Different literal values sort
		assertTrue(testComparator.compare(statement5, statement3) < 0);
		assertTrue(testComparator.compare(statement3, statement5) > 0);
		// Different literal values sort
		assertTrue(testComparator.compare(statement6, statement3) < 0);
		assertTrue(testComparator.compare(statement3, statement6) > 0);
		// Different literal values sort
		assertTrue(testComparator.compare(statement7, statement3) < 0);
		assertTrue(testComparator.compare(statement3, statement7) > 0);
		// Different literal values sort
		assertTrue(testComparator.compare(statement8, statement3) < 0);
		assertTrue(testComparator.compare(statement3, statement8) > 0);
	
		// Different literal values sort
		assertTrue(testComparator.compare(statement5, statement4) < 0);
		assertTrue(testComparator.compare(statement4, statement5) > 0);
		// Different literal values sort
		assertTrue(testComparator.compare(statement6, statement4) < 0);
		assertTrue(testComparator.compare(statement4, statement6) > 0);
		// Different literal values sort
		assertTrue(testComparator.compare(statement7, statement4) < 0);
		assertTrue(testComparator.compare(statement4, statement7) > 0);
		// Different literal values sort
		assertTrue(testComparator.compare(statement8, statement4) < 0);
		assertTrue(testComparator.compare(statement4, statement8) > 0);

		// Different literal values sort
		assertTrue(testComparator.compare(statement5, statement6) < 0);
		assertTrue(testComparator.compare(statement6, statement5) > 0);
		// Same literal values different types
		assertTrue(testComparator.compare(statement5, statement7) < 0);
		assertTrue(testComparator.compare(statement7, statement5) > 0);
		// Different literal values sort
		assertTrue(testComparator.compare(statement5, statement8) < 0);
		assertTrue(testComparator.compare(statement8, statement5) > 0);

		// Different literal values sort
		assertTrue(testComparator.compare(statement7, statement6) < 0);
		assertTrue(testComparator.compare(statement6, statement7) > 0);
		// Same literal values different types
		assertTrue(testComparator.compare(statement6, statement8) < 0);
		assertTrue(testComparator.compare(statement8, statement6) > 0);

		// Different literal values sort
		assertTrue(testComparator.compare(statement7, statement8) < 0);
		assertTrue(testComparator.compare(statement8, statement7) > 0);
	}

	/**
	 * Tests consistency of sorting between equivalent statements with 
	 * different typed and language literal objects
	 */
	@Test
	public void testCompareLiteralObjectsTypedAndLanguage()
	{
		statement1 = valueFactory.createStatement(testSubjectBNode1, testPredicateUri1, testObjectLiteral1);
		statement2 = valueFactory.createStatement(testSubjectBNode1, testPredicateUri1, testObjectLiteral2);
		
		statement3 = valueFactory.createStatement(testSubjectBNode1, testPredicateUri1, testObjectLangLiteral1EN);
		statement4 = valueFactory.createStatement(testSubjectBNode1, testPredicateUri1, testObjectLangLiteral1JA);
		statement5 = valueFactory.createStatement(testSubjectBNode1, testPredicateUri1, testObjectLangLiteral2EN);
		
		statement6 = valueFactory.createStatement(testSubjectBNode1, testPredicateUri1, testObjectTypedLiteral1String);
		statement7 = valueFactory.createStatement(testSubjectBNode1, testPredicateUri1, testObjectTypedLiteral2String);
		
		statement8 = valueFactory.createStatement(testSubjectBNode1, testPredicateUri1, testObjectTypedLiteral1Integer);
		statement9 = valueFactory.createStatement(testSubjectBNode1, testPredicateUri1, testObjectTypedLiteral2Integer);
		
		statement10 = valueFactory.createStatement(testSubjectBNode1, testPredicateUri1, testObjectTypedLiteral1IntegerString);
		statement11 = valueFactory.createStatement(testSubjectBNode1, testPredicateUri1, testObjectTypedLiteral2IntegerString);
		
		assertTrue(testComparator.compare(statement1, statement1) == 0);
		assertTrue(testComparator.compare(statement2, statement2) == 0);
		assertTrue(testComparator.compare(statement3, statement3) == 0);
		assertTrue(testComparator.compare(statement4, statement4) == 0);
		assertTrue(testComparator.compare(statement5, statement5) == 0);
		assertTrue(testComparator.compare(statement6, statement6) == 0);
		assertTrue(testComparator.compare(statement7, statement7) == 0);
		assertTrue(testComparator.compare(statement8, statement8) == 0);
		assertTrue(testComparator.compare(statement9, statement9) == 0);
		
		// Different literal values sort
		assertTrue(testComparator.compare(statement1, statement2) < 0);
		assertTrue(testComparator.compare(statement2, statement1) > 0);
		// Different literal values sort
		assertTrue(testComparator.compare(statement1, statement3) < 0);
		assertTrue(testComparator.compare(statement3, statement1) > 0);
		// Different literal values sort
		assertTrue(testComparator.compare(statement1, statement4) < 0);
		assertTrue(testComparator.compare(statement4, statement1) > 0);
		// Same literal values sort one lang and one plain literal
		// NOTE: This should not start failing in RDF-1.1, as the language should be checked before the datatype
		assertTrue(testComparator.compare(statement1, statement5) < 0);
		assertTrue(testComparator.compare(statement5, statement1) > 0);
		// Same literal values sort one missing type, other xsd:string
		// NOTE: This will start failing from Sesame-2.8/RDF-1.1 by switching to EQUALS in both cases
		assertTrue(testComparator.compare(statement1, statement6) < 0);
		assertTrue(testComparator.compare(statement6, statement1) > 0);
		// Different literal values sort
		assertTrue(testComparator.compare(statement1, statement7) < 0);
		assertTrue(testComparator.compare(statement7, statement1) > 0);
		// Different literal values sort
		assertTrue(testComparator.compare(statement8, statement1) < 0);
		assertTrue(testComparator.compare(statement1, statement8) > 0);
		// Different literal values sort
		assertTrue(testComparator.compare(statement9, statement1) < 0);
		assertTrue(testComparator.compare(statement1, statement9) > 0);
		// Different literal values sort
		assertTrue(testComparator.compare(statement10, statement1) < 0);
		assertTrue(testComparator.compare(statement1, statement10) > 0);
		// Different literal values sort
		assertTrue(testComparator.compare(statement11, statement1) < 0);
		assertTrue(testComparator.compare(statement1, statement11) > 0);

		// Different literal values sort one lang and one plain literal
		assertTrue(testComparator.compare(statement3, statement2) < 0);
		assertTrue(testComparator.compare(statement2, statement3) > 0);
		// Different literal values sort one lang and one plain literal
		assertTrue(testComparator.compare(statement4, statement2) < 0);
		assertTrue(testComparator.compare(statement2, statement4) > 0);
		// Same literal values sort one lang and one plain literal
		// NOTE: This should not start failing in RDF-1.1, as the language should be checked before the datatype
		assertTrue(testComparator.compare(statement2, statement5) < 0);
		assertTrue(testComparator.compare(statement5, statement2) > 0);
		// Different literal values sort
		assertTrue(testComparator.compare(statement6, statement2) < 0);
		assertTrue(testComparator.compare(statement2, statement6) > 0);
		// Same literal values sort one missing type, other xsd:string
		// NOTE: This will start failing from Sesame-2.8/RDF-1.1 by switching to EQUALS in both cases
		assertTrue(testComparator.compare(statement2, statement7) < 0);
		assertTrue(testComparator.compare(statement7, statement2) > 0);
		// Different literal values sort
		assertTrue(testComparator.compare(statement8, statement2) < 0);
		assertTrue(testComparator.compare(statement2, statement8) > 0);
		// Different literal values sort
		assertTrue(testComparator.compare(statement9, statement2) < 0);
		assertTrue(testComparator.compare(statement2, statement9) > 0);
		// Different literal values sort
		assertTrue(testComparator.compare(statement10, statement2) < 0);
		assertTrue(testComparator.compare(statement2, statement10) > 0);
		// Different literal values sort
		assertTrue(testComparator.compare(statement11, statement2) < 0);
		assertTrue(testComparator.compare(statement2, statement11) > 0);

		// Same literal values sort different langs
		assertTrue(testComparator.compare(statement3, statement4) < 0);
		assertTrue(testComparator.compare(statement4, statement3) > 0);
		// Different literal values sort
		assertTrue(testComparator.compare(statement3, statement5) < 0);
		assertTrue(testComparator.compare(statement5, statement3) > 0);
		// Same literal values sort one lang, other xsd:string
		assertTrue(testComparator.compare(statement6, statement3) < 0);
		assertTrue(testComparator.compare(statement3, statement6) > 0);
		// Different literal values sort
		assertTrue(testComparator.compare(statement3, statement7) < 0);
		assertTrue(testComparator.compare(statement7, statement3) > 0);
		// Different literal values sort
		assertTrue(testComparator.compare(statement8, statement3) < 0);
		assertTrue(testComparator.compare(statement3, statement8) > 0);
		// Different literal values sort
		assertTrue(testComparator.compare(statement9, statement3) < 0);
		assertTrue(testComparator.compare(statement3, statement9) > 0);
		// Different literal values sort
		assertTrue(testComparator.compare(statement10, statement3) < 0);
		assertTrue(testComparator.compare(statement3, statement10) > 0);
		// Different literal values sort
		assertTrue(testComparator.compare(statement11, statement3) < 0);
		assertTrue(testComparator.compare(statement3, statement11) > 0);
	}

	/**
	 * Tests consistency of sorting between equivalent statements with 
	 * different URI objects
	 */
	@Test
	public void testCompareUriObjects()
	{
		statement1 = valueFactory.createStatement(testSubjectBNode1, testPredicateUri1, testObjectUri1);
		statement2 = valueFactory.createStatement(testSubjectBNode1, testPredicateUri1, testObjectUri2);
		
		assertTrue(testComparator.compare(statement1, statement2) < 0);
		assertTrue(testComparator.compare(statement2, statement1) > 0);
	}

	/**
	 * Tests whether a statement with a URI object is sorted before a 
	 * statement with a Literal object
	 */
	@Test
	public void testCompareURIAndLiteralObjects()
	{
		statement1 = valueFactory.createStatement(testSubjectBNode1, testPredicateUri1, testObjectUri1);
		statement2 = valueFactory.createStatement(testSubjectBNode1, testPredicateUri1, testObjectLiteral1);
		
		assertTrue(testComparator.compare(statement1, statement2) < 0);
		assertTrue(testComparator.compare(statement2, statement1) > 0);
	}
	
	/**
	 * Tests whether a statement with a URI object is sorted before a 
	 * statement with a Literal object
	 */
	@Test
	public void testComparePredicates()
	{
		statement1 = valueFactory.createStatement(testSubjectUri1, testPredicateUri1, testObjectUri1);
		statement2 = valueFactory.createStatement(testSubjectUri1, testPredicateUri2, testObjectUri1);
		
		assertTrue(testComparator.compare(statement1, statement2) < 0);
		assertTrue(testComparator.compare(statement2, statement1) > 0);
	}

	/**
	 * Tests consistency of sorting for two equivalent statements with 
	 * different subject URIs
	 */
	@Test
	public void testCompareSubjects()
	{
		statement1 = valueFactory.createStatement(testSubjectUri1, testPredicateUri1, testObjectUri1);
		statement2 = valueFactory.createStatement(testSubjectUri2, testPredicateUri1, testObjectUri1);
		
		assertTrue(testComparator.compare(statement1, statement2) < 0);
		assertTrue(testComparator.compare(statement2, statement1) > 0);
	}
}
