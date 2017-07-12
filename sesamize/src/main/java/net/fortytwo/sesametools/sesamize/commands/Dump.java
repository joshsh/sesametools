package net.fortytwo.sesametools.sesamize.commands;

import net.fortytwo.sesametools.sesamize.SesamizeArgs;
import net.fortytwo.sesametools.sesamize.Command;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Dump extends Command {
    private final static Logger logger = LoggerFactory.getLogger(Dump.class);

    public Dump() {
        super("dump");

        addParameter(new Parameter<>(null, null, true, File.class, null,
                "NativeStore directory"));
        addParameter(new Parameter<>(null, null, true, File.class, null,
                "output location (e.g. 'dump.nt')"));
        addParameter(new Parameter<>(
                "outputFormat", "o", true, RDFFormat.class, RDFFormat.RDFXML,
                "output RDF format (e.g. 'N-Triples')"));
    }

    @Override
    public void execute(SesamizeArgs args) throws Exception {
        File dir = new File(args.nonOptions.get(1));
        File file = new File(args.nonOptions.get(2));

        RDFFormat outputFormat = args.getRDFFormat(RDFFormat.RDFXML, "o", "outputFormat");

        dumpNativeStoreToRDFDocument(dir, file, outputFormat);
    }

    private void dumpNativeStoreToRDFDocument(final File nativeStoreDirectory,
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
}
