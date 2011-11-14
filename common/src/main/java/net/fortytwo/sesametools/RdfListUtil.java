/**
 * 
 */
package net.fortytwo.sesametools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class RdfListUtil
{
    /**
	 * 
	 */
    private RdfListUtil()
    {
        
    }
    
    /**
     * Return the contents of the list serialized as an RDF list
     * 
     * @param nextValues
     *            the list
     * @param graphToAddTo
     *            the Graph to add the resulting list to
     * @return the list as RDF
     */
    public static void addListAtNode(Resource subject, URI predicate, List<Value> nextValues, Graph graphToAddTo,
            Resource... contexts)
    {
        final ValueFactoryImpl vf = ValueFactoryImpl.getInstance();
        final Resource aHead = vf.createBNode();
        
        if(nextValues.size() > 0)
        {
            graphToAddTo.add(subject, predicate, aHead, contexts);
        }
        
        addList(aHead, nextValues, graphToAddTo, contexts);
    }
    
    public static void addList(Resource head, List<Value> nextValues, Graph graphToAddTo, Resource... contexts)
    {
        final ValueFactoryImpl vf = ValueFactoryImpl.getInstance();
        
        Resource aCurr = head;

        int i = 0;
        
        for(Value nextValue : nextValues)
        {
            // increment counter
            i++;
            
            Resource aNext = vf.createBNode();
            
            graphToAddTo.add(aCurr, RDF.FIRST, nextValue, contexts);
            
            if(i < nextValues.size())
            {
                graphToAddTo.add(aCurr, RDF.REST, aNext, contexts);
            }
            else
            // assign the rest to the rdf:nil object
            {
                graphToAddTo.add(aCurr, RDF.REST, RDF.NIL, contexts);
            }
            
            aCurr = aNext;
        }
        
    }
    
    /**
     * Fetches a single headed list from the graph based on the given subject and predicate
     * 
     * Note: We silently fail if no list is detected at all and return an empty list
     * 
     * In addition, only the first triple matching the subject-predicate combination is used to
     * detect the head of the list.
     * 
     * @param subject
     * @param predicate
     * @param graphToSearch
     * @param context
     * @throws RuntimeException
     *             if the list structure was not complete, or it had cycles
     * @return
     */
    public static List<Value> getListAtNode(Resource subject, URI predicate, Graph graphToSearch, Resource... contexts)
    {
        Collection<List<Value>> allLists = getListsAtNode(subject, predicate, graphToSearch, contexts);
        
        if(allLists.size() > 1)
        {
            throw new RuntimeException("Found more than one list, possibly due to forking");
        }
        
        if(allLists.size() == 1)
        {
            return allLists.iterator().next();
        }
        
        // no lists found, return null
        return null;
    }
    
    public static Collection<List<Value>> getListsAtNode(Resource subject, URI predicate, Graph graphToSearch,
            Resource... contexts)
    {
        Collection<List<Value>> results = new LinkedList<List<Value>>();
        
        Map<Resource, List<Resource>> currentPointers = new HashMap<Resource, List<Resource>>();
        
        // FIXME: Need to workaround bug where if contexts is a single null value that was not cast to Resource, the following will through an IllegalArgumentException
        Iterator<Statement> headStatementMatches = graphToSearch.match(subject, predicate, null, contexts);
        
        Map<Resource, Set<Resource>> headsMap = new HashMap<Resource, Set<Resource>>();
        
        while(headStatementMatches.hasNext())
        {
            Statement nextHeadStatement = headStatementMatches.next();
            
            // TODO: best to silently fail here if the statement has a literal for its object??
            if(nextHeadStatement.getObject() instanceof Resource)
            {
                addPointerToContext(headsMap, nextHeadStatement.getContext(), (Resource)nextHeadStatement.getObject());
            }
        }
        
        results = getListsHelper(headsMap, graphToSearch);
        
        // TODO: should we return empty collections or null if nothing is found? I prefer empty
        // collections
        return results;
    }
    
    public static Collection<List<Value>> getLists(Collection<Resource> heads, Graph graphToSearch,
            Resource... contexts)
    {
        return null;
    }
    
    /**
     * Helper method to enable exact knowledge of which contexts each head should be expected to be
     * found in, as the source of this knowledge varies from case to case
     * 
     * @param heads
     * @param graphToSearch
     * @return
     */
    public static Collection<List<Value>> getListsHelper(Map<Resource, Set<Resource>> heads, Graph graphToSearch)
    {
        Collection<List<Value>> results = new LinkedList<List<Value>>();
        
        for(Resource nextHead : heads.keySet())
        {
            // this map makes sure that we don't have cycles for each head
            // it is fine for one head to attach to another through some method, so we reset this
            // map for each head
            Map<Resource, Set<Resource>> currentPointers = new HashMap<Resource, Set<Resource>>();
            
            if(nextHead != null && !nextHead.equals(RDF.NIL))
            {
                Resource[] contextArray = heads.get(nextHead).toArray(new Resource[0]);
                Iterator<Statement> relevantStatements;
                
                // TODO: test whether a single null context in a varargs will make an array of
                // length one, if it does not we may have an issue.
                // A set should contain a single null element if varargs can be coerced this way,
                // making it possible to restrict queries to the default context
                if(contextArray.length > 0)
                {
                    relevantStatements = graphToSearch.match(nextHead, RDF.FIRST, null, contextArray);
                }
                else
                {
                    relevantStatements = graphToSearch.match(nextHead, RDF.FIRST, null);
                }
                
                Resource nextPointer = nextHead;
                
                while(relevantStatements.hasNext())
                {
                    // for each of the matches for the head node, we plan to return at most one list
                    // as a result
                    // TODO: modify this to allow for forking
                    List<Value> nextResult = new LinkedList<Value>();
                    
                    Statement headStatement = relevantStatements.next();
                    
                    while(nextPointer != null && !nextPointer.equals(RDF.NIL))
                    {
                        addPointerToContext(currentPointers, headStatement.getContext(), nextPointer);
                        
                        // use the headStatement context to get the next value.
                        // TODO: Is there a rationale for recognising lists distributed across
                        // contexts?
                        // If so, replace headStatement.getContext() with contexts
                        Value nextValue = getNextValue(nextPointer, graphToSearch, headStatement.getContext());
                        
                        if(nextValue == null)
                        {
                            throw new RuntimeException("List structure was not complete");
                        }
                        
                        nextResult.add(nextValue);
                        
                        // TODO: Is there a rationale for recognising lists distributed across
                        // contexts?
                        // If so, replace headStatement.getContext() with contexts
                        nextPointer = getNextPointer(nextPointer, graphToSearch, headStatement.getContext());
                        
                        if(nextPointer == null)
                        {
                            throw new RuntimeException("List structure was not complete");
                        }
                        
                        if(currentPointers.containsKey(nextPointer)
                                && currentPointers.get(nextPointer).contains(headStatement.getContext()))
                        {
                            throw new RuntimeException("List structure cannot contain cycles");
                        }
                    }
                    
                    if(nextResult.size() > 0)
                    {
                        results.add(nextResult);
                    }
                }
            }
        }
        
        return results;
    }
    
    // TODO: is there any way to distinguish between null context (ie, the default graph) and no
    // context (ie, all graphs)
    private static void addPointerToContext(Map<Resource, Set<Resource>> map, Resource context, Resource nextPointer)
    {
        if(map.containsKey(nextPointer))
        {
            map.get(nextPointer).add(context);
        }
        else
        {
            Set<Resource> newSet = new HashSet<Resource>();
            newSet.add(context);
            map.put(nextPointer, newSet);
        }
    }
    
    // TODO: is there any way to distinguish between null context (ie, the default graph) and no
    // context (ie, all graphs)
    private static Resource getNextPointer(Resource nextPointer, Graph graphToSearch, Resource context)
    {
        Iterator<Statement> pointerMatch = graphToSearch.match(nextPointer, RDF.REST, null, context);
        
        if(pointerMatch.hasNext())
        {
            Statement nextPointerMatch = pointerMatch.next();
            
            if(pointerMatch.hasNext())
            {
                throw new RuntimeException("List structure cannot contain forks");
            }
            
            if(nextPointerMatch.getObject() instanceof Resource)
            {
                return (Resource)nextPointerMatch.getObject();
            }
            else
            {
                throw new RuntimeException("List structure cannot contain Literals as rdf:rest pointers");
            }
        }
        else
        {
            return null;
        }
    }
    
    // TODO: is there any way to distinguish between null context (ie, the default graph) and no
    // context (ie, all graphs)
    private static Value getNextValue(Resource nextPointer, Graph graphToSearch, Resource context)
    {
        Iterator<Statement> valueMatch = graphToSearch.match(nextPointer, RDF.FIRST, null, context);
        
        if(valueMatch.hasNext())
        {
            Statement nextValueMatch = valueMatch.next();
            
            if(valueMatch.hasNext())
            {
                throw new RuntimeException(
                        "List structure cannot contain multiple values for rdf:first items for a given subject resource");
            }
            
            return nextValueMatch.getObject();
        }
        else
        {
            return null;
        }
    }
    
}
