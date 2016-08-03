package net.fortytwo.sesametools.mappingsail;

import org.eclipse.rdf4j.model.IRI;

/**
 * Represents a rule to map an original URI to a new URI.
 * Rules are considered to be complete and self contained: MappingSail does not impose its own rewriting logic.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public interface RewriteRule {
    /**
     * @param original an complete URI (i.e. not only a URI prefix) to be rewritten
     * @return the resulting URI
     */
    IRI rewrite(IRI original);
}
