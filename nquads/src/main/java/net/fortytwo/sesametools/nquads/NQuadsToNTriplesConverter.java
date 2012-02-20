/**
 * 
 */
package net.fortytwo.sesametools.nquads;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;

/**
 * Program to convert NQuads to NTriples for systems that do not understand NQuads, but do understand NTriples.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 *
 */
public class NQuadsToNTriplesConverter
{
    
    /**
     * @param args
     * @throws IOException 
     * @throws RDFHandlerException 
     * @throws RDFParseException 
     */
    public static void main(String[] args) throws RDFParseException, RDFHandlerException, IOException
    {
        if(args.length != 2)
        {
            System.out.println("Usage: inputNquadsFile outputNTriplesFile");
            System.exit(1);
        }
        
        FileInputStream input = new FileInputStream(new File(args[0]));
        
        FileOutputStream output = new FileOutputStream(new File(args[1]));
        
        convertNQuadsToNTriples(input, output);
    }
    
    public static void convertNQuadsToNTriples(InputStream nquads, OutputStream ntriples) throws RDFParseException, RDFHandlerException, IOException
    {
        RDFHandler handler = Rio.createWriter(RDFFormat.NTRIPLES, ntriples);
        
        RDFParser parser = Rio.createParser(NQuadsFormat.NQUADS);
        
        parser.setRDFHandler(handler);
        
        // Turn these options off as we assume there are no errors
        parser.setStopAtFirstError(false);
        parser.setPreserveBNodeIDs(false);
        parser.setVerifyData(false);
        
        parser.parse(nquads, "http://base.uri.not.defined/");
    }
}
