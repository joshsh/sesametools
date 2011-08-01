package net.fortytwo.sesametools;

import java.util.Comparator;

import org.openrdf.model.Statement;

/**
 * Implements a Comparator for OpenRDF Statements 
 * using the order Subject->Predicate->Object->Context
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class StatementComparator implements Comparator<Statement>
{
	public final static int BEFORE = -1;
	public final static int EQUALS = 0;
	public final static int AFTER = 1;

	@Override
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
//							System.out.println("both contexts were null, returning EQUALS first="+first+" second="+second);
							return EQUALS;
						}
						else
						{
//							System.out.println("first context was null, but second was not null, returning BEFORE first="+first+" second="+second);
							return BEFORE;
						}
					}
					else if(second.getContext() == null)
					{
//						System.out.println("first context was not null, but second was null, returning AFTER first="+first+" second="+second);
						return AFTER;
					}
					else
					{
//						System.out.println("first context was not null, and second was not null, returning comparison first="+first+" second="+second+" result="+first.getContext().stringValue().compareTo(second.getContext().stringValue()));
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
