package net.fortytwo.sesametools.sesamize;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import org.apache.commons.io.IOUtils;
import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
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
import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * A collection of command-line tools for Sesame.
 *
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class Sesamize {
    private static final String
            NAME = "Sesamize",
            VERSION = "0.6";
    private static final String
            DEFAULT_BASEURI = "http://example.org/baseURI#";

    private static boolean quiet;

    static {
        // Note: this may no longer be necessary
        RDFFormat.register(RDFFormat.NQUADS);
    }
    
    private enum Subcommand {
        CONSTRUCT("construct"),
        DUMP("dump"),
        IMPORT("import"),
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
                + "  -q           Suppress normal output\n"
                + "  -v           Print version information and exit");
        System.out.println("E.g.");
        System.out.println("  sesamize translate -i trig -o nquads mydata.trig > mydata.nquads");
        System.out.println("For more information, please see:\n"
                + "  <URL:http://github.com/joshsh/sesametools/tree/master/sesamize>.");
    }

    private static void badUsage() {
        printUsage();
        System.exit(1);
    }

    private static void printVersion() {
        System.out.println(NAME + " " + VERSION);
    }

    public static void main(final String[] args) {
        SesamizeArgs a = new SesamizeArgs(Arrays.copyOfRange(args, 1, args.length));
        //Args a = new Args(args);
        //System.out.println("command = " + args[0]);
        Subcommand c = Subcommand.lookup(args[0]);

        if (null == c) {
            badUsage();
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
        //String[] newArgs = new String[]{"sesamize", "-v", "merge"};
        //mainOld(newArgs);
    }

    public static void mainOld(final String[] args) {
        // Default values.
        quiet = false;
        boolean showVersion = false, showHelp = false;
        //File inputFile = null;
        Subcommand subcommand;

        // Long options are available but are not advertised.
        LongOpt[] longOptions = {
                new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
                new LongOpt("quiet", LongOpt.NO_ARGUMENT, null, 'q'),
                new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'v')};

        Getopt g = new Getopt(NAME, args, "hqv", longOptions);
        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 'h':
                case 0:
                    showHelp = true;
                    break;
                case 'q':
                case 1:
                    quiet = true;
                    break;
                case 'v':
                case 2:
                    showVersion = true;
                    break;
                case '?':
                    // Note: getopt() already printed an error
                    printUsage();
                    System.exit(1);
                    break;
                default:
                    System.err.print("getopt() returned " + c + "\n");
            }
        }

        int i = g.getOptind();
        if (i < args.length) {
            // Too many non-option arguments.
            if (args.length - i > 2) {
                printUsage();
                System.exit(1);
            }

            //inputFile = new File(args[i]);
            System.out.println("a -> " + args[i]);
            subcommand = Subcommand.lookup(args[i].toLowerCase());
            if (null == subcommand) {
                System.out.println("found command: " + subcommand);
                printUsage();
                System.exit(1);
            }
        }

        if (showHelp) {
            printUsage();
            System.exit(0);
        }

        if (showVersion) {
            printVersion();
            System.exit(0);
        }

// System.out.println( "quiet = " + quiet );
// System.out.println( "showVersion = " + showVersion );
// System.out.println( "format = " + format );
// System.out.println( "store = " + store );

        try {
            execute(System.in, System.out, System.err);
        } catch (Throwable t) {
            System.out.println("Exited with error: " + t);
            t.printStackTrace();
            System.exit(1);
        }

        // Exit despite any remaining active threads.
        System.exit(0);
    }

    private static void execute(final InputStream in,
                                final PrintStream out,
                                final PrintStream err) {

    }

    private static String getBaseURI(final SesamizeArgs args) {
        return args.getOption(DEFAULT_BASEURI, "b", "baseuri");
    }

    private static void doTranslate(final SesamizeArgs args) throws Exception {
        File inputFile = new File(args.nonOptions.get(0));

        RDFFormat inputFormat = args.getRDFFormat(inputFile, RDFFormat.RDFXML, "i", "inputFormat");
        RDFFormat outputFormat = args.getRDFFormat(RDFFormat.RDFXML, "o", "outputFormat");

        translateRDFDocument(inputFile, System.out, inputFormat, outputFormat, getBaseURI(args));
    }

    private static void doImport(final SesamizeArgs args) throws Exception {
        File dir = new File(args.nonOptions.get(0));
        File file = new File(args.nonOptions.get(1));

        RDFFormat inputFormat = args.getRDFFormat(file, RDFFormat.RDFXML, "i", "inputFormat");

        importRDFDocumentIntoNativeStore(dir, file, inputFormat);
    }

    private static void doDump(final SesamizeArgs args) throws Exception {
        File dir = new File(args.nonOptions.get(0));
        File file = new File(args.nonOptions.get(1));

        RDFFormat outputFormat = args.getRDFFormat(RDFFormat.RDFXML, "o", "outputFormat");

        dumpNativeStoreToRDFDocument(dir, file, outputFormat);
    }

    private static void doConstruct(final SesamizeArgs args) throws Exception {
        File inputFile = new File(args.nonOptions.get(0));

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

    private static void doSelect(final SesamizeArgs args) throws Exception {
        File inputFile = new File(args.nonOptions.get(0));

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
                w = new SPARQLResultsTabWriter(out);
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
