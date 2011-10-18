/**
 * 
 */
package net.fortytwo.sesametools;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
	* @param nextValues the list
	* @param graphToAddTo the Graph to add the resulting list to
	* @return the list as RDF
	*/
	public static void addList(Resource subject, URI predicate, List<Value> nextValues, Graph graphToAddTo, Resource... contexts) 
	{
		ValueFactoryImpl vf = ValueFactoryImpl.getInstance();
		Resource aHead = vf.createBNode();
		Resource aCurr = aHead;
	
		if(nextValues.size() > 0)
		{
			graphToAddTo.add(subject, predicate, aHead, contexts);
		}
		
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
			else // assign the rest to the rdf:nil object
			{
				graphToAddTo.add(aCurr, RDF.REST, RDF.NIL, contexts);
			}
			
			aCurr = aNext;
		}
	
	}
	
	/**
	 * 
	 * Note: We silently fail if no list is detected at all and return an empty list
	 * 
	 * @param subject
	 * @param predicate
	 * @param graphToSearch
	 * @param context
	 * @return
	 */
	public static List<Value> getList(Resource subject, URI predicate, Graph graphToSearch, Resource context)
	{
		List<Value> results = new ArrayList<Value>();
		List<Resource> currentPointers = new ArrayList<Resource>();
		
		Iterator<Statement> headMatch = graphToSearch.match(subject, predicate, null, context);
		
		if(headMatch.hasNext())
		{
			Statement headStatement = headMatch.next();
			
			if(headStatement.getObject() instanceof Resource && !headStatement.getObject().equals(RDF.NIL))
			{
				Resource nextPointer = (Resource)headStatement.getObject();
				
				while(nextPointer != null && !nextPointer.equals(RDF.NIL))
				{
					// keep a track of the list of pointers to check for cycles
					currentPointers.add(nextPointer);
					
					Value nextValue = getNextValue(nextPointer, graphToSearch, context);
					
					if(nextValue == null)
					{
						throw new RuntimeException("List structure was not complete");
					}
					
					results.add(nextValue);
					
					nextPointer = getNextPointer(nextPointer, graphToSearch, context);
					
					if(nextPointer == null)
					{
						throw new RuntimeException("List structure was not complete");
					}
					
					if(currentPointers.contains(nextPointer))
					{
						throw new RuntimeException("List structure cannot contain cycles");
					}
				}				
			}
		}
		
		return results;
	}
	
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


	private static Value getNextValue(Resource nextPointer, Graph graphToSearch, Resource context)
	{
		Iterator<Statement> valueMatch = graphToSearch.match(nextPointer, RDF.FIRST, null, context);
		
		if(valueMatch.hasNext())
		{
			Statement nextValueMatch = valueMatch.next();
			
			if(valueMatch.hasNext())
			{
				throw new RuntimeException("List structure cannot contain multiple values for rdf:first items for a given subject resource");
			}
			
			return nextValueMatch.getObject();
		}
		else
		{
			return null;
		}
	}


}
