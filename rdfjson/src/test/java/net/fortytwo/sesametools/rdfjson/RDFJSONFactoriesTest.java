package net.fortytwo.sesametools.rdfjson;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.StringWriter;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;


/**
 *
 */
public class RDFJSONFactoriesTest {

	@Test
    public void testParserFactoryRegistered(){
        RDFFormat fmt = Rio.getParserFormatForMIMEType("application/json");
        RDFParser parser = Rio.createParser(fmt);
        assertTrue(parser instanceof RDFJSONParser);
    }
	
	@Test
    public void testWriterFactoryRegistered(){
        RDFFormat fmt = Rio.getParserFormatForMIMEType("application/json");
        RDFWriter writer = Rio.createWriter(fmt, new StringWriter());
        assertTrue(writer instanceof RDFJSONWriter);
    }
}
