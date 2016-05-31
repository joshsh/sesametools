package net.fortytwo.sesametools;

import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Translates between two IRI prefixes for a given set of triples.
 *
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class URITranslator {
    private final static Logger logger = LoggerFactory.getLogger(URITranslator.class);

    /**
     * Maps IRIs for all triples in the given contexts in the given repository, between the input
     * IRI prefix and the output IRI prefix.
     *
     * @param repository      The repository containing the input triples, and which will contain the output
     *                        triples
     * @param inputUriPrefix  The string defining the start of any IRIs to look for.
     * @param outputUriPrefix The string defining the start of the IRIs which matched the inputUriPrefix, after
     *                        the translation is complete.
     * @param contexts        The contexts in the repository that are relevant to the mapping
     * @throws RepositoryException      If the repository threw an exception during the course of the method.
     * @throws MalformedQueryException  If any of the translation queries could not be executed due to an error in the
     *                                  queries or a lack of understanding of the query by the repository.
     * @throws UpdateExecutionException If the SPARQL Update queries used by this method were not able
     *                                  to be successfully executed on the given repository for some reason.
     */
    public static void doTranslation(Repository repository, final String inputUriPrefix, final String outputUriPrefix,
                                     Resource... contexts)
            throws RepositoryException, MalformedQueryException, UpdateExecutionException {

        Collection<IRI> subjectMappingPredicates = Collections.emptyList();
        Collection<IRI> predicateMappingPredicates = Collections.emptyList();
        Collection<IRI> objectMappingPredicates = Collections.emptyList();

        doTranslation(repository, inputUriPrefix, outputUriPrefix, subjectMappingPredicates,
                predicateMappingPredicates, objectMappingPredicates, true, contexts);
    }

    /**
     * Maps IRIs for all triples in the given contexts in the given repository, between the input
     * IRI prefix and the output IRI prefix.
     * <p>
     * The mapping predicates are used to define extra triples to link the input and output IRIs.
     * <p>
     * NOTE: The results for queries with deleteTranslatedTriples set to false may not be consistent
     * with what you expect.
     *
     * @param repository                     The repository containing the input triples,
     *                                       and which will contain the output triples
     * @param inputUriPrefix                 The string defining the start of any IRIs to look for.
     * @param outputUriPrefix                The string defining the start of the IRIs which matched the inputUriPrefix,
     *                                       after the translation is complete.
     * @param nextSubjectMappingPredicates   The predicates used to map subject IRIs that are translated.
     * @param nextPredicateMappingPredicates The predicates used to map predicate IRIs that are translated.
     * @param nextObjectMappingPredicates    The predicates used to map object IRIs that are translated.
     * @param deleteTranslatedTriples        If this is true, then any triples which contained translated IRIs
     *                                       will be deleted. Mapping triples will still exist if any mapping
     *                                       predicates were utilised.
     * @param contexts                       The contexts in the repository that are relevant to the mapping
     * @throws RepositoryException      If the repository threw an exception during the course of the method.
     * @throws MalformedQueryException  If any of the translation queries could not be executed due to an error in the
     *                                  queries or a lack of understanding of the query by the repository.
     * @throws UpdateExecutionException If the SPARQL Update queries used by this method were not able
     *                                  to be successfully executed on the given repository for some reason.
     */
    public static void doTranslation(Repository repository, final String inputUriPrefix, final String outputUriPrefix,
                                     final Collection<IRI> nextSubjectMappingPredicates,
                                     final Collection<IRI> nextPredicateMappingPredicates,
                                     final Collection<IRI> nextObjectMappingPredicates,
                                     boolean deleteTranslatedTriples, Resource... contexts)
            throws RepositoryException, MalformedQueryException, UpdateExecutionException {

        doTranslation(repository, inputUriPrefix, outputUriPrefix, nextSubjectMappingPredicates, true, false,
                nextPredicateMappingPredicates, true, false, nextObjectMappingPredicates, true, false, deleteTranslatedTriples,
                contexts);
    }

    /**
     * Maps IRIs for all triples in the given contexts in the given repository, between the input
     * IRI prefix and the output IRI prefix.
     * <p>
     * The mapping predicates are used to define extra triples to link the input and output IRIs.
     * <p>
     * NOTE: The results for queries with deleteTranslatedTriples set to false may not be consistent
     * with what you expect.
     *
     * @param repository                     The repository containing the input triples, and which will contain
     *                                       the output triples
     * @param inputUriPrefix                 The string defining the start of any IRIs to look for.
     * @param outputUriPrefix                The string defining the start of the IRIs which matched the inputUriPrefix,
     *                                       after the translation is complete.
     * @param nextSubjectMappingPredicates   The predicates used to map subject IRIs that are translated.
     * @param translateSubjectUris           True to translate subject IRIs and false otherwise.
     * @param exactSubjectMatchRequired      True to indicate that translation should only occur if the subject IRI
     *                                       matched the inputUriPrefix exactly.
     * @param nextPredicateMappingPredicates The predicates used to map predicate IRIs that are translated.
     * @param translatePredicateUris         True to translate predicate IRIs and false otherwise.
     * @param exactPredicateMatchRequired    True to indicate that translation should only occur if the predicate IRI
     *                                       matched the inputUriPrefix exactly.
     * @param nextObjectMappingPredicates    The predicates used to map object IRIs that are translated.
     * @param translateObjectUris            True to translate object IRIs and false otherwise.
     * @param exactObjectMatchRequired       True to indicate that translation should only occur if the object IRI
     *                                       matched the inputUriPrefix exactly.
     * @param deleteTranslatedTriples        If this is true, then any triples which contained translated IRIs
     *                                       will be deleted. Mapping triples will still exist if any mapping
     *                                       predicates were utilised.
     * @param contexts                       The contexts in the repository that are relevant to the mapping
     * @throws RepositoryException      If the repository threw an exception during the course of the method.
     * @throws MalformedQueryException  If any of the translation queries could not be executed due to an error in the
     *                                  queries or a lack of understanding of the query by the repository.
     * @throws UpdateExecutionException If the SPARQL Update queries used by this method were not able
     *                                  to be successfully executed on the given repository for some reason.
     */
    public static void doTranslation(Repository repository, final String inputUriPrefix, final String outputUriPrefix,
                                     final Collection<IRI> nextSubjectMappingPredicates, boolean translateSubjectUris,
                                     boolean exactSubjectMatchRequired,
                                     final Collection<IRI> nextPredicateMappingPredicates,
                                     boolean translatePredicateUris, boolean exactPredicateMatchRequired,
                                     final Collection<IRI> nextObjectMappingPredicates, boolean translateObjectUris,
                                     boolean exactObjectMatchRequired, boolean deleteTranslatedTriples,
                                     Resource... contexts)
            throws RepositoryException, MalformedQueryException, UpdateExecutionException {

        RepositoryConnection repositoryConnection = null;

        try {
            repositoryConnection = repository.getConnection();

            final List<String> withClauses = new ArrayList<>();

            if (contexts != null) {
                for (Resource nextResource : contexts) {
                    if (nextResource != null && nextResource instanceof IRI) {
                        withClauses.add(" WITH <" + nextResource.stringValue() + "> ");
                    } else {
                        logger.error("Did not recognise (and ignoring) the context: " + nextResource);
                    }
                }
            }

            // add a single empty with clause if they didn't include any IRI resources as contexts
            // to make the rest of the code simpler
            if (withClauses.size() == 0) {
                withClauses.add("");
            }

            if (translateObjectUris) {
                for (String nextWithClause : withClauses) {
                    final StringBuilder objectConstructBuilder =
                            new StringBuilder(nextObjectMappingPredicates.size() * 120);

                    for (final IRI nextMappingPredicate : nextObjectMappingPredicates) {
                        objectConstructBuilder.append(" ?normalisedObjectUri <" + nextMappingPredicate.stringValue()
                                + "> ?objectUri . ");
                    }

                    final StringBuilder objectTemplateWhereBuilder = new StringBuilder();

                    objectTemplateWhereBuilder.append(" ?subjectUri ?predicateUri ?objectUri . ");

                    if (!exactObjectMatchRequired) {
                        objectTemplateWhereBuilder.append("filter(isIRI(?objectUri) && strStarts(str(?objectUri), \""
                                + inputUriPrefix + "\")");
                        objectTemplateWhereBuilder.append(") . ");
                        objectTemplateWhereBuilder.append("bind(iri(concat(\"");
                        objectTemplateWhereBuilder.append(outputUriPrefix);
                        objectTemplateWhereBuilder.append("\", encode_for_uri(substr(str(?objectUri), ");
                        objectTemplateWhereBuilder.append(inputUriPrefix.length() + 1);
                        objectTemplateWhereBuilder.append(")))) AS ?normalisedObjectUri) ");
                    } else {
                        // the following should be more efficient on large queries for exact matching,
                        // as it contains constants that can be compiled down to IRIs
                        // In addition, the branch above will work with exact matching,
                        // but is prone to collisions if the IRI is used as the base of a longer IRI
                        objectTemplateWhereBuilder.append("filter(isIRI(?objectUri) && sameTerm(?objectUri, IRI(\""
                                + inputUriPrefix + "\"))). bind(iri(\""
                                + outputUriPrefix + "\") AS ?normalisedObjectUri) . ");
                    }

                    String deleteObjectTemplate;

                    if (deleteTranslatedTriples) {
                        deleteObjectTemplate = " DELETE { ?subjectUri ?predicateUri ?objectUri . } ";
                    } else {
                        deleteObjectTemplate = "";
                    }

                    final String objectTemplate =
                            nextWithClause + " " + deleteObjectTemplate
                                    + " INSERT { ?subjectUri ?predicateUri ?normalisedObjectUri . "
                                    + objectConstructBuilder.toString() + " } " + " WHERE { "
                                    + objectTemplateWhereBuilder.toString()
                                    + " } ; ";

                    logger.debug("objectTemplate=" + objectTemplate);

                    // allQueries.add(objectTemplate);

                    executeSparqlUpdateQueries(repositoryConnection, objectTemplate);
                }

                // FIXME: Sesame seems to need this, or the following queries do not work correctly
                repositoryConnection.commit();

            }

            if (translateSubjectUris) {
                for (String nextWithClause : withClauses) {
                    final StringBuilder subjectConstructBuilder =
                            new StringBuilder(nextSubjectMappingPredicates.size() * 120);

                    for (final IRI nextMappingPredicate : nextSubjectMappingPredicates) {
                        subjectConstructBuilder.append(" ?normalisedSubjectUri <" + nextMappingPredicate.stringValue()
                                + "> ?subjectUri . ");
                    }

                    final StringBuilder subjectTemplateWhereBuilder = new StringBuilder();

                    subjectTemplateWhereBuilder.append(" ?subjectUri ?predicateUri ?objectUri . ");

                    if (!exactObjectMatchRequired) {
                        subjectTemplateWhereBuilder.append("filter(isIRI(?subjectUri) && strStarts(str(?subjectUri), \""
                                + inputUriPrefix + "\")");
                        subjectTemplateWhereBuilder.append(") . ");
                        subjectTemplateWhereBuilder.append("bind(iri(concat(\"");
                        subjectTemplateWhereBuilder.append(outputUriPrefix);
                        subjectTemplateWhereBuilder.append("\", encode_for_uri(substr(str(?subjectUri), ");
                        subjectTemplateWhereBuilder.append(inputUriPrefix.length() + 1);
                        subjectTemplateWhereBuilder.append(")))) AS ?normalisedSubjectUri) ");
                    } else {
                        // the following should be more efficient on large queries for exact matching,
                        // as it contains constants that can be compiled down to IRIs
                        // In addition, the branch above will work with exact matching,
                        // but is prone to collisions if the IRI is used as the base of a longer IRI
                        subjectTemplateWhereBuilder.append("filter(isIRI(?subjectUri) && sameTerm(?subjectUri, IRI(\""
                                + inputUriPrefix + "\"))). bind(iri(\""
                                + outputUriPrefix + "\") AS ?normalisedSubjectUri) . ");
                    }

                    String deleteSubjectTemplate;

                    if (deleteTranslatedTriples) {
                        deleteSubjectTemplate = " DELETE { ?subjectUri ?predicateUri ?objectUri . } ";
                    } else {
                        deleteSubjectTemplate = "";
                    }

                    final String subjectTemplate =
                            nextWithClause + " " + deleteSubjectTemplate
                                    + " INSERT { ?normalisedSubjectUri ?predicateUri ?objectUri . "
                                    + subjectConstructBuilder.toString() + " } " + " WHERE { "
                                    + subjectTemplateWhereBuilder.toString()
                                    + " } ; ";

                    // allQueries.add(subjectTemplate);

                    executeSparqlUpdateQueries(repositoryConnection, subjectTemplate);
                }

                // FIXME: Sesame seems to need this, or the following queries do not work correctly
                repositoryConnection.commit();
            }

            if (translatePredicateUris) {
                for (String nextWithClause : withClauses) {
                    final StringBuilder predicateConstructBuilder =
                            new StringBuilder(nextPredicateMappingPredicates.size() * 120);

                    for (final IRI nextMappingPredicate : nextPredicateMappingPredicates) {
                        predicateConstructBuilder.append(" ?normalisedPredicateUri <"
                                + nextMappingPredicate.stringValue() + "> ?predicateUri . ");
                    }

                    final StringBuilder predicateTemplateWhereBuilder = new StringBuilder();

                    predicateTemplateWhereBuilder.append(" ?subjectUri ?predicateUri ?objectUri . ");

                    if (!exactObjectMatchRequired) {
                        predicateTemplateWhereBuilder.append(
                                "filter(isIRI(?predicateUri) && strStarts(str(?predicateUri), \""
                                        + inputUriPrefix + "\")");
                        predicateTemplateWhereBuilder.append(") . ");
                        predicateTemplateWhereBuilder.append("bind(iri(concat(\"");
                        predicateTemplateWhereBuilder.append(outputUriPrefix);
                        predicateTemplateWhereBuilder.append("\", encode_for_uri(substr(str(?predicateUri), ");
                        predicateTemplateWhereBuilder.append(inputUriPrefix.length() + 1);
                        predicateTemplateWhereBuilder.append(")))) AS ?normalisedPredicateUri) ");
                    } else {
                        // the following should be more efficient on large queries for exact matching,
                        // as it contains constants that can be compiled down to IRIs
                        // In addition, the branch above will work with exact matching,
                        // but is prone to collisions if the IRI is used as the base of a longer IRI
                        predicateTemplateWhereBuilder.append(
                                "filter(isIRI(?predicateUri) && sameTerm(?predicateUri, IRI(\""
                                        + inputUriPrefix + "\"))). bind(iri(\""
                                        + outputUriPrefix + "\") AS ?normalisedPredicateUri) . ");
                    }

                    String deletePredicateTemplate;

                    if (deleteTranslatedTriples) {
                        deletePredicateTemplate = " DELETE { ?subjectUri ?predicateUri ?objectUri . } ";
                    } else {
                        deletePredicateTemplate = "";
                    }

                    final String predicateTemplate =
                            nextWithClause + deletePredicateTemplate
                                    + " INSERT { ?subjectUri ?normalisedPredicateUri ?objectUri . "
                                    + predicateConstructBuilder.toString() + " } " + " WHERE { "
                                    + predicateTemplateWhereBuilder.toString() + " } ; ";

                    // allQueries.add(predicateTemplate);

                    executeSparqlUpdateQueries(repositoryConnection, predicateTemplate);
                }

                // executeSparqlUpdateQueries(repositoryConnection, allQueries);

                repositoryConnection.commit();
            }
        } catch (RepositoryException rex) {
            // rollback the connection and then throw the resulting exception
            // TODO: Will this get called before the repositoryConnection.close() in the finally
            // block?
            repositoryConnection.rollback();
            throw rex;
        } catch (MalformedQueryException mqe) {
            // rollback the connection and then throw the resulting exception
            // TODO: Will this get called before the repositoryConnection.close() in the finally
            // block?
            repositoryConnection.rollback();
            throw mqe;
        } catch (UpdateExecutionException uee) {
            // rollback the connection and then throw the resulting exception
            // TODO: Will this get called before the repositoryConnection.close() in the finally
            // block?
            repositoryConnection.rollback();
            throw uee;
        } finally {
            if (repositoryConnection != null) {
                try {
                    repositoryConnection.close();
                } catch (RepositoryException rex) {
                    logger.error("Found repository exception while trying to close repository connection", rex);
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
            throws RepositoryException, MalformedQueryException, UpdateExecutionException {
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
            throws RepositoryException, MalformedQueryException, UpdateExecutionException {
        for (String nextQuery : nextQueries) {
            logger.info("nextQuery=" + nextQuery);

            Update preparedUpdate = repositoryConnection.prepareUpdate(QueryLanguage.SPARQL, nextQuery);

            preparedUpdate.execute();
        }
    }
}
