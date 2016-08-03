package net.fortytwo.sesametools.ldserver;

import net.fortytwo.sesametools.ldserver.query.SparqlResource;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.restlet.Component;
import org.restlet.data.Protocol;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class DemoApp {
    public static void main(final String[] args) throws Exception {
        Sail sail = new MemoryStore();
        sail.initialize();

        Repository repo = new SailRepository(sail);
        try (RepositoryConnection rc = repo.getConnection()) {
            rc.add(DemoApp.class.getResourceAsStream("demoApp.trig"), "", RDFFormat.TRIG);
        }

        LinkedDataServer server = new LinkedDataServer(
                sail,
                "http://example.org",
                "http://localhost:8001");

        Component component = new Component();
        component.getServers().add(Protocol.HTTP, 8001);
        component.getDefaultHost().attach("/person", WebResource.class);
        component.getDefaultHost().attach("/graph", GraphResource.class);
        component.getDefaultHost().attach("/sparql", new SparqlResource());
        server.setInboundRoot(component);
        server.start();

        /* Now try:
           wget http://localhost:8001/person/arthur
           wget --header="Accept: application/x-trig" http://localhost:8001/person/arthur
           wget --header="Accept: application/x-trig" http://localhost:8001/graph/demoGraph

           wget "http://localhost:8001/sparql?query=SELECT%20%3Fs%20%3Fp%20%3Fo%20WHERE%20%7B%20%3Fs%20%3Fp%20%3Fo%20%7D%20LIMIT%2010"
           curl --data-urlencode query@/tmp/myquery.rq http://localhost:8001/sparql
         */
    }
}
