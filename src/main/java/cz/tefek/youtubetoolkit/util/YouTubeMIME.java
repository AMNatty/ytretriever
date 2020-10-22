package cz.tefek.youtubetoolkit.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class YouTubeMIME
{
    private final String type;
    private final String name;
    private final String suffix;
    private final Map<String, String> rawParams;
    private final List<String> codecs;

    private YouTubeMIME(String type, String name, String suffix, Map<String, String> rawParams, List<String> codecs)
    {
        this.type = type;
        this.name = name;
        this.suffix = suffix;
        this.rawParams = rawParams;
        this.codecs = codecs;
    }

    private static String unquote(String string)
    {
        if (string.matches("\".*?\""))
        {
            return string.substring(1, string.length() - 1);
        }

        return string;
    }

    public static YouTubeMIME from(String ytMIME)
    {
        final var reType = "[a-z]+?";
        final var reName = "[a-z0-9.-]+?";
        final var reSuffix = "\\+.+?";
        final var reParameterList = ";\\s*[a-z]+=\"?.+?\"?";

        final var pattern = Pattern.compile("^(" + reType + ")/(" + reName + ")(" + reSuffix + ")?(" + reParameterList + ")*$", Pattern.CASE_INSENSITIVE);

        var matcher = pattern.matcher(ytMIME);

        if (!matcher.find())
        {
            throw new IllegalArgumentException("Unexpected MIME type: " + ytMIME);
        }

        var matchResult = matcher.toMatchResult();

        var type = matchResult.group(1);
        var name = matchResult.group(2);
        String suffix = null;
        var options = new HashMap<String, String>();

        int groupCnt = matchResult.groupCount();

        if (groupCnt >= 3)
        {
            int i = 3;
            var g3 = matchResult.group(i);

            if (g3 != null)
            {
                suffix = g3;
                i++;
            }

            for (; i <= groupCnt; i++)
            {
                var paramData = matchResult.group(i);

                if (paramData != null)
                {
                    var paramKV = paramData.split("=", 2);
                    var paramKey = paramKV[0].substring(";".length()).strip();
                    var paramValue = unquote(paramKV[1]);

                    options.put(paramKey, paramValue);
                }
            }
        }

        var codecList = Arrays.stream(options.get("codecs").split(",")).map(String::trim).collect(Collectors.toList());

        return new YouTubeMIME(type, name, suffix, options, codecList);
    }

    public List<String> getCodecs()
    {
        return Collections.unmodifiableList(this.codecs);
    }

    public String getName()
    {
        return this.name;
    }

    public Map<String, String> getRawParams()
    {
        return Collections.unmodifiableMap(this.rawParams);
    }

    public Optional<String> getSuffix()
    {
        return Optional.ofNullable(this.suffix);
    }

    public String getType()
    {
        return this.type;
    }
}
