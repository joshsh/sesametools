package net.fortytwo.sesametools.sesamize.commands;

import net.fortytwo.sesametools.sesamize.SesamizeArgs;
import net.fortytwo.sesametools.sesamize.Command;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static net.fortytwo.sesametools.sesamize.Sesamize.DEFAULT_BASEURI;

public class Import extends Command {
    private final static Logger logger = LoggerFactory.getLogger(Import.class);

    public Import() {
        super("import");

        addParameter(new Parameter<>(null, null, true, File.class, null,
                "NativeStore directory"));
        addParameter(new Parameter<>(null, null, true, File.class, null,
                "input location (e.g. 'dump.nt')"));
        addParameter(new Parameter<>(
                "inputFormat", "i", true, RDFFormat.class, RDFFormat.RDFXML,
                "input RDF format (e.g. 'N-Triples')"));
    }

    @Override
    public void execute(SesamizeArgs args) throws Exception {
            File dir = new File(args.nonOptions.get(1));
            File file = new File(args.nonOptions.get(2));

            RDFFormat inputFormat = args.getRDFFormat(file, RDFFormat.RDFXML, "i", "inputFormat");

            importRDFDocumentIntoNativeStore(dir, file, inputFormat);
    }

    private void importRDFDocumentIntoNativeStore(final File nativeStoreDirectory,
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
