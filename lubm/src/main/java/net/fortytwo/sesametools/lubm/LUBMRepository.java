package net.fortytwo.sesametools.lubm;

import edu.lehigh.swat.bench.ubt.api.Query;
import edu.lehigh.swat.bench.ubt.api.QueryResult;
import edu.lehigh.swat.bench.ubt.api.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;

/**
 * User: josh
 * Date: 2/18/11
 * Time: 5:43 PM
 *
 * @author Joshua Shinavier (josh@fortytwo.net).  Originally based on Aduna's LUBMRepository.
 */
public class LUBMRepository implements Repository {
    private final SailFactory factory;
    private Sail sail;
    private org.openrdf.repository.Repository repository;
    private String ontologyUrl;

    public LUBMRepository(SailFactory factory) {
        this.factory = factory;
    }

    public void open(final String database) {
        if (null != sail) {
            throw new IllegalStateException("previous Sail is still open");
        }

        sail = factory.createSail(database);
        repository = new SailRepository(sail);

        try {
            sail.initialize();
        } catch (SailException e) {
            e.printStackTrace(System.err);
            sail = null;
        }
    }

    public void close() {
        if (null == sail) {
            throw new IllegalStateException("no Sail to close");
        }

        try {
            sail.shutDown();
        } catch (SailException e) {
            e.printStackTrace(System.err);
        }

        sail = null;
    }

    public boolean load(final String dataDir) {
        File[] files = new File(dataDir).listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".owl");
            }
        });

        if (null == files) {
            System.err.println("No OWL files found in directory " + dataDir);
            return false;
        }

        try {
            RepositoryConnection rc = repository.getConnection();
            try {
                rc.add(new URL(ontologyUrl), ontologyUrl, RDFFormat.RDFXML);

                for (File file : files) {
                    rc.add(file, file.getPath(), RDFFormat.RDFXML);
                }
            } finally {
                rc.close();
            }
        } catch (Throwable e) {
            e.printStackTrace(System.err);
            return false;
        }

        return true;
    }

    public void setOntology(final String ontology) {
        ontologyUrl = ontology;
    }

    public QueryResult issueQuery(final Query query) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void clear() {
        if (null == sail) {
            throw new IllegalStateException("Sail is not open");
        }

        try {
            SailConnection sc = sail.getConnection();
            try {
                sc.clear();
                sc.commit();
            } finally {
                sc.close();
            }
        } catch (SailException e) {
            e.printStackTrace(System.err);
        }
    }

    public interface SailFactory {
        Sail createSail(final String path);
    }

    public class NewQueryResult implements QueryResult {
        public long getNum() {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public boolean next() {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
