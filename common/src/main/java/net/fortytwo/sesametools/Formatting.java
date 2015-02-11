
package net.fortytwo.sesametools;


/**
 * Miscellaneous helper methods for string formatting
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class Formatting {

    private static final int
            FOUR = 4,
            EIGHT = 8,
            SIXTEEN = 16;

    // Note: extended characters are not escaped for printing.
    public static String escapeString(final String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\"':
                    sb.append("\\\"");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                default:
                    sb.append(c);
            }
        }

        return sb.toString();
    }

    // Note: assumes a properly formatted (escaped) String argument.
    public static String unescapeString(final String s) {
        StringBuilder sb = new StringBuilder();
        String seq;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if ('\\' == c) {
                i++;

                switch (s.charAt(i)) {
                    case '\\':
                        sb.append('\\');
                        break;
                    case '\'':
                        sb.append('\'');
                        break;
                    case '\"':
                        sb.append('\"');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case 'n':
                        sb.append('\n');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 'u':
                        seq = s.substring(i + 1, i + FOUR + 1);
                        sb.append(toUnicodeChar(seq));
                        i += FOUR;
                        break;
                    case 'U':
                        seq = s.substring(i + 1, i + EIGHT + 1);
                        sb.append(toUnicodeChar(seq));
                        i += EIGHT;
                        break;
                    default:
                        throw new IllegalArgumentException("bad escape sequence: \\"
                                + s.charAt(i) + " at character " + (i - 1));
                }
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    private static char toUnicodeChar(final String unicode) {
        return (char) Integer.parseInt(unicode, SIXTEEN);
    }
}
