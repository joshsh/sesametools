package net.fortytwo.sesametools.ldserver;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.Sail;
import org.openrdf.sail.memory.MemoryStore;

/**
 * User: josh
 * Date: 3/15/11
 * Time: 6:37 PM
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
                "http://localhost:8182",
                8182);

        server.getRouter().attach("/person", WebResource.class);
        server.getRouter().attach("/graph", GraphResource.class);
        server.start();

        /* Now try:
           wget --header="Accept: application/x-trig" http://localhost:8182/person/arthur
           wget --header="Accept: application/x-trig" http://localhost:8182/graph/demoGraph
         */
    }
}
