/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package net.fortytwo.sesametools.nquads;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.ntriples.NTriplesUtil;

/**
 * An implementation of the RDFWriter interface that writes RDF documents in
 * N-Triples format. The N-Triples format is defined in <a
 * href="http://www.w3.org/TR/rdf-testcases/#ntriples">this section</a> of the
 * RDF Test Cases document.
 */
public class ModifiedNTriplesWriter implements RDFWriter {

	/*-----------*
	 * Variables *
	 *-----------*/

	protected final Writer writer;

	protected boolean writingStarted;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new NTriplesWriter that will write to the supplied OutputStream.
	 * 
	 * @param out
	 *        The OutputStream to write the N-Triples document to.
	 */
	public ModifiedNTriplesWriter(OutputStream out) {
		this(new OutputStreamWriter(out, Charset.forName("US-ASCII")));
	}

	/**
	 * Creates a new NTriplesWriter that will write to the supplied Writer.
	 * 
	 * @param writer
	 *        The Writer to write the N-Triples document to.
	 */
	public ModifiedNTriplesWriter(Writer writer) {
		this.writer = writer;
		writingStarted = false;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public RDFFormat getRDFFormat() {
		return RDFFormat.NTRIPLES;
	}

	public void startRDF()
		throws RDFHandlerException
	{
		if (writingStarted) {
			throw new RuntimeException("Document writing has already started");
		}

		writingStarted = true;
	}

	public void endRDF()
		throws RDFHandlerException
	{
		if (!writingStarted) {
			throw new RuntimeException("Document writing has not yet started");
		}

		try {
			writer.flush();
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
		finally {
			writingStarted = false;
		}
	}

	public void handleNamespace(String prefix, String name) {
		// N-Triples does not support namespace prefixes.
	}

	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
		if (!writingStarted) {
			throw new RuntimeException("Document writing has not yet been started");
		}

		try {
			NTriplesUtil.append(st.getSubject(), writer);
			writer.write(" ");
			NTriplesUtil.append(st.getPredicate(), writer);
			writer.write(" ");
			NTriplesUtil.append(st.getObject(), writer);
			writer.write(" .\n");
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
	}

	public void handleComment(String comment)
		throws RDFHandlerException
	{
		try {
			writer.write("# ");
			writer.write(comment);
			writer.write("\n");
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
	}
}
