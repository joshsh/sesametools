package net.fortytwo.sesametools.rdfjson;

import static org.junit.Assert.*;
import static net.fortytwo.sesametools.rdfjson.RDFJSONTestConstants.*;

import org.junit.Test;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;

/**
 * Tests the RDF/JSON writer by way of the RDF/JSON parser.
 *
 * User: josh
 * Date: Dec 21, 2010
 * Time: 5:26:27 PM
 */
public class RDFJSONWriterTest {
	@Test
    public void testAll() throws Exception {
        JSONObject j;
        JSONArray values;
        JSONArray contexts;

        j = RDFJSONTestUtils.parseAndWrite("example1.json");
//        System.out.println("j.toString="+j.toString(2));
        JSONObject a = j.getJSONObject(ARTHUR.toString());
        values = a.getJSONArray(RDF.TYPE.toString());
//        System.out.println(values.get(0));
//        System.out.println(values.get(1));
        assertEquals(2, values.length());
        assertEquals("uri", values.getJSONObject(0).getString("type"));
        assertEquals("uri", values.getJSONObject(1).getString("type"));
        JSONObject t = values.getJSONObject(0);
        if (FOAF.PERSON.toString().equals(t.getString("value"))) {
            t = values.getJSONObject(1);
        }
        //assertEquals(FOAF.PERSON.toString(), values.getJSONObject(0).getString("value"));
        assertEquals(OWL.NAMESPACE + "Thing", t.getString("value"));
        contexts = t.getJSONArray("graphs");
        assertEquals(2, contexts.length());
//        System.out.println(contexts.get(0));
//        System.out.println(contexts.get(1));
        
        assertTrue("null".equals(contexts.getString(0)) || "null".equals(contexts.getString(1)));
        values = a.getJSONArray(FOAF.KNOWS.toString());
        assertEquals(1, values.length());
        JSONObject f = values.getJSONObject(0);
        assertEquals("bnode", f.getString("type"));
        assertTrue(f.getString("value").startsWith("_:"));

        //j = parseAndWrite("example0.json");
    }
}
