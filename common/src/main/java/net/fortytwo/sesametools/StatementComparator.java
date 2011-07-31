package net.fortytwo.sesametools;

import java.util.Comparator;

import org.openrdf.model.Statement;

/**
 * Implements a Comparator for OpenRDF Statements using the order Subject->Predicate->Object->Context
 */
public class StatementComparator implements Comparator<Statement>
{
	public final static int BEFORE = -1;
	public final static int EQUALS = 0;
	public final static int AFTER = 1;

	// Uncomment this annotation when move to JRE6 as JRE5 has bug with this annotation => @Override
	public int compare(Statement first, Statement second)
	{
		if(first.equals(second))
		{
			return EQUALS;
		}
		
		if(first.getSubject().equals(second.getSubject()))
		{
			if(first.getPredicate().equals(second.getPredicate()))
			{
				if(first.getObject().equals(second.getObject()))
				{
					if(first.getContext() == null)
					{
						if(second.getContext() == null)
						{
							return EQUALS;
						}
						else
						{
							return BEFORE;
						}
					}
					else if(second.getContext() == null)
					{
						return AFTER;
					}
					else
					{
						return first.getContext().stringValue().compareTo(second.getContext().stringValue());
					}
				}
				else
				{
					return first.getObject().stringValue().compareTo(second.getObject().stringValue());
				}
			}
			else
			{
				return first.getPredicate().stringValue().compareTo(second.getPredicate().stringValue());
			}
		}
		else
		{
			return first.getSubject().stringValue().compareTo(second.getSubject().stringValue());
		}
	}

}
