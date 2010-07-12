
package net.fortytwo.sesametools.caching;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.StackableSail;
import org.openrdf.sail.memory.MemoryStore;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

// Note: assumes that the value factories of the base Sail and the MemoryStore

// cache are compatible.
public class CachingSail implements StackableSail {
    private static long DEFAULT_CAPACITY = 1000000l;

    private boolean cacheSubject, cachePredicate, cacheObject;

    private Sail baseSail;
    private Sail cache;

    private Set<Resource> cachedSubjects;
    private Set<URI> cachedPredicates;
    private Set<Value> cachedObjects;

    private long capacity;

    public CachingSail(final Sail baseSail,
                       final boolean cacheSubject,
                       final boolean cachePredicate,
                       final boolean cacheObject,
                       final long capacity) {
        this.baseSail = baseSail;
        this.cacheSubject = cacheSubject;
        this.cachePredicate = cachePredicate;
        this.cacheObject = cacheObject;

        this.capacity = (capacity <= 0) ? DEFAULT_CAPACITY : capacity;
    }

    public SailConnection getConnection() throws SailException {
        return new CachingSailConnection(baseSail, cache,
                cacheSubject, cachePredicate, cacheObject,
                cachedSubjects, cachedPredicates, cachedObjects);
    }

    public File getDataDir() {
        return baseSail.getDataDir();
    }

    public ValueFactory getValueFactory() {
        return baseSail.getValueFactory();
    }

    public void initialize() throws SailException {
        baseSail.initialize();

        cache = new MemoryStore();
        cache.initialize();

        if (cacheSubject) {
            cachedSubjects = new HashSet<Resource>();
        }

        if (cachePredicate) {
            cachedPredicates = new HashSet<URI>();
        }

        if (cacheObject) {
            cachedObjects = new HashSet<Value>();
        }
    }

    public boolean isWritable() throws SailException {
        return baseSail.isWritable();
    }

    public void setDataDir(final File dir) {
        baseSail.setDataDir(dir);
    }

    public void shutDown() throws SailException {
        baseSail.shutDown();
        cache.shutDown();
    }

    public Sail getBaseSail() {
        return baseSail;
    }

    public void setBaseSail(final Sail sail) {
        baseSail = sail;
    }

    ////////////////////////////////////////////////////////////////////////////

    public long getCapacity() {
        return this.capacity;

    }
}
