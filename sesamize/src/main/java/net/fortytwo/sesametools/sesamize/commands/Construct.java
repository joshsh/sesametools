package net.fortytwo.sesametools.sesamize.commands;

import net.fortytwo.sesametools.sesamize.SesamizeArgs;
import net.fortytwo.sesametools.sesamize.Command;
import org.apache.commons.io.IOUtils;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Construct extends Command {

    public Construct() {
        super("construct");

        addParameter(new Parameter<>(
                "query", null, true, File.class, null,
                "file with SPARQL CONSTRUCT query"));
        addParameter(new Parameter<>(
                "inputFormat", "i", true, RDFFormat.class, RDFFormat.RDFXML,
                "input RDF format (e.g. 'Turtle')"));
        addParameter(new Parameter<>(
                "outputFormat", "o", true, RDFFormat.class, RDFFormat.RDFXML,
                "output RDF format (e.g. 'N-Triples')"));
    }

    @Override
    public void execute(SesamizeArgs args) throws IOException {
        File inputFile = new File(args.nonOptions.get(1));

        RDFFormat inputFormat = args.getRDFFormat(inputFile, RDFFormat.RDFXML, "i", "inputFormat");
        RDFFormat outputFormat = args.getRDFFormat(RDFFormat.RDFXML, "o", "outputFormat");

        String qFile = args.getOption(null, "query");

        try (InputStream fileInput = new FileInputStream(qFile)) {

            String query = IOUtils.toString(fileInput, "UTF-8");

            translateRDFDocumentUseingConstructQuery(
                    query, inputFile, System.out, inputFormat, outputFormat, getBaseURI(args));
        }
    }

    private static void translateRDFDocumentUseingConstructQuery(final String query,
                                                                 final File inputFile,
                                                                 final OutputStream out,
                                                                 final RDFFormat inFormat,
                                                                 final RDFFormat outFormat,
                                                                 final String baseURI)
            throws SailException, IOException, RDFHandlerException, RDFParseException, RepositoryException,
            MalformedQueryException, QueryEvaluationException {

        Sail sail = new MemoryStore();
        sail.initialize();

        try {
            Repository repo = new SailRepository(sail);
            try (RepositoryConnection rc = repo.getConnection()) {
                rc.add(inputFile, baseURI, inFormat);
                rc.commit();

                RDFWriter w = Rio.createWriter(outFormat, out);

                rc.prepareGraphQuery(QueryLanguage.SPARQL, query).evaluate(w);
            }
        } finally {
            sail.shutDown();
        }
    }
}
