/**
 * 
 */
package net.fortytwo.sesametools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Translates between two URI prefixes for a given set of triples.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class URITranslator
{
    private final static Logger LOGGER = LoggerFactory.getLogger(URITranslator.class);
    
    /**
     * Maps URIs for all triples in the given contexts in the given repository, between the input
     * URI prefix and the output URI prefix.
     * 
     * @param repository
     *            The repository containing the input triples, and which will contain the output
     *            triples
     * @param inputUriPrefix
     *            The string defining the start of any URIs to look for.
     * @param outputUriPrefix
     *            The string defining the start of the URIs which matched the inputUriPrefix, after
     *            the translation is complete.
     * @param contexts
     *            The contexts in the repository that are relevant to the mapping
     * @throws RepositoryException
     *             If the repository threw an exception during the course of the method.
     * @throws MalformedQueryException
     *             If any of the translation queries could not be executed due to an error in the
     *             queries or a lack of understanding of the query by the repository.
     * @throws UpdateExecutionException
     *             If the SPARQL Update queries used by this method were not able to be successfully
     *             executed on the given repository for some reason.
     */
    public static void doTranslation(Repository repository, final String inputUriPrefix, final String outputUriPrefix,
            Resource... contexts) throws RepositoryException, MalformedQueryException, UpdateExecutionException
    {
        Collection<URI> subjectMappingPredicates = Collections.emptyList();
        Collection<URI> predicateMappingPredicates = Collections.emptyList();
        Collection<URI> objectMappingPredicates = Collections.emptyList();
        
        doTranslation(repository, inputUriPrefix, outputUriPrefix, subjectMappingPredicates,
                predicateMappingPredicates, objectMappingPredicates, true, contexts);
    }
    
    /**
     * Maps URIs for all triples in the given contexts in the given repository, between the input
     * URI prefix and the output URI prefix.
     * 
     * The mapping predicates are used to define extra triples to link the input and output URIs.
     * 
     * NOTE: The results for queries with deleteTranslatedTriples set to false may not be consistent
     * with what you expect.
     * 
     * @param repository
     *            The repository containing the input triples, and which will contain the output
     *            triples
     * @param inputUriPrefix
     *            The string defining the start of any URIs to look for.
     * @param outputUriPrefix
     *            The string defining the start of the URIs which matched the inputUriPrefix, after
     *            the translation is complete.
     * @param nextSubjectMappingPredicates
     * @param nextPredicateMappingPredicates
     * @param nextObjectMappingPredicates
     * @param deleteTranslatedTriples
     *            If this is true, then any triples which contained translated URIs will be deleted.
     *            Mapping triples will still exist if any mapping predicates were utilised.
     * @param contexts
     *            The contexts in the repository that are relevant to the mapping
     * @throws RepositoryException
     *             If the repository threw an exception during the course of the method.
     * @throws MalformedQueryException
     *             If any of the translation queries could not be executed due to an error in the
     *             queries or a lack of understanding of the query by the repository.
     * @throws UpdateExecutionException
     *             If the SPARQL Update queries used by this method were not able to be successfully
     *             executed on the given repository for some reason.
     */
    public static void doTranslation(Repository repository, final String inputUriPrefix, final String outputUriPrefix,
            final Collection<URI> nextSubjectMappingPredicates, final Collection<URI> nextPredicateMappingPredicates,
            final Collection<URI> nextObjectMappingPredicates, boolean deleteTranslatedTriples, Resource... contexts)
        throws RepositoryException, MalformedQueryException, UpdateExecutionException
    {
        RepositoryConnection repositoryConnection = null;
        
        try
        {
            repositoryConnection = repository.getConnection();
            repositoryConnection.setAutoCommit(false);
            
            final List<String> withClauses = new ArrayList<String>();
            final List<String> allQueries = new ArrayList<String>();
            
            if(contexts != null)
            {
                for(Resource nextResource : contexts)
                {
                    if(nextResource != null && nextResource instanceof URI)
                    {
                        withClauses.add(" WITH <" + nextResource.stringValue() + "> ");
                    }
                    else
                    {
                        LOGGER.error("Did not recognise (and ignoring) the context: " + nextResource);
                    }
                }
            }
            
            // add a single empty with clause if they didn't include any URI resources as contexts
            // to make the rest of the code simpler
            if(withClauses.size() == 0)
            {
                withClauses.add("");
            }
            
            for(String nextWithClause : withClauses)
            {
                final StringBuilder objectConstructBuilder =
                        new StringBuilder(nextObjectMappingPredicates.size() * 120);
                
                for(final URI nextMappingPredicate : nextObjectMappingPredicates)
                {
                    objectConstructBuilder.append(" ?normalisedObjectUri <" + nextMappingPredicate.stringValue()
                            + "> ?objectUri . ");
                }
                
                final String objectTemplateWhere =
                        " ?subjectUri ?predicateUri ?objectUri . filter(isIRI(?objectUri) && strStarts(str(?objectUri), \""
                                + inputUriPrefix + "\")) . bind(iri(concat(\"" + outputUriPrefix
                                + "\", encode_for_uri(substr(str(?objectUri), " + (inputUriPrefix.length() + 1)
                                + ")))) AS ?normalisedObjectUri) ";
                
                String deleteObjectTemplate;
                
                if(deleteTranslatedTriples)
                {
                    deleteObjectTemplate = " DELETE { ?subjectUri ?predicateUri ?objectUri . } ";
                }
                else
                {
                    deleteObjectTemplate = "";
                }
                
                final String objectTemplate =
                        nextWithClause + " " + deleteObjectTemplate
                                + " INSERT { ?subjectUri ?predicateUri ?normalisedObjectUri . "
                                + objectConstructBuilder.toString() + " } " + " WHERE { " + objectTemplateWhere
                                + " } ; ";
                
                LOGGER.debug("objectTemplate=" + objectTemplate);
                
                // allQueries.add(objectTemplate);
                
                executeSparqlUpdateQueries(repositoryConnection, objectTemplate);
            }
            
            // FIXME: Sesame seems to need this, or the following queries do not work correctly
            repositoryConnection.commit();
            
            for(String nextWithClause : withClauses)
            {
                final StringBuilder subjectConstructBuilder =
                        new StringBuilder(nextSubjectMappingPredicates.size() * 120);
                
                for(final URI nextMappingPredicate : nextSubjectMappingPredicates)
                {
                    subjectConstructBuilder.append(" ?normalisedSubjectUri <" + nextMappingPredicate.stringValue()
                            + "> ?subjectUri . ");
                }
                
                final String subjectTemplateWhere =
                        " ?subjectUri ?predicateUri ?objectUri . filter(isIRI(?subjectUri) && strStarts(str(?subjectUri), \""
                                + inputUriPrefix + "\")) . bind(iri(concat(\"" + outputUriPrefix
                                + "\", encode_for_uri(substr(str(?subjectUri), " + (inputUriPrefix.length() + 1)
                                + ")))) AS ?normalisedSubjectUri) ";
                
                String deleteSubjectTemplate;
                
                if(deleteTranslatedTriples)
                {
                    deleteSubjectTemplate = " DELETE { ?subjectUri ?predicateUri ?objectUri . } ";
                }
                else
                {
                    deleteSubjectTemplate = "";
                }
                
                final String subjectTemplate =
                        nextWithClause + " " + deleteSubjectTemplate
                                + " INSERT { ?normalisedSubjectUri ?predicateUri ?objectUri . "
                                + subjectConstructBuilder.toString() + " } " + " WHERE { " + subjectTemplateWhere
                                + " } ; ";
                
                // allQueries.add(subjectTemplate);
                
                executeSparqlUpdateQueries(repositoryConnection, subjectTemplate);
            }
            
            // FIXME: Sesame seems to need this, or the following queries do not work correctly
            repositoryConnection.commit();
            
            for(String nextWithClause : withClauses)
            {
                final StringBuilder predicateConstructBuilder =
                        new StringBuilder(nextPredicateMappingPredicates.size() * 120);
                
                for(final URI nextMappingPredicate : nextPredicateMappingPredicates)
                {
                    predicateConstructBuilder.append(" ?normalisedPredicateUri <" + nextMappingPredicate.stringValue()
                            + "> ?predicateUri . ");
                }
                
                final String predicateTemplateWhere =
                        " ?subjectUri ?predicateUri ?objectUri . filter(isIRI(?predicateUri) && strStarts(str(?predicateUri), \""
                                + inputUriPrefix + "\")) . bind(iri(concat(\"" + outputUriPrefix
                                + "\", encode_for_uri(substr(str(?predicateUri), " + (inputUriPrefix.length() + 1)
                                + ")))) AS ?normalisedPredicateUri) ";
                
                String deletePredicateTemplate;
                
                if(deleteTranslatedTriples)
                {
                    deletePredicateTemplate = " DELETE { ?subjectUri ?predicateUri ?objectUri . } ";
                }
                else
                {
                    deletePredicateTemplate = "";
                }
                
                final String predicateTemplate =
                        nextWithClause + deletePredicateTemplate
                                + " INSERT { ?subjectUri ?normalisedPredicateUri ?objectUri . "
                                + predicateConstructBuilder.toString() + " } " + " WHERE { " + predicateTemplateWhere
                                + " } ; ";
                
                // allQueries.add(predicateTemplate);
                
                executeSparqlUpdateQueries(repositoryConnection, predicateTemplate);
            }
            
            // executeSparqlUpdateQueries(repositoryConnection, allQueries);
            
            repositoryConnection.commit();
        }
        catch(RepositoryException rex)
        {
            // rollback the connection and then throw the resulting exception
            // TODO: Will this get called before the repositoryConnection.close() in the finally
            // block?
            repositoryConnection.rollback();
            throw rex;
        }
        catch(MalformedQueryException mqe)
        {
            // rollback the connection and then throw the resulting exception
            // TODO: Will this get called before the repositoryConnection.close() in the finally
            // block?
            repositoryConnection.rollback();
            throw mqe;
        }
        catch(UpdateExecutionException uee)
        {
            // rollback the connection and then throw the resulting exception
            // TODO: Will this get called before the repositoryConnection.close() in the finally
            // block?
            repositoryConnection.rollback();
            throw uee;
        }
        finally
        {
            if(repositoryConnection != null)
            {
                try
                {
                    repositoryConnection.close();
                }
                catch(RepositoryException rex)
                {
                    LOGGER.error("Found repository exception while trying to close repository connection", rex);
                }
            }
        }
    }
    
    /**
     * Executes the given SPARQL Update query against the given repository.
     * 
     * @param repositoryConnection
     * @param nextQuery
     * @throws RepositoryException
     * @throws MalformedQueryException
     * @throws UpdateExecutionException
     */
    private static void executeSparqlUpdateQueries(RepositoryConnection repositoryConnection, String nextQuery)
        throws RepositoryException, MalformedQueryException, UpdateExecutionException
    {
        executeSparqlUpdateQueries(repositoryConnection, Collections.singletonList(nextQuery));
    }
    
    /**
     * Executes the given SPARQL Update queries against the given repository.
     * 
     * @param repositoryConnection
     * @param nextQueries
     * @throws RepositoryException
     * @throws MalformedQueryException
     * @throws UpdateExecutionException
     */
    private static void executeSparqlUpdateQueries(RepositoryConnection repositoryConnection, List<String> nextQueries)
        throws RepositoryException, MalformedQueryException, UpdateExecutionException
    {
        for(String nextQuery : nextQueries)
        {
            LOGGER.info("nextQuery=" + nextQuery);
            
            Update preparedUpdate = repositoryConnection.prepareUpdate(QueryLanguage.SPARQL, nextQuery);
            
            preparedUpdate.execute();
        }
    }
}
