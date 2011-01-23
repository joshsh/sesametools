package net.fortytwo.sesamize;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import net.fortytwo.sesamize.nquads.NQuadsFormat;
import net.fortytwo.sesamize.nquads.NQuadsParser;
import net.fortytwo.sesamize.nquads.NQuadsWriter;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * User: josh
 * Date: Jul 2, 2010
 * Time: 8:23:16 PM
 */
public class Sesamize {
    private static final String
            NAME = "Sesamize",
            VERSION = "0.1";
    private static final String
            DEFAULT_BASEURI = "http://example.org/baseURI#";

    private static boolean quiet;

    private enum Command {
        CONSTRUCT("construct"),
        DUMP("dump"),
        IMPORT("import"),
        SELECT("select"),
        TRANSLATE("translate");

        private final String name;

        private Command(final String name) {
            this.name = name;
        }

        public static Command lookup(final String name) {
            for (Command c : Command.values()) {
                if (c.name.equals(name)) {
                    return c;
                }
            }

            return null;
        }
    }

    private static final Map<String, RDFFormat> rdfFormatByName;

    static {
        rdfFormatByName = new HashMap<String, RDFFormat>();
        rdfFormatByName.put("rdfxml", RDFFormat.RDFXML);
        rdfFormatByName.put("rdf/xml", RDFFormat.RDFXML);
        rdfFormatByName.put("rdf", RDFFormat.RDFXML);
        rdfFormatByName.put("xml", RDFFormat.RDFXML);
        rdfFormatByName.put("trig", RDFFormat.TRIG);
        rdfFormatByName.put("turtle", RDFFormat.TURTLE);
        rdfFormatByName.put("trix", RDFFormat.TRIX);
        rdfFormatByName.put("ntriples", RDFFormat.NTRIPLES);
        rdfFormatByName.put("ntriple", RDFFormat.NTRIPLES);
        rdfFormatByName.put("nquad", NQuadsFormat.NQUADS);
        rdfFormatByName.put("nquads", NQuadsFormat.NQUADS);
    }

    public static RDFFormat findRDFFormat(final String name) {
        return rdfFormatByName.get(name);
    }

    private static void printUsage() {
        System.out.println("Usage:  sesamize [options] command [arguments]");
        System.out.println("Options:\n"
                + "  -h           Print this help and exit\n"
                + "  -q           Suppress normal output\n"
                + "  -v           Print version information and exit");
        System.out.println("E.g.");
        System.out.println("  sesamize translate -i trig -o nquads mydata.trig > mydata.nquads");
        System.out.println("For more information, please see:\n"
                + "  <URL:http://github.com/joshsh/laboratory/tree/master/sesamize>.");
    }

    private static void badUsage() {
        printUsage();
        System.exit(1);
    }

    private static void printVersion() {
        System.out.println(NAME + " " + VERSION);
    }

    public static void main(final String[] args) {
        Args a = new Args(Arrays.copyOfRange(args, 1, args.length));
        //Args a = new Args(args);
        //System.out.println("command = " + args[0]);
        Command c = Command.lookup(args[0]);

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
        Command command;

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
            command = Command.lookup(args[i].toLowerCase());
            if (null == command) {
                System.out.println("found command: " + command);
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
        }

        catch (Throwable t) {
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

    private static String getBaseURI(final Args args) {
        return args.getOption(DEFAULT_BASEURI, "b", "baseuri");
    }

    private static void doTranslate(final Args args) throws Exception {
        File inputFile = new File(args.nonOptions.get(0));

        RDFFormat inputFormat = args.getRDFFormat(inputFile, RDFFormat.RDFXML, "i", "inputFormat");
        RDFFormat outputFormat = args.getRDFFormat(RDFFormat.RDFXML, "o", "outputFormat");

        translateRDFDocument(inputFile, System.out, inputFormat, outputFormat, getBaseURI(args));
    }

    private static void doImport(final Args args) throws Exception {
        File dir = new File(args.nonOptions.get(0));
        File file = new File(args.nonOptions.get(1));

        RDFFormat inputFormat = args.getRDFFormat(file, RDFFormat.RDFXML, "i", "inputFormat");

        importRDFDocumentIntoNativeStore(dir, file, inputFormat);
    }

    private static void doDump(final Args args) throws Exception {
        File dir = new File(args.nonOptions.get(0));
        File file = new File(args.nonOptions.get(1));

        RDFFormat outputFormat = args.getRDFFormat(RDFFormat.RDFXML, "o", "outputFormat");

        dumpNativeStoreToRDFDocument(dir, file, outputFormat);
    }

    private static void doConstruct(final Args args) throws Exception {
        File inputFile = new File(args.nonOptions.get(0));

        RDFFormat inputFormat = args.getRDFFormat(inputFile, RDFFormat.RDFXML, "i", "inputFormat");
        RDFFormat outputFormat = args.getRDFFormat(RDFFormat.RDFXML, "o", "outputFormat");

        String qFile = args.getOption(null, "query");
        String query = readFileAsString(qFile);

        translateRDFDocumentUseingConstructQuery(query, inputFile, System.out, inputFormat, outputFormat, getBaseURI(args));
    }

    private static void doSelect(final Args args) throws Exception {
        File inputFile = new File(args.nonOptions.get(0));

        RDFFormat inputFormat = args.getRDFFormat(inputFile, RDFFormat.RDFXML, "i", "inputFormat");
        SparqlResultFormat outputFormat = args.getSparqlResultFormat(SparqlResultFormat.XML, "o", "outputFormat");

        String qFile = args.getOption(null, "query");
        String query = readFileAsString(qFile);

        executeSparqlSelectQuery(query, inputFile, System.out, inputFormat, outputFormat, getBaseURI(args));
    }

    /*
    public static void executeSparqlSelectQuery(final String query,
                                                final File inputFile,
                                                final OutputStream out,
                                                final RDFFormat inFormat,
                                                final SparqlResultFormat outFormat,
                                                final String baseURI) throws Exception, IOException, RDFHandlerException, RDFParseException, RepositoryException, MalformedQueryException, QueryEvaluationException {
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
                throw new Exception(new Throwable("bad query result format: " + outFormat));
        }

        List<String> columnHeaders = new LinkedList<String>();
        // FIXME: *do* specify the column headers
        //columnHeaders.add("post");
        //columnHeaders.add("content");
        //columnHeaders.add("screen_name");

        Sail sail = new MemoryStore();
        sail.initialize();

        try {
            Repository repo = new SailRepository(sail);
            RepositoryConnection rc = repo.getConnection();
            try {
                rc.add(inputFile, baseURI, inFormat);
                rc.commit();
            } finally {
                rc.close();
            }

            SailConnection sc = sail.getConnection();
            try {


                w.startQueryResult(columnHeaders);

                TupleQuery tq = rc.prepareTupleQuery(QueryLanguage.SPARQL, query);

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


                w.startQueryResult(columnHeaders);

                CloseableIteration<? extends BindingSet, QueryEvaluationException> iter
                        = evaluateQuery(query, sc);
                try {
                    while (iter.hasNext()) {
                        w.handleSolution(iter.next());
                    }
                } finally {
                    iter.close();
                }

                w.endQueryResult();


            } finally {
                sc.close();
            }
        } finally {
            sail.shutDown();
        }
    }*/


    public static void executeSparqlSelectQuery(final String query,
                                                final File inputFile,
                                                final OutputStream out,
                                                final RDFFormat inFormat,
                                                final SparqlResultFormat outFormat,
                                                final String baseURI) throws Exception, IOException, RDFHandlerException, RDFParseException, RepositoryException, MalformedQueryException, QueryEvaluationException {
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
                throw new Exception(new Throwable("bad query result format: " + outFormat));
        }

        List<String> columnHeaders = new LinkedList<String>();
        // FIXME: *do* specify the column headers
        //columnHeaders.add("post");
        //columnHeaders.add("content");
        //columnHeaders.add("screen_name");

        Sail sail = new MemoryStore();
        sail.initialize();

        try {
            Repository repo = new SailRepository(sail);
            RepositoryConnection rc = repo.getConnection();
            try {
                rc.add(inputFile, baseURI, inFormat);
                rc.commit();

                w.startQueryResult(columnHeaders);

                TupleQuery tq = rc.prepareTupleQuery(QueryLanguage.SPARQL, query);

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
    //*/

    private static RDFWriter createWriter(final RDFFormat format,
                                             final OutputStream out) {
        return NQuadsFormat.NQUADS == format
                ? new NQuadsWriter(out)
                : Rio.createWriter(format, out);
    }

    private static RDFParser createParser(final RDFFormat format) {
        return NQuadsFormat.NQUADS == format
                ? new NQuadsParser()
                : Rio.createParser(format);
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

                RDFWriter w = createWriter(outFormat, out);

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
        RDFParser p = createParser(inFormat);
        RDFWriter w = createWriter(outFormat, out);

        p.setRDFHandler(w);

        InputStream in = new FileInputStream(inputFile);
        try {
            p.parse(in, baseURI);
        } finally {
            in.close();
        }
    }

    public static void dumpNativeStoreToRDFDocument(final File nativeStoreDirectory,
                                                    final File dumpFile,
                                                    final RDFFormat format) throws SailException, RepositoryException, IOException, RDFHandlerException {
        System.out.println("dumping store at " + nativeStoreDirectory + " to file " + dumpFile);

        Sail sail = new NativeStore(nativeStoreDirectory);
        sail.initialize();

        try {
            Repository repo = new SailRepository(sail);

            RepositoryConnection rc = repo.getConnection();
            try {
                OutputStream out = new FileOutputStream(dumpFile);
                try {
                    RDFHandler h = createWriter(format, out);
                    rc.export(h);
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
                                                        final RDFFormat format) throws SailException, RepositoryException, IOException, RDFParseException {
        System.out.println("importing file " + dumpFile + " into store at " + nativeStoreDirectory);
        Sail sail = new NativeStore(nativeStoreDirectory);
        sail.initialize();

        try {
            Repository repo = new SailRepository(sail);

            RepositoryConnection rc = repo.getConnection();
            try {
                rc.add(dumpFile, DEFAULT_BASEURI, format);
                rc.commit();
            } finally {
                rc.close();
            }
        } finally {
            sail.shutDown();
        }
    }

    private static String readFileAsString(final String filePath) throws IOException {
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(
                new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        reader.close();
        return fileData.toString();
    }

    /*
    private static ParsedQuery parseQuery(final String query) throws MalformedQueryException {
        SPARQLParser parser = new SPARQLParser();
        return parser.parseQuery(query, BASE_URI);
    }

    private static synchronized CloseableIteration<? extends BindingSet, QueryEvaluationException>
    evaluateQuery(final String queryStr,
                  final SailConnection sc) throws QueryException {
        ParsedQuery query = null;
        try {
            query = parseQuery(queryStr);
        } catch (MalformedQueryException e) {
            throw new QueryException(e);
        }

        MapBindingSet bindings = new MapBindingSet();
        boolean includeInferred = false;
        try {
            return sc.evaluate(query.getTupleExpr(), query.getDataset(), bindings, includeInferred);
        } catch (SailException e) {
            throw new QueryException(e);
        }
    }*/

}
