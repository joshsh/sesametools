package net.fortytwo.sesametools.mappingsail;

import org.openrdf.model.URI;

/**
 * Author: josh
 * Date: Aug 11, 2009
 * Time: 3:35:10 PM
 */
public interface URIRewriter {
    URI rewrite(URI original);
}
