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
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.ParseLocationListener;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.io.StringWriter;
import java.util.Collection;

import static org.hamcrest.core.Is.is;

/**
 * Test case for {@link NQuadsParser}.
 *
 * @author Michele Mostarda (mostarda@fbk.eu)
 */
public class NQuadsParserTest {

    private static final Logger logger = LoggerFactory.getLogger(NQuadsParser.class);

    private NQuadsParser parser;
    private TestRDFHandler rdfHandler;

    @Before
    public void setUp() {
        rdfHandler = new TestRDFHandler();
        parser = new NQuadsParser();
        parser.setVerifyData(true);
        parser.setDatatypeHandling(RDFParser.DatatypeHandling.VERIFY);
        parser.setStopAtFirstError(true);
        parser.setRDFHandler(this.rdfHandler);
    }

    @After
    public void tearDown() {
        parser = null;
    }
    
    /**
     * Tests the correct behavior with incomplete input.
     *
     * @throws RDFHandlerException
     * @throws IOException
     * @throws RDFParseException
     */
    @Test
    public void testIncompleteParsingWithoutPeriod() throws RDFHandlerException, IOException, RDFParseException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(
                "<http://s> <http://p> <http://o> <http://g>".getBytes()
        );
        try
        {
            parser.parse(bais, "http://test.base.uri");
            Assert.fail("Expected exception when not inserting a trailing period at the end of a statement.");
        }
        catch(RDFParseException rdfpe)
        {
            // FIXME: Enable this test when first line number is 1 in parser instead of -1
            //Assert.assertEquals(1, rdfpe.getLineNumber());
            // FIXME: Enable column numbers when parser supports them
            //Assert.assertEquals(44, rdfpe.getColumnNumber());
        }
    }

    /**
     * Tests the correct behavior with incomplete input.
     *
     * @throws RDFHandlerException
     * @throws IOException
     * @throws RDFParseException
     */
    @Ignore
    @Test
    public void testIncompleteParsingWithPeriod() throws RDFHandlerException, IOException, RDFParseException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(
                "<http://s> <http://p> <http://o> <http://g>.".getBytes()
        );
        try
        {
            parser.parse(bais, "http://test.base.uri");
            Assert.fail("Expected exception when not inserting a new line character at the end of a statement.");
        }
        catch(RDFParseException rdfpe)
        {
            Assert.assertEquals(1, rdfpe.getLineNumber());
            Assert.assertEquals(44, rdfpe.getColumnNumber());
        }
    }

    /**
     * Tests the behaviour with non-whitespace characters after a period character without a context.
     *
     * @throws RDFHandlerException
     * @throws IOException
     * @throws RDFParseException
     */
    @Ignore
    @Test
    public void testNonWhitespaceAfterPeriodNoContext() throws RDFHandlerException, IOException, RDFParseException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(
                "<http://www.wrong.com> <http://wrong.com/1.1/tt> \"x\"^^<http://xxx.net/int> . <http://path.to.graph> ".getBytes()
        );
        try
        {
            parser.parse(bais, "http://base-uri");
            Assert.fail("Expected exception when there is non-whitespace characters after a period.");
        }
        catch(RDFParseException rdfpe)
        {
            Assert.assertEquals(1, rdfpe.getLineNumber());
            Assert.assertEquals(44, rdfpe.getColumnNumber());
        }
    }

    /**
     * Tests the behaviour with non-whitespace characters after a period character without a context.
     *
     * @throws RDFHandlerException
     * @throws IOException
     * @throws RDFParseException
     */
    @Ignore
    @Test
    public void testNonWhitespaceAfterPeriodWithContext() throws RDFHandlerException, IOException, RDFParseException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(
                "<http://www.wrong.com> <http://wrong.com/1.1/tt> \"x\"^^<http://xxx.net/int> <http://path.to.graph> . <thisisnotlegal> ".getBytes()
        );
        try
        {
            parser.parse(bais, "http://base-uri");
            Assert.fail("Expected exception when there is non-whitespace characters after a period.");
        }
        catch(RDFParseException rdfpe)
        {
            Assert.assertEquals(1, rdfpe.getLineNumber());
            Assert.assertEquals(44, rdfpe.getColumnNumber());
        }
    }

    
    /**
     * Tests the correct behavior with no context.
     *
     * @throws RDFHandlerException
     * @throws IOException
     * @throws RDFParseException
     */
    @Test
    public void testParseNoContext() throws RDFHandlerException, IOException, RDFParseException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(
                "<http://s> <http://p> <http://o> .".getBytes()
        );
        parser.parse(bais, "http://base-uri");
    }

    
    /**
     * Tests parsing of empty lines and comments.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testParseEmptyLinesAndComments() throws RDFHandlerException, IOException, RDFParseException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(
            "  \n\n\n# This is a comment\n\n#this is another comment."
            .getBytes()
        );
        final TestRDFHandler rdfHandler = new TestRDFHandler();
        parser.setRDFHandler(rdfHandler);
        parser.parse(bais, "http://test.base.uri");
        Assert.assertEquals(rdfHandler.getStatements().size(), 0);
    }

    /**
     * Tests basic N-Quads parsing.
     *
     * @throws RDFHandlerException
     * @throws IOException
     * @throws RDFParseException
     */
    @Test
    public void testParseBasic() throws RDFHandlerException, IOException, RDFParseException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(
            "<http://www.v/dat/4b> <http://www.w3.org/20/ica#dtend> <http://sin/value/2> <http://sin.siteserv.org/def/> ."
            .getBytes()
        );
        final TestRDFHandler rdfHandler = new TestRDFHandler();
        parser.setRDFHandler(rdfHandler);
        parser.parse(bais, "http://test.base.uri");
        Assert.assertThat(rdfHandler.getStatements().size(), is(1));
        final Statement statement = rdfHandler.getStatements().iterator().next();
        Assert.assertEquals("http://www.v/dat/4b", statement.getSubject().stringValue());
        Assert.assertEquals("http://www.w3.org/20/ica#dtend", statement.getPredicate().stringValue());
        Assert.assertTrue(statement.getObject() instanceof URI);
        Assert.assertEquals("http://sin/value/2", statement.getObject().stringValue());
        Assert.assertEquals("http://sin.siteserv.org/def/", statement.getContext().stringValue());
    }

    /**
     * Tests basic N-Quads parsing with blank node.
     *
     * @throws RDFHandlerException
     * @throws IOException
     * @throws RDFParseException
     */
    @Test
    public void testParseBasicBNode() throws RDFHandlerException, IOException, RDFParseException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(
            "_:a123456768 <http://www.w3.org/20/ica#dtend> <http://sin/value/2> <http://sin.siteserv.org/def/>."
            .getBytes()
        );
        final TestRDFHandler rdfHandler = new TestRDFHandler();
        parser.setRDFHandler(rdfHandler);
        parser.parse(bais, "http://test.base.uri");
        Assert.assertThat(rdfHandler.getStatements().size(), is(1));
        final Statement statement = rdfHandler.getStatements().iterator().next();
        Assert.assertTrue(statement.getSubject() instanceof BNode);
        Assert.assertEquals("http://www.w3.org/20/ica#dtend", statement.getPredicate().stringValue());
        Assert.assertTrue(statement.getObject() instanceof URI);
        Assert.assertEquals("http://sin/value/2", statement.getObject().stringValue());
        Assert.assertEquals("http://sin.siteserv.org/def/", statement.getContext().stringValue());
    }

    /**
     * Tests basic N-Quads parsing with literal.
     *
     * @throws RDFHandlerException
     * @throws IOException
     * @throws RDFParseException
     */
    @Test
    public void testParseBasicLiteral() throws RDFHandlerException, IOException, RDFParseException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(
            "_:a123456768 <http://www.w3.org/20/ica#dtend> \"2010-05-02\" <http://sin.siteserv.org/def/>."
            .getBytes()
        );
        final TestRDFHandler rdfHandler = new TestRDFHandler();
        parser.setRDFHandler(rdfHandler);
        parser.parse(bais, "http://test.base.uri");
        Assert.assertThat(rdfHandler.getStatements().size(), is(1));
        final Statement statement = rdfHandler.getStatements().iterator().next();
        Assert.assertTrue(statement.getSubject() instanceof BNode);
        Assert.assertEquals("http://www.w3.org/20/ica#dtend", statement.getPredicate().stringValue());
        Assert.assertTrue(statement.getObject() instanceof Literal);
        Assert.assertEquals("2010-05-02", statement.getObject().stringValue());
        Assert.assertEquals("http://sin.siteserv.org/def/", statement.getContext().stringValue());
    }

    /**
     * Tests N-Quads parsing with literal and language.
     * 
     * @throws RDFHandlerException
     * @throws IOException
     * @throws RDFParseException
     */
    @Test
    public void testParseBasicLiteralLang() throws RDFHandlerException, IOException, RDFParseException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(
            "<http://www.v/dat/4b2-21> <http://www.w3.org/20/ica#dtend> \"2010-05-02\"@en <http://sin.siteserv.org/def/>."
            .getBytes()
        );
        final TestRDFHandler rdfHandler = new TestRDFHandler();
        parser.setRDFHandler(rdfHandler);
        parser.parse(bais, "http://test.base.uri");
        final Statement statement = rdfHandler.getStatements().iterator().next();
        Assert.assertEquals("http://www.v/dat/4b2-21", statement.getSubject().stringValue());
        Assert.assertEquals("http://www.w3.org/20/ica#dtend", statement.getPredicate().stringValue());
        Assert.assertTrue(statement.getObject() instanceof Literal);
        Literal object = (Literal) statement.getObject();
        Assert.assertEquals("2010-05-02", object.stringValue());
        Assert.assertEquals("en", object.getLanguage());
        Assert.assertNull("en", object.getDatatype());
        Assert.assertEquals("http://sin.siteserv.org/def/", statement.getContext().stringValue());
    }

    /**
     * Tests N-Quads parsing with literal and datatype.
     * 
     * @throws RDFHandlerException
     * @throws IOException
     * @throws RDFParseException
     */
    @Test
    public void testParseBasicLiteralDatatype() throws RDFHandlerException, IOException, RDFParseException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(
            ("<http://www.v/dat/4b2-21> " +
             "<http://www.w3.org/20/ica#dtend> " +
             "\"2010\"^^<http://www.w3.org/2001/XMLSchema#integer> " +
             "<http://sin.siteserv.org/def/>."
            ).getBytes()
        );
        final TestRDFHandler rdfHandler = new TestRDFHandler();
        parser.setRDFHandler(rdfHandler);
        parser.parse(bais, "http://test.base.uri");
        final Statement statement = rdfHandler.getStatements().iterator().next();
        Assert.assertEquals("http://www.v/dat/4b2-21", statement.getSubject().stringValue());
        Assert.assertEquals("http://www.w3.org/20/ica#dtend", statement.getPredicate().stringValue());
        Assert.assertTrue(statement.getObject() instanceof Literal);
        Literal object = (Literal) statement.getObject();
        Assert.assertEquals("2010", object.stringValue());
        Assert.assertNull(object.getLanguage());
        Assert.assertEquals("http://www.w3.org/2001/XMLSchema#integer", object.getDatatype().toString());
        Assert.assertEquals("http://sin.siteserv.org/def/", statement.getContext().stringValue());
    }

    /**
     * Tests N-Quads parsing with literal and datatype using a prefix, which is illegal in NQuads, but legal in N3/Turtle that may otherwise look like NQuads
     * 
     * @throws RDFHandlerException
     * @throws IOException
     * @throws RDFParseException
     */
    @Test
    public void testParseBasicLiteralDatatypePrefix() throws RDFHandlerException, IOException{
        
        final ByteArrayInputStream bais = new ByteArrayInputStream(
            ("<http://www.v/dat/4b2-21> " +
             "<http://www.w3.org/20/ica#dtend> " +
             "\"2010\"^^xsd:integer " +
             "<http://sin.siteserv.org/def/>."
            ).getBytes()
        );
        final TestRDFHandler rdfHandler = new TestRDFHandler();
        parser.setRDFHandler(rdfHandler);
        try
        {
            parser.parse(bais, "http://test.base.uri");
            Assert.fail("Expected exception when passing in a datatype using an N3 style prefix");
        }
        catch(RDFParseException rdfpe)
        {
            Assert.assertEquals(1, rdfpe.getLineNumber());
            // FIXME: Enable column numbers when parser supports them
            // Assert.assertEquals(69, rdfpe.getColumnNumber());
        }
    }

    /**
     * Tests the correct support for literal escaping.
     *
     * @throws RDFHandlerException
     * @throws IOException
     * @throws RDFParseException
     */
    @Test
    public void testLiteralEscapeManagement1()
    throws RDFHandlerException, IOException, RDFParseException {
        TestParseLocationListener parseLocationListener = new TestParseLocationListener();
        TestRDFHandler rdfHandler = new TestRDFHandler();
        parser.setParseLocationListener(parseLocationListener);
        parser.setRDFHandler(rdfHandler);

        final ByteArrayInputStream bais = new ByteArrayInputStream(
            "<http://a> <http://b> \"\\\\\" <http://c> .".getBytes()
        );
        parser.parse(bais, "http://base-uri");

        rdfHandler.assertHandler(1);
//        parseLocationListener.assertListener(1, 40);
        // FIXME: Enable column numbers when parser supports them
      parseLocationListener.assertListener(1, 1);
    }

    /**
     * Tests the correct support for literal escaping.
     *
     * @throws RDFHandlerException
     * @throws IOException
     * @throws RDFParseException
     */
    @Test
    public void testLiteralEscapeManagement2()
    throws RDFHandlerException, IOException, RDFParseException {
        TestParseLocationListener parseLocationListener = new TestParseLocationListener();
        TestRDFHandler rdfHandler = new TestRDFHandler();
        parser.setParseLocationListener(parseLocationListener);
        parser.setRDFHandler(rdfHandler);

        final ByteArrayInputStream bais = new ByteArrayInputStream(
            "<http://a> <http://b> \"Line text 1\\nLine text 2\" <http://c> .".getBytes()
        );
        parser.parse(bais, "http://base-uri");

        rdfHandler.assertHandler(1);
        final Value object = rdfHandler.getStatements().iterator().next().getObject();
        Assert.assertTrue( object instanceof Literal);
        final String literalContent = ((Literal) object).getLabel();
        Assert.assertEquals("Line text 1\nLine text 2", literalContent);
    }

    /**
     * Tests the correct decoding of UTF-8 encoded chars in URIs.
     *
     * @throws RDFHandlerException
     * @throws IOException
     * @throws RDFParseException
     */
    @Test
    public void testURIDecodingManagement() throws RDFHandlerException, IOException, RDFParseException {
        TestParseLocationListener parseLocationListener = new TestParseLocationListener();
        TestRDFHandler rdfHandler = new TestRDFHandler();
        parser.setParseLocationListener(parseLocationListener);
        parser.setRDFHandler(rdfHandler);

        final ByteArrayInputStream bais = new ByteArrayInputStream(
            "<http://s/\\u306F\\u3080> <http://p/\\u306F\\u3080> <http://o/\\u306F\\u3080> <http://g/\\u306F\\u3080> ."
            .getBytes()
        );
        parser.parse(bais, "http://base-uri");

        rdfHandler.assertHandler(1);
        final Statement statement = rdfHandler.getStatements().iterator().next();

        final Resource subject = statement.getSubject();
        Assert.assertTrue( subject instanceof URI);
        final String subjectURI = subject.toString();
        Assert.assertEquals("http://s/はむ", subjectURI);

        final Resource predicate = statement.getPredicate();
        Assert.assertTrue( predicate instanceof URI);
        final String predicateURI = predicate.toString();
        Assert.assertEquals("http://p/はむ", predicateURI);

        final Value object = statement.getObject();
        Assert.assertTrue( object instanceof URI);
        final String objectURI = object.toString();
        Assert.assertEquals("http://o/はむ", objectURI);

        final Resource graph = statement.getContext();
        Assert.assertTrue( graph instanceof URI);
        final String graphURI = graph.toString();
        Assert.assertEquals("http://g/はむ", graphURI);
    }

    /**
     * FIXME: This test needs to be converted to a failing test, as NQuads documents 
     * do not contain bare UTF-8 characters, since they are limited to US-ASCII.
     * 
     * @throws RDFHandlerException
     * @throws IOException
     * @throws RDFParseException
     */
    @Ignore
    @Test
    public void testUnicodeLiteralManagement() throws RDFHandlerException, IOException, RDFParseException {
        TestRDFHandler rdfHandler = new TestRDFHandler();
        parser.setRDFHandler(rdfHandler);
        final String INPUT_LITERAL = "[は、イギリスおよびイングランドの首都である] [是大不列顛及北愛爾蘭聯合王國和英格蘭的首都]";
        final String INPUT_STRING = String.format(
                "<http://a> <http://b> \"%s\" <http://c> .",
                INPUT_LITERAL
        );
        final ByteArrayInputStream bais = new ByteArrayInputStream(
            INPUT_STRING.getBytes("US-ASCII")
        );
        parser.parse(bais, "http://base-uri");

        rdfHandler.assertHandler(1);
        final Literal obj = (Literal) rdfHandler.getStatements().iterator().next().getObject();
        Assert.assertEquals(INPUT_LITERAL, obj.getLabel());
    }

    @Test
    public void testUnicodeLiteralDecoding() throws RDFHandlerException, IOException, RDFParseException {
        TestRDFHandler rdfHandler = new TestRDFHandler();
        parser.setRDFHandler(rdfHandler);
        final String INPUT_LITERAL_PLAIN   = "[は]";
        final String INPUT_LITERAL_ENCODED = "[\\u306F]";
        final String INPUT_STRING = String.format(
                "<http://a> <http://b> \"%s\" <http://c> .",
                INPUT_LITERAL_ENCODED
        );
        final ByteArrayInputStream bais = new ByteArrayInputStream(
            INPUT_STRING.getBytes()
        );
        parser.parse(bais, "http://base-uri");

        rdfHandler.assertHandler(1);
        final Literal obj = (Literal) rdfHandler.getStatements().iterator().next().getObject();
        Assert.assertEquals(INPUT_LITERAL_PLAIN, obj.getLabel());
    }

    @Test
    public void testWrongUnicodeEncodedCharFail() throws RDFHandlerException, IOException, RDFParseException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(
                "<http://s> <http://p> \"\\u123X\" <http://g> .".getBytes()
        );
        try
        {
            parser.parse(bais, "http://test.base.uri");
            Assert.fail("Expected exception when an incorrect unicode character is included");
        }
        catch(RDFParseException rdfpe)
        {
            Assert.assertEquals(1, rdfpe.getLineNumber());
            // FIXME: Enable column numbers when parser supports them
            // Assert.assertEquals(30, rdfpe.getColumnNumber());
        }
    }

    /**
     * Tests the correct support for EOS exception.
     *
     * @throws RDFHandlerException
     * @throws IOException
     * @throws RDFParseException
     */
    @Test
    public void testEndOfStreamReached()
    throws RDFHandlerException, IOException, RDFParseException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(
            "<http://a> <http://b> \"\\\" <http://c> .".getBytes()
        );
        try
        {
            parser.parse(bais, "http://test.base.uri");
            Assert.fail("Expected exception when a literal is not closed");
        }
        catch(RDFParseException rdfpe)
        {
            // FIXME: Enable this test when first line number is 1 in parser instead of -1
            //Assert.assertEquals(1, rdfpe.getLineNumber());
            // FIXME: Enable column numbers when parser supports them
            // Assert.assertEquals(39, rdfpe.getColumnNumber());
        }
    }

    /**
     * Tests the parser with all cases defined by the NQuads grammar.
     *
     * @throws IOException
     * @throws RDFParseException
     * @throws RDFHandlerException
     */
    @Test
    public void testParserWithAllCases()
    throws IOException, RDFParseException, RDFHandlerException {
        TestParseLocationListener parseLocationListerner = new TestParseLocationListener();
        //SpecificTestRDFHandler rdfHandler = new SpecificTestRDFHandler();
        parser.setParseLocationListener(parseLocationListerner);
        parser.setRDFHandler(rdfHandler);

        BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        this.getClass().getClassLoader().getResourceAsStream("application/nquads/test1.nq")
                ) 
        );
        parser.parse(
            br,
            "http://test.base.uri"
        );

        rdfHandler.assertHandler(6);
        parseLocationListerner.assertListener(9, 1);
    }

    /**
     * Tests parser with real data.
     *
     * @throws IOException
     * @throws RDFParseException
     * @throws RDFHandlerException
     */
    @Test
    public void testParserWithRealData()
    throws IOException, RDFParseException, RDFHandlerException {
        TestParseLocationListener parseLocationListener = new TestParseLocationListener();
        TestRDFHandler rdfHandler = new TestRDFHandler();
        parser.setParseLocationListener(parseLocationListener);
        parser.setRDFHandler(rdfHandler);

        parser.parse(
            this.getClass().getClassLoader().getResourceAsStream("application/nquads/test2.nq"),
            "http://test.base.uri"
        );

        rdfHandler.assertHandler(400);
        parseLocationListener.assertListener(401, 1);
    }

    @Test
    public void testStatementWithInvalidLiteralContentAndIgnoreValidation()
    throws RDFHandlerException, IOException, RDFParseException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(
                (
                "<http://dbpedia.org/resource/Camillo_Benso,_conte_di_Cavour> " +
                "<http://dbpedia.org/property/mandatofine> " +
                "\"1380.0\"^^<http://www.w3.org/2001/XMLSchema#int> " + // Float declared as int.
                "<http://it.wikipedia.org/wiki/Camillo_Benso,_conte_di_Cavour#absolute-line=20> ."
                ).getBytes()
        );
        parser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);
        parser.parse(bais, "http://base-uri");
    }

    @Test
    public void testStatementWithInvalidLiteralContentAndStrictValidation()
    throws RDFHandlerException, IOException, RDFParseException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(
                (
                "<http://dbpedia.org/resource/Camillo_Benso,_conte_di_Cavour> " +
                "<http://dbpedia.org/property/mandatofine> " +
                "\"1380.0\"^^<http://www.w3.org/2001/XMLSchema#int> " + // Float declared as int.
                "<http://it.wikipedia.org/wiki/Camillo_Benso,_conte_di_Cavour#absolute-line=20> ."
                ).getBytes()
        );
        parser.setDatatypeHandling(RDFParser.DatatypeHandling.VERIFY);
        try
        {
            parser.parse(bais, "http://test.base.uri");
            Assert.fail("Expected exception when passing in a datatype using an N3 style prefix");
        }
        catch(RDFParseException rdfpe)
        {
            Assert.assertEquals(1, rdfpe.getLineNumber());
            // FIXME: Enable column numbers when parser supports them
            // Assert.assertEquals(152, rdfpe.getColumnNumber());
        }
    }

    @Test
    public void testStatementWithInvalidDatatypeAndIgnoreValidation()
    throws RDFHandlerException, IOException, RDFParseException {
        verifyStatementWithInvalidDatatype(RDFParser.DatatypeHandling.IGNORE);
    }

    @Test
    public void testStatementWithInvalidDatatypeAndVerifyValidation()
    throws RDFHandlerException, IOException, RDFParseException {
        verifyStatementWithInvalidDatatype(RDFParser.DatatypeHandling.VERIFY);
    }

    @Test
    public void testStopAtFirstErrorStrictParsing() throws RDFHandlerException, IOException, RDFParseException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(
                (
                    "<http://s0> <http://p0> <http://o0> <http://g0> .\n" +
                    "<http://sX>                                     .\n" + // Line with error.
                    "<http://s1> <http://p1> <http://o1> <http://g1> .\n"
                ).getBytes()
        );
        parser.setStopAtFirstError(true);
        try
        {
            parser.parse(bais, "http://test.base.uri");
            Assert.fail("Expected exception when encountering an invalid line");
        }
        catch(RDFParseException rdfpe)
        {
            Assert.assertEquals(2, rdfpe.getLineNumber());
//            Assert.assertEquals(50, rdfpe.getColumnNumber());
        }
    }

    @Test
    public void testStopAtFirstErrorTolerantParsing() throws RDFHandlerException, IOException, RDFParseException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(
                (
                    "<http://s0> <http://p0> <http://o0> <http://g0> .\n" +
                    "<http://sX>                                     .\n" + // Line with error.
                    "<http://s1> <http://p1> <http://o1> <http://g1> .\n"
                ).getBytes()
        );
        final TestRDFHandler rdfHandler = new TestRDFHandler();
        parser.setRDFHandler(rdfHandler);
        parser.setStopAtFirstError(false);
        parser.parse(bais, "http://base-uri");
        rdfHandler.assertHandler(2);
        final Collection<Statement> statements = rdfHandler.getStatements();
        int i = 0;
        for(Statement nextStatement : statements) {
            Assert.assertEquals("http://s" + i, nextStatement.getSubject().stringValue()  );
            Assert.assertEquals("http://p" + i, nextStatement.getPredicate().stringValue());
            Assert.assertEquals("http://o" + i, nextStatement.getObject().stringValue()   );
            Assert.assertEquals("http://g" + i, nextStatement.getContext().stringValue()  );
            i++;
        }
    }

    private void verifyStatementWithInvalidDatatype(RDFParser.DatatypeHandling datatypeHandling)
    throws RDFHandlerException, IOException, RDFParseException {
        TestRDFHandler rdfHandler = new TestRDFHandler();
        parser.setRDFHandler(rdfHandler);
        parser.setDatatypeHandling(datatypeHandling);
        final ByteArrayInputStream bais = new ByteArrayInputStream(
                (
                        "<http://dbpedia.org/resource/Camillo_Benso,_conte_di_Cavour> " +
                        "<http://dbpedia.org/property/mandatofine> " +
                        "\"1380.0\"^^<http://dbpedia.org/datatype/second> " +
                        "<http://it.wikipedia.org/wiki/Camillo_Benso,_conte_di_Cavour#absolute-line=20> ."
                ).getBytes()
        );
        parser.parse(bais, "http://base-uri");
        rdfHandler.assertHandler(1);
    }

    private class TestParseLocationListener implements ParseLocationListener {

        private int lastRow, lastCol;

        public void parseLocationUpdate(int r, int c) {
            lastRow = r;
            lastCol = c;
        }

        private void assertListener(int row, int col) {
            Assert.assertEquals("Unexpected last row", row , lastRow);
            Assert.assertEquals("Unexpected last col", col , lastCol);
        }

    }

    private class TestRDFHandler extends StatementCollector {

        private boolean started = false;
        private boolean ended   = false;

        @Override
        public void startRDF() throws RDFHandlerException {
            super.startRDF();
            started = true;
        }

        @Override
        public void endRDF() throws RDFHandlerException {
            super.endRDF();
            ended = true;
        }

        @Override
        public void handleStatement(Statement statement) {
            super.handleStatement(statement);
            logger.debug(statement.toString());
        }

        public void assertHandler(int expected) {
            Assert.assertTrue("Never started.", started);
            Assert.assertTrue("Never ended." , ended  );
            Assert.assertEquals("Unexpected number of statements.", expected, getStatements().size());
        }
    }

    /**
    private class SpecificTestRDFHandler extends TestRDFHandler {

        public void handleStatement(Statement statement) {
            int statements = getStatements().size();
            if(statements == 0){
                Assert.assertEquals(new URIImpl("http://example.org/alice/foaf.rdf#me"), statement.getSubject() );

            } else {
                Assert.assertTrue(statement.getSubject() instanceof BNode);
            }
            if( statements == 4 ) {
                Assert.assertEquals(new URIImpl("http://test.base.uri#like"), statement.getPredicate() );
            }
            else if( statements == 5 ) {
                Assert.assertEquals(new URIImpl("http://test.base.uri#type"), statement.getPredicate() );
            }
            if( statements != 5) {
                Assert.assertEquals(
                        new URIImpl( String.format("http://example.org/alice/foaf%s.rdf", statements + 1) ),
                        statement.getContext()
                );
            }
            
            super.handleStatement(statement);
        }
    }
    **/
}
