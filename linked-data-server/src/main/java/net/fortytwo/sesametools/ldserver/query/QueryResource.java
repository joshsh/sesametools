package net.fortytwo.sesametools.ldserver.query;

import net.fortytwo.sesametools.ldserver.LinkedDataServer;
import org.openrdf.sail.Sail;
import org.restlet.Request;
import org.restlet.Restlet;
import org.restlet.resource.ResourceException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public abstract class QueryResource extends Restlet {
    private static final Logger logger = Logger.getLogger(QueryResource.class.getName());

    private static final String UTF_8 = "UTF-8";

    private static final int
            MAX_LIMIT = 500,
            DEFAULT_LIMIT = 300;

    private static final String LIMIT_PARAM = "limit";

    protected String selfURI;

    protected Sail sail;
    //private final String query;

    protected Map<String, String> getArguments(final Request request) throws ResourceException {
         Map<String, String> arguments;
        selfURI = request.getResourceRef().toString();

        /*
        System.out.println("selfURI = " + selfURI);
        System.out.println("baseRef = " + request.getResourceRef().getBaseRef());
        System.out.println("host domain = " + request.getResourceRef().getHostDomain());
        System.out.println("host identifier = " + request.getResourceRef().getHostIdentifier());
        System.out.println("hierarchical part = " + request.getResourceRef().getHierarchicalPart());
        System.out.println("host ref = " + request.getHostRef().toString());
        //*/

        sail = LinkedDataServer.getInstance().getSail();

        //getVariants().add(new Variant(MediaType.APPLICATION_JSON));

        int i = selfURI.lastIndexOf("?");
        arguments = new HashMap<String, String>();
        if (0 < i) {
            String args = selfURI.substring(i + 1);
            if (0 < args.length()) {
                try {
                    arguments = parseParams(args);
                } catch (UnsupportedEncodingException e) {
                    throw new ResourceException(e);
                }
            }
        }
        /*
        for (String name : arguments.keySet()) {
            System.out.println("argument: " + name + " = " + arguments.get(name));
        }
        //*/

        return arguments;
    }

    protected Map<String, String> parseParams(final String s) throws UnsupportedEncodingException {
        Map<String, String> map = new HashMap<String, String>();

        String[] a = s.split("&");
        for (String p : a) {
            String[] b = p.split("=");
            map.put(urlDecode(b[0]), urlDecode(b[1]));
        }

        return map;
    }

    protected int readLimit(final Map<String, String> arguments) {
        String l = arguments.get(LIMIT_PARAM);
        int limit;
        if (null == l) {
            limit = DEFAULT_LIMIT;
        } else {
            try {
                limit = Integer.valueOf(l);

                if (limit > MAX_LIMIT) {
                    limit = MAX_LIMIT;
                } else if (limit < 1) {
                    limit = DEFAULT_LIMIT;
                }
            } catch (NumberFormatException e) {
                logger.warning("bad limit value: " + l);
                limit = DEFAULT_LIMIT;
            }
        }

        return limit;
    }

    protected String urlDecode(final String encoded) throws UnsupportedEncodingException {
        return URLDecoder.decode(encoded, UTF_8);
    }
}
