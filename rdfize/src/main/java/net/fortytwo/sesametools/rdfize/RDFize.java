package net.fortytwo.sesametools.rdfize;

import net.fortytwo.sesametools.RandomValueFactory;
import net.fortytwo.sesametools.SesameTools;
import org.apache.commons.io.IOUtils;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriter;
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.eclipse.rdf4j.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.eclipse.rdf4j.query.resultio.text.tsv.SPARQLResultsTSVWriter;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.RDFParserRegistry;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.RDFWriterRegistry;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A collection of command-line tools for Sesame
 *
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class RDFize {
    private static final Logger logger = Logger.getLogger(RDFize.class.getName());

    private static final String
            NAME = "Sesamize";
    private static final String
            DEFAULT_BASEURI = "http://example.org/baseURI#";

    private enum Subcommand {
        CONSTRUCT("construct"),
        DUMP("dump"),
        IMPORT("import"),
        RANDOM("random"),
        SELECT("select"),
        TRANSLATE("translate");

        private final String name;

        Subcommand(final String name) {
            this.name = name;
        }

        public static Subcommand lookup(final String name) {
            for (Subcommand c : Subcommand.values()) {
                if (c.name.equals(name)) {
                    return c;
                }
            }

            return null;
        }
    }

    private static void printUsage(final RDFizeArgs args) {
        System.out.println("Usage:  rdfize [options] subcommand [arguments]");
        System.out.println("Options:\n"
                + "  -h           Print this help and exit\n"
                + "  -v           Print version information and exit");
        System.out.println("E.g.");
        System.out.println("  rdfize translate -i trig -o nq mydata.trig > mydata.nq");
        System.out.println("Input formats: " + formatsToCommaSeparatedString(args.getInputFormats()));
        System.out.println("Output formats: " + formatsToCommaSeparatedString(args.getOutputFormats()));
        System.out.println("For more information, please see:\n"
                + "  <URL:http://github.com/joshsh/sesametools/tree/master/rdfize>.");
    }

    private static void printVersion() {
        String version = SesameTools.getProperties().getProperty(SesameTools.VERSION_PROP);
        System.out.println(NAME + " " + version);
    }

    private static String formatsToCommaSeparatedString(final Collection<RDFFormat> formats) {
        StringBuilder sb = new StringBuilder();
        for (RDFFormat format : formats) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(format.getName().toLowerCase());
        }
        return sb.toString();
    }

    public static void main(final String[] commandLineArgs) {
        RDFizeArgs args = new RDFizeArgs(commandLineArgs);

        if (args.isEmpty()) {
            printUsage(args);
            System.exit(1);
        }

        if (null != args.getOption(null, "h", "help")) {
            printUsage(args);
            System.exit(0);
        }

        if (null != args.getOption(null, "v", "version")) {
            printVersion();
            System.exit(0);
        }

        Subcommand c = Subcommand.lookup(commandLineArgs[0]);

        if (null == c) {
            printUsage(args);
            System.exit(1);
        }

        try {
            switch (c) {
                case CONSTRUCT:
                    doConstruct(args);
                    break;
                case DUMP:
                    doDump(args);
                    break;
                case IMPORT:
                    doImport(args);
                    break;
                case RANDOM:
                    doRandom(args);
                    break;
                case SELECT:
                    doSelect(args);
                    break;
                case TRANSLATE:
                    doTranslate(args);
                    break;
            }
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Exited with error", t);
            System.exit(1);
        }
    }

    private static String getBaseURI(final RDFizeArgs args) {
        return args.getOption(DEFAULT_BASEURI, "b", "baseuri");
    }

    private static void doTranslate(final RDFizeArgs args) throws Exception {
        File inputFile = new File(args.nonOptions.get(1));

        RDFFormat inputFormat = args.getRDFFormat(inputFile, RDFFormat.RDFXML, "i", "inputFormat");
        RDFFormat outputFormat = args.getRDFFormat(RDFFormat.RDFXML, "o", "outputFormat");

        translateRDFDocument(inputFile, System.out, inputFormat, outputFormat, getBaseURI(args));
    }

    private static void doImport(final RDFizeArgs args) throws Exception {
        File dir = new File(args.nonOptions.get(1));
        File file = new File(args.nonOptions.get(2));

        RDFFormat inputFormat = args.getRDFFormat(file, RDFFormat.RDFXML, "i", "inputFormat");

        importRDFDocumentIntoNativeStore(dir, file, inputFormat);
    }

    private static void doDump(final RDFizeArgs args) throws Exception {
        File dir = new File(args.nonOptions.get(1));
        File file = new File(args.nonOptions.get(2));

        RDFFormat outputFormat = args.getRDFFormat(RDFFormat.RDFXML, "o", "outputFormat");

        dumpNativeStoreToRDFDocument(dir, file, outputFormat);
    }

    private static void doConstruct(final RDFizeArgs args) throws Exception {
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

    private static void doRandom(final RDFizeArgs args) throws Exception {
        Long totalTriples = Long.valueOf(args.nonOptions.get(1));
        RDFFormat outputFormat = args.getRDFFormat(RDFFormat.RDFXML, "o", "outputFormat");

        OutputStream os = System.out;

        RandomValueFactory rvf = new RandomValueFactory(
                SimpleValueFactory.getInstance());

        RDFWriter writer = Rio.createWriter(outputFormat, os);
        writer.startRDF();
        for (long l = 0L; l < totalTriples; l++) {
            Statement st = rvf.randomStatement();
            writer.handleStatement(st);
        }
        writer.endRDF();

        os.close();
    }

    private static void doSelect(final RDFizeArgs args) throws Exception {
        File inputFile = new File(args.nonOptions.get(1));

        RDFFormat inputFormat = args.getRDFFormat(inputFile, RDFFormat.RDFXML, "i", "inputFormat");
        SparqlResultFormat outputFormat = args.getSparqlResultFormat(SparqlResultFormat.XML, "o", "outputFormat");

        String qFile = args.getOption(null, "query");

        try (InputStream fileInput = new FileInputStream(qFile)) {
            String query = IOUtils.toString(fileInput, "UTF-8");

            executeSparqlSelectQuery(query, inputFile, System.out, inputFormat, outputFormat, getBaseURI(args));
        }
    }

    public static void executeSparqlSelectQuery(final String query,
                                                final File inputFile,
                                                final OutputStream out,
                                                final RDFFormat inFormat,
                                                final SparqlResultFormat outFormat,
                                                final String baseURI) throws Exception {
        TupleQueryResultWriter writer;

        switch (outFormat) {
            case JSON:
                writer = new SPARQLResultsJSONWriter(out);
                break;
            case XML:
                writer = new SPARQLResultsXMLWriter(out);
                break;
            case TAB:
                writer = new SPARQLResultsTSVWriter(out);
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

                writer.startQueryResult(columnHeaders);

                // Evaluate the first query to get all names
                try (TupleQueryResult result = tq.evaluate()) {
                    // Loop over all names, and retrieve the corresponding e-mail address.
                    while (result.hasNext()) {
                        BindingSet b = result.next();

                        writer.handleSolution(b);
                    }
                }

                writer.endQueryResult();
            }
        } finally {
            sail.shutDown();
        }
    }

    public static void translateRDFDocumentUseingConstructQuery(final String query,
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

    public static void translateRDFDocument(final File inputFile,
                                            final OutputStream out,
                                            final RDFFormat inFormat,
                                            final RDFFormat outFormat,
                                            final String baseURI)
            throws SailException, IOException, RDFHandlerException, RDFParseException {

        try (InputStream in = new FileInputStream(inputFile)) {
            translateRDFDocument(in, out, inFormat, outFormat, baseURI);
        }
    }

    public static void translateRDFDocument(final InputStream in,
                                            final OutputStream out,
                                            final RDFFormat inFormat,
                                            final RDFFormat outFormat,
                                            final String baseURI)
            throws SailException, IOException, RDFHandlerException, RDFParseException {

        RDFParser p = Rio.createParser(inFormat);
        RDFWriter w = Rio.createWriter(outFormat, out);

        p.setRDFHandler(w);

        p.parse(in, baseURI);
    }

    public static void dumpNativeStoreToRDFDocument(final File nativeStoreDirectory,
                                                    final File dumpFile,
                                                    final RDFFormat format,
                                                    final Resource... contexts)
            throws SailException, RepositoryException, IOException, RDFHandlerException {

        logger.info("dumping store at " + nativeStoreDirectory + " to file " + dumpFile);

        Sail sail = new NativeStore(nativeStoreDirectory);
        sail.initialize();

        try {
            Repository repo = new SailRepository(sail);

            try (RepositoryConnection rc = repo.getConnection()) {
                try (OutputStream out = new FileOutputStream(dumpFile)) {
                    RDFHandler h = Rio.createWriter(format, out);
                    rc.export(h, contexts);
                }
            }
        } finally {
            sail.shutDown();
        }
    }

    public static void importRDFDocumentIntoNativeStore(final File nativeStoreDirectory,
                                                        final File dumpFile,
                                                        final RDFFormat format,
                                                        final Resource... contexts)
            throws SailException, RepositoryException, IOException, RDFParseException {

        logger.info("importing file " + dumpFile + " into store at " + nativeStoreDirectory);
        Sail sail = new NativeStore(nativeStoreDirectory);
        sail.initialize();

        try {
            Repository repo = new SailRepository(sail);

            try (RepositoryConnection rc = repo.getConnection()) {
                rc.add(dumpFile, DEFAULT_BASEURI, format, contexts);
                rc.commit();
            }
        } finally {
            sail.shutDown();
        }
    }
}
