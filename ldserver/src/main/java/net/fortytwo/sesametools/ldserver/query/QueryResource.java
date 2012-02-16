package net.fortytwo.sesametools.ldserver.query;

import net.fortytwo.sesametools.ldserver.LinkedDataServer;
import org.openrdf.sail.Sail;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Resource;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public abstract class QueryResource extends Resource {
    private static final Logger LOGGER = Logger.getLogger(QueryResource.class.getName());

    private static final String UTF_8 = "UTF-8";

    private static final int
            MAX_LIMIT = 500,
            DEFAULT_LIMIT = 300;

    private static final String LIMIT_PARAM = "limit";

    protected final String selfURI;
    protected Map<String, String> arguments;

    protected final Sail sail;
    //private final String query;

    public QueryResource(final Context context,
                         final Request request,
                         final Response response) throws Exception {
        super(context, request, response);

        selfURI = request.getResourceRef().toString();

        /*
        System.out.println("selfURI = " + selfURI);
        System.out.println("baseRef = " + request.getResourceRef().getBaseRef());
        System.out.println("host domain = " + request.getResourceRef().getHostDomain());
        System.out.println("host identifier = " + request.getResourceRef().getHostIdentifier());
        System.out.println("hierarchical part = " + request.getResourceRef().getHierarchicalPart());
        System.out.println("host ref = " + request.getHostRef().toString());
        //*/

        sail = LinkedDataServer.getServer(context).getSail();

        //getVariants().add(new Variant(MediaType.APPLICATION_JSON));

        int i = selfURI.lastIndexOf("?");
        arguments = new HashMap<String, String>();
        if (0 < i) {
            String args = selfURI.substring(i + 1);
            if (0 < args.length()) {
                arguments = parseParams(args);
            }
        }
        /*
        for (String name : arguments.keySet()) {
            System.out.println("argument: " + name + " = " + arguments.get(name));
        }
        //*/
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

    protected int readLimit() {
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
                LOGGER.warning("bad limit value: " + l);
                limit = DEFAULT_LIMIT;
            }
        }

        return limit;
    }

    protected String urlDecode(final String encoded) throws UnsupportedEncodingException {
        return URLDecoder.decode(encoded, UTF_8);
    }

    public boolean allowDelete() {
        return false;
    }

    public boolean allowGet() {
        return true;
    }

    public boolean allowPost() {
        return true;
    }

    public boolean allowPut() {
        return false;
    }
}