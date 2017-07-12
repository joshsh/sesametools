package net.fortytwo.sesametools.sesamize;

import net.fortytwo.sesametools.SesameTools;
import net.fortytwo.sesametools.sesamize.commands.Construct;
import net.fortytwo.sesametools.sesamize.commands.Dump;
import net.fortytwo.sesametools.sesamize.commands.Import;
import net.fortytwo.sesametools.sesamize.commands.Random;
import net.fortytwo.sesametools.sesamize.commands.Select;
import net.fortytwo.sesametools.sesamize.commands.Translate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A collection of command-line tools for Sesame
 *
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class Sesamize {
    private final static Logger logger = LoggerFactory.getLogger(Sesamize.class);

    private static final String
            NAME = "Sesamize";
    public static final String
            DEFAULT_BASEURI = "http://example.org/baseURI#";

    private static Map<String, Command> subcommands = new HashMap<>();

    static {
        constructSubcommands();
    }

    private static void printUsage() {
        System.out.println("Usage: sesamize [options] subcommand [arguments]");
        System.out.println("Options:\n"
                + "  -h           Print this help and exit\n"
                + "  -v           Print version information and exit");
        System.out.println("Subcommands:");
        StringBuilder sb = new StringBuilder();
        printSubcommands(sb);
        System.out.print(sb);
        System.out.println("E.g.");
        System.out.println("  sesamize translate -i trig -o nq mydata.trig > mydata.nq");
        System.out.println("For more information, please see:\n"
                + "  <URL:http://github.com/joshsh/sesametools/tree/master/sesamize>.");
    }

    private static void printUsageAndExit(int exitCode) {
        printUsage();
        System.exit(exitCode);
    }

    private static void printVersion() {
        String version = SesameTools.getProperties().getProperty(SesameTools.VERSION_PROP);
        System.out.println(NAME + " " + version);
    }

    public static void main(final String[] args) {
        SesamizeArgs a = new SesamizeArgs(args);

        if (args.length < 1) {
            printUsageAndExit(1);
        }

        if (null != a.getOption(null, "h", "help")) {
            printUsageAndExit(0);
        }

        if (null != a.getOption(null, "v", "version")) {
            printVersion();
            System.exit(0);
        }

        Command c = subcommands.get(args[0]);

        if (null == c) {
            printUsageAndExit(1);
        } else {
            try {
                c.execute(a);
            } catch (Exception e) {
                logger.error("Exited with error", e);
                System.exit(1);
            }
        }
    }

    private static void constructSubcommands() {
        for (Command command : new Command[]{
                new Construct(), new Dump(), new Import(), new Random(), new Select(), new Translate()}) {
            subcommands.put(command.getName(), command);
        }
    }

    private static void printSubcommands(final StringBuilder out) {
        List<String> commandNames = new LinkedList<>();
        commandNames.addAll(subcommands.keySet());
        Collections.sort(commandNames);

        for (String name : commandNames) {
            Command command = subcommands.get(name);
            out.append("  sesamize").append(" ").append(command.getName());
            for (Command.Parameter param : command.getAnonymousParameters()) {
                out.append(" ");
                if (!param.isRequiredAndHasNoDefaultValue()) {
                    out.append("[");
                }
                out.append(classNameForHelp(param));
                if (!param.isRequiredAndHasNoDefaultValue()) {
                    out.append("]");
                }
            }
            for (Command.Parameter param : command.getNamedParameters().values()) {
                out.append(" ");
                if (!param.isRequiredAndHasNoDefaultValue()) {
                    out.append("[");
                }
                out.append(getShortestParamNameForHelp(param));
                out.append(" ").append(classNameForHelp(param));
                if (!param.isRequiredAndHasNoDefaultValue()) {
                    out.append("]");
                }
            }
            out.append("\n");
            List<TwoCol> table = new LinkedList<>();
            for (Command.Parameter param : command.getAnonymousParameters()) {
                String className = classNameForHelp(param);
                table.add(new TwoCol(className, null == param.getDescription() ? "" : param.getDescription()));
            }
            List<String> paramNames = new LinkedList<>();
            paramNames.addAll(command.getNamedParameters().keySet());
            Collections.sort(paramNames);
            for (String pName : paramNames) {
                Command.Parameter param = command.getNamedParameters().get(pName);
                StringBuilder sb = new StringBuilder();
                String className = classNameForHelp(param);
                if (null != param.getName()) {
                    if (null != param.getShortName()) {
                        sb.append("-").append(param.getShortName()).append(" ").append(className).append(", ");
                    }
                    sb.append("--").append(param.getName()).append(" ").append(className);
                } else {
                    sb.append(className);
                }
                table.add(new TwoCol(sb.toString(), null == param.getDescription() ? "" : param.getDescription()));
            }
            int longest = 0;
            for (TwoCol twoCol : table) {
                if (twoCol.first.length() > longest) {
                    longest = twoCol.first.length();
                }
            }
            for (TwoCol twoCol : table) {
                out.append("    ").append(twoCol.first);
                for (int i = 0; i < longest - twoCol.first.length() + 1; i++) {
                    out.append(" ");
                }
                out.append(twoCol.second).append("\n");
            }
        }
    }

    private static String getShortestParamNameForHelp(final Command.Parameter param) {
        return null != param.getShortName()
                ? "-" + param.getShortName()
                : "--" + param.getName();
    }

    private static String classNameForHelp(final Command.Parameter param) {
        return param.getValueClass().getSimpleName().toUpperCase();
    }

    private static class TwoCol {
        private final String first, second;

        private TwoCol(String first, String second) {
            this.first = first;
            this.second = second;
        }
    }
}
