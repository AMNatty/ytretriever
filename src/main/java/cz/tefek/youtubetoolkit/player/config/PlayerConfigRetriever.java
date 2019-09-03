package cz.tefek.youtubetoolkit.player.config;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.json.JSONTokener;

public class PlayerConfigRetriever
{
    public static PlayerConfig get(String videoID) throws IOException
    {
        URL url = new URL("https://www.youtube.com/watch?v=" + videoID);
        Scanner streamScanner = new Scanner(url.openStream());

        StringBuilder pageStringBuilder = new StringBuilder();

        while (streamScanner.hasNext())
        {
            pageStringBuilder.append(streamScanner.nextLine());
        }

        streamScanner.close();

        Pattern playerConfigPattern = Pattern.compile("<script.*?>(.*?)</script>");
        Matcher playerConfigMatcher = playerConfigPattern.matcher(pageStringBuilder.toString());

        String configScript = null;

        while (playerConfigMatcher.find())
        {
            String script = playerConfigMatcher.group(1);

            if (script.contains("ytplayer.config"))
            {
                configScript = script;
                break;
            }
        }

        if (configScript != null)
        {
            configScript = configScript.substring(configScript.indexOf("ytplayer.config"));
            configScript = configScript.substring(configScript.indexOf("{"));

            int bracketCounter = 0;

            for (int i = 0; i < configScript.length(); i++)
            {
                if (configScript.charAt(i) == '{')
                {
                    bracketCounter++;
                }

                if (configScript.charAt(i) == '}')
                {
                    if (--bracketCounter == 0)
                    {
                        configScript = configScript.substring(0, i + 1);
                    }
                }
            }
        }

        JSONTokener jsonTokener = new JSONTokener(configScript);

        JSONObject mainObj = new JSONObject(jsonTokener);

        JSONObject assets = mainObj.getJSONObject("assets");
        JSONObject args = mainObj.getJSONObject("args");

        String jsUrl = "http://s.ytimg.com" + assets.getString("js");

        String fmts = args.optString("url_encoded_fmt_stream_map", null);
        String adaptiveFmts = args.optString("adaptive_fmts", null);
        String playerResponse = args.optString("player_response", null);

        return new PlayerConfig(jsUrl, fmts, adaptiveFmts, playerResponse);
    }
}
