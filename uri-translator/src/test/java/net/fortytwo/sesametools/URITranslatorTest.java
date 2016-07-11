/**
 *
 */
package net.fortytwo.sesametools;

import java.util.Collection;
import java.util.Collections;

import info.aduna.iteration.CloseableIteration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class URITranslatorTest {
    private static final Logger logger = LoggerFactory.getLogger(URITranslatorTest.class);

    private Repository testRepository;
    private ValueFactory testValueFactory;
    private RepositoryConnection testRepositoryConnection;

    private String testInputUriPrefix1;
    private String testOutputUriPrefix1;

    private IRI testInputSubjectUri1;
    private IRI testInputPredicateUri1;
    private IRI testInputObjectUri1;

    private Collection<IRI> testSubjectMappingPredicatesEmpty;
    private Collection<IRI> testPredicateMappingPredicatesEmpty;
    private Collection<IRI> testObjectMappingPredicatesEmpty;

    private boolean testDeleteTranslatedTriplesTrue;

    private Resource testContext1;

    private IRI testOutputSubjectUri1;
    private IRI testOutputPredicateUri1;
    private IRI testOutputObjectUri1;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        testRepository = new SailRepository(new MemoryStore());
        testRepository.initialize();

        testValueFactory = testRepository.getValueFactory();

        testRepositoryConnection = testRepository.getConnection();

        testDeleteTranslatedTriplesTrue = true;

        testInputUriPrefix1 = "urn:temp:";
        testOutputUriPrefix1 = "http://test.example.org/after/translation/";

        testInputSubjectUri1 = testValueFactory.createIRI(
                "urn:temp:testInputSubjectUri1");
        testOutputSubjectUri1 = testValueFactory.createIRI(
                "http://test.example.org/after/translation/testInputSubjectUri1");

        testInputPredicateUri1 = testValueFactory.createIRI(
                "urn:temp:testInputPredicateUri1");
        testOutputPredicateUri1 = testValueFactory.createIRI(
                "http://test.example.org/after/translation/testInputPredicateUri1");

        testInputObjectUri1 = testValueFactory.createIRI(
                "urn:temp:testInputObjectUri1");
        testOutputObjectUri1 = testValueFactory.createIRI(
                "http://test.example.org/after/translation/testInputObjectUri1");

        testSubjectMappingPredicatesEmpty = Collections.emptyList();
        testPredicateMappingPredicatesEmpty = Collections.emptyList();
        testObjectMappingPredicatesEmpty = Collections.emptyList();

        testContext1 = testValueFactory.createIRI("urn:test:context:1");
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        if (testRepositoryConnection != null) {
            try {
                testRepositoryConnection.close();
            } catch (RepositoryException e) {
                logger.error("Found unexpected exception while closing test repository connection");
            }
        }

        testValueFactory = null;

        if (testRepository != null) {
            try {
                testRepository.shutDown();
            } catch (RepositoryException e) {
                logger.error("Repository did not shut down correctly in test tearDown", e);
            }
        }

        testRepository = null;
        testInputUriPrefix1 = null;
        testOutputUriPrefix1 = null;

        testInputSubjectUri1 = null;
        testOutputSubjectUri1 = null;
        testInputPredicateUri1 = null;
        testOutputPredicateUri1 = null;
        testInputObjectUri1 = null;
        testOutputObjectUri1 = null;

        testSubjectMappingPredicatesEmpty = null;
        testPredicateMappingPredicatesEmpty = null;
        testObjectMappingPredicatesEmpty = null;
        testDeleteTranslatedTriplesTrue = true;
        testContext1 = null;
    }

    /**
     * @throws UpdateExecutionException
     * @throws MalformedQueryException
     * @throws RepositoryException
     */
    @Test
    public void testDoTranslationNoMappingNoExactOneContext() throws RepositoryException, MalformedQueryException,
            UpdateExecutionException {
        testRepositoryConnection.add(testInputSubjectUri1, testInputPredicateUri1, testInputObjectUri1, testContext1);

        Assert.assertEquals(1, testRepositoryConnection.size());
        Assert.assertEquals(1, testRepositoryConnection.size(testContext1));

        try (CloseableIteration<Statement, RepositoryException> iter
                     = testRepositoryConnection.getStatements(null, null, null, false, testContext1)) {
            while (iter.hasNext()) {
                logger.info("nextBeforeStatement: " + iter.next().toString());
            }
        }

        URITranslator.doTranslation(testRepository, testInputUriPrefix1, testOutputUriPrefix1,
                testSubjectMappingPredicatesEmpty, true, false, testPredicateMappingPredicatesEmpty, true, false,
                testObjectMappingPredicatesEmpty, true, false, testDeleteTranslatedTriplesTrue, testContext1);

        try (CloseableIteration<Statement, RepositoryException> iter
                     = testRepositoryConnection.getStatements(null, null, null, false, testContext1)) {
            while (iter.hasNext()) {
                logger.info("nextAfterStatement: " + iter.next().toString());
            }
        }

        Assert.assertEquals(1, testRepositoryConnection.size(testContext1));
        Assert.assertEquals(1, testRepositoryConnection.size());

        Assert.assertTrue(testRepositoryConnection.hasStatement(
                testOutputSubjectUri1, testOutputPredicateUri1, testOutputObjectUri1, false, testContext1));
    }

    /**
     * @throws UpdateExecutionException
     * @throws MalformedQueryException
     * @throws RepositoryException
     */
    @Test
    public void testDoTranslationNoMappingExactSubjectOnlyOneContext()
            throws RepositoryException, MalformedQueryException, UpdateExecutionException {
        testRepositoryConnection.add(testInputSubjectUri1, testInputPredicateUri1, testInputObjectUri1, testContext1);

        Assert.assertEquals(1, testRepositoryConnection.size());
        Assert.assertEquals(1, testRepositoryConnection.size(testContext1));

        try (CloseableIteration<Statement, RepositoryException> iter
                     = testRepositoryConnection.getStatements(null, null, null, false, testContext1)) {
            while (iter.hasNext()) {
                logger.info("nextBeforeStatement: " + iter.next().toString());
            }
        }

        URITranslator.doTranslation(
                testRepository, testInputSubjectUri1.stringValue(), testOutputSubjectUri1.stringValue(),
                testSubjectMappingPredicatesEmpty, true, true, testPredicateMappingPredicatesEmpty, true, true,
                testObjectMappingPredicatesEmpty, true, true, testDeleteTranslatedTriplesTrue, testContext1);

        try (CloseableIteration<Statement, RepositoryException> iter
                     = testRepositoryConnection.getStatements(null, null, null, false, testContext1)) {
            while (iter.hasNext()) {
                logger.info("nextAfterStatement: " + iter.next().toString());
            }
        }

        Assert.assertEquals(1, testRepositoryConnection.size(testContext1));
        Assert.assertEquals(1, testRepositoryConnection.size());

        Assert.assertTrue(testRepositoryConnection.hasStatement(
                testOutputSubjectUri1, testInputPredicateUri1, testInputObjectUri1, false, testContext1));
    }

    /**
     * @throws UpdateExecutionException
     * @throws MalformedQueryException
     * @throws RepositoryException
     */
    @Test
    public void testDoTranslationNoMappingExactPredicateOnlyOneContext()
            throws RepositoryException, MalformedQueryException, UpdateExecutionException {
        testRepositoryConnection.add(testInputSubjectUri1, testInputPredicateUri1, testInputObjectUri1, testContext1);

        Assert.assertEquals(1, testRepositoryConnection.size());
        Assert.assertEquals(1, testRepositoryConnection.size(testContext1));

        try (CloseableIteration<Statement, RepositoryException> iter
                     = testRepositoryConnection.getStatements(null, null, null, false, testContext1)) {
            while (iter.hasNext()) {
                logger.info("nextBeforeStatement: " + iter.next().toString());
            }
        }

        URITranslator.doTranslation(
                testRepository, testInputPredicateUri1.stringValue(), testOutputPredicateUri1.stringValue(),
                testSubjectMappingPredicatesEmpty, true, true, testPredicateMappingPredicatesEmpty, true, true,
                testObjectMappingPredicatesEmpty, true, true, testDeleteTranslatedTriplesTrue, testContext1);

        try (CloseableIteration<Statement, RepositoryException> iter
                     = testRepositoryConnection.getStatements(null, null, null, false, testContext1)) {
            while (iter.hasNext()) {
                logger.info("nextAfterStatement: " + iter.next().toString());
            }
        }

        Assert.assertEquals(1, testRepositoryConnection.size(testContext1));
        Assert.assertEquals(1, testRepositoryConnection.size());

        Assert.assertTrue(testRepositoryConnection.hasStatement(
                testInputSubjectUri1, testOutputPredicateUri1, testInputObjectUri1, false, testContext1));
    }

    /**
     * @throws UpdateExecutionException
     * @throws MalformedQueryException
     * @throws RepositoryException
     */
    @Test
    public void testDoTranslationNoMappingExactObjectOnlyOneContext()
            throws RepositoryException, MalformedQueryException, UpdateExecutionException {
        testRepositoryConnection.add(testInputSubjectUri1, testInputPredicateUri1, testInputObjectUri1, testContext1);

        Assert.assertEquals(1, testRepositoryConnection.size());
        Assert.assertEquals(1, testRepositoryConnection.size(testContext1));

        try (CloseableIteration<Statement, RepositoryException> iter
                     = testRepositoryConnection.getStatements(null, null, null, false, testContext1)) {
            while (iter.hasNext()) {
                logger.info("nextBeforeStatement: " + iter.next().toString());
            }
        }

        URITranslator.doTranslation(
                testRepository, testInputObjectUri1.stringValue(), testOutputObjectUri1.stringValue(),
                testSubjectMappingPredicatesEmpty, true, true, testPredicateMappingPredicatesEmpty, true, true,
                testObjectMappingPredicatesEmpty, true, true, testDeleteTranslatedTriplesTrue, testContext1);

        try (CloseableIteration<Statement, RepositoryException> iter
                     = testRepositoryConnection.getStatements(null, null, null, false, testContext1)) {
            while (iter.hasNext()) {
                logger.info("nextAfterStatement: " + iter.next().toString());
            }
        }

        Assert.assertEquals(1, testRepositoryConnection.size(testContext1));
        Assert.assertEquals(1, testRepositoryConnection.size());

        Assert.assertTrue(testRepositoryConnection.hasStatement(
                testInputSubjectUri1, testInputPredicateUri1, testOutputObjectUri1, false, testContext1));
    }
}
