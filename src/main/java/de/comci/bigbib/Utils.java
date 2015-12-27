/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.comci.bigbib;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author Sebastian
 */
public class Utils {
    
    // http://stackoverflow.com/questions/5902090/how-to-extract-parameters-from-a-given-url
    public static Map<String, List<String>> getQueryParams(String url) {
    try {
        Map<String, List<String>> params = new HashMap<String, List<String>>();
        String[] urlParts = url.split("\\?");
        if (urlParts.length > 1) {
            String query = urlParts[1];
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                String key = URLDecoder.decode(pair[0], "UTF-8");
                String value = "";
                if (pair.length > 1) {
                    value = URLDecoder.decode(pair[1], "UTF-8");
                }

                List<String> values = params.get(key);
                if (values == null) {
                    values = new ArrayList<String>();
                    params.put(key, values);
                }
                values.add(value);
            }
        }

        return params;
    } catch (UnsupportedEncodingException ex) {
        throw new AssertionError(ex);
    }
    
}

    public static String getFunction(String type, String name) throws IOException {
        InputStream mapFunction = Utils.class.getClassLoader().getResourceAsStream(String.format("%s/%s.js", type, name));
        StringWriter writer = new StringWriter();
        IOUtils.copy(mapFunction, writer, "utf-8");
        return writer.toString();
    }

    public static String getReduceFunction(String name) throws IOException {
        return getFunction("reduce", name);
    }

    public static String getMapFunction(String name) throws IOException {
        return getFunction("map", name);
    }
    
}
