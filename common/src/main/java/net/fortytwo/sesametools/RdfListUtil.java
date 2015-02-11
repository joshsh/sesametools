package net.fortytwo.sesametools;

import org.openrdf.OpenRDFUtil;
import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * A utility for translating RDF lists to and from native Java lists.
 *
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class RdfListUtil {
    private static final Logger log = LoggerFactory
            .getLogger(RdfListUtil.class);

    /**
     * The default value for checkCycles if no other value is given.
     */
    public final static boolean DEFAULT_CHECK_CYCLES = true;

    /**
     * The default value for checkIncomplete if no other value is given.
     */
    public final static boolean DEFAULT_CHECK_INCOMPLETE = true;

    /**
     * The default value for useIterativeOnError if no other value is given.
     */
    public final static boolean DEFAULT_USE_ITERATIVE_ON_ERROR = true;

    /**
     * If enabled, this causes the getLists method to throw RuntimeExceptions if cyclic lists are found.
     * <p/>
     * Disabling this property should not cause infinite loops,
     * as otherwise simple cyclic loops would always cause OutOfMemoryExceptions or StackOverflowExceptions.
     * <p/>
     * Disabling this property may result in missing lists from results.
     * <p/>
     * NOTE: Tests will fail if you disable this property.
     * <p/>
     * Defaults to the constant defined in RdfListUtil.DEFAULT_CHECK_CYCLES
     */
    private final boolean checkCycles;

    /**
     * If enabled, this causes the getLists method to throw RuntimeExceptions
     * when incomplete or invalid lists are found.
     * <p/>
     * Some of the cases checked include:
     * <p/>
     * <ul>
     * <li>whether RDF.REST predicates all map to Resource Objects</li>
     * <li>whether all of the given heads are Resources</li>
     * <li>whether RDF.REST predicates map to Resource objects
     * that contain both RDF.FIRST and valid RDF.REST statements</li>
     * </ul>
     * <p/>
     * Disabling this check may cause unexpected results, including incomplete and missing lists.
     * <p/>
     * NOTE: Tests will fail if you disable this property.
     * <p/>
     * Defaults to the constant defined in RdfListUtil.DEFAULT_CHECK_INCOMPLETE
     */
    private final boolean checkIncomplete;

    /**
     * If enabled, this causes the getLists method to switch from the recursive
     * method to the iterative method when the hardcoded recursion limit is
     * reached for a list.
     * <p/>
     * The iterative approach is slower in general than the recursive approach,
     * but can handle much deeper and wider lists.
     * <p/>
     * Defaults to the constant defined in RdfListUtil.DEFAULT_USE_ITERATIVE_ON_ERROR
     */
    private final boolean useIterativeOnError;

    /**
     * Constructs an instance of the RDF List Processing Utility using the
     * default error checking and redundancy values.
     */
    public RdfListUtil() {
        this(DEFAULT_CHECK_CYCLES, DEFAULT_CHECK_INCOMPLETE, DEFAULT_USE_ITERATIVE_ON_ERROR);
    }

    /**
     * Constructs an instance of the RDF List Processing Utility using the
     * given values to define operational checking and redundancy parameters.
     *
     * @param checkCycles         Defines whether to check for cycles in lists.
     * @param checkIncomplete     Defines whether to check for properly ended lists.
     * @param useIterativeOnError Defines whether to use iterative approach when recursive
     *                            approach fails with out of memory or stack overflow.
     */
    public RdfListUtil(boolean checkCycles, boolean checkIncomplete, boolean useIterativeOnError) {
        this.checkCycles = checkCycles;
        this.checkIncomplete = checkIncomplete;
        this.useIterativeOnError = useIterativeOnError;
    }

    /**
     * Adds an RDF List with the given elements to a graph.
     *
     * @param head         the head resource of the list
     * @param nextValues   the list to add. If this list is empty, no statements will be
     *                     written
     * @param graphToAddTo the Graph to add the resulting list to
     * @param contexts     the graph contexts into which to add the new statements. If no
     *                     contexts are given, statements will be added to the default
     *                     (null) context.
     */
    public void addList(final Resource head,
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
     * @param subject      the subject of a new statement pointing to the head of the
     *                     list
     * @param predicate    the predicate of a new statement pointing to the head of the
     *                     list
     * @param nextValues   the list to add. If this list is empty, only the pointer
     *                     statement will be written.
     * @param graphToAddTo the Graph to add the resulting list to
     * @param contexts     the graph contexts into which to add the new statements. If no
     *                     contexts are given, statements will be added to the default
     *                     (null) context.
     */
    public void addListAtNode(final Resource subject,
                              final URI predicate, final List<Value> nextValues,
                              final Graph graphToAddTo, final Resource... contexts) {
        OpenRDFUtil.verifyContextNotNull(contexts);

        final ValueFactory vf = graphToAddTo.getValueFactory();

        final Resource aHead = vf.createBNode();

        if (nextValues.size() > 0) {
            graphToAddTo.add(subject, predicate, aHead, contexts);
        }

        this.addList(aHead, nextValues, graphToAddTo, contexts);
    }

    /**
     * Fetches a simple (non-branching) list from a graph.
     *
     * @param head          the head of the list
     * @param graphToSearch the graph from which the list is to be fetched
     * @param contexts      the graph contexts from which the list is to be fetched
     * @return the contents of the list
     */
    public List<Value> getList(final Resource head,
                               final Graph graphToSearch, final Resource... contexts) {
        OpenRDFUtil.verifyContextNotNull(contexts);

        final Collection<List<Value>> results = this.getLists(Collections.singleton(head),
                graphToSearch, contexts);

        if (results.size() > 1) {
            throw new RuntimeException(
                    "Found more than one list, possibly due to forking");
        }

        if (results.size() == 1) {
            return results.iterator().next();
        }

        // no lists found, return empty collection
        return Collections.emptyList();
    }

    /**
     * Fetches a single headed list from the graph based on the given subject
     * and predicate
     * <p>
     * Note: We silently fail if no list is detected at all and return null
     * </p>
     * <p>
     * In addition, only the first triple matching the subject-predicate
     * combination is used to detect the head of the list.
     * </p>
     *
     * @param subject       the subject of a statement pointing to the list
     * @param predicate     the predicate of a statement pointing to the list
     * @param graphToSearch the graph from which the list is to be fetched
     * @param contexts      the graph contexts from which the list is to be fetched
     * @return the contents of the list
     * @throws RuntimeException if the list structure was not complete, or it had cycles
     */
    public List<Value> getListAtNode(final Resource subject,
                                     final URI predicate,
                                     final Graph graphToSearch,
                                     final Resource... contexts) {
        OpenRDFUtil.verifyContextNotNull(contexts);

        final Collection<List<Value>> allLists = this.getListsAtNode(
                subject, predicate, graphToSearch, contexts);

        if (allLists.size() > 1) {
            throw new RuntimeException(
                    "Found more than one list, possibly due to forking");
        }

        if (allLists.size() == 1) {
            return allLists.iterator().next();
        }

        // no lists found, return empty collection
        return Collections.emptyList();
    }

    /**
     * Fetches a collection of generalized lists, where lists are allowed to
     * branch from head to tail.
     *
     * @param heads         the heads of the lists to fetch
     * @param graphToSearch the graph from which the list is to be fetched
     * @param contexts      the graph contexts from which the list is to be fetched
     * @return all matching lists. If no matching lists are found, an empty
     * collection is returned.
     */
    //*
    public Collection<List<Value>> getListsIterative(final Set<Resource> heads,
                                                     final Graph graphToSearch,
                                                     final Resource... contexts) {
        OpenRDFUtil.verifyContextNotNull(contexts);

        final List<List<Value>> results = new ArrayList<List<Value>>(heads.size());

        List<List<Resource>> completedPointerTrails = new ArrayList<List<Resource>>(
                heads.size());

        for (final Resource nextHead : heads) {

            if (nextHead == null || nextHead.equals(RDF.NIL)) {
                throw new RuntimeException(
                        "List structure contains nulls or RDF.NIL in a head position");
            }

            followPointerTrails(nextHead, graphToSearch,
                    completedPointerTrails, contexts);

            results.addAll(getValuesForPointerTrails(graphToSearch, completedPointerTrails, contexts));

            completedPointerTrails.clear();
        }

//      results = getValuesForPointerTrails(
//                graphToSearch, completedPointerTrails, contexts);

        return results;
    }
    //*/

    //*
    public Collection<List<Value>> getLists(final Set<Resource> heads,
                                            final Graph graphToSearch,
                                            final Resource... contexts) {
        OpenRDFUtil.verifyContextNotNull(contexts);

        Collection<List<Value>> matches = new LinkedList<List<Value>>();

        try {
            for (Resource h : heads) {
                matches.addAll(getListsRecursive(h, graphToSearch, contexts));
            }
        } catch (RuntimeException rex) {
            if (this.getUseIterativeOnError() && rex.getMessage().contains("List was too long")) {
                matches.clear();
                matches = getListsIterative(heads, graphToSearch, contexts);
            } else {
                throw rex;
            }
        }

        return matches;
    }
    //*/

    public Collection<List<Value>> getListsRecursive(final Resource head,
                                                     final Graph graph,
                                                     final Resource... contexts) {
        OpenRDFUtil.verifyContextNotNull(contexts);

        Collection<List<Value>> matches = new LinkedList<List<Value>>();
        Set<Resource> prev = new HashSet<Resource>();

        // The length of this buffer corresponds to both the longest list and the 
        // maximum number of iterations that are supported by this implementation
        // Attempting to process a list longer than this will throw a RuntimeException 
        // after the maximum number of iterations, as it is not possible to know how 
        // long the longest list will be in advance
        Value[] buffer = new Value[1000];

        matchLists(head, graph, matches, prev, buffer, 0, contexts);

        return matches;
    }

    private void matchLists(final Resource head,
                            final Graph graph,
                            final Collection<List<Value>> matches,
                            final Set<Resource> prev,
                            final Value[] buffer,
                            final int i,
                            final Resource... contexts) {
        if (head.equals(RDF.NIL)) {  // End of list
            List<Value> finalisedList = new ArrayList<Value>(i);
            for (int j = 0; j < i; j++) {
                finalisedList.add(j, buffer[j]);
            }
            matches.add(finalisedList);
        } else if (this.getCheckIncomplete() && !(head instanceof Resource)) {
            throw new RuntimeException("List structure was not complete");
        } else if (!prev.contains(head)) {  // List continues, no cycle so far.
            prev.add(head);

            Iterator<Statement> first = graph.match(head, RDF.FIRST, null, contexts);

            if (this.getCheckIncomplete() && !first.hasNext()) {
                throw new RuntimeException("List structure was not complete");
            }

            while (first.hasNext()) {
                buffer[i] = first.next().getObject();

                Iterator<Statement> rest = graph.match(head, RDF.REST, null, contexts);

                if (this.getCheckIncomplete() && !rest.hasNext()) {
                    throw new RuntimeException("List structure was not complete");
                }

                while (rest.hasNext()) {
                    Value r = rest.next().getObject();

                    if (r instanceof Resource) {
                        if ((i + 1) >= buffer.length) {
                            throw new RuntimeException(String.format(
                                    "List was too long, maximum is %d elements long", buffer.length));
                        }
                        matchLists((Resource) r, graph, matches, prev, buffer, i + 1);
                    } else if (this.getCheckIncomplete()) {
                        throw new RuntimeException("List structure was not complete");
                    }
                }
            }

            prev.remove(head);
        } else if (prev.contains(head) && this.getCheckCycles()) {
            throw new RuntimeException("List cannot contain cycles");
        } else if (this.getCheckIncomplete()) {
            throw new RuntimeException("List structure was not complete");
        }

    }

    private List<List<Value>> getValuesForPointerTrails(
            final Graph graphToSearch,
            List<List<Resource>> completedPointerTrails,
            final Resource... contexts) {
        final List<List<Value>> results = new ArrayList<List<Value>>(
                completedPointerTrails.size());

        // Go through the pointer trails finding the corresponding
        // RDF.FIRST/Value combinations to generate the result lists
        for (List<Resource> nextPointerTrail : completedPointerTrails) {
            final List<Value> nextResult = new ArrayList<Value>();

            for (int i = 0; i < nextPointerTrail.size(); i++) {
                Resource nextPointer = nextPointerTrail.get(i);

                // Check to make sure that the last element is RDF.NIL
                if (i == (nextPointerTrail.size() - 1)) {
                    if (!nextPointer.equals(RDF.NIL)) {
                        throw new RuntimeException(
                                "Did not find RDF.NIL as the terminating element of a list");
                    }
                } else {
                    if (nextPointer.equals(RDF.NIL)) {
                        throw new RuntimeException(
                                "Found RDF.NIL inside a list trail");
                    }

                    Value nextValue = null;

                    final Iterator<Statement> valueMatch = graphToSearch.match(
                            nextPointer, RDF.FIRST, null, contexts);

                    if (valueMatch.hasNext()) {
                        final Statement nextValueMatch = valueMatch.next();

                        nextValue = nextValueMatch.getObject();

                        if (valueMatch.hasNext()) {
                            Statement errorValueMatch = valueMatch.next();

                            log.error("Found multiple rdf:first items nextValueMatch="
                                    + nextValueMatch
                                    + " errorValueMatch="
                                    + errorValueMatch);

                            throw new RuntimeException(
                                    "List structure cannot contain multiple values" +
                                            " for rdf:first items for a given subject resource");
                        }
                    }

                    if (nextValue == null) {
                        throw new RuntimeException(
                                "List structure was not complete");
                    }

                    nextResult.add(nextValue);
                }
            }

            if (nextResult.size() > 0) {
                results.add(nextResult);
            }
        }

        return results;
    }

    private void followPointerTrails(Resource nextHead,
                                     Graph graphToSearch, List<List<Resource>> completedPointerTrails,
                                     Resource... contexts) {
        OpenRDFUtil.verifyContextNotNull(contexts);

        List<Resource> firstPointerTrail = new ArrayList<Resource>();
        // add the first head to the currentPointerTrail
        firstPointerTrail.add(nextHead);

        // start off our currentPointerTrail marker list with the contents of
        // the firstPointerTrail
        List<Resource> currentPointerTrail = new ArrayList<Resource>(
                firstPointerTrail);

        List<List<Resource>> uncompletedPointerTrails = new ArrayList<List<Resource>>();

        Resource nextPointer = nextHead;

        boolean allDone = true;

        do {
            // start off thinking all are done, and then set to false as
            // necessary before the end of the loop
            allDone = true;

            // match the nextPointer with RDF.REST predicate to find next hops
            final Iterator<Statement> nextMatch = graphToSearch.match(
                    nextPointer, RDF.REST, null, contexts);

            // if there are no matches complain and throw a runtime exception
            if (!nextMatch.hasNext()) {
                throw new RuntimeException("List structure was not complete");
            }

            allDone = resolveNextMatch(completedPointerTrails,
                    currentPointerTrail, uncompletedPointerTrails, allDone,
                    nextMatch);

            if (nextMatch.hasNext()) {
                // start a loop to add all of the matches for each of the forks
                // to uncompleted list
                while (nextMatch.hasNext()) {
                    if (!resolveNextMatch(completedPointerTrails,
                            currentPointerTrail, uncompletedPointerTrails,
                            allDone, nextMatch)) {
                        allDone = false;
                    }
                }
            }

            // TODO: is allDone needed above or can we rely completely on
            // uncompletedPointerTrails
            if (uncompletedPointerTrails.isEmpty()) {
                currentPointerTrail = null;
                nextPointer = null;
                allDone = true;
            } else {
                allDone = false;
                // TODO what is the best, or different strategies for choosing
                // the next pointer trail
                currentPointerTrail = uncompletedPointerTrails
                        .remove(uncompletedPointerTrails.size() - 1);
                nextPointer = currentPointerTrail.get(currentPointerTrail
                        .size() - 1);
            }
        } while (!allDone);
    }

    private boolean resolveNextMatch(
            List<List<Resource>> completedPointerTrails,
            List<Resource> currentPointerTrail,
            List<List<Resource>> uncompletedPointerTrails, boolean allDone,
            final Iterator<Statement> nextMatch) {
        Statement nextMatchStatement = nextMatch.next();

        Value nextValue = nextMatchStatement.getObject();

        if (nextValue instanceof Resource) {
            Resource nextResource = (Resource) nextValue;

            if (this.getCheckCycles() && currentPointerTrail.contains(nextResource)) {
                throw new RuntimeException("List cannot contain cycles");
            }

            ArrayList<Resource> nextTrail = new ArrayList<Resource>(
                    currentPointerTrail);

            nextTrail.add(nextResource);

            if (nextResource.equals(RDF.NIL)) {
                // uncompletedPointerTrails.remove(currentPointerTrail);

                completedPointerTrails.add(nextTrail);
            } else {
                allDone = false;
                uncompletedPointerTrails.add(nextTrail);
            }
        } else {
            throw new RuntimeException("List structure not valid");
        }

        return allDone;
    }

    /**
     * Fetches a collection of generalized lists based on the given subject and
     * predicate, where lists are allowed to branch from head to tail.
     *
     * @param subject       the subject of a statement pointing to the list
     * @param predicate     the predicate of a statement pointing to the list
     * @param graphToSearch the graph from which the list is to be fetched
     * @param contexts      the graph contexts from which the list is to be fetched
     * @return all matching lists. If no matching lists are found, an empty
     * collection is returned.
     */
    public Collection<List<Value>> getListsAtNode(
            final Resource subject, final URI predicate,
            final Graph graphToSearch, final Resource... contexts) {
        OpenRDFUtil.verifyContextNotNull(contexts);

        Collection<List<Value>> results;

        final Iterator<Statement> headStatementMatches = graphToSearch.match(
                subject, predicate, null, contexts);

        final Set<Resource> heads = new HashSet<Resource>();

        while (headStatementMatches.hasNext()) {
            final Statement nextHeadStatement = headStatementMatches.next();

            if (nextHeadStatement.getObject() instanceof Resource) {
                heads.add((Resource) nextHeadStatement.getObject());
            }
        }

        results = this.getLists(heads, graphToSearch, contexts);

        return results;
    }

    /**
     * @return True if this utility is setup to check for cycles and throw
     * exceptions if it finds cycles in lists.
     */
    public boolean getCheckCycles() {
        return checkCycles;
    }

    /**
     * @return True if this utility is setup to check for incomplete,
     * unterminated lists, and throw exceptions if it finds any.
     */
    public boolean getCheckIncomplete() {
        return checkIncomplete;
    }

    /**
     * @return True if this utility is setup to use a slower iterative approach
     * then a recursive approach fails.
     */
    public boolean getUseIterativeOnError() {
        return useIterativeOnError;
    }

}
