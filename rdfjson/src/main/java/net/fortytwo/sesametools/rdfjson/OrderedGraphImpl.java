/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package net.fortytwo.sesametools.rdfjson;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

import info.aduna.collections.iterators.FilterIterator;

import net.fortytwo.sesametools.StatementComparator;

import org.openrdf.OpenRDFUtil;
import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * An implementation of Graph that is guaranteed to be ordered to improve performance on RDFWriter's that require ordering
 * 
 * By default the ordering is subject->predicate->object->context
 * 
 * @author Peter Ansell - based on org.openrdf.model.impl.GraphImpl by Arjohn Kampman
 */
public class OrderedGraphImpl extends AbstractCollection<Statement> implements Graph {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5307095904382050478L;

	// Don't enforce the Set interface so others can override this class with non-Set-ordered-Collections as necessary
	protected Collection<Statement> statements;

	transient protected ValueFactory valueFactory;

	public OrderedGraphImpl(ValueFactory valueFactory) 
	{
		super();
		statements = new TreeSet<Statement>(new StatementComparator());
		setValueFactory(valueFactory);
	}

	public OrderedGraphImpl() {
		this(new ValueFactoryImpl());
	}

	public OrderedGraphImpl(ValueFactory valueFactory, Collection<? extends Statement> statements) {
		this(valueFactory);
		addAll(statements);
	}

	public OrderedGraphImpl(Collection<? extends Statement> statements) {
		this(new ValueFactoryImpl(), statements);
	}

	public ValueFactory getValueFactory() {
		return valueFactory;
	}

	public void setValueFactory(ValueFactory valueFactory) {
		this.valueFactory = valueFactory;
	}

	@Override
	public Iterator<Statement> iterator()
	{
		return statements.iterator();
	}

	@Override
	public int size()
	{
		return statements.size();
	}

	@Override
	public boolean add(Statement st)
	{
		return statements.add(st);
	}

	public boolean add(Resource subj, URI pred, Value obj, Resource... contexts) {
		OpenRDFUtil.verifyContextNotNull(contexts);

		boolean graphChanged = false;

		if (contexts.length == 0) {
			graphChanged = add(valueFactory.createStatement(subj, pred, obj));
		}
		else {
			for (Resource context : contexts) {
				graphChanged |= add(valueFactory.createStatement(subj, pred, obj, context));
			}
		}

		return graphChanged;
	}

	public Iterator<Statement> match(Resource subj, URI pred, Value obj, Resource... contexts) {
		OpenRDFUtil.verifyContextNotNull(contexts);
		return new PatternIterator(iterator(), subj, pred, obj, contexts);
	}
	
	private void writeObject(ObjectOutputStream out) 
		throws IOException 
	{
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in) 
		throws IOException, ClassNotFoundException 
	{
		in.defaultReadObject();
		setValueFactory(new ValueFactoryImpl());
	}

	/*-----------------------------*
	 * Inner class PatternIterator *
	 *-----------------------------*/

	private static class PatternIterator extends FilterIterator<Statement> {

		private Resource subj;

		private URI pred;

		private Value obj;

		private Resource[] contexts;

		public PatternIterator(Iterator<? extends Statement> iter, Resource subj, URI pred, Value obj,
				Resource... contexts)
		{
			super(iter);
			this.subj = subj;
			this.pred = pred;
			this.obj = obj;
			this.contexts = contexts;
		}

		@Override
		protected boolean accept(Statement st)
		{
			if (subj != null && !subj.equals(st.getSubject())) {
				return false;
			}
			if (pred != null && !pred.equals(st.getPredicate())) {
				return false;
			}
			if (obj != null && !obj.equals(st.getObject())) {
				return false;
			}

			if (contexts.length == 0) {
				// Any context matches
				return true;
			}
			else {
				// Accept if one of the contexts from the pattern matches
				Resource stContext = st.getContext();

				for (Resource context : contexts) {
					if (context == null && stContext == null) {
						return true;
					}
					if (context != null && context.equals(stContext)) {
						return true;
					}
				}

				return false;
			}
		}
	}
}
