package net.fortytwo.sesametools;

import info.aduna.iteration.CloseableIteration;
import org.openrdf.model.IRI;
import org.openrdf.model.Model;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * An adapter which wraps a RepositoryConnection as a Graph
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class RepositoryGraph implements Model {
    private static final boolean INFER = false;

    private final RepositoryConnection rc;

    public RepositoryGraph(final RepositoryConnection rc) throws RepositoryException {
        this.rc = rc;
    }

    @Override
    public ValueFactory getValueFactory() {
        return rc.getValueFactory();
    }

    @Override
    public Model unmodifiable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Namespace> getNamespaces() {
        Function<Namespace, Namespace> f = identity();
        return toSet(rc.getNamespaces(), f);
    }

    private <T> Function<T, T> identity() {
        return new Function<T, T>() {
            @Override
            public T apply(T t) {
                return t;
            }
        };
    }

    private <T, S> Set<S> toSet(CloseableIteration<T, RepositoryException> iter,
                             Function<T, S> mapping) {
        Set<S> result  = new HashSet<>();
        try {
            while (iter.hasNext()) {
                result.add(mapping.apply(iter.next()));
            }
        } finally {
            iter.close();
        }

        return result;
    }

    @Override
    public void setNamespace(Namespace namespace) {
        rc.setNamespace(namespace.getPrefix(), namespace.getName());
    }

    @Override
    public Optional<Namespace> removeNamespace(String s) {
        rc.removeNamespace(s);
        return Optional.empty();
    }

    @Override
    public boolean contains(Resource resource, IRI iri, Value value, Resource... resources) {

        try (CloseableIteration<Statement, RepositoryException> iter
                     = rc.getStatements(resource, iri, value, resources)) {
            return iter.hasNext();
        }
    }

    @Override
    public boolean add(Resource s, IRI p, Value o, Resource... c) {
        try {
            rc.begin();
            rc.add(s, p, o, c);
            rc.commit();
            return true;
        } catch (RepositoryException e) {
            throw new RepositoryGraphRuntimeException(e);
        }
    }

    @Override
    public boolean clear(Resource... resources) {
        rc.clear(resources);
        // note: we don't know whether any statements are removed
        return false;
    }

    @Override
    public boolean remove(Resource resource, IRI iri, Value value, Resource... resources) {
        rc.remove(resource, iri, value, resources);
        // note: we don't know whether any statements are removed
        return false;
    }

    @Override
    public Model filter(Resource resource, IRI iri, Value value, Resource... resources) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Resource> subjects() {
        Function<Statement, Resource> f = new Function<Statement, Resource>() {
            @Override
            public Resource apply(Statement statement) {
                return statement.getSubject();
            }
        };
        return toSet(rc.getStatements(null, null, null), f);
    }

    @Override
    public Set<IRI> predicates() {
        Function<Statement, IRI> f = new Function<Statement, IRI>() {
            @Override
            public IRI apply(Statement statement) {
                return statement.getPredicate();
            }
        };
        return toSet(rc.getStatements(null, null, null), f);
    }

    @Override
    public Set<Value> objects() {
        Function<Statement, Value> f = new Function<Statement, Value>() {
            @Override
            public Value apply(Statement statement) {
                return statement.getObject();
            }
        };
        return toSet(rc.getStatements(null, null, null), f);
    }

    // note: the returned iterator contains a CloseableIteration which will not be closed
    @Override
    public Iterator<Statement> match(Resource s, IRI p, Value o, Resource... c) {
        RepositoryResult<Statement> result;
        try {
            result = rc.getStatements(s, p, o, INFER, c);
        } catch (RepositoryException e) {
            throw new RepositoryGraphRuntimeException(e);
        }
        return new RepositoryResultIterator(result);
    }

    @Override
    public int size() {
        try {
            return (int) rc.size();
        } catch (RepositoryException e) {
            throw new RepositoryGraphRuntimeException(e);
        }
    }

    @Override
    public boolean isEmpty() {
        return 0 == size();
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof Statement) {
            Statement st = (Statement) o;
            try {
                try (RepositoryResult result = rc.getStatements(
                        st.getSubject(), st.getPredicate(), st.getObject(), INFER, st.getContext())) {
                    return result.hasNext();
                }
            } catch (Exception e) {
                throw new RepositoryGraphRuntimeException(e);
            }
        } else {
            return false;
        }
    }

    @Override
    public Iterator<Statement> iterator() {
        return match(null, null, null);
    }

    @Override
    public Object[] toArray() {
        int size = size();
        Object[] a = new Object[size];
        if (size > 0) try {
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
            throw new RepositoryGraphRuntimeException(e);
        }

        return a;
    }

    @Override
    public <T> T[] toArray(T[] ts) {
        // TODO: only Statement is acceptable as T
        return (T[]) toArray();
    }

    @Override
    public boolean add(Statement statement) {
        try {
            rc.add(statement);
        } catch (RepositoryException e) {
            throw new RepositoryGraphRuntimeException(e);
        }

        // the RepositoryConnection API does not provide an efficient means
        // of knowing whether the repository was changed
        return false;
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof Statement) {
            Statement st = (Statement) o;
            try {
                rc.remove(st.getSubject(), st.getPredicate(), st.getObject(), st.getContext());
            } catch (RepositoryException e) {
                throw new RepositoryGraphRuntimeException(e);
            }
        }
        // the RepositoryConnection API does not provide an efficient means of knowing whether a statement was removed
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> objects) {
        for (Object o : objects) {
            if (!contains(o)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean addAll(Collection<? extends Statement> statements) {
        for (Statement s : statements) {
            add(s);
        }

        return false;
    }

    @Override
    public boolean removeAll(Collection<?> objects) {
        for (Object o : objects) {
            remove(o);
        }

        return false;
    }

    @Override
    public boolean retainAll(Collection<?> objects) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        try {
            rc.clear();
        } catch (RepositoryException e) {
            throw new RepositoryGraphRuntimeException(e);
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
                throw new RepositoryGraphRuntimeException(e);
            }
        }

        public Statement next() {
            try {
                return (Statement) result.next();
            } catch (RepositoryException e) {
                throw new RepositoryGraphRuntimeException(e);
            }
        }

        public void remove() {
            try {
                result.remove();
            } catch (RepositoryException e) {
                throw new RepositoryGraphRuntimeException(e);
            }
        }
    }

    public class RepositoryGraphRuntimeException extends RuntimeException {
        public RepositoryGraphRuntimeException(final Throwable cause) {
            super(cause);
        }
    }
}
