/**
 * 
 */
package net.fortytwo.sesametools;

import java.util.Collection;
import java.util.Collections;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
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
 * 
 */
public class URITranslatorTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(URITranslatorTest.class);
    
    private Repository testRepository;
    private ValueFactory testValueFactory;
    private RepositoryConnection testRepositoryConnection;
    
    private String testInputUriPrefix1;
    private String testOutputUriPrefix1;
    
    private URI testInputSubjectUri1;
    private URI testInputPredicateUri1;
    private URI testInputObjectUri1;
    
    private Collection<URI> testSubjectMappingPredicatesEmpty;
    private Collection<URI> testPredicateMappingPredicatesEmpty;
    private Collection<URI> testObjectMappingPredicatesEmpty;
    
    private boolean testDeleteTranslatedTriplesTrue;
    
    private Resource testContext1;
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        testRepository = new SailRepository(new MemoryStore());
        testRepository.initialize();
        
        testValueFactory = testRepository.getValueFactory();
        
        testRepositoryConnection = testRepository.getConnection();
        
        testDeleteTranslatedTriplesTrue = true;
        
        testInputUriPrefix1 = "urn:temp:";
        testOutputUriPrefix1 = "http://test.example.org/after/translation/";
        
        testInputSubjectUri1 = testValueFactory.createURI("urn:temp:testInputSubjectUri1");
        testInputPredicateUri1 = testValueFactory.createURI("urn:temp:testInputPredicateUri1");
        testInputObjectUri1 = testValueFactory.createURI("urn:temp:testInputObjectUri1");
        
        testSubjectMappingPredicatesEmpty = Collections.emptyList();
        testPredicateMappingPredicatesEmpty = Collections.emptyList();
        testObjectMappingPredicatesEmpty = Collections.emptyList();
        
        testContext1 = testValueFactory.createURI("urn:test:context:1");
    }
    
    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
        if(testRepositoryConnection != null)
        {
            try
            {
                testRepositoryConnection.close();
            }
            catch(RepositoryException e)
            {
                LOGGER.error("Found unexpected exception while closing test repository connection");
            }
        }
        
        testValueFactory = null;
        
        if(testRepository != null)
        {
            try
            {
                testRepository.shutDown();
            }
            catch(RepositoryException e)
            {
                LOGGER.error("Repository did not shut down correctly in test tearDown", e);
            }
        }
        
        testRepository = null;
        testInputUriPrefix1 = null;
        testOutputUriPrefix1 = null;
        
        testInputSubjectUri1 = null;
        testInputPredicateUri1 = null;
        testInputObjectUri1 = null;
        
        testSubjectMappingPredicatesEmpty = null;
        testPredicateMappingPredicatesEmpty = null;
        testObjectMappingPredicatesEmpty = null;
        testDeleteTranslatedTriplesTrue = true;
        testContext1 = null;
    }
    
    /**
     * Test method for
     * {@link net.fortytwo.sesametools.URITranslator#doTranslation(org.openrdf.repository.Repository, java.lang.String, java.lang.String, java.util.Collection, java.util.Collection, java.util.Collection, boolean, org.openrdf.model.Resource[])}
     * .
     * 
     * @throws UpdateExecutionException
     * @throws MalformedQueryException
     * @throws RepositoryException
     */
    @Test
    public void testDoTranslationNoMappingOneContext() throws RepositoryException, MalformedQueryException,
        UpdateExecutionException
    {
        testRepositoryConnection.add(testInputSubjectUri1, testInputPredicateUri1, testInputObjectUri1, testContext1);
        
        Assert.assertEquals(1, testRepositoryConnection.size());
        Assert.assertEquals(1, testRepositoryConnection.size(testContext1));
        
        for(Statement nextBeforeStatement : testRepositoryConnection.getStatements(null, null, null, false,
                testContext1).asList())
        {
            LOGGER.info("nextBeforeStatement: " + nextBeforeStatement.toString());
        }
        
        URITranslator.doTranslation(testRepository, testInputUriPrefix1, testOutputUriPrefix1,
                testSubjectMappingPredicatesEmpty, testPredicateMappingPredicatesEmpty,
                testObjectMappingPredicatesEmpty, testDeleteTranslatedTriplesTrue, testContext1);
        
        for(Statement nextAfterStatement : testRepositoryConnection
                .getStatements(null, null, null, false, testContext1).asList())
        {
            LOGGER.info("nextAfterStatement: " + nextAfterStatement.toString());
        }
        
        Assert.assertEquals(1, testRepositoryConnection.size(testContext1));
        Assert.assertEquals(1, testRepositoryConnection.size());
    }
    
}
