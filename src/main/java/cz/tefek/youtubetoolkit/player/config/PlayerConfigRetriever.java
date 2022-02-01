package cz.tefek.youtubetoolkit.player.config;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.nio.file.Files;
import java.nio.file.Path;

import cz.tefek.youtubetoolkit.config.Configuration;

public class PlayerConfigRetriever
{
    public static PlayerConfig get(String videoID) throws Exception
    {
        Document videoDocument = Jsoup.connect(Configuration.YOUTUBE_BASE_URL + "/watch?v=" + videoID)
            .userAgent(Configuration.USER_AGENT)
            // The so-called cookie consent cookie actually seems to be necessary, because YouTube checks it server-side for some reason
            .cookie("CONSENT", "YES+cb.99999999-99-p0.en+FX+000")
            .get();

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

            if (scriptContents.contains("ytInitialPlayerResponse ="))
            {
                configScript = scriptContents;
            }
        }

        if (jsUrl == null)
            throw new RuntimeException("Did not find the YouTube player config JavaScript file.");
        else
            System.out.printf("Detected player JS URL: %s%n", jsUrl);

        if (configScript == null)
            throw new RuntimeException("Did not find ytInitialPlayerResponse.");
        else
            System.out.printf("Detected ytInitialPlayerResponse.%n");

        configScript = configScript.substring(configScript.indexOf("ytInitialPlayerResponse ="));
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

        var playabilityStatus = mainObj.optJSONObject("playabilityStatus");
        if (playabilityStatus != null)
        {
            var status = playabilityStatus.optString("status");

            if ("LOGIN_REQUIRED".equalsIgnoreCase(status))
                throw new UnsupportedOperationException("YTRetriever does not support (age/...) restricted videos.");
        }

        return new PlayerConfig(jsUrl,
            mainObj.getJSONObject("playerConfig"),
            mainObj.getJSONObject("videoDetails"),
            mainObj.getJSONObject("streamingData"));
    }
}
