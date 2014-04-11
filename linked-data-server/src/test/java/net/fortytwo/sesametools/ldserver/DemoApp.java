package net.fortytwo.sesametools.ldserver;

import net.fortytwo.sesametools.ldserver.query.SparqlResource;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.Sail;
import org.openrdf.sail.memory.MemoryStore;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class DemoApp {
    public static void main(final String[] args) throws Exception {
        Sail sail = new MemoryStore();
        sail.initialize();

        Repository repo = new SailRepository(sail);
        RepositoryConnection rc = repo.getConnection();
        try {
            rc.add(DemoApp.class.getResourceAsStream("demoApp.trig"), "", RDFFormat.TRIG);
        } finally {
            rc.close();
        }

        LinkedDataServer server = new LinkedDataServer(
                sail,
                "http://example.org",
                "http://localhost:8001",
                8001);

        server.getHost().attach("/person", WebResource.class);
        server.getHost().attach("/graph", GraphResource.class);
        server.getHost().attach("/sparql", new SparqlResource());

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
