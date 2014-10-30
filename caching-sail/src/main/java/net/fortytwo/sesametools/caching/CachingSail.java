
package net.fortytwo.sesametools.caching;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.StackableSail;
import org.openrdf.sail.helpers.SailBase;
import org.openrdf.sail.memory.MemoryStore;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * A <code>Sail</code> which caches statements retrieved from a base <code>Sail</code>
 * in an internal <code>MemoryStore</code>, speeding up subsequent queries for the same data.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
// Note: assumes that the value factories of the base Sail and the MemoryStore
// cache are compatible.
public class CachingSail extends SailBase implements StackableSail {
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

    public SailConnection getConnectionInternal() throws SailException {
        return new CachingSailConnection(this, baseSail, cache,
                cacheSubject, cachePredicate, cacheObject,
                cachedSubjects, cachedPredicates, cachedObjects);
    }

    @Override
    public File getDataDir() {
        return baseSail.getDataDir();
    }

    @Override
    public ValueFactory getValueFactory() {
        return baseSail.getValueFactory();
    }

    public void initializeInternal() throws SailException {
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

    @Override
    public boolean isWritable() throws SailException {
        return baseSail.isWritable();
    }

    @Override
    public void setDataDir(final File dir) {
        baseSail.setDataDir(dir);
    }

    public void shutDownInternal() throws SailException {
        baseSail.shutDown();
        cache.shutDown();
    }

    public Sail getBaseSail() {
        return baseSail;
    }

    public void setBaseSail(final Sail sail) {
        baseSail = sail;
    }

    public long getCapacity() {
        return this.capacity;
    }
}
