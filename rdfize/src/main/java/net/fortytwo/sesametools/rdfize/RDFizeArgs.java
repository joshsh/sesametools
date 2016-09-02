package net.fortytwo.sesametools.rdfize;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParserRegistry;
import org.eclipse.rdf4j.rio.RDFWriterRegistry;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
class RDFizeArgs {
    private static final Logger logger = Logger.getLogger(RDFizeArgs.class.getName());

    public final Set<String> flags;
    public final Map<String, String> pairs;
    public final List<String> nonOptions;

    public final Map<String, RDFFormat> formatsByName = new HashMap<>();

    public RDFizeArgs(final String[] args) {
        flags = new HashSet<>();
        pairs = new HashMap<>();
        nonOptions = new LinkedList<>();

        boolean inOption = false;
        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            if (a.startsWith("-")) {
                if (inOption) {
                    flags.add(getOptionName(args[i - 1]));
                } else {
                    inOption = true;
                }
            } else {
                if (inOption) {
                    pairs.put(getOptionName(args[i - 1]), a);
                    inOption = false;
                } else {
                    nonOptions.add(a);
                }
            }
        }

        if (inOption) {
            flags.add(getOptionName(args[args.length - 1]));
        }

        prepareFormats();
    }

    public boolean isEmpty() {
        return flags.isEmpty() && pairs.isEmpty() && nonOptions.isEmpty();
    }

    public String getOption(final String defaultValue,
                            final String... alternatives) {
        for (String s : alternatives) {
            if (flags.contains(s)) {
                return "true";
            }

            String o = pairs.get(s);
            if (null != o) {
                return o;
            }
        }

        return defaultValue;
    }

    public SparqlResultFormat getSparqlResultFormat(final SparqlResultFormat defaultValue,
                                                    final String... alternatives) {
        String s = getOption(null, alternatives);
        return null == s ? defaultValue : SparqlResultFormat.lookupByNickname(s);
    }

    public RDFFormat getRDFFormat(final RDFFormat defaultValue,
                                  final String... alternatives) {
        String s = getOption(null, alternatives);

        // If they specified an option, try to find it out of the non-standard
        // list of descriptors in Sesamize.rdfFormatByName
        if (null != s) {
            return findRDFFormat(s);
        } else { // otherwise return the default value
            return defaultValue;
        }
    }

    public RDFFormat getRDFFormat(final File file,
                                  final RDFFormat defaultValue,
                                  final String... alternatives) {
        String s = getOption(null, alternatives);
        RDFFormat format;

        // If they specified an option, try to find it out of the non-standard
        // list of descriptors in Sesamize.rdfFormatByName
        if (null != s) {
            format = findRDFFormat(s);
        } else {
            // otherwise try to find the format based on the file name extension,
            // using the specified default value as a fallback
            Optional<RDFFormat> f = RDFFormat.matchFileName(file.getName(), null);
            format = f.isPresent() ? f.get() : defaultValue;
        }

        return format;
    }

    private void prepareFormats() {
        for (RDFFormat format : getInputFormats()) {
            formatsByName.put(format.getName().toLowerCase(), format);
        }
        for (RDFFormat format : getOutputFormats()) {
            formatsByName.put(format.getName().toLowerCase(), format);
        }
    }

    public Set<RDFFormat> getInputFormats() {
        return RDFParserRegistry.getInstance().getKeys();
    }

    public Set<RDFFormat> getOutputFormats() {
        return RDFWriterRegistry.getInstance().getKeys();
    }

    private RDFFormat findRDFFormat(final String formatName) {
        RDFFormat format = formatsByName.get(formatName.toLowerCase());
        if (null != format) {
            return format;
        }

        Optional<RDFFormat> optFormat;

        optFormat = RDFParserRegistry.getInstance().getFileFormatForMIMEType(formatName);

        if (!optFormat.isPresent()) {
            optFormat = RDFParserRegistry.getInstance().getFileFormatForFileName("example." + formatName);
        }
        
        if (!optFormat.isPresent()) {
            throw new IllegalArgumentException("no matching RDF format for '" + formatName + "'");
        }

        return optFormat.get();
    }

    private String getOptionName(final String option) {
        if (option.startsWith("--")) {
            return option.substring(2);
        } else if (option.startsWith("-")) {
            return option.substring(1);
        } else {
            return option;
        }
    }

}
