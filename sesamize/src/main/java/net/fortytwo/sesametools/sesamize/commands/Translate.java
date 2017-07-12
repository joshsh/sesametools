package net.fortytwo.sesametools.sesamize.commands;

import net.fortytwo.sesametools.sesamize.SesamizeArgs;
import net.fortytwo.sesametools.sesamize.Command;
import net.fortytwo.sesametools.sesamize.SparqlResultFormat;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.SailException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Translate extends Command {

    public Translate() {
        super("translate");

        addParameter(new Parameter<>(null, null, true, File.class, null,
                "input file"));
        addParameter(new Parameter<>(
                "inputFormat", "i", true, RDFFormat.class, RDFFormat.RDFXML,
                "input RDF format (e.g. 'N-Triples')"));
        addParameter(new Parameter<>(
                "outputFormat", "o", true, SparqlResultFormat.class, SparqlResultFormat.XML,
                "output SPARQL format (e.g. 'XML')"));
    }

    @Override
    public void execute(SesamizeArgs args) throws IOException {
        File inputFile = new File(args.nonOptions.get(1));

        RDFFormat inputFormat = args.getRDFFormat(inputFile, RDFFormat.RDFXML, "i", "inputFormat");
        RDFFormat outputFormat = args.getRDFFormat(RDFFormat.RDFXML, "o", "outputFormat");

        translateRDFDocument(inputFile, System.out, inputFormat, outputFormat, getBaseURI(args));
    }

    private void translateRDFDocument(final File inputFile,
                                           final OutputStream out,
                                           final RDFFormat inFormat,
                                           final RDFFormat outFormat,
                                           final String baseURI)
            throws SailException, IOException, RDFHandlerException, RDFParseException {

        try (InputStream in = new FileInputStream(inputFile)) {
            translateRDFDocument(in, out, inFormat, outFormat, baseURI);
        }
    }

    private void translateRDFDocument(final InputStream in,
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
}
