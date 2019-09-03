package cz.tefek.youtubetoolkit.parser;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;
import org.json.JSONTokener;

import cz.tefek.youtubetoolkit.descrambler.Descrambler;
import cz.tefek.youtubetoolkit.descrambler.DescramblerHelper;
import cz.tefek.youtubetoolkit.metadata.YouTubeMetadata;
import cz.tefek.youtubetoolkit.multimedia.Multimedia;
import cz.tefek.youtubetoolkit.multimedia.YouTubeMultimedia;
import cz.tefek.youtubetoolkit.player.response.PlayerResponse;

public class PlayerResponseParser
{
    public static PlayerResponse parse(Descrambler descrambler, String inputJsonString)
    {
        if (inputJsonString == null || inputJsonString.isBlank())
        {
            return null;
        }

        var jsonTokener = new JSONTokener(inputJsonString);
        var rootObj = new JSONObject(jsonTokener);

        var streamingData = rootObj.optJSONObject("streamingData");

        if (streamingData == null)
        {
            return null;
        }

        var videoDetailsObj = rootObj.getJSONObject("videoDetails");

        var videoID = videoDetailsObj.getString("videoId");
        var title = videoDetailsObj.getString("title");

        var author = videoDetailsObj.getString("author");

        var views = videoDetailsObj.getLong("viewCount");
        var length = videoDetailsObj.getLong("lengthSeconds");

        var allowRatings = videoDetailsObj.getBoolean("allowRatings");
        var rating = videoDetailsObj.getDouble("averageRating");

        var useCipher = videoDetailsObj.optBoolean("useCipher", false);

        var loudness = 0.0d;

        var playerConfig = rootObj.getJSONObject("playerConfig");

        if (playerConfig != null)
        {
            var audioConfig = playerConfig.optJSONObject("audioConfig");

            if (audioConfig != null)
            {
                loudness = playerConfig.optDouble("loudnessDb", 0.0d);
            }
        }

        var metadata = new YouTubeMetadata(videoID, title, author, views, length, allowRatings, rating, loudness, useCipher);

        var legacyFormats = streamingData.getJSONArray("formats");
        var legacyFmts = new ArrayList<YouTubeMultimedia>();

        legacyFormats.forEach(obj ->
        {
            var formatData = (JSONObject) obj;

            var quality = formatData.getString("qualityLabel");
            var itag = formatData.getInt("itag");
            var width = formatData.getInt("width");
            var height = formatData.getInt("height");

            var mime = formatData.getString("mimeType").split(";");

            var mediaType = mime[0];
            var codecs = mime[1].split("=")[1].replace("\"", "");

            var bitrate = formatData.getInt("bitrate");

            var fps = formatData.optInt("fps", Multimedia.UNKNOWN_FPS);

            var format = new Multimedia(false, quality, bitrate, width, height, itag, fps, mediaType, codecs);

            format.printData();

            var url = "";
            var actuallyUseCipher = useCipher || formatData.has("cipher");

            if (actuallyUseCipher)
            {
                System.out.println(" Cipher: enabled");
                var cipher = formatData.getString("cipher");

                var args = cipher.split("&");

                var argMap = new HashMap<String, String>();

                for (var arg : args)
                {
                    var argPair = arg.split("=");
                    var key = argPair[0];
                    var value = argPair[1];

                    argMap.put(key, DescramblerHelper.urlDecode(value));
                }

                argMap.forEach((k, v) ->
                {
                    System.out.printf("  %s: %s\n", k, v);
                });

                var signature = descrambler.descramble(argMap.get("s"));
                url = argMap.get("url") + "&" + argMap.get("sp") + "=" + DescramblerHelper.urlEncode(signature);
                System.out.println(" Deciphered signature: " + signature);
                System.out.println(" Deciphered URL: " + url);
            }
            else
            {
                System.out.println(" Cipher: disabled");
                url = formatData.getString("url");
                System.out.println(" URL: " + url);
            }

            legacyFmts.add(new YouTubeMultimedia(url, format));
        });

        var adaptiveFormats = streamingData.getJSONArray("adaptiveFormats");
        var adaptiveFmts = new ArrayList<YouTubeMultimedia>();

        adaptiveFormats.forEach(obj ->
        {
            var formatData = (JSONObject) obj;

            var itag = formatData.getInt("itag");
            var width = formatData.optInt("width", Multimedia.NOT_VIDEO);
            var height = formatData.optInt("height", Multimedia.NOT_VIDEO);

            var mime = formatData.getString("mimeType").split(";");

            var mediaType = mime[0];
            var codecs = mime[1].split("=")[1].replace("\"", "");

            var bitrate = formatData.getInt("bitrate");

            var fps = formatData.optInt("fps", Multimedia.UNKNOWN_FPS);

            var quality = mediaType.startsWith("audio") ? formatData.getString("audioSampleRate") + "hz"
                    : formatData.getString("qualityLabel");

            var format = new Multimedia(true, quality, bitrate, width, height, itag, fps, mediaType, codecs);

            format.printData();

            var url = "";
            var actuallyUseCipher = useCipher || formatData.has("cipher");

            if (actuallyUseCipher)
            {
                System.out.println(" Cipher: enabled");
                var cipher = formatData.getString("cipher");

                var args = cipher.split("&");

                var argMap = new HashMap<String, String>();

                for (var arg : args)
                {
                    var argPair = arg.split("=");
                    var key = argPair[0];
                    var value = argPair[1];

                    argMap.put(key, DescramblerHelper.urlDecode(value));
                }

                argMap.forEach((k, v) ->
                {
                    System.out.printf("  %s: %s\n", k, v);
                });

                var signature = descrambler.descramble(argMap.get("s"));
                url = argMap.get("url") + "&" + argMap.get("sp") + "=" + DescramblerHelper.urlEncode(signature);
                System.out.println(" Deciphered signature: " + signature);
                System.out.println(" Deciphered URL: " + url);
            }
            else
            {
                System.out.println(" Cipher: disabled");
                url = formatData.getString("url");
                System.out.println(" URL: " + url);
            }

            adaptiveFmts.add(new YouTubeMultimedia(url, format));
        });

        return new PlayerResponse(metadata, legacyFmts, adaptiveFmts);
    }
}
