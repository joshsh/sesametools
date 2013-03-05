
package net.fortytwo.sesametools;

import org.openrdf.query.Dataset;
import org.openrdf.model.URI;

import java.util.Set;
import java.util.HashSet;

/**
 * Author: josh
 * Date: Apr 4, 2008
 * Time: 4:37:29 PM
 */
public class SimpleDatasetImpl implements Dataset {
    private Set<URI> defaultGraphs;
    private Set<URI> namedGraphs;

    public SimpleDatasetImpl() {
        this.defaultGraphs = new HashSet<URI>();
        this.namedGraphs = new HashSet<URI>();
    }

    public SimpleDatasetImpl(final Set<URI> defaultGraphs,
                             final Set<URI> namedGraphs) {
        this.defaultGraphs = defaultGraphs;
        this.namedGraphs = namedGraphs;
    }

    public Set<URI> getDefaultGraphs() {
        return defaultGraphs;
    }

    public Set<URI> getNamedGraphs() {
        return namedGraphs;
    }

    @Override
    public URI getDefaultInsertGraph() {
        // FIXME: Implement me!
        return null;
    }

    @Override
    public Set<URI> getDefaultRemoveGraphs() {
        // FIXME: Implement me!
        return null;
    }
}
