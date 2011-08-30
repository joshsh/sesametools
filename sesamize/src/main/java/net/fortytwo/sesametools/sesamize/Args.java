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
        return null == s ? defaultValue : Sesamize.findRDFFormat(s);
    }

    public RDFFormat getRDFFormat(final File file,
                                  final RDFFormat defaultValue,
                                  final String... alternatives) {
        String s = getOption(null, alternatives);
        RDFFormat f = null == s ? RDFFormat.forFileName(file.getName(), defaultValue) : Sesamize.findRDFFormat(s);
        if (null == f) {
            String n = file.getName();
            if (n.endsWith(".nq")
                    || (n.endsWith(".nquad"))
                    || (n.endsWith(".nquads"))) {
                f = NQuadsFormat.NQUADS;
            }
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
