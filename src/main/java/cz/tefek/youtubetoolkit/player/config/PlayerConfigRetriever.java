package cz.tefek.youtubetoolkit.player.config;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import cz.tefek.youtubetoolkit.config.Configuration;

public class PlayerConfigRetriever
{
    public static PlayerConfig get(String videoID) throws Exception
    {
        Document videoDocument = Jsoup.connect(Configuration.YOUTUBE_BASE_URL + "/watch?v=" + videoID).userAgent(Configuration.USER_AGENT).get();
        Elements scriptTags = videoDocument.getElementsByTag("script");

        String jsUrl = null;
        String configScript = null;

        for (Element element : scriptTags)
        {
            String scriptContents = element.data();
            String src = element.attr("src");

            if (src.matches("/s/player/[0-9a-z]+/player_.+?/.+?/base.js"))
            {
                jsUrl = Configuration.SCRIPT_BASE_URL + src;
            }

            if (scriptContents.contains("ytplayer.config ="))
            {
                configScript = scriptContents;
            }
        }

        if (jsUrl == null)
            throw new RuntimeException("Did not find the YouTube player config JavaScript file.");
        else
            System.out.printf("Detected player JS URL: %s%n", jsUrl);

        if (configScript == null)
            throw new RuntimeException("Did not find ytplayer.config.");
        else
            System.out.printf("Detected ytplayer.config.%n");

        configScript = configScript.substring(configScript.indexOf("ytplayer.config ="));
        configScript = configScript.substring(configScript.indexOf("{"));

        int bracketCounter = 0;

        boolean foundEnd = false;
        boolean escapeNext = false;
        boolean inString = false;

        for (int i = 0; i < configScript.length(); i++)
        {
            switch (configScript.charAt(i))
            {
                case '{':
                    if (!inString)
                        bracketCounter++;
                    break;
                case '}':
                    if (!inString)
                        foundEnd = --bracketCounter == 0;
                    break;
                case '\\':
                    escapeNext = !escapeNext;
                    break;
                case '"':
                    if (!escapeNext)
                        inString = !inString;
                    else
                        escapeNext = false;
                    break;
                default:
                    escapeNext = false;
                    break;
            }

            if (foundEnd)
            {
                configScript = configScript.substring(0, i + 1);
                break;
            }
        }

        JSONTokener jsonTokener = new JSONTokener(configScript);

        JSONObject mainObj = new JSONObject(jsonTokener);
        JSONObject args = mainObj.getJSONObject("args");
        JSONObject playerResponse = new JSONObject(new JSONTokener(args.getString("player_response")));

        return new PlayerConfig(jsUrl,
                playerResponse.getJSONObject("playerConfig"),
                playerResponse.getJSONObject("videoDetails"),
                playerResponse.getJSONObject("streamingData"));
    }
}
