/**
 * 
 */
package net.fortytwo.sesametools;
/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 * 
 * Based on GraphImpl from sesame-model.jar
 */

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


import org.openrdf.OpenRDFUtil;
import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * @author Arjohn Kampman
 */
public class ArrayListGraphImpl extends AbstractCollection<Statement> implements Graph {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5307095904382050478L;

	protected List<Statement> statements;

	transient protected ValueFactory valueFactory;

	public ArrayListGraphImpl(ValueFactory valueFactory) {
		super();
		statements = new ArrayList<Statement>(50);
		setValueFactory(valueFactory);
	}

	public ArrayListGraphImpl() {
		this(new ValueFactoryImpl());
	}

	public ArrayListGraphImpl(ValueFactory valueFactory, Collection<? extends Statement> statements) {
		this(valueFactory);
		addAll(statements);
	}

	public ArrayListGraphImpl(Collection<? extends Statement> statements) {
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
}
