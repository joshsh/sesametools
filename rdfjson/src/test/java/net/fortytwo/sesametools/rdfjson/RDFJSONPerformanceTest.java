/**
 * 
 */
package net.fortytwo.sesametools.rdfjson;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the performance of the RDFJSONParser and Writer
 * against the relative performance of the Sesame Turtle Parser and Writer
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class RDFJSONPerformanceTest
{
	private long queryStartTime;
	private long queryEndTime;
	private long nextTotalTime;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
        queryStartTime = System.currentTimeMillis();
		queryEndTime = 0L;
    	nextTotalTime = 0L;
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception
	{
        queryEndTime = System.currentTimeMillis();
        nextTotalTime = queryEndTime - queryStartTime;
        System.out.println("testTiming: nextTotalTime="+nextTotalTime);
        
        queryStartTime = 0L;
		queryEndTime = 0L;
    	nextTotalTime = 0L;
	}
	
	@Test
    public void testTurtleAndTurtlePerformance() throws Exception
    {
        assertNotNull(RDFJSONTestUtils.parseTurtleAndWriteTurtle("bio2rdf-configuration.ttl"));
    }
	
	@Test
    public void testJsonAndTurtlePerformance() throws Exception
    {
        assertNotNull(RDFJSONTestUtils.parseJsonAndWriteTurtle("bio2rdf-configuration.json"));
        
    }
	
	@Test
    public void testTurtleAndJsonPerformance() throws Exception
    {
        assertNotNull(RDFJSONTestUtils.parseTurtleAndWriteJson("bio2rdf-configuration.ttl"));
    }
	
	@Test
    public void testJsonAndJsonPerformance() throws Exception
    {
        assertNotNull(RDFJSONTestUtils.parseAndWrite("bio2rdf-configuration.json"));
    }
}
