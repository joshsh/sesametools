package net.fortytwo.sesametools;

import java.io.IOException;
import java.util.Properties;

/**
 * Global constants
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class SesameTools {
    private static final Properties PROPERTIES;

    public static final String
        VERSION_PROP = "net.fortytwo.sesametools.version";

    static {
        try {
            PROPERTIES = new Properties();

            PROPERTIES.load(SesameTools.class.getResourceAsStream("sesametools.properties"));
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    
    private SesameTools() {
        
    }

    public static Properties getProperties() {
        return PROPERTIES;
    }
}
