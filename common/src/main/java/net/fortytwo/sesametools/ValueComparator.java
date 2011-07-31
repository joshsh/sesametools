package net.fortytwo.sesametools;

import java.util.Comparator;

import org.openrdf.model.Value;

/**
 * Implements a Comparator for OpenRDF Value objects
 */
public class ValueComparator implements Comparator<Value>
{
	public final static int BEFORE = -1;
	public final static int EQUALS = 0;
	public final static int AFTER = 1;

	// Uncomment this annotation when move to JRE6 as JRE5 has bug with this annotation => @Override
	public int compare(Value first, Value second)
	{
		if(first == null)
		{
			if(second == null)
			{
				return EQUALS;
			}
			else
			{
				return BEFORE;
			}
		}
		
		if(first == second)
		{
			return EQUALS;
		}
		
		return first.stringValue().compareTo(second.stringValue());
	}

}
