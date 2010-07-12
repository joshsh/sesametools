
package net.fortytwo.sesametools;

import org.openrdf.model.Statement;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Author: josh
 * Date: Feb 26, 2008
 * Time: 5:55:30 PM
 */
public class DumpFileGenerator {
    private static void printUsage() {
        System.out.println("Usage:\tdump #triples file");
    }

    public static void main(final String[] args) throws Exception {
        if (2 != args.length) {
            printUsage();
            System.exit(1);
        }

        long totalTriples = new Long(args[0]).longValue();
        File dumpFile = new File(args[1]);
        OutputStream os = new FileOutputStream(dumpFile);

        RandomValueFactory rvf = new RandomValueFactory(
                new ValueFactoryImpl());

        RDFWriter writer = Rio.createWriter(RDFFormat.NTRIPLES, os);
        writer.startRDF();
        for (long l = 0l; l < totalTriples; l++) {
            Statement st = rvf.randomStatement();
            writer.handleStatement(st);
        }
        writer.endRDF();

        os.close();
    }
}
