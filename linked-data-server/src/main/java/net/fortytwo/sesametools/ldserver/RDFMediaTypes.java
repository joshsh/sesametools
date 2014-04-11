package net.fortytwo.sesametools.ldserver;

import org.openrdf.rio.RDFFormat;
import org.restlet.data.MediaType;
import org.restlet.representation.Variant;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class RDFMediaTypes {

    private static final List<Variant> RDF_VARIANTS;
    private static final Map<RDFFormat, MediaType> MEDATYPE_BY_RDFFORMAT;
    private static final Map<MediaType, RDFFormat> RDFFORMAT_BY_MEDIATYPE;

    static {
        RDF_VARIANTS = new LinkedList<Variant>();
        MEDATYPE_BY_RDFFORMAT = new HashMap<RDFFormat, MediaType>();
        RDFFORMAT_BY_MEDIATYPE = new HashMap<MediaType, RDFFormat>();

        LinkedHashSet<RDFFormat> toAdd = new LinkedHashSet<RDFFormat>();
        // add RDF/XML first, as the default format
        toAdd.add(RDFFormat.RDFXML);
        // now add the others
        toAdd.addAll(RDFFormat.values());

        for (RDFFormat f : toAdd) {
            MediaType mt = new MediaType(f.getDefaultMIMEType());
            Variant v = new Variant(mt);
            MEDATYPE_BY_RDFFORMAT.put(f, mt);
            RDFFORMAT_BY_MEDIATYPE.put(mt, f);
            RDF_VARIANTS.add(v);
        }
    }

    public static List<Variant> getRDFVariants() {
        return RDF_VARIANTS;
    }

    public static RDFFormat findRdfFormat(final MediaType mediaType) {
        return RDFFORMAT_BY_MEDIATYPE.get(mediaType);
    }

    public static MediaType findMediaType(final RDFFormat format) {
        return MEDATYPE_BY_RDFFORMAT.get(format);
    }

    public static Variant findVariant(final RDFFormat format) {
        MediaType mt = findMediaType(format);
        return null == mt ? null : new Variant(MEDATYPE_BY_RDFFORMAT.get(format));
    }
}
