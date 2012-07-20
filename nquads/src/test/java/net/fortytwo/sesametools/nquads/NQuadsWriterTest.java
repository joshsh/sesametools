package net.fortytwo.sesametools.nquads;
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.StatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Test case for {@link NQuadsWriter}.
 *
 * @author Michele Mostarda (mostarda@fbk.eu)
 */
public class NQuadsWriterTest {

    private static final Logger logger  = LoggerFactory.getLogger(NQuadsWriterTest.class);

    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    private NQuadsWriter writer;

    private ValueFactory vf;

    @Before
    public void setUp() {
        writer = new NQuadsWriter(baos);
        vf = ValueFactoryImpl.getInstance();
    }

    @After
    public void tearDown() {
        logger.debug( "\n" + baos.toString() );
        baos.reset();
        writer = null;
        vf = null;
    }

    @Test
    public void testWrite() throws RDFHandlerException {
        Statement s1 = vf.createStatement(
                vf.createURI("http://sub"),
                vf.createURI("http://pre"),
                vf.createURI("http://obj"),
                vf.createURI("http://gra1")
        );
        Statement s2 = vf.createStatement(
                vf.createBNode("1"),
                vf.createURI("http://pre"),
                vf.createBNode("2"),
                vf.createURI("http://gra2")
        );
        Statement s3 = vf.createStatement(
                vf.createBNode("3"),
                vf.createURI("http://pre"),
                vf.createLiteral("Sample text 1"),
                vf.createURI("http://gra2")
        );
        Statement s4 = vf.createStatement(
                vf.createBNode("4"),
                vf.createURI("http://pre"),
                vf.createLiteral("Sample text 2", "en"),
                vf.createURI("http://gra2")
        );
        Statement s5 = vf.createStatement(
                vf.createBNode("5"),
                vf.createURI("http://pre"),
                vf.createLiteral("12345", new URIImpl("http://www.w3.org/2001/XMLSchema#integer")),
                vf.createURI("http://gra2")
        );
        Statement s6 = vf.createStatement(
                vf.createURI("p1:sub"),
                vf.createURI("p1:pre"),
                vf.createURI("p1:obj"),
                vf.createURI("p1:gra2")
        );
        Statement s7 = vf.createStatement(
                vf.createURI("http://sub"),
                vf.createURI("http://pre"),
                vf.createLiteral("This is line 1.\nThis is line 2.\n"),
                vf.createURI("http://gra3")
        );

        // Sending events.
        writer.startRDF();
        //writer.handleNamespace("p1", "http://test.com/");
        writer.handleStatement(s1);
        writer.handleStatement(s2);
        writer.handleStatement(s3);
        writer.handleStatement(s4);
        writer.handleStatement(s5);
        writer.handleStatement(s6);
        writer.handleStatement(s7);
        writer.endRDF();

        // Checking content.
        String content = baos.toString();
        String[] lines = content.split("\n");
        Assert.assertEquals("Unexpected number of lines.", 7, lines.length);
        Assert.assertTrue( lines[0].matches("<.*> <.*> <.*> <.*> \\.") );
        Assert.assertTrue( lines[1].matches("_:.* <.*> _:.* <.*> \\.") );
        Assert.assertTrue( lines[2].matches("_:.* <.*> \".*\" <.*> \\.") );
        Assert.assertTrue( lines[3].matches("_:.* <.*> \".*\"@en <.*> \\.") );
        Assert.assertTrue( lines[4].matches("_:.* <.*> \".*\"\\^\\^<.*> <.*> \\.") );
        Assert.assertTrue( lines[5].matches("<p1:.*> <p1:.*> <p1:.*> <p1:.*> \\.") );
        Assert.assertEquals(
                "<http://sub> <http://pre> \"This is line 1.\\nThis is line 2.\\n\" <http://gra3> .",
                lines[6]
        );
    }

    @Test
    public void testReadWrite() throws RDFHandlerException, IOException, RDFParseException {
        NQuadsParser parser = new NQuadsParser();
        StatementCollector statementCollector = new StatementCollector();
        parser.setRDFHandler(statementCollector);
        parser.parse(
            this.getClass().getClassLoader().getResourceAsStream("application/nquads/test2.nq"),
            "http://test.base.uri"
        );
        
        Assert.assertEquals(400, statementCollector.getStatements().size());
        
        writer.startRDF();
        for(Statement nextStatement : statementCollector.getStatements())
        {
            writer.handleStatement(nextStatement);
        }
        writer.endRDF();
        
        Assert.assertEquals("Unexpected number of lines.", 400, baos.toString().split("\n").length);
    }
    
    @Test
    public void testNoContext() throws RDFHandlerException 
    {
        Statement s1 = vf.createStatement(vf.createURI("http://test.example.org/test/subject/1"), vf.createURI("http://other.example.com/test/predicate/1"), vf.createLiteral("test literal"));
        
        writer.startRDF();
        writer.handleStatement(s1);
        writer.endRDF();
        
        String content = baos.toString();
        logger.info(content);
        String[] lines = content.split("\n");
        Assert.assertEquals("Unexpected number of lines.", 1, lines.length);
        Assert.assertEquals("<http://test.example.org/test/subject/1> <http://other.example.com/test/predicate/1> \"test literal\" .", lines[0] );
    }
    
    @Test
    public void testBlankNodeContext() throws RDFHandlerException
    {
        Statement s1 = vf.createStatement(vf.createURI("http://test.example.org/test/subject/1"), vf.createURI("http://other.example.com/test/predicate/1"), vf.createLiteral("test literal"), vf.createBNode());
        
        writer.startRDF();
        writer.handleStatement(s1);
        writer.endRDF();
        
        String content = baos.toString();
        logger.info(content);
        String[] lines = content.split("\n");
        Assert.assertEquals("Unexpected number of lines.", 1, lines.length);
        Assert.assertTrue(lines[0].startsWith("<http://test.example.org/test/subject/1> <http://other.example.com/test/predicate/1> \"test literal\" _:"));
    }
}
