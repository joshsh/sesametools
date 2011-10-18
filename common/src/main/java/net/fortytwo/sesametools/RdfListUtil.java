/**
 * 
 */
package net.fortytwo.sesametools;

import java.util.List;

import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
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
}
