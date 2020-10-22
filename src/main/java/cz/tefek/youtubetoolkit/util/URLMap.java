package cz.tefek.youtubetoolkit.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class URLMap
{
    public static final String PAIR_SEPARATOR = "&";
    public static final String KEYVAL_SEPARATOR = "=";

    public static Map<String, String> decode(String urlMap)
    {
        var map = new HashMap<String, String>();

        Arrays.stream(urlMap.split(PAIR_SEPARATOR)).map(kv -> kv.split(KEYVAL_SEPARATOR, 2)).forEach(kv ->
        {
            if (kv.length == 1)
            {
                System.err.printf("Expected a key and a value, got '%s' instead.\n", kv[0]);
                return;
            }

            map.put(kv[0], URLMap.urlDecode(kv[1]));
        });

        return map;
    }

    public static String urlDecode(String input)
    {
        return URLDecoder.decode(input, StandardCharsets.UTF_8);
    }

    public static String urlEncode(String input)
    {
        return URLEncoder.encode(input, StandardCharsets.UTF_8);
    }
}
