package net.fortytwo.sesametools.ldserver;

import java.net.URI;
import java.util.logging.Level;

import net.fortytwo.sesametools.ldserver.query.SparqlResource;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;

public class SparqlAskTest {

    private static final URI ENDPOINT_URL = URI.create("http://localhost:8001/sparql");
    private static final String DATA_FILE = "demoApp.trig";

    private static final Sail sail;
    private static final LinkedDataServer server;

    static {
        sail = new MemoryStore();
        sail.initialize();
        server = new LinkedDataServer(sail, "", "");
    }

    @BeforeClass
    public static void setUp() throws Exception {
        // add test data
        final Repository repo = new SailRepository(sail);
        try (RepositoryConnection con = repo.getConnection()) {
            con.add(SparqlAskTest.class.getResourceAsStream(DATA_FILE), "", RDFFormat.TRIG);
        }
        
        // turn off verbose logging in Restlet engine
        Engine.setLogLevel(Level.WARNING);

        // configure SPARQL endpoint
        final Component component = new Component();
        component.getServers().add(Protocol.HTTP, ENDPOINT_URL.getPort());
        component.getDefaultHost().attach(ENDPOINT_URL.getPath(), new SparqlResource());
        server.setInboundRoot(component);
        server.start();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        server.stop();
        sail.shutDown();
    }

    @Test
    public void testSatisfiableAskQuery() throws Exception {
        Assert.assertTrue(executeAskQuery(ENDPOINT_URL, "ASK { [] a ?type }"));
    }

    @Test
    public void testUnsatisfiableAskQuery() throws Exception {
        Assert.assertFalse(executeAskQuery(ENDPOINT_URL, "ASK { [] ?p <http://ex.com> }"));
    }

    private Boolean executeAskQuery(final URI endpoint, final String query) {
        
        final SPARQLRepository repo = new SPARQLRepository(endpoint.toString());
        try {
            repo.initialize();

            try (RepositoryConnection con = repo.getConnection()) {
                return con.prepareBooleanQuery(QueryLanguage.SPARQL, query).evaluate();
            } catch (Exception e) {
                Assert.fail(e.getMessage());
            }

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            repo.shutDown();
        }
        return null;
    }

}
