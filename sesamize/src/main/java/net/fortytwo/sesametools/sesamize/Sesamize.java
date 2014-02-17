package net.fortytwo.sesametools.sesamize;

import net.fortytwo.sesametools.RandomValueFactory;
import net.fortytwo.sesametools.SesameTools;
import org.apache.commons.io.IOUtils;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.openrdf.query.resultio.text.tsv.SPARQLResultsTSVWriter;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.sail.nativerdf.NativeStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * A collection of command-line tools for Sesame.
 *
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class Sesamize {
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

        private Subcommand(final String name) {
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

    private static void printUsage() {
        System.out.println("Usage:  sesamize [options] subcommand [arguments]");
        System.out.println("Options:\n"
                + "  -h           Print this help and exit\n"
                + "  -v           Print version information and exit");
        System.out.println("E.g.");
        System.out.println("  sesamize translate -i trig -o nq mydata.trig > mydata.nq");
        System.out.println("For more information, please see:\n"
                + "  <URL:http://github.com/joshsh/sesametools/tree/master/sesamize>.");
    }

    private static void printVersion() {
        String version = SesameTools.getProperties().getProperty(SesameTools.VERSION_PROP);
        System.out.println(NAME + " " + version);
    }

    public static void main(final String[] args) {
        SesamizeArgs a = new SesamizeArgs(args);

        if (null != a.getOption(null, "h", "help")) {
            printUsage();
            System.exit(0);
        }

        if (null != a.getOption(null, "v", "version")) {
            printVersion();
            System.exit(0);
        }

        Subcommand c = Subcommand.lookup(args[0]);

        if (null == c) {
            printUsage();
            System.exit(1);
        }

        try {
            switch (c) {
                case CONSTRUCT:
                    doConstruct(a);
                    break;
                case DUMP:
                    doDump(a);
                    break;
                case IMPORT:
                    doImport(a);
                    break;
                case RANDOM:
                    doRandom(a);
                    break;
                case SELECT:
                    doSelect(a);
                    break;
                case TRANSLATE:
                    doTranslate(a);
                    break;
            }
        } catch (Throwable t) {
            System.out.println("Exited with error: " + t);
            t.printStackTrace();
            System.exit(1);
        }
    }

    private static String getBaseURI(final SesamizeArgs args) {
        return args.getOption(DEFAULT_BASEURI, "b", "baseuri");
    }

    private static void doTranslate(final SesamizeArgs args) throws Exception {
        File inputFile = new File(args.nonOptions.get(1));

        RDFFormat inputFormat = args.getRDFFormat(inputFile, RDFFormat.RDFXML, "i", "inputFormat");
        RDFFormat outputFormat = args.getRDFFormat(RDFFormat.RDFXML, "o", "outputFormat");

        translateRDFDocument(inputFile, System.out, inputFormat, outputFormat, getBaseURI(args));
    }

    private static void doImport(final SesamizeArgs args) throws Exception {
        File dir = new File(args.nonOptions.get(1));
        File file = new File(args.nonOptions.get(2));

        RDFFormat inputFormat = args.getRDFFormat(file, RDFFormat.RDFXML, "i", "inputFormat");

        importRDFDocumentIntoNativeStore(dir, file, inputFormat);
    }

    private static void doDump(final SesamizeArgs args) throws Exception {
        File dir = new File(args.nonOptions.get(1));
        File file = new File(args.nonOptions.get(2));

        RDFFormat outputFormat = args.getRDFFormat(RDFFormat.RDFXML, "o", "outputFormat");

        dumpNativeStoreToRDFDocument(dir, file, outputFormat);
    }

    private static void doConstruct(final SesamizeArgs args) throws Exception {
        File inputFile = new File(args.nonOptions.get(1));

        RDFFormat inputFormat = args.getRDFFormat(inputFile, RDFFormat.RDFXML, "i", "inputFormat");
        RDFFormat outputFormat = args.getRDFFormat(RDFFormat.RDFXML, "o", "outputFormat");

        String qFile = args.getOption(null, "query");
        InputStream fileInput = null;

        try {
            fileInput = new FileInputStream(qFile);
            String query = IOUtils.toString(fileInput, "UTF-8");

            translateRDFDocumentUseingConstructQuery(query, inputFile, System.out, inputFormat, outputFormat, getBaseURI(args));
        } finally {
            if (fileInput != null) {
                fileInput.close();
            }
        }
    }

    private static void doRandom(final SesamizeArgs args) throws Exception {
        Long totalTriples = Long.valueOf(args.nonOptions.get(1));
        RDFFormat outputFormat = args.getRDFFormat(RDFFormat.RDFXML, "o", "outputFormat");

        OutputStream os = System.out;

        RandomValueFactory rvf = new RandomValueFactory(
                new ValueFactoryImpl());

        RDFWriter writer = Rio.createWriter(outputFormat, os);
        writer.startRDF();
        for (long l = 0l; l < totalTriples; l++) {
            Statement st = rvf.randomStatement();
            writer.handleStatement(st);
        }
        writer.endRDF();

        os.close();
    }

    private static void doSelect(final SesamizeArgs args) throws Exception {
        File inputFile = new File(args.nonOptions.get(1));

        RDFFormat inputFormat = args.getRDFFormat(inputFile, RDFFormat.RDFXML, "i", "inputFormat");
        SparqlResultFormat outputFormat = args.getSparqlResultFormat(SparqlResultFormat.XML, "o", "outputFormat");

        String qFile = args.getOption(null, "query");
        InputStream fileInput = null;

        try {
            fileInput = new FileInputStream(qFile);
            String query = IOUtils.toString(fileInput, "UTF-8");

            executeSparqlSelectQuery(query, inputFile, System.out, inputFormat, outputFormat, getBaseURI(args));
        } finally {
            if (fileInput != null) {
                fileInput.close();
            }
        }
    }

    public static void executeSparqlSelectQuery(final String query,
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
            RepositoryConnection rc = repo.getConnection();
            try {
                rc.add(inputFile, baseURI, inFormat);
                rc.commit();

                TupleQuery tq = rc.prepareTupleQuery(QueryLanguage.SPARQL, query);
                List<String> columnHeaders = new LinkedList<String>();
                columnHeaders.addAll(tq.getBindings().getBindingNames());

                w.startQueryResult(columnHeaders);

                // Evaluate the first query to get all names
                TupleQueryResult result = tq.evaluate();
                try {
                    // Loop over all names, and retrieve the corresponding e-mail address.
                    while (result.hasNext()) {
                        BindingSet b = result.next();

                        w.handleSolution(b);
                    }
                } finally {
                    result.close();
                }

                w.endQueryResult();
            } finally {
                rc.close();
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
                                                                final String baseURI) throws SailException, IOException, RDFHandlerException, RDFParseException, RepositoryException, MalformedQueryException, QueryEvaluationException {
        Sail sail = new MemoryStore();
        sail.initialize();

        try {
            Repository repo = new SailRepository(sail);
            RepositoryConnection rc = repo.getConnection();
            try {
                rc.add(inputFile, baseURI, inFormat);
                rc.commit();

                RDFWriter w = Rio.createWriter(outFormat, out);

                rc.prepareGraphQuery(QueryLanguage.SPARQL, query).evaluate(w);
            } finally {
                rc.close();
            }
        } finally {
            sail.shutDown();
        }
    }

    public static void translateRDFDocument(final File inputFile,
                                            final OutputStream out,
                                            final RDFFormat inFormat,
                                            final RDFFormat outFormat,
                                            final String baseURI) throws SailException, IOException, RDFHandlerException, RDFParseException {
        InputStream in = null;
        try {
            in = new FileInputStream(inputFile);
            translateRDFDocument(in, out, inFormat, outFormat, baseURI);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    public static void translateRDFDocument(final InputStream in,
                                            final OutputStream out,
                                            final RDFFormat inFormat,
                                            final RDFFormat outFormat,
                                            final String baseURI) throws SailException, IOException, RDFHandlerException, RDFParseException {

        RDFParser p = Rio.createParser(inFormat);
        RDFWriter w = Rio.createWriter(outFormat, out);

        p.setRDFHandler(w);

        p.parse(in, baseURI);
    }

    public static void dumpNativeStoreToRDFDocument(final File nativeStoreDirectory,
                                                    final File dumpFile,
                                                    final RDFFormat format,
                                                    final Resource... contexts) throws SailException, RepositoryException, IOException, RDFHandlerException {
        System.out.println("dumping store at " + nativeStoreDirectory + " to file " + dumpFile);

        Sail sail = new NativeStore(nativeStoreDirectory);
        sail.initialize();

        try {
            Repository repo = new SailRepository(sail);

            RepositoryConnection rc = repo.getConnection();
            try {
                OutputStream out = new FileOutputStream(dumpFile);
                try {
                    RDFHandler h = Rio.createWriter(format, out);
                    rc.export(h, contexts);
                } finally {
                    out.close();
                }
            } finally {
                rc.close();
            }
        } finally {
            sail.shutDown();
        }
    }

    public static void importRDFDocumentIntoNativeStore(final File nativeStoreDirectory,
                                                        final File dumpFile,
                                                        final RDFFormat format,
                                                        final Resource... contexts) throws SailException, RepositoryException, IOException, RDFParseException {
        System.out.println("importing file " + dumpFile + " into store at " + nativeStoreDirectory);
        Sail sail = new NativeStore(nativeStoreDirectory);
        sail.initialize();

        try {
            Repository repo = new SailRepository(sail);

            RepositoryConnection rc = repo.getConnection();
            try {
                rc.add(dumpFile, DEFAULT_BASEURI, format, contexts);
                rc.commit();
            } finally {
                rc.close();
            }
        } finally {
            sail.shutDown();
        }
    }
}
