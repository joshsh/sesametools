package net.fortytwo.sesametools.caching;

import org.openrdf.model.Value;

/**
 * Note: not thread-safe
 * <p/>
 * Author: josh
 * Date: Feb 21, 2008
 * Time: 6:07:49 PM
 */
public class GenerationalCache<T extends Value> { /*
    public enum PartOfSpeech { Subject, Predicate, Object };
    
    private Sail baseSail;
    private Sail cacheSail;
    private long size;
    private LinkedHashMap<T, long> valueMap;

    public GenerationalCache(final Sail base) {
        this.baseSail = base;
        this.cacheSail = new MemoryStore();
        this.size = 0l;
        this.valueMap = new LinkedHashMap<T, long>();
    }

    public Sail getSail() {
        return this.cacheSail;
    }

    public long getSize() {
        return this.size;
    }

    public void cacheStatements(final Resource subj, final URI pred, final Value obj) throws SailException {
		boolean includeInferred = false;

		CloseableIteration<? extends Statement, SailException> iter
				= baseSailConnection.getStatements(subj, pred, obj, includeInferred);

		while (iter.hasNext()) {
			Statement st = iter.next();
			cacheConnection.addStatement(st.getSubject(), st.getPredicate(), st.getObject(), st.getContext());
		}

		iter.close();

		cacheConnection.commit();
	}  */
}
