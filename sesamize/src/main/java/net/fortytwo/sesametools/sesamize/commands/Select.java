package net.fortytwo.sesametools.sesamize.commands;

import net.fortytwo.sesametools.sesamize.SesamizeArgs;
import net.fortytwo.sesametools.sesamize.SparqlResultFormat;
import net.fortytwo.sesametools.sesamize.Command;
import org.apache.commons.io.IOUtils;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriter;
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.eclipse.rdf4j.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.eclipse.rdf4j.query.resultio.text.tsv.SPARQLResultsTSVWriter;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

public class Select extends Command {

    public Select() {
        super("select");

        addParameter(new Parameter<>(null, null, true, File.class, null,
                "input file"));
        addParameter(new Parameter<>(
                "inputFormat", "i", true, RDFFormat.class, RDFFormat.RDFXML,
                "input RDF format (e.g. 'N-Triples')"));
        addParameter(new Parameter<>(
                "outputFormat", "o", true, SparqlResultFormat.class, SparqlResultFormat.XML,
                "output SPARQL format (e.g. 'XML')"));
        addParameter(new Parameter<>(
                "query", null, true, File.class, null,
                "file with SPARQL SELECT query"));
    }

    @Override
    public void execute(SesamizeArgs args) throws Exception {
        File inputFile = new File(args.nonOptions.get(1));

        RDFFormat inputFormat = args.getRDFFormat(inputFile, RDFFormat.RDFXML, "i", "inputFormat");
        SparqlResultFormat outputFormat = args.getSparqlResultFormat(SparqlResultFormat.XML, "o", "outputFormat");
        String qFile = args.getOption(null, "query");

        try (InputStream fileInput = new FileInputStream(qFile)) {
            String query = IOUtils.toString(fileInput, "UTF-8");

            executeSparqlSelectQuery(query, inputFile, System.out, inputFormat, outputFormat, getBaseURI(args));
        }
    }

    private void executeSparqlSelectQuery(final String query,
                                          final File inputFile,
                                          final OutputStream out,
                                          final RDFFormat inFormat,
                                          final SparqlResultFormat outFormat,
                                          final String baseURI) throws Exception {
        TupleQueryResultWriter w;

        switch (outFormat) {
            case JSON:
                w = new SPARQLResultsJSONWriter(out);
                break;
            case XML:
                w = new SPARQLResultsXMLWriter(out);
                break;
            case TAB:
                w = new SPARQLResultsTSVWriter(out);
                break;
            default:
                throw new IllegalArgumentException("bad query result format: " + outFormat);
        }

        Sail sail = new MemoryStore();
        sail.initialize();

        try {
            Repository repo = new SailRepository(sail);
            try (RepositoryConnection rc = repo.getConnection()) {
                rc.add(inputFile, baseURI, inFormat);
                rc.commit();

                TupleQuery tq = rc.prepareTupleQuery(QueryLanguage.SPARQL, query);
                List<String> columnHeaders = new LinkedList<>();
                columnHeaders.addAll(tq.getBindings().getBindingNames());

                w.startQueryResult(columnHeaders);

                // Evaluate the first query to get all names
                try (TupleQueryResult result = tq.evaluate()) {
                    // Loop over all names, and retrieve the corresponding e-mail address.
                    while (result.hasNext()) {
                        BindingSet b = result.next();

                        w.handleSolution(b);
                    }
                }

                w.endQueryResult();
            }
        } finally {
            sail.shutDown();
        }
    }
}
