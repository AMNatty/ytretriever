package cz.tefek.youtubetoolkit.parser;

import java.util.ArrayList;

import org.json.JSONObject;

import cz.tefek.youtubetoolkit.descrambler.Descrambler;
import cz.tefek.youtubetoolkit.metadata.YouTubeMetadata;
import cz.tefek.youtubetoolkit.multimedia.Multimedia;
import cz.tefek.youtubetoolkit.multimedia.YouTubeMultimedia;
import cz.tefek.youtubetoolkit.player.response.PlayerResponse;
import cz.tefek.youtubetoolkit.util.URLMap;
import cz.tefek.youtubetoolkit.util.YouTubeMIME;

public class PlayerResponseParser
{
    public static PlayerResponse parse(Descrambler descrambler, JSONObject playerConfig, JSONObject videoDetails, JSONObject streamingData)
    {
        var title = videoDetails.getString("title");

        var author = videoDetails.getString("author");

        var views = videoDetails.getLong("viewCount");
        var length = videoDetails.getLong("lengthSeconds");

        var allowRatings = videoDetails.getBoolean("allowRatings");
        var rating = videoDetails.getDouble("averageRating");

        var useCipher = videoDetails.optBoolean("useCipher", false);

        var loudness = 0.0d;

        if (playerConfig != null)
        {
            var audioConfig = playerConfig.optJSONObject("audioConfig");

            if (audioConfig != null)
            {
                loudness = playerConfig.optDouble("loudnessDb", 0.0d);
            }
        }

        var metadata = new YouTubeMetadata(title, author, views, length, allowRatings, rating, loudness, useCipher);

        var legacyFormats = streamingData.getJSONArray("formats");

        var legacyFmts = new ArrayList<YouTubeMultimedia>();

        legacyFormats.forEach(obj ->
        {
            var formatData = (JSONObject) obj;

            var quality = formatData.getString("qualityLabel");
            var itag = formatData.getInt("itag");
            var width = formatData.getInt("width");
            var height = formatData.getInt("height");

            var mime = YouTubeMIME.from(formatData.getString("mimeType"));

            var bitrate = formatData.optInt("bitrate", Multimedia.UNKNOWN_BITRATE);

            var fps = formatData.optInt("fps", Multimedia.UNKNOWN_FPS);

            var format = new Multimedia(false, quality, bitrate, width, height, itag, fps, mime);

            format.printData();

            var url = getURL(formatData, descrambler, useCipher);

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

            var mime = YouTubeMIME.from(formatData.getString("mimeType"));

            var bitrate = formatData.getInt("bitrate");

            var fps = formatData.optInt("fps", Multimedia.UNKNOWN_FPS);

            var quality = mime.getType().equals("audio") ? formatData.getString("audioSampleRate") + "hz" : formatData.getString("qualityLabel");

            var format = new Multimedia(true, quality, bitrate, width, height, itag, fps, mime);

            format.printData();

            var url = getURL(formatData, descrambler, useCipher);

            adaptiveFmts.add(new YouTubeMultimedia(url, format));
        });

        return new PlayerResponse(metadata, legacyFmts, adaptiveFmts);
    }

    private static String getURL(JSONObject formatData, Descrambler descrambler, boolean useCipher)
    {
        String url;

        if (useCipher || formatData.has("cipher") || formatData.has("signatureCipher"))
        {
            var signatureField = formatData.has("signatureCipher") ? "signatureCipher" : "cipher";
            System.out.printf(" Cipher: [%s mode]%n", signatureField);
            var cipher = formatData.getString(signatureField);

            var argMap = URLMap.decode(cipher);

            argMap.forEach((k, v) -> System.out.printf("  %s: %s%n", k, v));

            var signature = descrambler.descramble(argMap.get("s"));
            url = String.format("%s&%s=%s", argMap.get("url"), argMap.get("sp"), URLMap.urlEncode(signature));
            System.out.println(" Deciphered signature: " + signature);
            System.out.println(" Deciphered URL: " + url);
        }
        else
        {
            System.out.println(" Cipher: disabled");
            url = formatData.getString("url");
            System.out.println(" URL: " + url);
        }


        var argMap = URLMap.decode(url.split("\\?")[1]);

        argMap.forEach((k, v) -> System.out.printf("      %s: %s%n", k, v));

        return url;
    }
}
