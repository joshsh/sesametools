package net.fortytwo.sesametools.sesamize;

import net.fortytwo.sesametools.nquads.NQuadsFormat;
import org.openrdf.rio.RDFFormat;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
class Args {
    public final Set<String> flags;
    public final Map<String, String> pairs;
    public final List<String> nonOptions;

    public Args(final String[] args) {
        flags = new HashSet<String>();
        pairs = new HashMap<String, String>();
        nonOptions = new LinkedList<String>();

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
    }

    public String getOption(final String defaultValue,
                            final String... alternatives) {
        for (String s : alternatives) {
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
        
        // If they specified an option, try to find it out of the non-standard list of descriptors in Sesamize.rdfFormatByName
        if(null != s) { 
            return Sesamize.findRDFFormat(s);
        } else { // otherwise return the default value
            return defaultValue;
        }
    }

    public RDFFormat getRDFFormat(final File file,
                                  final RDFFormat defaultValue,
                                  final String... alternatives) {
        String s = getOption(null, alternatives);
        RDFFormat f = null;
                
        // If they specified an option, try to find it out of the non-standard list of descriptors in Sesamize.rdfFormatByName
        if(null != s) {
            f = Sesamize.findRDFFormat(s);
        } else { // otherwise try to find the format based on the file name extension, using the specified default value as a fallback
            f = RDFFormat.forFileName(file.getName(), defaultValue);
        }
        
        return f;
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
