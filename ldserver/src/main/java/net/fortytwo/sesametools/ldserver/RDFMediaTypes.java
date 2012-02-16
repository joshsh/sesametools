package net.fortytwo.sesametools.ldserver;

import org.openrdf.rio.RDFFormat;
import org.restlet.data.MediaType;
import org.restlet.representation.Variant;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class RDFMediaTypes {
    private static final Map<RDFFormat, MediaType> rdfFormatToMediaTypeMap;
    private static final Map<MediaType, RDFFormat> mediaTypeToRdfFormatMap;
    private static final List<Variant> rdfVariants;

    private static final Map<String, RDFFormat> SUFFIX_TO_FORMAT;
    private static final Map<RDFFormat, String> FORMAT_TO_SUFFIX;

    static {
        rdfFormatToMediaTypeMap = new HashMap<RDFFormat, MediaType>();

        // Note: preserves order of insertion
        mediaTypeToRdfFormatMap = new LinkedHashMap<MediaType, RDFFormat>();

        // Note: the first format registered becomes the default format.
        registerRdfFormat(RDFFormat.RDFXML);
        registerRdfFormat(RDFFormat.TURTLE);
        registerRdfFormat(RDFFormat.N3);
        registerRdfFormat(RDFFormat.NTRIPLES);
        registerRdfFormat(RDFFormat.TRIG);
        registerRdfFormat(RDFFormat.TRIX);

        rdfVariants = new LinkedList<Variant>();
        for (MediaType mediaType : mediaTypeToRdfFormatMap.keySet()) {
            rdfVariants.add(new Variant(mediaType));
        }

        FORMAT_TO_SUFFIX = new HashMap<RDFFormat, String>();
        FORMAT_TO_SUFFIX.put(RDFFormat.RDFXML, "rdf");
        FORMAT_TO_SUFFIX.put(RDFFormat.TURTLE, "ttl");
        FORMAT_TO_SUFFIX.put(RDFFormat.N3, "n3");
        FORMAT_TO_SUFFIX.put(RDFFormat.NTRIPLES, "nt");
        FORMAT_TO_SUFFIX.put(RDFFormat.TRIG, "trig");
        FORMAT_TO_SUFFIX.put(RDFFormat.TRIX, "trix");

        SUFFIX_TO_FORMAT = new HashMap<String, RDFFormat>();
        for (RDFFormat format : FORMAT_TO_SUFFIX.keySet()) {
            SUFFIX_TO_FORMAT.put(FORMAT_TO_SUFFIX.get(format), format);
        }
    }

    public static List<Variant> getRDFVariants() {
        return rdfVariants;
    }

    public static RDFFormat findRdfFormat(final MediaType mediaType) {
        return mediaTypeToRdfFormatMap.get(mediaType);
    }

    public static MediaType findMediaType(final RDFFormat format) {
        return rdfFormatToMediaTypeMap.get(format);
    }

    public static Variant findVariant(final RDFFormat format) {
        return new Variant(findMediaType(format));
    }

    public static RDFFormat findFormat(final String suffix) {
        return SUFFIX_TO_FORMAT.get(suffix);
    }

    public static String findSuffix(final RDFFormat format) {
        return FORMAT_TO_SUFFIX.get(format);
    }

    private static void registerRdfFormat(final RDFFormat format) {
        MediaType t;

        if (RDFFormat.RDFXML == format) {
            t = MediaType.APPLICATION_RDF_XML;
        } else {
            t = new MediaType(format.getDefaultMIMEType());
        }

        rdfFormatToMediaTypeMap.put(format, t);
        mediaTypeToRdfFormatMap.put(t, format);
    }
}
