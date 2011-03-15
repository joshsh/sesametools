package net.fortytwo.sesametools.mappingsail;

import org.openrdf.model.URI;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: josh
 * Date: Aug 11, 2009
 * Time: 3:48:36 PM
 */
public class MappingSchema {
    public enum PartOfSpeech {
        SUBJECT, PREDICATE, OBJECT, GRAPH
    }

    public enum Action {
        TO_STORE, FROM_STORE
    }

    private final URIRewriter defaultRewriter = new URIRewriter() {
        public URI rewrite(final URI original) {
            return original;
        }
    };

    private final Map<String, URIRewriter> rewriters
            = new HashMap<String, URIRewriter>();

    public URIRewriter getRewriter(final PartOfSpeech partOfSpeech,
                                   final Action action) {
        URIRewriter r = rewriters.get("" + partOfSpeech + action);
        return null == r
                ? defaultRewriter
                : r;
    }

    public void setRewriter(final PartOfSpeech partOfSpeech,
                            final Action action,
                            final URIRewriter rewriter) {
        rewriters.put("" + partOfSpeech + action, rewriter);
    }
}
