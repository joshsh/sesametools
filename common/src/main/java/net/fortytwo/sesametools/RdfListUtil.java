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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
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

	private static boolean CHECK_CYCLES = true;

	/**
	 * Adds an RDF List with the given elements to a graph.
	 * 
	 * @param head
	 *            the head resource of the list
	 * @param nextValues
	 *            the list to add. If this list is empty, no statements will be
	 *            written
	 * @param graphToAddTo
	 *            the Graph to add the resulting list to
	 * @param contexts
	 *            the graph contexts into which to add the new statements. If no
	 *            contexts are given, statements will be added to the default
	 *            (null) context.
	 */
	public static void addList(final Resource head,
			final List<Value> nextValues, final Graph graphToAddTo,
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
	 * @param subject
	 *            the subject of a new statement pointing to the head of the
	 *            list
	 * @param predicate
	 *            the predicate of a new statement pointing to the head of the
	 *            list
	 * @param nextValues
	 *            the list to add. If this list is empty, only the pointer
	 *            statement will be written.
	 * @param graphToAddTo
	 *            the Graph to add the resulting list to
	 * @param contexts
	 *            the graph contexts into which to add the new statements. If no
	 *            contexts are given, statements will be added to the default
	 *            (null) context.
	 */
	public static void addListAtNode(final Resource subject,
			final URI predicate, final List<Value> nextValues,
			final Graph graphToAddTo, final Resource... contexts) {
		OpenRDFUtil.verifyContextNotNull(contexts);

		final ValueFactory vf = graphToAddTo.getValueFactory();

		final Resource aHead = vf.createBNode();

		if (nextValues.size() > 0) {
			graphToAddTo.add(subject, predicate, aHead, contexts);
		}

		RdfListUtil.addList(aHead, nextValues, graphToAddTo, contexts);
	}

	/**
	 * Fetches a simple (non-branching) list from a graph.
	 * 
	 * @param head
	 *            the head of the list
	 * @param graphToSearch
	 *            the graph from which the list is to be fetched
	 * @param contexts
	 *            the graph contexts from which the list is to be fetched
	 * @return the contents of the list
	 */
	public static List<Value> getList(final Resource head,
			final Graph graphToSearch, final Resource... contexts) {
		OpenRDFUtil.verifyContextNotNull(contexts);

		final Set<Resource> heads = new HashSet<Resource>(1);
		heads.add(head);

		final Collection<List<Value>> results = RdfListUtil.getLists(heads,
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
	 * <p/>
	 * Note: We silently fail if no list is detected at all and return null
	 * <p/>
	 * In addition, only the first triple matching the subject-predicate
	 * combination is used to detect the head of the list.
	 * 
	 * @param subject
	 *            the subject of a statement pointing to the list
	 * @param predicate
	 *            the predicate of a statement pointing to the list
	 * @param graphToSearch
	 *            the graph from which the list is to be fetched
	 * @param contexts
	 *            the graph contexts from which the list is to be fetched
	 * @return the contents of the list
	 * @throws RuntimeException
	 *             if the list structure was not complete, or it had cycles
	 */
	public static List<Value> getListAtNode(final Resource subject,
			final URI predicate, final Graph graphToSearch,
			final Resource... contexts) {
		OpenRDFUtil.verifyContextNotNull(contexts);

		final Collection<List<Value>> allLists = RdfListUtil.getListsAtNode(
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
	 * @param heads
	 *            the heads of the lists to fetch
	 * @param graphToSearch
	 *            the graph from which the list is to be fetched
	 * @param contexts
	 *            the graph contexts from which the list is to be fetched
	 * @return all matching lists. If no matching lists are found, an empty
	 *         collection is returned.
	 */
	public static Collection<List<Value>> getLists(final Set<Resource> heads,
			final Graph graphToSearch, final Resource... contexts) {
		OpenRDFUtil.verifyContextNotNull(contexts);

		List<List<Resource>> completedPointerTrails = new ArrayList<List<Resource>>(
				heads.size());

		for (final Resource nextHead : heads) {

			if (nextHead == null || nextHead.equals(RDF.NIL)) {
				throw new RuntimeException(
						"List structure contains nulls or RDF.NIL in a head position");
			}

			followPointerTrails(nextHead, graphToSearch,
					completedPointerTrails, contexts);
		}

		final List<List<Value>> results = getValuesForPointerTrails(
				graphToSearch, completedPointerTrails, contexts);

		return results;
	}

	private static List<List<Value>> getValuesForPointerTrails(
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
									"List structure cannot contain multiple values for rdf:first items for a given subject resource");
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

	private static void followPointerTrails(Resource nextHead,
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

	private static boolean resolveNextMatch(
			List<List<Resource>> completedPointerTrails,
			List<Resource> currentPointerTrail,
			List<List<Resource>> uncompletedPointerTrails, boolean allDone,
			final Iterator<Statement> nextMatch) {
		Statement nextMatchStatement = nextMatch.next();

		Value nextValue = nextMatchStatement.getObject();

		if (nextValue instanceof Resource) {
			Resource nextResource = (Resource) nextValue;

			ArrayList<Resource> nextTrail = new ArrayList<Resource>(
					currentPointerTrail);
			nextTrail.add(nextResource);

			if (nextResource.equals(RDF.NIL)) {
				// uncompletedPointerTrails.remove(currentPointerTrail);

				if (CHECK_CYCLES && completedPointerTrails.contains(nextTrail)) {
					throw new RuntimeException("List cannot contain cycles");
				}

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
	 * @param subject
	 *            the subject of a statement pointing to the list
	 * @param predicate
	 *            the predicate of a statement pointing to the list
	 * @param graphToSearch
	 *            the graph from which the list is to be fetched
	 * @param contexts
	 *            the graph contexts from which the list is to be fetched
	 * @return all matching lists. If no matching lists are found, an empty
	 *         collection is returned.
	 */
	public static Collection<List<Value>> getListsAtNode(
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

		results = RdfListUtil.getLists(heads, graphToSearch, contexts);

		return results;
	}

	/**
     *
     */
	private RdfListUtil() {
	}
}
