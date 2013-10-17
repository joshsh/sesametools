package net.fortytwo.sesametools;

import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import java.util.Collection;
import java.util.Iterator;

/**
 * An adapter which wraps a Repository as a Graph
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class RepositoryGraph implements Graph {
    private static final boolean INFER = false;

    private RepositoryConnection rc;
    private ValueFactory vf;

    public RepositoryGraph(final Repository repo) throws RepositoryException {
        rc = repo.getConnection();
        vf = repo.getValueFactory();
    }

    public void close() {
        try {
            rc.close();
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    public ValueFactory getValueFactory() {
        return vf;
    }

    public boolean add(Resource s, URI p, Value o, Resource... c) {
        try {
            rc.begin();
            rc.add(s, p, o, c);
            rc.commit();
            return true;
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    // note: the returned iterator contains a CloseableIteration which will not be closed
    public Iterator<Statement> match(Resource s, URI p, Value o, Resource... c) {
        RepositoryResult<Statement> result = null;
        try {
            result = rc.getStatements(s, p, o, INFER, c);
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
        return new RepositoryResultIterator(result);
    }

    public int size() {
        try {
            return (int) rc.size();
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isEmpty() {
        return 0 == size();
    }

    public boolean contains(Object o) {
        if (o instanceof Statement) {
            Statement st = (Statement) o;
            try {
                RepositoryResult result = rc.getStatements(st.getSubject(), st.getPredicate(), st.getObject(), INFER, st.getContext());
                try {
                    return result.hasNext();
                } finally {
                    result.close();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            return false;
        }
    }

    public Iterator<Statement> iterator() {
        return match(null, null, null);
    }

    public Object[] toArray() {
        int size = size();
        Object[] a = new Object[size];
        if (size > 0) {
            try {
                int i = 0;
                RepositoryResult result = rc.getStatements(null, null, null, INFER);
                try {
                    while (result.hasNext()) {
                        a[i] = result.next();
                        i++;
                    }
                } finally {
                    result.close();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return a;
    }

    public <T> T[] toArray(T[] ts) {
        // TODO: only Statement is acceptable as T
        return (T[]) toArray();
    }

    public boolean add(Statement statement) {
        try {
            rc.add(statement);
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }

        // the RepositoryConnection API does not provide an efficient means of knowing whether the repository was changed
        return false;
    }

    public boolean remove(Object o) {
        if (o instanceof Statement) {
            Statement st = (Statement) o;
            try {
                rc.remove(st.getSubject(), st.getPredicate(), st.getObject(), st.getContext());
            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        }
        // the RepositoryConnection API does not provide an efficient means of knowing whether a statement was removed
        return false;
    }

    public boolean containsAll(Collection<?> objects) {
        for (Object o : objects) {
            if (!contains(o)) {
                return false;
            }
        }

        return true;
    }

    public boolean addAll(Collection<? extends Statement> statements) {
        for (Statement s : statements) {
            add(s);
        }

        return false;
    }

    public boolean removeAll(Collection<?> objects) {
        for (Object o : objects) {
            remove(o);
        }

        return false;
    }

    public boolean retainAll(Collection<?> objects) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        try {
            rc.clear();
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    private class RepositoryResultIterator implements Iterator<Statement> {
        private final RepositoryResult result;

        private RepositoryResultIterator(RepositoryResult result) {
            this.result = result;
        }

        public boolean hasNext() {
            try {
                return result.hasNext();
            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        }

        public Statement next() {
            try {
                return (Statement) result.next();
            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        }

        public void remove() {
            try {
                result.remove();
            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
