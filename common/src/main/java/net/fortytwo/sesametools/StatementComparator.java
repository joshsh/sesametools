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
		// Cannot use Statement.equals as it does not take Context into account, 
		// but can check for reference equality (==)
		if(first == second)
		{
			return EQUALS;
		}
		
		if(first.getSubject().equals(second.getSubject()))
		{
			if(first.getPredicate().equals(second.getPredicate()))
			{
				if(first.getObject().equals(second.getObject()))
				{
					// Context is the only part of a statement that should legitimately be null
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
						return new ValueComparator().compare(first.getContext(), second.getContext());
					}
				}
				else
				{
					return new ValueComparator().compare(first.getObject(), second.getObject());
				}
			}
			else
			{
				return new ValueComparator().compare(first.getPredicate(), second.getPredicate());
			}
		}
		else
		{
			return new ValueComparator().compare(first.getSubject(), second.getSubject());
		}
	}

}
