package net.fortytwo.sesametools.sesamize.commands;

import net.fortytwo.sesametools.RandomValueFactory;
import net.fortytwo.sesametools.sesamize.SesamizeArgs;
import net.fortytwo.sesametools.sesamize.Command;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;

import java.io.File;
import java.io.OutputStream;

public class Random extends Command {

    public Random() {
        super("random");

        addParameter(new Parameter<>(null, null, true, Long.class, null,
                "total triples"));
        addParameter(new Parameter<>(
                "outputFormat", "o", true, RDFFormat.class, RDFFormat.RDFXML,
                "output RDF format (e.g. 'N-Triples')"));
    }

    @Override
    public void execute(SesamizeArgs args) throws Exception {
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
}
