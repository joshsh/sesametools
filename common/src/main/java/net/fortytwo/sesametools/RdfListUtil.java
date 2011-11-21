/**
 *
 */
package net.fortytwo.sesametools;

import org.openrdf.OpenRDFUtil;
import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A utility for translating RDF lists to and from native Java lists.
 *
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class RdfListUtil {
    /**
     * Adds an RDF List with the given elements to a graph.
     *
     * @param head         the head resource of the list
     * @param nextValues   the list to add.  If this list is empty, no statements will be written
     * @param graphToAddTo the Graph to add the resulting list to
     * @param contexts     the graph contexts into which to add the new statements.
     *                     If no contexts are given, statements will be added to the default (null) context.
     */
    public static void addList(final Resource head,
                               final List<Value> nextValues,
                               final Graph graphToAddTo,
                               final Resource... contexts) {
        OpenRDFUtil.verifyContextNotNull(contexts);

        final ValueFactory vf = graphToAddTo.getValueFactory();

        Resource aCurr = head;

        int i = 0;

        for (final Value nextValue : nextValues) {
            // increment counter
            i++;

            final Resource aNext = vf.createBNode();

            graphToAddTo.add(aCurr, RDF.FIRST, nextValue, contexts);

            if (i < nextValues.size()) {
                graphToAddTo.add(aCurr, RDF.REST, aNext, contexts);
            } else
            // assign the rest to the rdf:nil object
            {
                graphToAddTo.add(aCurr, RDF.REST, RDF.NIL, contexts);
            }

            aCurr = aNext;
        }
    }

    /**
     * Return the contents of the list serialized as an RDF list
     *
     * @param subject      the subject of a new statement pointing to the head of the list
     * @param predicate    the predicate of a new statement pointing to the head of the list
     * @param nextValues   the list to add.  If this list is empty, only the pointer statement will be written.
     * @param graphToAddTo the Graph to add the resulting list to
     * @param contexts     the graph contexts into which to add the new statements.
     *                     If no contexts are given, statements will be added to the default (null) context.
     */
    public static void addListAtNode(final Resource subject,
                                     final URI predicate,
                                     final List<Value> nextValues,
                                     final Graph graphToAddTo,
                                     final Resource... contexts) {
        OpenRDFUtil.verifyContextNotNull(contexts);

        final ValueFactory vf = graphToAddTo.getValueFactory();

        final Resource aHead = vf.createBNode();

        if (nextValues.size() > 0) {
            graphToAddTo.add(subject, predicate, aHead, contexts);
        }

        RdfListUtil.addList(aHead, nextValues, graphToAddTo, contexts);
    }

    // TODO: is there any way to distinguish between null context (ie, the default graph) and no
    // context (ie, all graphs)
    private static void addPointerToContext(final Map<Resource, Set<Resource>> map,
                                            final Resource nextPointer,
                                            final Resource... contexts) {
        OpenRDFUtil.verifyContextNotNull(contexts);

        final List<Resource> contextsList = Arrays.asList(contexts);

        if (map.containsKey(nextPointer)) {
            map.get(nextPointer).addAll(contextsList);
        } else {
            final Set<Resource> newSet = new HashSet<Resource>();
            newSet.addAll(contextsList);
            map.put(nextPointer, newSet);
        }
    }

    /**
     * Fetches a simple (non-branching) list from a graph.
     *
     * @param head          the head of the list
     * @param graphToSearch the graph from which the list is to be fetched
     * @param contexts      the graph contexts from which the list is to be fetched
     * @return the contents of the list
     */
    public static List<Value> getList(final Resource head,
                                      final Graph graphToSearch,
                                      final Resource... contexts) {
        OpenRDFUtil.verifyContextNotNull(contexts);

        final Collection<Resource> heads = new ArrayList<Resource>(1);
        heads.add(head);

        final Collection<List<Value>> results = RdfListUtil.getLists(heads, graphToSearch, contexts);

        if (results.size() > 1) {
            throw new RuntimeException("Found more than one list, possibly due to forking");
        }

        if (results.size() == 1) {
            return results.iterator().next();
        }

        return null;
    }

    /**
     * Fetches a single headed list from the graph based on the given subject and predicate
     * <p/>
     * Note: We silently fail if no list is detected at all and return null
     * <p/>
     * In addition, only the first triple matching the subject-predicate combination is used to
     * detect the head of the list.
     *
     * @param subject       the subject of a statement pointing to the list
     * @param predicate     the predicate of a statement pointing to the list
     * @param graphToSearch the graph from which the list is to be fetched
     * @param contexts      the graph contexts from which the list is to be fetched
     * @return the contents of the list
     * @throws RuntimeException if the list structure was not complete, or it had cycles
     */
    public static List<Value> getListAtNode(final Resource subject,
                                            final URI predicate,
                                            final Graph graphToSearch,
                                            final Resource... contexts) {
        OpenRDFUtil.verifyContextNotNull(contexts);

        final Collection<List<Value>> allLists =
                RdfListUtil.getListsAtNode(subject, predicate, graphToSearch, contexts);

        if (allLists.size() > 1) {
            throw new RuntimeException("Found more than one list, possibly due to forking");
        }

        if (allLists.size() == 1) {
            return allLists.iterator().next();
        }

        // no lists found, return null
        return null;
    }

    /**
     * Fetches a collection of generalized lists, where lists are allowed to branch from head to tail.
     *
     * @param heads         the heads of the lists to fetch
     * @param graphToSearch the graph from which the list is to be fetched
     * @param contexts      the graph contexts from which the list is to be fetched
     * @return all matching lists.  If no matching lists are found, an empty collection is returned.
     */
    public static Collection<List<Value>> getLists(final Collection<Resource> heads,
                                                   final Graph graphToSearch,
                                                   final Resource... contexts) {
        OpenRDFUtil.verifyContextNotNull(contexts);

        final Map<Resource, Set<Resource>> headsMap = new HashMap<Resource, Set<Resource>>();

        for (final Resource nextHead : heads) {
            // No idea which of the contexts the head will be in, so need to use all of them
            RdfListUtil.addPointerToContext(headsMap, nextHead, contexts);
        }

        return RdfListUtil.getListsHelper(headsMap, graphToSearch);
    }

    /**
     * Fetches a collection of generalized lists based on the given subject and predicate,
     * where lists are allowed to branch from head to tail.
     *
     * @param subject       the subject of a statement pointing to the list
     * @param predicate     the predicate of a statement pointing to the list
     * @param graphToSearch the graph from which the list is to be fetched
     * @param contexts      the graph contexts from which the list is to be fetched
     * @return all matching lists.  If no matching lists are found, an empty collection is returned.
     */
    public static Collection<List<Value>> getListsAtNode(final Resource subject,
                                                         final URI predicate,
                                                         final Graph graphToSearch,
                                                         final Resource... contexts) {
        OpenRDFUtil.verifyContextNotNull(contexts);

        Collection<List<Value>> results;

        final Iterator<Statement> headStatementMatches = graphToSearch.match(subject, predicate, null, contexts);

        final Map<Resource, Set<Resource>> headsMap = new HashMap<Resource, Set<Resource>>();

        while (headStatementMatches.hasNext()) {
            final Statement nextHeadStatement = headStatementMatches.next();

            // TODO: best to silently fail here if the statement has a literal for its object??
            if (nextHeadStatement.getObject() instanceof Resource) {
                RdfListUtil.addPointerToContext(headsMap, (Resource) nextHeadStatement.getObject(),
                        nextHeadStatement.getContext());
            }
        }

        results = RdfListUtil.getListsHelper(headsMap, graphToSearch);

        return results;
    }

    /**
     * Helper method to enable exact knowledge of which contexts each head should be expected to be
     * found in, as the source of this knowledge varies from case to case
     * <p/>
     * Note: You should not need to use this method normally, it is package private to enable unit
     * testing
     *
     * @param heads         all potential heads of lists
     * @param graphToSearch the graph from which the list is to be fetched
     * @return all matching lists
     */
    static Collection<List<Value>> getListsHelper(final Map<Resource, Set<Resource>> heads,
                                                  final Graph graphToSearch) {
        final Collection<List<Value>> results = new LinkedList<List<Value>>();

        for (final Resource nextHead : heads.keySet()) {
            // this map makes sure that we don't have cycles for each head
            // it is fine for one head to attach to another through some method, so we reset this
            // map for each head
            final Map<Resource, Set<Resource>> currentPointers = new HashMap<Resource, Set<Resource>>();

            if (nextHead != null && !nextHead.equals(RDF.NIL)) {
                final Resource[] contextArray = heads.get(nextHead).toArray(new Resource[0]);

                Iterator<Statement> relevantStatements;

                // TODO: test whether a single null context in a varargs will make an array of
                // length one, if it does not we may have an issue.
                // A set should contain a single null element if varargs can be coerced this way,
                // making it possible to restrict queries to the default context
                if (contextArray.length > 0) {
                    relevantStatements = graphToSearch.match(nextHead, RDF.FIRST, null, contextArray);
                } else {
                    relevantStatements = graphToSearch.match(nextHead, RDF.FIRST, null);
                }

                Resource nextPointer = nextHead;

                while (relevantStatements.hasNext()) {
                    // for each of the matches for the head node, we plan to return at most one list
                    // as a result
                    // TODO: modify this section to clone the list each time a fork occurs
                    final List<Value> nextResult = new LinkedList<Value>();

                    final Statement headStatement = relevantStatements.next();

                    while (!nextPointer.equals(RDF.NIL)) {
                        RdfListUtil.addPointerToContext(currentPointers, nextPointer, headStatement.getContext());

                        // use the headStatement context to get the next value.
                        // TODO: Is there a rationale for recognising lists distributed across
                        // contexts?
                        // If so, replace headStatement.getContext() with contexts
                        final Value nextValue =
                                RdfListUtil.getNextValue(nextPointer, graphToSearch, headStatement.getContext());

                        if (nextValue == null) {
                            throw new RuntimeException("List structure was not complete");
                        }

                        nextResult.add(nextValue);

                        // TODO: Is there a rationale for recognising lists distributed across
                        // contexts?
                        // If so, replace headStatement.getContext() with contexts
                        nextPointer =
                                RdfListUtil.getNextPointer(nextPointer, graphToSearch, headStatement.getContext());

                        if (nextPointer == null) {
                            throw new RuntimeException("List structure was not complete");
                        }

                        if (currentPointers.containsKey(nextPointer)
                                && currentPointers.get(nextPointer).contains(headStatement.getContext())) {
                            throw new RuntimeException("List structure cannot contain cycles");
                        }
                    }

                    if (nextResult.size() > 0) {
                        results.add(nextResult);
                    }
                }
            }
        }

        return results;
    }

    // TODO: is there any way to distinguish between null context (ie, the default graph) and no
    // context (ie, all graphs)
    private static Resource getNextPointer(final Resource nextPointer,
                                           final Graph graphToSearch,
                                           final Resource... contexts) {
        OpenRDFUtil.verifyContextNotNull(contexts);

        final Iterator<Statement> pointerMatch = graphToSearch.match(nextPointer, RDF.REST, null, contexts);

        if (pointerMatch.hasNext()) {
            final Statement nextPointerMatch = pointerMatch.next();

            if (pointerMatch.hasNext()) {
                throw new RuntimeException("List structure cannot contain forks");
            }

            if (nextPointerMatch.getObject() instanceof Resource) {
                return (Resource) nextPointerMatch.getObject();
            } else {
                throw new RuntimeException("List structure cannot contain Literals as rdf:rest pointers");
            }
        } else {
            return null;
        }
    }

    // TODO: is there any way to distinguish between null context (ie, the default graph) and no
    // context (ie, all graphs)
    private static Value getNextValue(final Resource nextPointer,
                                      final Graph graphToSearch,
                                      final Resource... contexts) {
        OpenRDFUtil.verifyContextNotNull(contexts);

        final Iterator<Statement> valueMatch = graphToSearch.match(nextPointer, RDF.FIRST, null, contexts);

        if (valueMatch.hasNext()) {
            final Statement nextValueMatch = valueMatch.next();

            if (valueMatch.hasNext()) {
                throw new RuntimeException(
                        "List structure cannot contain multiple values for rdf:first items for a given subject resource");
            }

            return nextValueMatch.getObject();
        } else {
            return null;
        }
    }

    /**
     *
     */
    private RdfListUtil() {
    }
}
