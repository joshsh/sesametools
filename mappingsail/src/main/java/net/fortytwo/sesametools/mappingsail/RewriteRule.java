package net.fortytwo.sesametools.mappingsail;

import org.openrdf.model.URI;

/**
 * Represents a rule to map an original URI to a new URI.
 * Rules are considered to be complete and self contained: MappingSail does not impose its own rewriting logic.
 * <p/>
 * Author: josh
 * Date: Aug 11, 2009
 * Time: 3:35:10 PM
 */
public interface RewriteRule {
    /**
     * @param original an complete URI (i.e. not only a URI prefix) to be rewritten
     * @return the resulting URI
     */
    URI rewrite(URI original);
}
