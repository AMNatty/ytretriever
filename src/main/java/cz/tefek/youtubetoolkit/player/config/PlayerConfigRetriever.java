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

        String configScript = null;

        for (Element element : scriptTags)
        {
            String scriptContents = element.data();

            if (scriptContents.contains("ytplayer.config"))
            {
                configScript = scriptContents;
                break;
            }
        }

        if (configScript == null)
            throw new RuntimeException("Did not find ytplayer.config.");

        configScript = configScript.substring(configScript.indexOf("ytplayer.config"));
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

        JSONObject assets = mainObj.getJSONObject("assets");
        JSONObject args = mainObj.getJSONObject("args");

        String jsUrl = Configuration.SCRIPT_BASE_URL + assets.getString("js");

        String fmts = args.optString("url_encoded_fmt_stream_map", null);
        String adaptiveFmts = args.optString("adaptive_fmts", null);
        String playerResponse = args.optString("player_response", null);

        return new PlayerConfig(jsUrl, fmts, adaptiveFmts, playerResponse);
    }
}
