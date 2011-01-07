package net.fortytwo.sesametools.rdfjson;

import java.io.StringWriter;
import junit.framework.Assert;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;


/**
 *
 */
public class RDFJSONFactoriesTest extends RDFJSONTestBase {

    public void testParserFactoryRegistered(){
        RDFFormat fmt = Rio.getParserFormatForMIMEType("application/json");
        RDFParser parser = Rio.createParser(fmt);
        Assert.assertTrue(parser instanceof RDFJSONParser);
    }

    public void testWriterFactoryRegistered(){
        RDFFormat fmt = Rio.getParserFormatForMIMEType("application/json");
        RDFWriter writer = Rio.createWriter(fmt, new StringWriter());
        Assert.assertTrue(writer instanceof RDFJSONWriter);
    }
}
